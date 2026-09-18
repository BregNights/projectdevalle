import { STATUS_LABELS } from '../api/labels';
import type { RegistrationStatus } from '../api/types';

export function StatusBadge({ status }: { status: RegistrationStatus }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{STATUS_LABELS[status] ?? status}</span>;
}
