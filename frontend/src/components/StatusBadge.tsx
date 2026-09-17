import type { RegistrationStatus } from '../api/types';

const LABELS: Record<RegistrationStatus, string> = {
  PENDING: 'Pendente',
  APPROVED: 'Aprovado',
  REJECTED: 'Rejeitado',
  SUSPENDED: 'Suspenso',
};

export function StatusBadge({ status }: { status: RegistrationStatus }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{LABELS[status]}</span>;
}
