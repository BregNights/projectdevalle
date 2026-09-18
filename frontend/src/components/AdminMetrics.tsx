import { useEffect, useState } from 'react';
import { apiClient, ApiError } from '../api/client';
import { CATEGORY_LABELS, PRODUCTION_TYPE_LABELS, ROLE_LABELS, STATUS_LABELS } from '../api/labels';
import type { AdminMetricsResponse } from '../api/types';
import { useAuth } from '../auth/AuthContext';

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function Breakdown({ title, data, labels }: { title: string; data: Record<string, number>; labels: Record<string, string> }) {
  const total = Object.values(data).reduce((sum, value) => sum + value, 0);
  const entries = Object.entries(data).filter(([, value]) => value > 0);

  return (
    <div className="metric-breakdown">
      <h3>{title}</h3>
      {entries.length === 0 ? (
        <p className="admin-empty">Sem dados ainda.</p>
      ) : (
        <ul className="metric-bars">
          {entries.map(([key, value]) => (
            <li key={key}>
              <div className="metric-bar-label">
                <span>{labels[key] ?? key}</span>
                <span>{value}</span>
              </div>
              <div className="metric-bar-track">
                <div className="metric-bar-fill" style={{ width: `${total > 0 ? (value / total) * 100 : 0}%` }} />
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function AdminMetrics() {
  const { token } = useAuth();
  const [metrics, setMetrics] = useState<AdminMetricsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      setError(null);
      try {
        const data = await apiClient.get<AdminMetricsResponse>('/api/v1/admin/metrics', token);
        if (!cancelled) setMetrics(data);
      } catch (err) {
        if (!cancelled) setError(err instanceof ApiError ? err.message : 'Não foi possível carregar as métricas.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [token]);

  if (loading) return <p>Carregando métricas...</p>;
  if (error) return <p className="form-error">{error}</p>;
  if (!metrics) return null;

  const approvedProducers = metrics.producersByStatus.APPROVED ?? 0;
  const rejectedProducers = metrics.producersByStatus.REJECTED ?? 0;
  const producerDecisions = approvedProducers + rejectedProducers;
  const approvalRate = producerDecisions > 0 ? Math.round((approvedProducers / producerDecisions) * 100) : null;

  return (
    <div className="admin-metrics">
      <div className="metric-cards">
        <div className="metric-card">
          <span className="metric-value">{metrics.totalProducers}</span>
          <span className="metric-label">Produtores cadastrados</span>
        </div>
        <div className="metric-card">
          <span className="metric-value">{metrics.totalRestaurants}</span>
          <span className="metric-label">Restaurantes cadastrados</span>
        </div>
        <div className="metric-card">
          <span className="metric-value">
            {metrics.activeUsers}
            <span className="metric-value-total">/{metrics.totalUsers}</span>
          </span>
          <span className="metric-label">Usuários ativos</span>
        </div>
        <div className="metric-card">
          <span className="metric-value">{approvalRate !== null ? `${approvalRate}%` : '—'}</span>
          <span className="metric-label">Taxa de aprovação (produtores)</span>
        </div>
        <div className="metric-card">
          <span className="metric-value">{metrics.newProducersLast7Days + metrics.newRestaurantsLast7Days}</span>
          <span className="metric-label">Novos cadastros (7 dias)</span>
        </div>
        <div className="metric-card">
          <span className="metric-value">{metrics.producersGeocodingPending}</span>
          <span className="metric-label">Produtores sem localização resolvida</span>
        </div>
      </div>

      <div className="metric-grid">
        <Breakdown title="Produtores por status" data={metrics.producersByStatus} labels={STATUS_LABELS} />
        <Breakdown title="Restaurantes por status" data={metrics.restaurantsByStatus} labels={STATUS_LABELS} />
        <Breakdown title="Tipo de produção" data={metrics.producersByProductionType} labels={PRODUCTION_TYPE_LABELS} />
        <Breakdown title="Categoria de restaurante" data={metrics.restaurantsByCategory} labels={CATEGORY_LABELS} />
      </div>

      <div className="metric-grid">
        <div className="metric-breakdown">
          <h3>Produtores por cidade</h3>
          {Object.keys(metrics.topProducerCities).length === 0 ? (
            <p className="admin-empty">Sem dados ainda.</p>
          ) : (
            <ul className="metric-list">
              {Object.entries(metrics.topProducerCities).map(([city, count]) => (
                <li key={city}>
                  <span>{city}</span>
                  <strong>{count}</strong>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="metric-breakdown">
          <h3>Cadastros recentes (30 dias)</h3>
          <ul className="metric-list">
            <li>
              <span>Produtores</span>
              <strong>{metrics.newProducersLast30Days}</strong>
            </li>
            <li>
              <span>Restaurantes</span>
              <strong>{metrics.newRestaurantsLast30Days}</strong>
            </li>
          </ul>
        </div>
      </div>

      <div className="metric-breakdown">
        <h3>Usuários recentes</h3>
        <table className="metric-table">
          <thead>
            <tr>
              <th>E-mail</th>
              <th>Papel</th>
              <th>Status</th>
              <th>Criado em</th>
            </tr>
          </thead>
          <tbody>
            {metrics.recentUsers.map((user) => (
              <tr key={user.email}>
                <td>{user.email}</td>
                <td>{ROLE_LABELS[user.role] ?? user.role}</td>
                <td>{user.active ? 'Ativo' : 'Inativo'}</td>
                <td>{formatDate(user.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
