// Módulo 1.4 — pedidos e negociação (RF16–RF21).
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'IN_PREPARATION' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
export type OrderParty = 'RESTAURANT' | 'PRODUCER' | 'SYSTEM';

export interface OrderItem {
  id: string;
  offerId: string;
  productName: string;
  category: string;
  unit: string;
  listUnitPrice: number;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  harvestDate: string | null;
  lot: string | null;
}

export interface NegotiationRound {
  number: number;
  proposedBy: OrderParty;
  proposedAt: string;
  message: string | null;
  deliveryDate: string | null;
  terms: { itemId: string; quantity: number; unitPrice: number }[];
}

export interface OrderEvent {
  status: OrderStatus;
  actor: OrderParty;
  occurredAt: string;
  note: string | null;
}

export interface OrderResponse {
  id: string;
  checkoutId: string;
  restaurantId: string;
  restaurantName: string | null;
  producerId: string;
  producerName: string | null;
  destination: {
    label: string | null;
    street: string;
    number: string;
    neighborhood: string;
    city: string;
    state: string;
    zipCode: string;
    complement: string | null;
  };
  requestedDeliveryDate: string;
  notes: string | null;
  status: OrderStatus;
  awaitingResponseFrom: OrderParty | null;
  yourRole: OrderParty;
  yourTurn: boolean;
  total: number;
  items: OrderItem[];
  negotiation: NegotiationRound[];
  history: OrderEvent[];
  placedAt: string;
  confirmedAt: string | null;
  preparationStartedAt: string | null;
  pickedUpAt: string | null;
  deliveredAt: string | null;
  deliveryAutoConfirmed: boolean;
  cancellation: {
    cancelledBy: OrderParty;
    reason: string;
    cancelledAt: string;
    afterConfirmation: boolean;
    penaltyAmount: number;
  } | null;
  recurringOrderId: string | null;
  cancellationPenaltyIfCancelledNow: number | null;
}

export interface RecurringOrderResponse {
  id: string;
  deliveryAddressId: string;
  deliveryDay: string;
  notes: string | null;
  status: 'ACTIVE' | 'SUSPENDED';
  nextDeliveryDate: string;
  nextRunDate: string;
  suspendAfterNextRun: boolean;
  lastRunAt: string | null;
  lastRunSummary: string | null;
  items: { offerId: string; productName: string; quantity: number }[];
}

export interface ProducerFulfillment {
  producerId: string;
  deliveredOrders: number;
  producerCancellations: number;
  fulfillmentRate: number | null;
}

export interface OrderMetrics {
  totalOrders: number;
  ordersByStatus: Record<string, number>;
  deliveredVolume: number;
  averageTicket: number | null;
  cancellationRate: number | null;
  averageDeliveryHours: number | null;
}

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: 'Pendente',
  CONFIRMED: 'Confirmado',
  IN_PREPARATION: 'Em preparo',
  IN_TRANSIT: 'Em transporte',
  DELIVERED: 'Entregue',
  CANCELLED: 'Cancelado',
};

export const PARTY_LABELS: Record<OrderParty, string> = {
  RESTAURANT: 'Restaurante',
  PRODUCER: 'Produtor',
  SYSTEM: 'Sistema',
};

export function formatMoney(value: number | null | undefined): string {
  if (value == null) return '—';
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

export function formatQuantity(value: number): string {
  return value.toLocaleString('pt-BR', { maximumFractionDigits: 3 });
}

export function formatDate(isoDate: string | null | undefined): string {
  if (!isoDate) return '—';
  const [year, month, day] = isoDate.slice(0, 10).split('-');
  return `${day}/${month}/${year}`;
}

export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

// Data local no formato ISO (AAAA-MM-DD), para inputs de data e comparações.
export function isoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}
