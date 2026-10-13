import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { usePlatformCoverage, type PlatformCoverage } from '../api/platform';
import type { CatalogEntryResponse, ProductCategory } from '../api/types';
import { OfferComparison } from '../components/OfferComparison';
import {
  EMPTY_SERVER_FILTERS,
  matchesSearch,
  sortEntries,
  useCatalog,
  type ServerFilters,
  type SortOption,
} from './catalog';
import { AtacadoLayout } from './layouts/AtacadoLayout';
import { FeiraLayout } from './layouts/FeiraLayout';
import { VitrineLayout } from './layouts/VitrineLayout';
import './marketplace.css';

export type LayoutId = 'feira' | 'vitrine' | 'atacado';

const LAYOUTS: { id: LayoutId; name: string; hint: string }[] = [
  { id: 'feira', name: 'Feira', hint: 'Grade de produtos com categorias no topo' },
  { id: 'vitrine', name: 'Vitrine', hint: 'Prateleiras por categoria e produtores em destaque' },
  { id: 'atacado', name: 'Atacado', hint: 'Lista densa com filtros laterais, para compra em volume' },
];

// Estado compartilhado pelos três layouts: o mesmo catálogo, filtros e carrinho, só a apresentação muda.
export interface MarketplaceView {
  // Ofertas após filtros do servidor e busca: base da faixa de produtores.
  searchMatches: CatalogEntryResponse[];
  // Ofertas após filtros do servidor, busca e produtor (sem o filtro de categoria): base de contagens e prateleiras.
  entries: CatalogEntryResponse[];
  // Ofertas exibidas: `entries` filtradas pela categoria e ordenadas.
  results: CatalogEntryResponse[];
  loading: boolean;
  error: string | null;
  search: string;
  category: ProductCategory | '';
  setCategory: (category: ProductCategory | '') => void;
  producerId: string;
  setProducerId: (producerId: string) => void;
  sort: SortOption;
  setSort: (sort: SortOption) => void;
  serverFilters: ServerFilters;
  setServerFilters: (filters: ServerFilters) => void;
  coverage: PlatformCoverage | null;
  hasActiveFilters: boolean;
  clearAll: () => void;
  resetCount: number;
  onCompare: (offerId: string) => void;
}

// RN47 — vitrine pública do marketplace: qualquer visitante navega, busca e monta o carrinho sem cadastro.
export function MarketplacePage() {
  const [params, setParams] = useSearchParams();
  const layoutParam = params.get('layout');
  const layout: LayoutId = LAYOUTS.some((item) => item.id === layoutParam) ? (layoutParam as LayoutId) : 'feira';
  const search = params.get('q') ?? '';

  const [serverFilters, setServerFilters] = useState<ServerFilters>(EMPTY_SERVER_FILTERS);
  const [category, setCategory] = useState<ProductCategory | ''>('');
  const [producerId, setProducerId] = useState('');
  const [sort, setSort] = useState<SortOption>('relevance');
  const [resetCount, setResetCount] = useState(0);
  const [comparingOfferId, setComparingOfferId] = useState<string | null>(null);
  const coverage = usePlatformCoverage();
  const catalog = useCatalog(serverFilters);

  const searchMatches = useMemo(
    () => catalog.entries.filter((entry) => matchesSearch(entry, search)),
    [catalog.entries, search],
  );
  const entries = useMemo(
    () => (producerId ? searchMatches.filter((entry) => entry.producerId === producerId) : searchMatches),
    [searchMatches, producerId],
  );
  const results = useMemo(
    () => sortEntries(category ? entries.filter((entry) => entry.offer.category === category) : entries, sort),
    [entries, category, sort],
  );

  const hasActiveFilters =
    search !== '' ||
    category !== '' ||
    producerId !== '' ||
    Object.values(serverFilters).some((value) => value !== '');

  const clearAll = () => {
    setServerFilters(EMPTY_SERVER_FILTERS);
    setCategory('');
    setProducerId('');
    setResetCount((count) => count + 1);
    if (search) {
      setParams((prev) => {
        const next = new URLSearchParams(prev);
        next.delete('q');
        return next;
      });
    }
  };

  const view: MarketplaceView = {
    searchMatches,
    entries,
    results,
    loading: catalog.loading,
    error: catalog.error,
    search,
    category,
    setCategory,
    producerId,
    setProducerId,
    sort,
    setSort,
    serverFilters,
    setServerFilters,
    coverage,
    hasActiveFilters,
    clearAll,
    resetCount,
    onCompare: setComparingOfferId,
  };

  const chooseLayout = (id: LayoutId) =>
    setParams((prev) => {
      const next = new URLSearchParams(prev);
      next.set('layout', id);
      return next;
    });

  return (
    <div className="mk">
      {layout === 'feira' && <FeiraLayout view={view} />}
      {layout === 'vitrine' && <VitrineLayout view={view} />}
      {layout === 'atacado' && <AtacadoLayout view={view} />}

      {comparingOfferId && (
        <ComparisonDialog offerId={comparingOfferId} onClose={() => setComparingOfferId(null)} />
      )}

      {/* Protótipo: escolha entre os três layouts. Depois de decidir, basta remover este seletor. */}
      <nav className="mk-switcher" aria-label="Escolher layout do catálogo">
        <span className="mk-switcher-label">Layout</span>
        {LAYOUTS.map((item, index) => (
          <button
            key={item.id}
            type="button"
            title={item.hint}
            aria-pressed={layout === item.id}
            onClick={() => chooseLayout(item.id)}
          >
            {index + 1}. {item.name}
          </button>
        ))}
      </nav>
    </div>
  );
}

// RF17 — comparação de ofertas equivalentes por cima do catálogo.
function ComparisonDialog({ offerId, onClose }: { offerId: string; onClose: () => void }) {
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  return (
    <div className="mk-modal" role="dialog" aria-modal="true" aria-label="Comparar ofertas" onClick={onClose}>
      <div className="mk-modal-panel" onClick={(event) => event.stopPropagation()}>
        <OfferComparison offerId={offerId} onClose={onClose} />
      </div>
    </div>
  );
}
