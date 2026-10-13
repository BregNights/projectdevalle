import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { SORT_LABELS, type SortOption } from './catalog';
import type { MarketplaceView } from './MarketplacePage';
import { ProductCard, ProductCardSkeleton } from './ProductCard';

export function SortSelect({ view }: { view: MarketplaceView }) {
  const hasDistance = view.entries.some((entry) => entry.distanceKilometers != null);
  const options = (Object.keys(SORT_LABELS) as SortOption[]).filter(
    (option) => option !== 'distance' || hasDistance,
  );
  return (
    <label className="mk-sort">
      <span>Ordenar por</span>
      <select value={view.sort} onChange={(e) => view.setSort(e.target.value as SortOption)}>
        {options.map((option) => (
          <option key={option} value={option}>
            {SORT_LABELS[option]}
          </option>
        ))}
      </select>
    </label>
  );
}

export function EmptyState({ view }: { view: MarketplaceView }) {
  return (
    <div className="mk-empty">
      <span className="mk-empty-icon" aria-hidden="true">
        🧺
      </span>
      <h3>Nenhuma oferta encontrada</h3>
      <p>
        {view.hasActiveFilters
          ? 'Tente outra busca ou remova alguns filtros.'
          : 'Ainda não há ofertas publicadas na região. Volte em breve.'}
      </p>
      {view.hasActiveFilters && (
        <button type="button" className="mk-button-ghost" onClick={view.clearAll}>
          Limpar filtros
        </button>
      )}
    </div>
  );
}

// Grade de cards com esqueleto durante o carregamento e estado vazio.
export function ResultsGrid({ view }: { view: MarketplaceView }) {
  if (view.error) {
    return <p className="form-error">{view.error}</p>;
  }
  if (view.loading && view.results.length === 0) {
    return (
      <div className="mk-grid">
        {Array.from({ length: 8 }, (_, index) => (
          <ProductCardSkeleton key={index} />
        ))}
      </div>
    );
  }
  if (view.results.length === 0) {
    return <EmptyState view={view} />;
  }
  return (
    <div className="mk-grid">
      {view.results.map((entry) => (
        <ProductCard key={entry.offer.id} entry={entry} onCompare={view.onCompare} />
      ))}
    </div>
  );
}

// RN33 — a distância só aparece para restaurante autenticado; o visitante é avisado de como vê-la.
export function DistanceHint() {
  const { token } = useAuth();
  if (token) return null;
  return (
    <p className="mk-hint">
      📍 <Link to="/login">Entre como restaurante</Link> para ver a distância de cada produtor até a sua cozinha.
    </p>
  );
}
