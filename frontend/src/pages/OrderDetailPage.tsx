import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiClient, ApiError } from '../api/client';
import { MEASUREMENT_UNIT_LABELS } from '../api/labels';
import {
  formatDate,
  formatDateTime,
  formatMoney,
  formatQuantity,
  isoDate,
  ORDER_STATUS_LABELS,
  PARTY_LABELS,
  type OrderResponse,
} from '../api/orders';
import { useAuth } from '../auth/AuthContext';
import { OrderStatusBadge } from '../components/OrderStatusBadge';

type Panel = 'none' | 'counter' | 'cancel' | 'dispatch';

// RF18/RF19/RF20 — detalhe do pedido com negociação, andamento, rastreabilidade (RN23) e cancelamento.
export function OrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { token } = useAuth();
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [panel, setPanel] = useState<Panel>('none');

  const load = useCallback(async () => {
    try {
      setOrder(await apiClient.get<OrderResponse>(`/api/v1/orders/${id}`, token));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível carregar o pedido.');
    }
  }, [id, token]);

  useEffect(() => {
    load();
  }, [load]);

  const run = async (action: string, body?: unknown) => {
    setBusy(true);
    setError(null);
    try {
      setOrder(await apiClient.post<OrderResponse>(`/api/v1/orders/${id}/${action}`, body, token));
      setPanel('none');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível concluir a ação.');
    } finally {
      setBusy(false);
    }
  };

  if (!order) {
    return <div className="page">{error ? <p className="form-error">{error}</p> : <p>Carregando...</p>}</div>;
  }

  const isProducer = order.yourRole === 'PRODUCER';
  const counterpart = isProducer ? order.restaurantName : order.producerName;
  const cancellable = ['PENDING', 'CONFIRMED', 'IN_PREPARATION'].includes(order.status);
  const cancelLabel =
    order.status === 'PENDING' ? (isProducer ? 'Recusar pedido' : 'Desistir do pedido') : 'Cancelar pedido';

  return (
    <div className="page">
      <p>
        <Link to="/pedidos">← Pedidos</Link>
      </p>
      <div className="dashboard-card">
        <div className="admin-row-info">
          <h1>{counterpart ?? 'Pedido'}</h1>
          <OrderStatusBadge status={order.status} />
        </div>
        <div className="dashboard-meta">
          <span>
            Total: <strong>{formatMoney(order.total)}</strong>
          </span>
          <span>
            Entrega desejada: <strong>{formatDate(order.requestedDeliveryDate)}</strong>
          </span>
          <span>
            Local: <strong>{order.destination.label ?? 'Endereço'}</strong> — {order.destination.street},{' '}
            {order.destination.number}, {order.destination.neighborhood}, {order.destination.city}/
            {order.destination.state}
          </span>
          {order.recurringOrderId && <span>Gerado por um pedido recorrente</span>}
        </div>
        {order.notes && <p className="form-notice">Observações: {order.notes}</p>}
        {order.status === 'PENDING' && (
          <p className={order.yourTurn ? 'form-notice' : 'admin-empty'}>
            {order.yourTurn
              ? 'É a sua vez: aceite os termos abaixo, faça uma contraproposta ou recuse.'
              : `Aguardando resposta do ${PARTY_LABELS[order.awaitingResponseFrom ?? 'PRODUCER'].toLowerCase()}.`}
          </p>
        )}
        {order.cancellation && (
          <p className="form-error">
            Cancelado por {PARTY_LABELS[order.cancellation.cancelledBy].toLowerCase()} em{' '}
            {formatDateTime(order.cancellation.cancelledAt)}: {order.cancellation.reason}
            {order.cancellation.penaltyAmount > 0 && ` · multa: ${formatMoney(order.cancellation.penaltyAmount)}`}
          </p>
        )}
        {order.deliveryAutoConfirmed && (
          <p className="admin-empty">Recebimento confirmado automaticamente por prazo.</p>
        )}
      </div>

      <section className="card-form">
        <h2>Itens</h2>
        <table className="metric-table">
          <thead>
            <tr>
              <th>Produto</th>
              <th>Quantidade</th>
              <th>Preço</th>
              <th>Subtotal</th>
              <th>Rastreabilidade</th>
            </tr>
          </thead>
          <tbody>
            {order.items.map((item) => (
              <tr key={item.id}>
                <td>{item.productName}</td>
                <td>
                  {formatQuantity(item.quantity)} {MEASUREMENT_UNIT_LABELS[item.unit] ?? item.unit}
                </td>
                <td>
                  {formatMoney(item.unitPrice)}
                  {item.unitPrice !== item.listUnitPrice && (
                    <span className="admin-row-subtitle"> (tabela: {formatMoney(item.listUnitPrice)})</span>
                  )}
                </td>
                <td>{formatMoney(item.subtotal)}</td>
                <td>
                  {item.harvestDate
                    ? `Colheita/captura ${formatDate(item.harvestDate)}${item.lot ? ` · lote ${item.lot}` : ''}`
                    : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {error && <p className="form-error">{error}</p>}

      <div className="admin-row-actions order-actions">
        {order.yourTurn && (
          <>
            <button type="button" className="admin-action-approve" disabled={busy} onClick={() => run('accept')}>
              Aceitar termos
            </button>
            <button type="button" className="admin-action-ghost" disabled={busy} onClick={() => setPanel('counter')}>
              Fazer contraproposta
            </button>
          </>
        )}
        {isProducer && order.status === 'CONFIRMED' && (
          <button type="button" className="admin-action-approve" disabled={busy} onClick={() => run('start-preparation')}>
            Iniciar preparo
          </button>
        )}
        {isProducer && (order.status === 'CONFIRMED' || order.status === 'IN_PREPARATION') && (
          <button type="button" className="admin-action-approve" disabled={busy} onClick={() => setPanel('dispatch')}>
            Despachar
          </button>
        )}
        {!isProducer && order.status === 'IN_TRANSIT' && (
          <button type="button" className="admin-action-approve" disabled={busy} onClick={() => run('confirm-receipt')}>
            Confirmar recebimento
          </button>
        )}
        {cancellable && (
          <button type="button" className="admin-action-reject" disabled={busy} onClick={() => setPanel('cancel')}>
            {cancelLabel}
          </button>
        )}
      </div>

      {panel === 'counter' && <CounterProposalForm order={order} busy={busy} onCancel={() => setPanel('none')} onSubmit={(body) => run('counter-proposal', body)} />}
      {panel === 'dispatch' && <DispatchForm order={order} busy={busy} onCancel={() => setPanel('none')} onSubmit={(body) => run('dispatch', body)} />}
      {panel === 'cancel' && (
        <CancelForm
          label={cancelLabel}
          warning={cancelWarning(order)}
          busy={busy}
          onCancel={() => setPanel('none')}
          onSubmit={(reason) => run('cancel', { reason })}
        />
      )}

      {order.negotiation.length > 1 && (
        <section className="card-form">
          <h2>Negociação</h2>
          <ul className="metric-list">
            {order.negotiation.map((round) => (
              <li key={round.number}>
                <span>
                  {round.number === 1 ? 'Proposta inicial' : 'Contraproposta'} do{' '}
                  {PARTY_LABELS[round.proposedBy].toLowerCase()} · {formatDateTime(round.proposedAt)}
                  {round.deliveryDate && round.number > 1 && ` · entrega ${formatDate(round.deliveryDate)}`}
                  {round.message && ` · "${round.message}"`}
                </span>
                <strong>
                  {round.terms
                    .map((terms) => {
                      const item = order.items.find((candidate) => candidate.id === terms.itemId);
                      return `${item?.productName ?? 'item'}: ${formatQuantity(terms.quantity)} × ${formatMoney(terms.unitPrice)}`;
                    })
                    .join('; ')}
                </strong>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="card-form">
        <h2>Linha do tempo</h2>
        <ul className="metric-list">
          {order.history.map((event, index) => (
            <li key={index}>
              <span>
                {ORDER_STATUS_LABELS[event.status]} · {PARTY_LABELS[event.actor]}
                {event.note && ` · ${event.note}`}
              </span>
              <strong>{formatDateTime(event.occurredAt)}</strong>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}

// RN10/RN11 — avisos antes de cancelar.
function cancelWarning(order: OrderResponse): string | null {
  if (order.yourRole === 'RESTAURANT' && order.cancellationPenaltyIfCancelledNow != null) {
    return `O produtor já iniciou o preparo: cancelar agora gera multa de ${formatMoney(order.cancellationPenaltyIfCancelledNow)}.`;
  }
  if (order.yourRole === 'PRODUCER' && order.status !== 'PENDING') {
    return 'Cancelar um pedido já confirmado não gera multa, mas reduz sua taxa de cumprimento no perfil.';
  }
  return null;
}

function CounterProposalForm({
  order,
  busy,
  onCancel,
  onSubmit,
}: {
  order: OrderResponse;
  busy: boolean;
  onCancel: () => void;
  onSubmit: (body: unknown) => void;
}) {
  const [terms, setTerms] = useState(() =>
    order.items.map((item) => ({ itemId: item.id, quantity: String(item.quantity), unitPrice: String(item.unitPrice) })),
  );
  const [deliveryDate, setDeliveryDate] = useState('');
  const [message, setMessage] = useState('');

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    onSubmit({
      items: terms.map((entry) => ({
        itemId: entry.itemId,
        quantity: Number(entry.quantity),
        unitPrice: Number(entry.unitPrice),
      })),
      deliveryDate: deliveryDate || undefined,
      message: message || undefined,
    });
  };

  return (
    <form className="card-form" onSubmit={handleSubmit}>
      <fieldset>
        <legend>Contraproposta</legend>
        {order.items.map((item, index) => (
          <div className="cart-line" key={item.id}>
            <strong className="cart-line-name">{item.productName}</strong>
            <label>
              Quantidade
              <input
                type="number"
                min="0.001"
                step="any"
                value={terms[index].quantity}
                onChange={(e) =>
                  setTerms((prev) => prev.map((entry, i) => (i === index ? { ...entry, quantity: e.target.value } : entry)))
                }
                required
              />
            </label>
            <label>
              Preço unitário (R$)
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={terms[index].unitPrice}
                onChange={(e) =>
                  setTerms((prev) => prev.map((entry, i) => (i === index ? { ...entry, unitPrice: e.target.value } : entry)))
                }
                required
              />
            </label>
          </div>
        ))}
        <label>
          Nova data de entrega (opcional)
          <input type="date" min={isoDate(new Date())} value={deliveryDate} onChange={(e) => setDeliveryDate(e.target.value)} />
        </label>
        <label>
          Mensagem (opcional)
          <textarea rows={2} maxLength={1000} value={message} onChange={(e) => setMessage(e.target.value)} />
        </label>
      </fieldset>
      <div className="admin-row-actions">
        <button type="submit" disabled={busy}>
          Enviar contraproposta
        </button>
        <button type="button" className="admin-action-ghost" onClick={onCancel} disabled={busy}>
          Voltar
        </button>
      </div>
    </form>
  );
}

// RN23 — data de colheita/captura (obrigatória) e lote (opcional) de cada item.
function DispatchForm({
  order,
  busy,
  onCancel,
  onSubmit,
}: {
  order: OrderResponse;
  busy: boolean;
  onCancel: () => void;
  onSubmit: (body: unknown) => void;
}) {
  const today = isoDate(new Date());
  const [entries, setEntries] = useState(() =>
    order.items.map((item) => ({ itemId: item.id, harvestDate: item.harvestDate ?? today, lot: item.lot ?? '' })),
  );

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    onSubmit({ items: entries.map((entry) => ({ ...entry, lot: entry.lot || undefined })) });
  };

  return (
    <form className="card-form" onSubmit={handleSubmit}>
      <fieldset>
        <legend>Despachar pedido — rastreabilidade</legend>
        {order.items.map((item, index) => (
          <div className="cart-line" key={item.id}>
            <strong className="cart-line-name">{item.productName}</strong>
            <label>
              Data de colheita/captura
              <input
                type="date"
                max={today}
                value={entries[index].harvestDate}
                onChange={(e) =>
                  setEntries((prev) => prev.map((entry, i) => (i === index ? { ...entry, harvestDate: e.target.value } : entry)))
                }
                required
              />
            </label>
            <label>
              Lote (opcional)
              <input
                maxLength={80}
                value={entries[index].lot}
                onChange={(e) =>
                  setEntries((prev) => prev.map((entry, i) => (i === index ? { ...entry, lot: e.target.value } : entry)))
                }
              />
            </label>
          </div>
        ))}
      </fieldset>
      <div className="admin-row-actions">
        <button type="submit" disabled={busy}>
          Confirmar saída para entrega
        </button>
        <button type="button" className="admin-action-ghost" onClick={onCancel} disabled={busy}>
          Voltar
        </button>
      </div>
    </form>
  );
}

function CancelForm({
  label,
  warning,
  busy,
  onCancel,
  onSubmit,
}: {
  label: string;
  warning: string | null;
  busy: boolean;
  onCancel: () => void;
  onSubmit: (reason: string) => void;
}) {
  const [reason, setReason] = useState('');

  return (
    <form
      className="card-form"
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit(reason.trim());
      }}
    >
      <fieldset>
        <legend>{label}</legend>
        {warning && <p className="form-error">{warning}</p>}
        <label>
          Motivo (obrigatório)
          <textarea rows={2} maxLength={1000} value={reason} onChange={(e) => setReason(e.target.value)} required />
        </label>
      </fieldset>
      <div className="admin-row-actions">
        <button type="submit" className="admin-action-reject" disabled={busy || !reason.trim()}>
          Confirmar
        </button>
        <button type="button" className="admin-action-ghost" onClick={onCancel} disabled={busy}>
          Voltar
        </button>
      </div>
    </form>
  );
}
