import { OFFER_STATUS_LABELS } from '../api/labels';
import type { OfferStatus } from '../api/types';

export function OfferStatusBadge({ status }: { status: OfferStatus }) {
  return (
    <span className={`status-badge offer-status-${status.toLowerCase()}`}>
      {OFFER_STATUS_LABELS[status] ?? status}
    </span>
  );
}
