import { useEffect, useState } from 'react';
import { apiClient, ApiError } from '../api/client';
import { MEASUREMENT_UNIT_LABELS } from '../api/labels';
import { formatDate, formatMoney, formatQuantity, type ProducerFulfillment } from '../api/orders';
import type { CatalogEntryResponse } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { AddToCart } from './AddToCart';

// RF17 — ofertas equivalentes (mesmo produto, produtores diferentes) lado a lado: preço, prazo, origem,
// distância e taxa de cumprimento do produtor (a avaliação por notas virá com o módulo de reputação — RF33).
export function OfferComparison({ offerId, onClose }: { offerId: string; onClose: () => void }) {
  const { token, claims } = useAuth();
  const [entries, setEntries] = useState<CatalogEntryResponse[] | null>(null);
  const [fulfillment, setFulfillment] = useState<Record<string, ProducerFulfillment>>({});
  const [error, setError] = useState<string | null>(null);
  const canBuy = claims?.role === 'RESTAURANT';

  useEffect(() => {
    let cancelled = false;
    apiClient
      .get<CatalogEntryResponse[]>(`/api/v1/catalog/offers/${offerId}/equivalents`, token)
      .then(async (list) => {
        if (cancelled) return;
        setEntries(list);
        const producers = [...new Set(list.map((entry) => entry.producerId))];
        const results = await Promise.all(
          producers.map((producerId) =>
            apiClient
              .get<ProducerFulfillment>(`/api/v1/producers/${producerId}/fulfillment`, token)
              .catch(() => null),
          ),
        );
        if (!cancelled) {
          setFulfillment(
            Object.fromEntries(results.filter((result) => result !== null).map((result) => [result!.producerId, result!])),
          );
        }
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Não foi possível comparar as ofertas.'));
    return () => {
      cancelled = true;
    };
  }, [offerId, token]);

  return (
    <section className="card-form comparison">
      <div className="admin-row-info">
        <h2>Comparar ofertas equivalentes</h2>
        <button type="button" className="admin-action-ghost" onClick={onClose}>
          Fechar
        </button>
      </div>
      {error && <p className="form-error">{error}</p>}
      {!entries ? (
        <p>Carregando...</p>
      ) : entries.length <= 1 ? (
        <p className="admin-empty">Nenhuma outra oferta equivalente no catálogo agora.</p>
      ) : (
        <div className="comparison-scroll">
          <table className="metric-table">
            <thead>
              <tr>
                <th>Produto / produtor</th>
                <th>Origem</th>
                <th>Preço</th>
                <th>Disponível</th>
                <th>Prazo</th>
                <th>Distância</th>
                <th>Cumprimento</th>
                {canBuy && <th />}
              </tr>
            </thead>
            <tbody>
              {entries.map((entry) => {
                const stats = fulfillment[entry.producerId];
                return (
                  <tr key={entry.offer.id} className={entry.offer.id === offerId ? 'comparison-reference' : ''}>
                    <td>
                      <strong>{entry.offer.productName}</strong>
                      <br />
                      {entry.producerName ?? '—'}
                    </td>
                    <td>{entry.producerCity ?? '—'}</td>
                    <td>
                      {formatMoney(entry.offer.price)} / {MEASUREMENT_UNIT_LABELS[entry.offer.unit] ?? entry.offer.unit}
                    </td>
                    <td>{formatQuantity(entry.offer.quantityAvailable)}</td>
                    <td>
                      {entry.offer.availabilityFrom ? `de ${formatDate(entry.offer.availabilityFrom)} ` : ''}
                      {entry.offer.availabilityUntil ? `até ${formatDate(entry.offer.availabilityUntil)}` : 'sem prazo'}
                    </td>
                    <td>
                      {entry.distanceKilometers != null
                        ? `${entry.distanceKilometers.toFixed(1)} km${entry.distanceDurationMinutes != null ? ` (${entry.distanceDurationMinutes} min)` : ''}`
                        : '—'}
                    </td>
                    <td>
                      {stats?.fulfillmentRate != null
                        ? `${stats.fulfillmentRate}% (${stats.deliveredOrders} entregas)`
                        : 'sem histórico'}
                    </td>
                    {canBuy && (
                      <td>
                        <AddToCart entry={entry} compact />
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
