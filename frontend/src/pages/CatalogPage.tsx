import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { apiClient, ApiError } from '../api/client';
import { CERTIFICATION_LABELS, MEASUREMENT_UNIT_LABELS, PRODUCT_CATEGORY_LABELS } from '../api/labels';
import type { CatalogEntryResponse, ProductCategory } from '../api/types';
import { useAuth } from '../auth/AuthContext';

const CATEGORY_OPTIONS = Object.keys(PRODUCT_CATEGORY_LABELS) as ProductCategory[];
const CERTIFICATION_OPTIONS = Object.keys(CERTIFICATION_LABELS);

interface FilterState {
  category: ProductCategory | '';
  city: string;
  certificationType: string;
  minPrice: string;
  maxPrice: string;
}

const INITIAL_FILTERS: FilterState = {
  category: '',
  city: '',
  certificationType: '',
  minPrice: '',
  maxPrice: '',
};

export function CatalogPage() {
  const { token } = useAuth();
  const [filters, setFilters] = useState<FilterState>(INITIAL_FILTERS);
  const [entries, setEntries] = useState<CatalogEntryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const update = <K extends keyof FilterState>(key: K, value: FilterState[K]) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  const search = useCallback(
    async (current: FilterState) => {
      setLoading(true);
      setError(null);
      try {
        const params = new URLSearchParams();
        if (current.category) params.set('category', current.category);
        if (current.city) params.set('city', current.city);
        if (current.certificationType) params.set('certificationType', current.certificationType);
        if (current.minPrice) params.set('minPrice', current.minPrice);
        if (current.maxPrice) params.set('maxPrice', current.maxPrice);
        const query = params.toString();
        const list = await apiClient.get<CatalogEntryResponse[]>(
          `/api/v1/catalog${query ? `?${query}` : ''}`,
          token,
        );
        setEntries(list);
      } catch (err) {
        setError(err instanceof ApiError ? err.message : 'Não foi possível carregar o catálogo.');
      } finally {
        setLoading(false);
      }
    },
    [token],
  );

  useEffect(() => {
    search(INITIAL_FILTERS);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    search(filters);
  };

  return (
    <div className="page">
      <h1>Catálogo</h1>
      <p className="admin-subtitle">Busque ofertas de produtores por categoria, cidade, certificação ou preço.</p>

      <form className="card-form catalog-filters" onSubmit={handleSubmit}>
        <label>
          Categoria
          <select value={filters.category} onChange={(e) => update('category', e.target.value as ProductCategory | '')}>
            <option value="">Todas</option>
            {CATEGORY_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {PRODUCT_CATEGORY_LABELS[option]}
              </option>
            ))}
          </select>
        </label>
        <label>
          Cidade
          <input value={filters.city} onChange={(e) => update('city', e.target.value)} placeholder="ex.: Blumenau" />
        </label>
        <label>
          Certificação
          <select value={filters.certificationType} onChange={(e) => update('certificationType', e.target.value)}>
            <option value="">Qualquer</option>
            {CERTIFICATION_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {CERTIFICATION_LABELS[option]}
              </option>
            ))}
          </select>
        </label>
        <label>
          Preço mínimo (R$)
          <input type="number" min="0" step="0.01" value={filters.minPrice} onChange={(e) => update('minPrice', e.target.value)} />
        </label>
        <label>
          Preço máximo (R$)
          <input type="number" min="0" step="0.01" value={filters.maxPrice} onChange={(e) => update('maxPrice', e.target.value)} />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Buscando...' : 'Buscar'}
        </button>
      </form>

      {error && <p className="form-error">{error}</p>}

      {!loading && entries.length === 0 && !error && (
        <p className="admin-empty">Nenhuma oferta encontrada com esses filtros.</p>
      )}

      <div className="catalog-grid">
        {entries.map((entry) => (
          <div className="catalog-card" key={entry.offer.id}>
            <div className="admin-row-info">
              <strong>{entry.offer.productName}</strong>
            </div>
            <span className="admin-row-subtitle">
              {PRODUCT_CATEGORY_LABELS[entry.offer.category] ?? entry.offer.category}
            </span>
            <div className="catalog-card-price">
              R$ {entry.offer.price.toFixed(2)} / {MEASUREMENT_UNIT_LABELS[entry.offer.unit] ?? entry.offer.unit}
            </div>
            <div className="dashboard-meta">
              <span>
                Produtor: <strong>{entry.producerName ?? '—'}</strong>
              </span>
              {entry.producerCity && (
                <span>
                  Cidade: <strong>{entry.producerCity}</strong>
                </span>
              )}
              <span>
                Disponível: <strong>{entry.offer.quantityAvailable}</strong>
              </span>
              {entry.distanceKilometers != null && (
                <span>
                  Distância: <strong>{entry.distanceKilometers.toFixed(1)} km</strong>
                  {entry.distanceDurationMinutes != null && ` (${entry.distanceDurationMinutes} min)`}
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
