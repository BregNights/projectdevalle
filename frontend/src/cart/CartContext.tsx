import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useAuth } from '../auth/AuthContext';

// RF16 — carrinho do restaurante, com itens de um ou mais produtores. Fica no navegador (por usuário) até o
// fechamento do pedido; preços e disponibilidade são revalidados pelo servidor no checkout.
// RN75 — visitantes sem login também montam carrinho; ao entrar como restaurante, os itens vão para a conta.
export interface CartItem {
  offerId: string;
  productName: string;
  producerId: string;
  producerName: string | null;
  unit: string;
  price: number;
  quantity: number;
  quantityAvailable: number;
  availableUntil: string | null;
  // RF18 — preço proposto pelo restaurante (opcional).
  proposedUnitPrice: number | null;
}

interface CartState {
  // Visitante ou restaurante podem montar carrinho; produtor e administrador, não.
  enabled: boolean;
  items: CartItem[];
  add: (item: Omit<CartItem, 'proposedUnitPrice'>) => void;
  update: (offerId: string, changes: Partial<Pick<CartItem, 'quantity' | 'proposedUnitPrice'>>) => void;
  remove: (offerId: string) => void;
  clear: () => void;
}

const CartContext = createContext<CartState | undefined>(undefined);

const GUEST = 'guest';

function storageKey(owner: string): string {
  return `projectdevalle.cart.${owner}`;
}

function readCart(owner: string | undefined): CartItem[] {
  if (!owner) return [];
  try {
    const stored = localStorage.getItem(storageKey(owner));
    return stored ? (JSON.parse(stored) as CartItem[]) : [];
  } catch {
    return [];
  }
}

function addItem(items: CartItem[], item: Omit<CartItem, 'proposedUnitPrice'>): CartItem[] {
  const existing = items.find((entry) => entry.offerId === item.offerId);
  if (existing) {
    return items.map((entry) =>
      entry.offerId === item.offerId
        ? { ...entry, ...item, quantity: Math.min(entry.quantity + item.quantity, item.quantityAvailable) }
        : entry,
    );
  }
  return [...items, { ...item, proposedUnitPrice: null }];
}

// Ao entrar como restaurante, o carrinho de visitante é somado ao da conta e deixa de existir no navegador.
function takeGuestCartInto(owner: string): CartItem[] {
  const merged = readCart(GUEST).reduce(addItem, readCart(owner));
  try {
    localStorage.removeItem(storageKey(GUEST));
  } catch {
    // Sem armazenamento local, não há carrinho de visitante guardado.
  }
  return merged;
}

export function CartProvider({ children }: { children: ReactNode }) {
  const { claims } = useAuth();
  const userId = !claims ? GUEST : claims.role === 'RESTAURANT' ? claims.sub : undefined;
  // O carrinho guarda de quem é: ao trocar de usuário, o do novo é carregado antes de qualquer gravação,
  // para nunca salvar os itens de um usuário na chave de outro.
  const [cart, setCart] = useState<{ owner: string | undefined; items: CartItem[] }>(() => ({
    owner: userId,
    items: readCart(userId),
  }));
  const items = cart.owner === userId ? cart.items : readCart(userId);

  useEffect(() => {
    if (cart.owner !== userId) {
      const fromGuest = cart.owner === GUEST && userId !== undefined && userId !== GUEST;
      setCart({ owner: userId, items: fromGuest ? takeGuestCartInto(userId) : readCart(userId) });
      return;
    }
    if (!userId) return;
    try {
      localStorage.setItem(storageKey(userId), JSON.stringify(cart.items));
    } catch {
      // Sem armazenamento local, o carrinho vale só para esta aba.
    }
  }, [cart, userId]);

  const setItems = (updater: (prev: CartItem[]) => CartItem[]) =>
    setCart((prev) => ({ owner: userId, items: updater(prev.owner === userId ? prev.items : readCart(userId)) }));

  const value = useMemo<CartState>(
    () => ({
      enabled: userId !== undefined,
      items,
      add: (item) => setItems((prev) => addItem(prev, item)),
      update: (offerId, changes) =>
        setItems((prev) => prev.map((entry) => (entry.offerId === offerId ? { ...entry, ...changes } : entry))),
      remove: (offerId) => setItems((prev) => prev.filter((entry) => entry.offerId !== offerId)),
      clear: () => setItems(() => []),
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [items, userId],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useCart(): CartState {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
}
