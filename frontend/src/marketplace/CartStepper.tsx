import { useState } from 'react';
import type { CatalogEntryResponse } from '../api/types';
import { useCart } from '../cart/CartContext';
import { cartItemFrom } from '../cart/cartItem';
import { unitLabel } from './catalog';

// RF16/RN75 — botão "Adicionar" que vira um seletor de quantidade quando a oferta já está no carrinho.
// Visitantes também adicionam; produtor e administrador não veem o botão.
export function CartStepper({ entry, wide }: { entry: CatalogEntryResponse; wide?: boolean }) {
  const cart = useCart();
  const [draft, setDraft] = useState<string | null>(null);
  const { offer } = entry;
  const item = cart.items.find((candidate) => candidate.offerId === offer.id);
  const max = offer.quantityAvailable;

  if (!cart.enabled) return null;

  if (!item) {
    return (
      <button
        type="button"
        className={`mk-add${wide ? ' mk-add-wide' : ''}`}
        onClick={() => cart.add(cartItemFrom(entry, 1))}
        aria-label={`Adicionar ${offer.productName} ao carrinho`}
      >
        <span aria-hidden="true">+</span> Adicionar
      </button>
    );
  }

  const setQuantity = (quantity: number) => {
    if (quantity <= 0) {
      cart.remove(offer.id);
    } else {
      cart.update(offer.id, { quantity: Math.min(quantity, max) });
    }
  };

  const commitDraft = () => {
    if (draft !== null) {
      const value = Number(draft.replace(',', '.'));
      if (Number.isFinite(value)) setQuantity(value);
      setDraft(null);
    }
  };

  return (
    <div className={`mk-stepper${wide ? ' mk-stepper-wide' : ''}`}>
      <button type="button" onClick={() => setQuantity(item.quantity - 1)} aria-label="Diminuir quantidade">
        {item.quantity <= 1 ? '×' : '−'}
      </button>
      <label>
        <span className="mk-visually-hidden">Quantidade em {unitLabel(offer)}</span>
        <input
          inputMode="decimal"
          value={draft ?? String(item.quantity).replace('.', ',')}
          onChange={(e) => setDraft(e.target.value)}
          onBlur={commitDraft}
          onKeyDown={(e) => {
            if (e.key === 'Enter') commitDraft();
          }}
        />
        <span className="mk-stepper-unit" aria-hidden="true">
          {unitLabel(offer)}
        </span>
      </label>
      <button
        type="button"
        onClick={() => setQuantity(item.quantity + 1)}
        disabled={item.quantity >= max}
        aria-label="Aumentar quantidade"
      >
        +
      </button>
    </div>
  );
}
