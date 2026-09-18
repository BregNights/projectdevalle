import { useEffect, useState } from 'react';
import { apiClient, ApiError } from '../api/client';
import { CATEGORY_LABELS, CERTIFICATION_LABELS, PRODUCTION_TYPE_LABELS } from '../api/labels';
import type { ProducerResponse, RestaurantResponse } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { StatusBadge } from '../components/StatusBadge';
import { AdminPanel } from '../components/AdminPanel';

export function DashboardPage() {
  const { token, claims } = useAuth();
  const [producer, setProducer] = useState<ProducerResponse | null>(null);
  const [restaurant, setRestaurant] = useState<RestaurantResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token || !claims) return;

    async function load() {
      setLoading(true);
      setError(null);
      try {
        if (claims!.role === 'PRODUCER') {
          setProducer(await apiClient.get<ProducerResponse>('/api/v1/producers/me', token));
        } else if (claims!.role === 'RESTAURANT') {
          setRestaurant(await apiClient.get<RestaurantResponse>('/api/v1/restaurants/me', token));
        }
      } catch (err) {
        setError(err instanceof ApiError ? err.message : 'Não foi possível carregar seu cadastro.');
      } finally {
        setLoading(false);
      }
    }

    load();
  }, [token, claims]);

  if (!claims) return null;

  if (loading) {
    return (
      <div className="page">
        <p>Carregando...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page">
        <p className="form-error">{error}</p>
      </div>
    );
  }

  if (claims.role === 'ADMINISTRATOR') {
    return <AdminPanel />;
  }

  if (claims.role === 'LOGISTICS_OPERATOR') {
    return (
      <div className="page">
        <h1>Painel de logística</h1>
        <p>Ainda não existe uma interface para este perfil.</p>
      </div>
    );
  }

  if (producer) {
    return (
      <div className="page">
        <div className="dashboard-card">
          <h1>{producer.name}</h1>
          <StatusBadge status={producer.status} />
          <div className="dashboard-meta">
            <span>
              Tipo de produção: <strong>{PRODUCTION_TYPE_LABELS[producer.productionType] ?? producer.productionType}</strong>
            </span>
          </div>
          <div className="cert-chips">
            {producer.visibleCertifications.length > 0 ? (
              producer.visibleCertifications.map((cert) => (
                <span className="cert-chip" key={cert}>
                  ✓ {CERTIFICATION_LABELS[cert] ?? cert}
                </span>
              ))
            ) : (
              <span className="admin-empty">Nenhuma certificação verificada ainda.</span>
            )}
          </div>
          {producer.geocodingPending && (
            <p className="form-notice">
              Ainda não localizamos seu endereço no mapa. Isso será resolvido antes da aprovação do seu cadastro.
            </p>
          )}
        </div>
      </div>
    );
  }

  if (restaurant) {
    return (
      <div className="page">
        <div className="dashboard-card">
          <h1>{restaurant.corporateName}</h1>
          <StatusBadge status={restaurant.status} />
          <div className="dashboard-meta">
            <span>
              Categoria: <strong>{CATEGORY_LABELS[restaurant.category] ?? restaurant.category}</strong>
            </span>
            <span>
              Endereços de entrega: <strong>{restaurant.deliveryAddressCount}</strong>
            </span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <p>Nenhum cadastro encontrado para esta conta.</p>
    </div>
  );
}
