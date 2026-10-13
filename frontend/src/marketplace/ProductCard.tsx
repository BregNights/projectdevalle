import { formatMoney } from '../api/orders';
import type { CatalogEntryResponse } from '../api/types';
import { CartStepper } from './CartStepper';
import { availabilityLabel, distanceLabel, stockLabel, unitLabel } from './catalog';
import { ProductImage } from './ProductImage';

// Card de oferta usado nos layouts em grade e em prateleira.
export function ProductCard({
  entry,
  onCompare,
}: {
  entry: CatalogEntryResponse;
  onCompare: (offerId: string) => void;
}) {
  const { offer } = entry;
  const availability = availabilityLabel(offer);
  const distance = distanceLabel(entry);

  return (
    <article className="mk-card">
      <div className="mk-card-media">
        <ProductImage offer={offer} />
        {availability && (
          <span className={`mk-badge${offer.recurrenceType === 'RECURRING' ? ' mk-badge-recurring' : ''}`}>
            {availability}
          </span>
        )}
      </div>
      <div className="mk-card-body">
        <span className="mk-card-producer">
          {entry.producerName ?? 'Produtor'}
          {entry.producerCity && <> · {entry.producerCity}</>}
        </span>
        <h3 className="mk-card-title">{offer.productName}</h3>
        <span className="mk-card-meta">
          {stockLabel(offer)}
          {distance && <> · {distance}</>}
        </span>
        <div className="mk-card-footer">
          <p className="mk-price">
            <strong>{formatMoney(offer.price)}</strong>
            <span>/{unitLabel(offer)}</span>
          </p>
          <CartStepper entry={entry} />
        </div>
        <button type="button" className="mk-link" onClick={() => onCompare(offer.id)}>
          Comparar produtores
        </button>
      </div>
    </article>
  );
}

export function ProductCardSkeleton() {
  return (
    <div className="mk-card mk-card-skeleton" aria-hidden="true">
      <div className="mk-card-media mk-shimmer" />
      <div className="mk-card-body">
        <span className="mk-shimmer mk-shimmer-line" style={{ width: '60%' }} />
        <span className="mk-shimmer mk-shimmer-line" style={{ width: '85%', height: '1.1rem' }} />
        <span className="mk-shimmer mk-shimmer-line" style={{ width: '45%' }} />
      </div>
    </div>
  );
}
