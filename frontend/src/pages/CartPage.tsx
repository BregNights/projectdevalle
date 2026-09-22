import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiClient, ApiError } from '../api/client';
import { DAY_OF_WEEK_LABELS, MEASUREMENT_UNIT_LABELS } from '../api/labels';
import { formatMoney, formatQuantity, isoDate, type OrderResponse } from '../api/orders';
import { useAuth } from '../auth/AuthContext';
import { useCart, type CartItem } from '../cart/CartContext';

interface DeliveryAddress {
  id: string;
  label: string;
  primary: boolean;
  address: { street: string; number: string; city: string };
}

type Mode = 'once' | 'recurring';

// RF16 — carrinho com itens de vários produtores; ao finalizar, vira um pedido por produtor.
// RF18 — o restaurante pode propor um preço por item. RF21 — ou transformar o carrinho em pedido semanal.
export function CartPage() {
  const { token } = useAuth();
  const cart = useCart();
  const navigate = useNavigate();
  const [addresses, setAddresses] = useState<DeliveryAddress[]>([]);
  const [deliveryAddressId, setDeliveryAddressId] = useState('');
  const [deliveryDate, setDeliveryDate] = useState(() => isoDate(new Date(Date.now() + 2 * 86_400_000)));
  const [notes, setNotes] = useState('');
  const [mode, setMode] = useState<Mode>('once');
  const [deliveryDay, setDeliveryDay] = useState('THURSDAY');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiClient
      .get<DeliveryAddress[]>('/api/v1/restaurants/me/delivery-addresses', token)
      .then((list) => {
        setAddresses(list);
        setDeliveryAddressId((current) => current || list.find((address) => address.primary)?.id || list[0]?.id || '');
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Não foi possível carregar seus endereços.'));
  }, [token]);

  const groups = useMemo(() => {
    const byProducer = new Map<string, CartItem[]>();
    cart.items.forEach((item) => byProducer.set(item.producerId, [...(byProducer.get(item.producerId) ?? []), item]));
    return [...byProducer.values()];
  }, [cart.items]);

  const lineTotal = (item: CartItem) => (item.proposedUnitPrice ?? item.price) * item.quantity;
  const total = cart.items.reduce((sum, item) => sum + lineTotal(item), 0);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    if (!deliveryAddressId) {
      setError('Cadastre um endereço de entrega em "Editar cadastro".');
      return;
    }
    setSubmitting(true);
    try {
      if (mode === 'once') {
        await apiClient.post<OrderResponse[]>(
          '/api/v1/orders/checkout',
          {
            deliveryAddressId,
            requestedDeliveryDate: deliveryDate,
            notes: notes || undefined,
            items: cart.items.map((item) => ({
              offerId: item.offerId,
              quantity: item.quantity,
              proposedUnitPrice: item.proposedUnitPrice ?? undefined,
            })),
          },
          token,
        );
        cart.clear();
        navigate('/pedidos', { state: { justPlaced: true } });
      } else {
        await apiClient.post(
          '/api/v1/recurring-orders',
          {
            deliveryAddressId,
            deliveryDay,
            notes: notes || undefined,
            items: cart.items.map((item) => ({ offerId: item.offerId, quantity: item.quantity })),
          },
          token,
        );
        cart.clear();
        navigate('/pedidos-recorrentes');
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível finalizar o pedido.');
    } finally {
      setSubmitting(false);
    }
  };

  if (cart.items.length === 0) {
    return (
      <div className="page">
        <h1>Carrinho</h1>
        <p className="admin-empty">
          Seu carrinho está vazio. <Link to="/catalogo">Ir para o catálogo</Link>
        </p>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>Carrinho</h1>
      <p className="admin-subtitle">
        Cada produtor recebe um pedido separado e pode aceitar, recusar ou fazer uma contraproposta.
      </p>

      {groups.map((items) => (
        <section className="card-form cart-group" key={items[0].producerId}>
          <h2>{items[0].producerName ?? 'Produtor'}</h2>
          {items.map((item) => (
            <div className="cart-line" key={item.offerId}>
              <div className="cart-line-name">
                <strong>{item.productName}</strong>
                <span className="admin-row-subtitle">
                  {formatMoney(item.price)} / {MEASUREMENT_UNIT_LABELS[item.unit] ?? item.unit} · disponível:{' '}
                  {formatQuantity(item.quantityAvailable)}
                </span>
              </div>
              <label>
                Quantidade
                <input
                  type="number"
                  min="0.001"
                  step="any"
                  max={item.quantityAvailable}
                  value={item.quantity}
                  onChange={(e) => cart.update(item.offerId, { quantity: Number(e.target.value) })}
                  required
                />
              </label>
              <label>
                Propor preço (opcional)
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  placeholder={item.price.toFixed(2)}
                  value={item.proposedUnitPrice ?? ''}
                  disabled={mode === 'recurring'}
                  onChange={(e) =>
                    cart.update(item.offerId, {
                      proposedUnitPrice: e.target.value ? Number(e.target.value) : null,
                    })
                  }
                />
              </label>
              <span className="cart-line-total">{formatMoney(lineTotal(item))}</span>
              <button type="button" className="admin-action-ghost" onClick={() => cart.remove(item.offerId)}>
                Remover
              </button>
            </div>
          ))}
        </section>
      ))}

      <form className="card-form" onSubmit={handleSubmit}>
        <fieldset>
          <legend>Entrega</legend>
          <label>
            Endereço de entrega
            <select value={deliveryAddressId} onChange={(e) => setDeliveryAddressId(e.target.value)} required>
              {addresses.map((address) => (
                <option key={address.id} value={address.id}>
                  {address.label} — {address.address.street}, {address.address.number} ({address.address.city})
                </option>
              ))}
            </select>
          </label>
          <div className="settings-categories">
            <label>
              <input type="radio" checked={mode === 'once'} onChange={() => setMode('once')} />
              Pedido único
            </label>
            <label>
              <input type="radio" checked={mode === 'recurring'} onChange={() => setMode('recurring')} />
              Repetir toda semana
            </label>
          </div>
          {mode === 'once' ? (
            <label>
              Data de entrega desejada
              <input
                type="date"
                min={isoDate(new Date())}
                value={deliveryDate}
                onChange={(e) => setDeliveryDate(e.target.value)}
                required
              />
            </label>
          ) : (
            <>
              <label>
                Dia da entrega
                <select value={deliveryDay} onChange={(e) => setDeliveryDay(e.target.value)}>
                  {Object.entries(DAY_OF_WEEK_LABELS).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </label>
              <p className="form-notice">
                Os pedidos de cada semana são gerados 2 dias antes da entrega, com o preço vigente das ofertas.
                Para suspender, avise com pelo menos 48 horas de antecedência.
              </p>
            </>
          )}
          <label>
            Observações para os produtores (opcional)
            <textarea rows={2} maxLength={1000} value={notes} onChange={(e) => setNotes(e.target.value)} />
          </label>
        </fieldset>
        <p className="cart-total">
          Total estimado: <strong>{formatMoney(total)}</strong>
        </p>
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Enviando...' : mode === 'once' ? 'Fazer pedido' : 'Criar pedido recorrente'}
        </button>
      </form>
    </div>
  );
}
