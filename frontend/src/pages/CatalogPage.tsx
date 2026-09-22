import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { apiClient, ApiError } from '../api/client';
import { publicFileSrc } from '../api/files';
import { usePlatformCoverage } from '../api/platform';
import { CERTIFICATION_LABELS, MEASUREMENT_UNIT_LABELS, PRODUCT_CATEGORY_LABELS } from '../api/labels';
import type { CatalogEntryResponse, ProductCategory } from '../api/types';
import { useAuth } from '../auth/AuthContext';

const CATEGORY_OPTIONS = Object.keys(PRODUCT_CATEGORY_LABELS) as ProductCategory[];
const CERTIFICATION_OPTIONS = Object.keys(CERTIFICATION_LABELS);

interface FilterState {
  category: ProductCategory | '';
  region: string;
  city: string;
  certificationType: string;
  minPrice: string;
  maxPrice: string;
  availableBy: string;
}

const INITIAL_FILTERS: FilterState = {
  category: '',
  region: '',
  city: '',
  certificationType: '',
  minPrice: '',
  maxPrice: '',
  availableBy: '',
};

export function CatalogPage() {
  const { token } = useAuth();
  const [filters, setFilters] = useState<FilterState>(INITIAL_FILTERS);
  const [entries, setEntries] = useState<CatalogEntryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const coverage = usePlatformCoverage();
  const categoryOptions = coverage
    ? CATEGORY_OPTIONS.filter((option) => coverage.enabledProductCategories.includes(option))
    : CATEGORY_OPTIONS;
  const cityOptions = coverage
    ? coverage.coverageRegions
        .filter((region) => !filters.region || region.name === filters.region)
        .flatMap((region) => region.cities)
    : [];

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
        if (current.region) params.set('region', current.region);
        if (current.city) params.set('city', current.city);
        if (current.certificationType) params.set('certificationType', current.certificationType);
        if (current.minPrice) params.set('minPrice', current.minPrice);
        if (current.maxPrice) params.set('maxPrice', current.maxPrice);
        if (current.availableBy) params.set('availableBy', current.availableBy);
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
      <p className="admin-subtitle">Busque ofertas de produtores por categoria, região, cidade, certificação, preço ou prazo.</p>

      <form className="card-form catalog-filters" onSubmit={handleSubmit}>
        <label>
          Categoria
          <select value={filters.category} onChange={(e) => update('category', e.target.value as ProductCategory | '')}>
            <option value="">Todas</option>
            {categoryOptions.map((option) => (
              <option key={option} value={option}>
                {PRODUCT_CATEGORY_LABELS[option]}
              </option>
            ))}
          </select>
        </label>
        <label>
          Região
          <select value={filters.region} onChange={(e) => update('region', e.target.value)}>
            <option value="">Todas</option>
            {coverage?.coverageRegions.map((region) => (
              <option key={region.name} value={region.name}>
                {region.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Cidade
          <input
            value={filters.city}
            onChange={(e) => update('city', e.target.value)}
            placeholder="ex.: Blumenau"
            list="catalog-cities"
          />
          <datalist id="catalog-cities">
            {cityOptions.map((city) => (
              <option key={city} value={city} />
            ))}
          </datalist>
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
        <label>
          Preciso até
          <input type="date" value={filters.availableBy} onChange={(e) => update('availableBy', e.target.value)} />
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
            {entry.offer.photoUrls.length > 0 && (
              <img className="catalog-photo" src={publicFileSrc(entry.offer.photoUrls[0])} alt={entry.offer.productName} />
            )}
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
              {(entry.offer.availabilityFrom || entry.offer.availabilityUntil) && (
                <span>
                  Janela: <strong>{formatWindow(entry.offer.availabilityFrom, entry.offer.availabilityUntil)}</strong>
                </span>
              )}
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

function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-');
  return `${day}/${month}/${year}`;
}

function formatWindow(from: string | null, until: string | null): string {
  if (from && until) return `${formatDate(from)} a ${formatDate(until)}`;
  if (from) return `a partir de ${formatDate(from)}`;
  return `até ${formatDate(until as string)}`;
}
