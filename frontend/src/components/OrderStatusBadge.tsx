import { ORDER_STATUS_LABELS, type OrderStatus } from '../api/orders';

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  return <span className={`status-badge order-status-${status.toLowerCase()}`}>{ORDER_STATUS_LABELS[status]}</span>;
}
