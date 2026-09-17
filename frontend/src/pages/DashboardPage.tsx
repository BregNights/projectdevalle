import { useEffect, useState } from 'react';
import { apiClient, ApiError } from '../api/client';
import type { ProducerResponse, RestaurantResponse } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { StatusBadge } from '../components/StatusBadge';

const PRODUCTION_TYPE_LABELS: Record<string, string> = {
  FARMING: 'Agricultura',
  FISHING: 'Pesca',
  LIVESTOCK: 'Pecuária',
  ARTISANAL_PROCESSING: 'Processamento artesanal',
};

const CATEGORY_LABELS: Record<string, string> = {
  FINE_DINING: 'Alta gastronomia',
  BISTRO: 'Bistrô',
  CHAIN: 'Rede',
  OTHER: 'Outro',
};

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

  if (claims.role === 'ADMINISTRATOR' || claims.role === 'LOGISTICS_OPERATOR') {
    return (
      <div className="page">
        <h1>Painel de {claims.role === 'ADMINISTRATOR' ? 'administração' : 'logística'}</h1>
        <p>Ainda não existe uma interface para este perfil.</p>
      </div>
    );
  }

  if (producer) {
    return (
      <div className="page">
        <h1>{producer.name}</h1>
        <p>
          Status do cadastro: <StatusBadge status={producer.status} />
        </p>
        <p>Tipo de produção: {PRODUCTION_TYPE_LABELS[producer.productionType] ?? producer.productionType}</p>
        {producer.geocodingPending && (
          <p className="form-notice">
            Ainda não localizamos seu endereço no mapa. Isso será resolvido antes da aprovação do seu cadastro.
          </p>
        )}
        <p>
          Certificações visíveis:{' '}
          {producer.visibleCertifications.length > 0 ? producer.visibleCertifications.join(', ') : 'nenhuma'}
        </p>
      </div>
    );
  }

  if (restaurant) {
    return (
      <div className="page">
        <h1>{restaurant.corporateName}</h1>
        <p>
          Status do cadastro: <StatusBadge status={restaurant.status} />
        </p>
        <p>Categoria: {CATEGORY_LABELS[restaurant.category] ?? restaurant.category}</p>
        <p>Endereços de entrega cadastrados: {restaurant.deliveryAddressCount}</p>
      </div>
    );
  }

  return (
    <div className="page">
      <p>Nenhum cadastro encontrado para esta conta.</p>
    </div>
  );
}
