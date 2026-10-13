import type { CatalogEntryResponse } from '../api/types';
import type { CartItem } from './CartContext';

// Item de carrinho a partir de uma oferta do catálogo, com a quantidade limitada ao disponível.
export function cartItemFrom(entry: CatalogEntryResponse, quantity: number): Omit<CartItem, 'proposedUnitPrice'> {
  return {
    offerId: entry.offer.id,
    productName: entry.offer.productName,
    producerId: entry.producerId,
    producerName: entry.producerName,
    unit: entry.offer.unit,
    price: entry.offer.price,
    quantity: Math.min(quantity, entry.offer.quantityAvailable),
    quantityAvailable: entry.offer.quantityAvailable,
    availableUntil: entry.offer.availabilityUntil,
  };
}
