import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { apiClient, ApiError } from '../api/client';
import { formatDate, formatMoney, type OrderResponse, type OrderStatus } from '../api/orders';
import { useAuth } from '../auth/AuthContext';
import { OrderStatusBadge } from '../components/OrderStatusBadge';

const FILTERS: { value: 'ALL' | 'ACTIVE' | OrderStatus; label: string }[] = [
  { value: 'ACTIVE', label: 'Em andamento' },
  { value: 'DELIVERED', label: 'Entregues' },
  { value: 'CANCELLED', label: 'Cancelados' },
  { value: 'ALL', label: 'Todos' },
];

// RF19 — pedidos do restaurante ou do produtor, com status e indicação de quando é a sua vez de responder.
export function OrdersPage() {
  const { token, claims } = useAuth();
  const location = useLocation();
  const justPlaced = (location.state as { justPlaced?: boolean } | null)?.justPlaced === true;
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [filter, setFilter] = useState<(typeof FILTERS)[number]['value']>('ACTIVE');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const isProducer = claims?.role === 'PRODUCER';

  useEffect(() => {
    apiClient
      .get<OrderResponse[]>('/api/v1/orders', token)
      .then(setOrders)
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Não foi possível carregar os pedidos.'))
      .finally(() => setLoading(false));
  }, [token]);

  const visible = orders.filter((order) => {
    if (filter === 'ALL') return true;
    if (filter === 'ACTIVE') return order.status !== 'DELIVERED' && order.status !== 'CANCELLED';
    return order.status === filter;
  });
  const waitingForMe = orders.filter((order) => order.yourTurn).length;

  return (
    <div className="page">
      <h1>Pedidos</h1>
      {justPlaced && <p className="form-notice">Pedido enviado! Agora é a vez de cada produtor responder.</p>}
      {waitingForMe > 0 && (
        <p className="form-notice">
          {waitingForMe} pedido(s) aguardando sua resposta.
        </p>
      )}

      <div className="admin-filter">
        {FILTERS.map((option) => (
          <button
            key={option.value}
            type="button"
            className={option.value === filter ? 'admin-filter-active' : ''}
            onClick={() => setFilter(option.value)}
          >
            {option.label}
          </button>
        ))}
      </div>

      {error && <p className="form-error">{error}</p>}
      {loading ? (
        <p>Carregando...</p>
      ) : visible.length === 0 ? (
        <p className="admin-empty">Nenhum pedido aqui.</p>
      ) : (
        <div className="admin-list">
          {visible.map((order) => (
            <Link to={`/pedidos/${order.id}`} className="admin-row order-row" key={order.id}>
              <div className="admin-row-info">
                <div>
                  <strong>{isProducer ? order.restaurantName : order.producerName}</strong>
                  <span className="admin-row-subtitle">
                    {order.items.map((item) => item.productName).join(', ')} · entrega {formatDate(order.requestedDeliveryDate)}
                    {order.recurringOrderId && ' · recorrente'}
                  </span>
                </div>
                <div className="order-row-side">
                  <strong>{formatMoney(order.total)}</strong>
                  <OrderStatusBadge status={order.status} />
                  {order.yourTurn && <span className="status-badge order-your-turn">Sua vez</span>}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
