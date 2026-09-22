import { useState } from 'react';
import type { CatalogEntryResponse } from '../api/types';
import { useCart } from '../cart/CartContext';

// RF16 — adiciona uma oferta do catálogo ao carrinho do restaurante.
export function AddToCart({ entry, compact }: { entry: CatalogEntryResponse; compact?: boolean }) {
  const cart = useCart();
  const [quantity, setQuantity] = useState('1');
  const [added, setAdded] = useState(false);
  const inCart = cart.items.find((item) => item.offerId === entry.offer.id);

  const add = () => {
    const value = Number(quantity);
    if (!value || value <= 0) return;
    cart.add({
      offerId: entry.offer.id,
      productName: entry.offer.productName,
      producerId: entry.producerId,
      producerName: entry.producerName,
      unit: entry.offer.unit,
      price: entry.offer.price,
      quantity: Math.min(value, entry.offer.quantityAvailable),
      quantityAvailable: entry.offer.quantityAvailable,
      availableUntil: entry.offer.availabilityUntil,
    });
    setAdded(true);
  };

  return (
    <div className={compact ? 'add-to-cart add-to-cart-compact' : 'add-to-cart'}>
      <input
        type="number"
        min="0.001"
        step="any"
        max={entry.offer.quantityAvailable}
        value={quantity}
        aria-label="Quantidade"
        onChange={(e) => {
          setQuantity(e.target.value);
          setAdded(false);
        }}
      />
      <button type="button" onClick={add}>
        {compact ? 'Adicionar' : 'Adicionar ao carrinho'}
      </button>
      {(added || inCart) && !compact && (
        <span className="form-notice">No carrinho: {inCart?.quantity ?? quantity}</span>
      )}
    </div>
  );
}
