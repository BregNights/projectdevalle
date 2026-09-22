import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useAuth } from '../auth/AuthContext';

// RF16 — carrinho do restaurante, com itens de um ou mais produtores. Fica no navegador (por usuário) até o
// fechamento do pedido; preços e disponibilidade são revalidados pelo servidor no checkout.
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
  items: CartItem[];
  add: (item: Omit<CartItem, 'proposedUnitPrice'>) => void;
  update: (offerId: string, changes: Partial<Pick<CartItem, 'quantity' | 'proposedUnitPrice'>>) => void;
  remove: (offerId: string) => void;
  clear: () => void;
}

const CartContext = createContext<CartState | undefined>(undefined);

function storageKey(userId: string): string {
  return `projectdevalle.cart.${userId}`;
}

function readCart(userId: string | undefined): CartItem[] {
  if (!userId) return [];
  try {
    const stored = localStorage.getItem(storageKey(userId));
    return stored ? (JSON.parse(stored) as CartItem[]) : [];
  } catch {
    return [];
  }
}

export function CartProvider({ children }: { children: ReactNode }) {
  const { claims } = useAuth();
  const userId = claims?.role === 'RESTAURANT' ? claims.sub : undefined;
  // O carrinho guarda de quem é: ao trocar de usuário, o do novo é carregado antes de qualquer gravação,
  // para nunca salvar os itens de um usuário na chave de outro.
  const [cart, setCart] = useState<{ owner: string | undefined; items: CartItem[] }>(() => ({
    owner: userId,
    items: readCart(userId),
  }));
  const items = cart.owner === userId ? cart.items : readCart(userId);

  useEffect(() => {
    if (cart.owner !== userId) {
      setCart({ owner: userId, items: readCart(userId) });
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
      items,
      add: (item) =>
        setItems((prev) => {
          const existing = prev.find((entry) => entry.offerId === item.offerId);
          if (existing) {
            return prev.map((entry) =>
              entry.offerId === item.offerId
                ? { ...entry, ...item, quantity: Math.min(entry.quantity + item.quantity, item.quantityAvailable) }
                : entry,
            );
          }
          return [...prev, { ...item, proposedUnitPrice: null }];
        }),
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
