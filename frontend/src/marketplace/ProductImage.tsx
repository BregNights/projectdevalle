import { useState } from 'react';
import { publicFileSrc } from '../api/files';
import type { OfferResponse } from '../api/types';
import { CATEGORY_META } from './catalog';

// RF07 — primeira foto da oferta; sem foto (ou se ela falhar), um bloco com o ícone da categoria.
export function ProductImage({ offer, className }: { offer: OfferResponse; className?: string }) {
  const [failed, setFailed] = useState(false);
  const photo = offer.photoUrls[0];
  const meta = CATEGORY_META[offer.category] ?? CATEGORY_META.OTHER;

  if (photo && !failed) {
    return (
      <img
        className={`mk-image${className ? ` ${className}` : ''}`}
        src={publicFileSrc(photo)}
        alt={offer.productName}
        loading="lazy"
        onError={() => setFailed(true)}
      />
    );
  }
  return (
    <div
      className={`mk-image mk-image-placeholder${className ? ` ${className}` : ''}`}
      style={{ background: meta.tint }}
      role="img"
      aria-label={offer.productName}
    >
      <span aria-hidden="true">{meta.icon}</span>
    </div>
  );
}
