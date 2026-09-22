import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient, ApiError } from '../api/client';
import { DAY_OF_WEEK_LABELS } from '../api/labels';
import { formatDate, formatDateTime, formatQuantity, type RecurringOrderResponse } from '../api/orders';
import { useAuth } from '../auth/AuthContext';

// RF21/RN12 — pedidos recorrentes do restaurante: próxima entrega, último resultado, suspender e retomar.
export function RecurringOrdersPage() {
  const { token } = useAuth();
  const [orders, setOrders] = useState<RecurringOrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  useEffect(() => {
    apiClient
      .get<RecurringOrderResponse[]>('/api/v1/recurring-orders', token)
      .then(setOrders)
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Não foi possível carregar.'))
      .finally(() => setLoading(false));
  }, [token]);

  const replace = (updated: RecurringOrderResponse) =>
    setOrders((prev) => prev.map((order) => (order.id === updated.id ? updated : order)));

  const suspend = async (id: string) => {
    setBusyId(id);
    setError(null);
    setNotice(null);
    try {
      const result = await apiClient.post<{ recurringOrder: RecurringOrderResponse; immediate: boolean }>(
        `/api/v1/recurring-orders/${id}/suspend`,
        undefined,
        token,
      );
      replace(result.recurringOrder);
      setNotice(
        result.immediate
          ? 'Pedido recorrente suspenso.'
          : 'Faltam menos de 48 horas para a próxima execução: ela ainda acontece e a suspensão vale logo depois.',
      );
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível suspender.');
    } finally {
      setBusyId(null);
    }
  };

  const resume = async (id: string) => {
    setBusyId(id);
    setError(null);
    setNotice(null);
    try {
      replace(await apiClient.post<RecurringOrderResponse>(`/api/v1/recurring-orders/${id}/resume`, undefined, token));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível retomar.');
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="page">
      <h1>Pedidos recorrentes</h1>
      <p className="admin-subtitle">
        Crie um pedido recorrente no <Link to="/carrinho">carrinho</Link>, escolhendo "Repetir toda semana".
      </p>
      {notice && <p className="form-notice">{notice}</p>}
      {error && <p className="form-error">{error}</p>}
      {loading ? (
        <p>Carregando...</p>
      ) : orders.length === 0 ? (
        <p className="admin-empty">Nenhum pedido recorrente.</p>
      ) : (
        <div className="admin-list">
          {orders.map((order) => {
            const active = order.status === 'ACTIVE';
            return (
              <div className="admin-row" key={order.id}>
                <div className="admin-row-info">
                  <div>
                    <strong>Toda {DAY_OF_WEEK_LABELS[order.deliveryDay]?.toLowerCase() ?? order.deliveryDay}</strong>
                    <span className="admin-row-subtitle">
                      {order.items.map((item) => `${item.productName} (${formatQuantity(item.quantity)})`).join(', ')}
                    </span>
                  </div>
                  <span className={`status-badge ${active ? 'status-approved' : 'status-suspended'}`}>
                    {active ? (order.suspendAfterNextRun ? 'Suspensão agendada' : 'Ativo') : 'Suspenso'}
                  </span>
                </div>
                {active && (
                  <p className="admin-row-note form-notice">
                    Próxima entrega: {formatDate(order.nextDeliveryDate)} (pedidos gerados em{' '}
                    {formatDate(order.nextRunDate)})
                  </p>
                )}
                {order.lastRunAt && (
                  <p className="admin-row-note admin-empty">
                    Última execução {formatDateTime(order.lastRunAt)}: {order.lastRunSummary}
                  </p>
                )}
                <div className="admin-row-actions">
                  {active && !order.suspendAfterNextRun ? (
                    <button type="button" className="admin-action-reject" disabled={busyId === order.id} onClick={() => suspend(order.id)}>
                      Suspender
                    </button>
                  ) : (
                    <button type="button" className="admin-action-approve" disabled={busyId === order.id} onClick={() => resume(order.id)}>
                      {active ? 'Cancelar suspensão agendada' : 'Retomar'}
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
