import { useState } from 'react';
import { CERTIFICATION_LABELS, PRODUCT_CATEGORY_LABELS } from '../../api/labels';
import { formatMoney } from '../../api/orders';
import type { CatalogEntryResponse } from '../../api/types';
import { useCart } from '../../cart/CartContext';
import { CartStepper } from '../CartStepper';
import {
  CATEGORY_ORDER,
  availabilityLabel,
  countLabel,
  distanceLabel,
  initials,
  stockLabel,
  unitLabel,
  type ServerFilters,
} from '../catalog';
import type { MarketplaceView } from '../MarketplacePage';
import { DistanceHint, EmptyState, SortSelect } from '../parts';
import { ProductImage } from '../ProductImage';

// Layout 3 — "Atacado": filtros laterais e lista densa, pensado para o comprador de restaurante que repõe em
// volume e compara preço por unidade, prazo e estoque de relance (RF10).
export function AtacadoLayout({ view }: { view: MarketplaceView }) {
  const [filtersOpen, setFiltersOpen] = useState(false);
  // Produtor e administrador não compram: a coluna de quantidade some.
  const canBuy = useCart().enabled;
  const activeFilterCount = Object.values(view.serverFilters).filter((value) => value !== '').length +
    (view.category ? 1 : 0);

  return (
    <div className="mk-inner atacado">
      <button
        type="button"
        className="atacado-filters-toggle mk-button-ghost"
        aria-expanded={filtersOpen}
        aria-controls="atacado-sidebar"
        onClick={() => setFiltersOpen((open) => !open)}
      >
        {filtersOpen ? 'Fechar filtros' : 'Filtros'}
        {activeFilterCount > 0 ? ` (${activeFilterCount})` : ''}
      </button>

      <aside id="atacado-sidebar" className={`atacado-sidebar${filtersOpen ? ' is-open' : ''}`}>
        {/* Remonta quando os preços mudam por fora (chip removido, limpar filtros), para os campos não ficarem velhos. */}
        <FilterSidebar
          view={view}
          key={`${view.resetCount}:${view.serverFilters.minPrice}:${view.serverFilters.maxPrice}`}
        />
      </aside>

      <section className="atacado-results">
        <div className="atacado-head">
          <div>
            <p className="atacado-breadcrumb">
              Catálogo{view.category && <> › {PRODUCT_CATEGORY_LABELS[view.category]}</>}
            </p>
            <h1>{view.category ? PRODUCT_CATEGORY_LABELS[view.category] : 'Todo o catálogo'}</h1>
            <p className="mk-toolbar-count">
              {countLabel(view.results.length, 'oferta', 'ofertas')}
              {view.search && <> para “{view.search}”</>} · preços por unidade de venda
            </p>
          </div>
          <SortSelect view={view} />
        </div>

        <ActiveFilters view={view} />
        <DistanceHint />

        {view.error ? (
          <p className="form-error">{view.error}</p>
        ) : view.loading && view.results.length === 0 ? (
          <div className="atacado-list" aria-hidden="true">
            {Array.from({ length: 6 }, (_, index) => (
              <div className="atacado-row atacado-row-skeleton" key={index}>
                <span className="mk-shimmer atacado-thumb" />
                <span className="mk-shimmer mk-shimmer-line" style={{ width: '70%' }} />
              </div>
            ))}
          </div>
        ) : view.results.length === 0 ? (
          <EmptyState view={view} />
        ) : (
          <div className={`atacado-list${canBuy ? '' : ' atacado-list-readonly'}`} role="list">
            <div className="atacado-row atacado-row-head" aria-hidden="true">
              <span />
              <span>Produto</span>
              <span>Produtor</span>
              <span>Disponibilidade</span>
              <span>Preço</span>
              {canBuy && <span />}
            </div>
            {view.results.map((entry) => (
              <AtacadoRow key={entry.offer.id} entry={entry} canBuy={canBuy} onCompare={view.onCompare} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

function AtacadoRow({
  entry,
  canBuy,
  onCompare,
}: {
  entry: CatalogEntryResponse;
  canBuy: boolean;
  onCompare: (offerId: string) => void;
}) {
  const { offer } = entry;
  const distance = distanceLabel(entry);
  return (
    <div className="atacado-row" role="listitem">
      <ProductImage offer={offer} className="atacado-thumb" />
      <div className="atacado-product">
        <strong>{offer.productName}</strong>
        <span>{PRODUCT_CATEGORY_LABELS[offer.category] ?? offer.category}</span>
        <button type="button" className="mk-link" onClick={() => onCompare(offer.id)}>
          Comparar produtores
        </button>
      </div>
      <div className="atacado-producer">
        <span className="mk-avatar mk-avatar-sm" aria-hidden="true">
          {initials(entry.producerName)}
        </span>
        <div>
          <strong>{entry.producerName ?? 'Produtor'}</strong>
          <span>
            {entry.producerCity ?? '—'}
            {distance && <> · {distance}</>}
          </span>
        </div>
      </div>
      <div className="atacado-availability">
        <strong>{availabilityLabel(offer) ?? 'Sem prazo'}</strong>
        <span>{stockLabel(offer)}</span>
      </div>
      <div className="atacado-price">
        <strong>{formatMoney(offer.price)}</strong>
        <span>por {unitLabel(offer)}</span>
      </div>
      {canBuy && (
        <div className="atacado-buy">
          <CartStepper entry={entry} wide />
        </div>
      )}
    </div>
  );
}

function FilterSidebar({ view }: { view: MarketplaceView }) {
  const [minPrice, setMinPrice] = useState(view.serverFilters.minPrice);
  const [maxPrice, setMaxPrice] = useState(view.serverFilters.maxPrice);
  const filters = view.serverFilters;
  const set = (changes: Partial<ServerFilters>) => view.setServerFilters({ ...filters, ...changes });
  const regions = view.coverage?.coverageRegions ?? [];
  const cities = regions.filter((region) => !filters.region || region.name === filters.region).flatMap((r) => r.cities);
  const counts = new Map<string, number>();
  view.entries.forEach((entry) => counts.set(entry.offer.category, (counts.get(entry.offer.category) ?? 0) + 1));
  const applyPrices = () => {
    if (minPrice !== filters.minPrice || maxPrice !== filters.maxPrice) set({ minPrice, maxPrice });
  };

  return (
    <div className="atacado-filters">
      <fieldset>
        <legend>Categoria</legend>
        <button type="button" className="atacado-facet" aria-pressed={view.category === ''} onClick={() => view.setCategory('')}>
          <span>Todas</span>
          <span className="atacado-facet-count">{view.entries.length}</span>
        </button>
        {CATEGORY_ORDER.filter((category) => counts.has(category) || view.category === category).map((category) => (
          <button
            key={category}
            type="button"
            className="atacado-facet"
            aria-pressed={view.category === category}
            onClick={() => view.setCategory(view.category === category ? '' : category)}
          >
            <span>{PRODUCT_CATEGORY_LABELS[category]}</span>
            <span className="atacado-facet-count">{counts.get(category) ?? 0}</span>
          </button>
        ))}
      </fieldset>

      <fieldset>
        <legend>Origem</legend>
        <label>
          Região
          <select value={filters.region} onChange={(e) => set({ region: e.target.value, city: '' })}>
            <option value="">Todas as regiões</option>
            {regions.map((region) => (
              <option key={region.name} value={region.name}>
                {region.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Cidade
          <select value={filters.city} onChange={(e) => set({ city: e.target.value })}>
            <option value="">Todas as cidades</option>
            {cities.map((city) => (
              <option key={city} value={city}>
                {city}
              </option>
            ))}
          </select>
        </label>
        <label>
          Certificação
          <select value={filters.certificationType} onChange={(e) => set({ certificationType: e.target.value })}>
            <option value="">Qualquer</option>
            {Object.entries(CERTIFICATION_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>
      </fieldset>

      <fieldset>
        <legend>Preço por unidade (R$)</legend>
        <div className="atacado-price-range">
          <label>
            <span className="mk-visually-hidden">Preço mínimo</span>
            <input
              type="number"
              min="0"
              step="0.01"
              placeholder="Mín."
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
              onBlur={applyPrices}
              onKeyDown={(e) => e.key === 'Enter' && applyPrices()}
            />
          </label>
          <span aria-hidden="true">–</span>
          <label>
            <span className="mk-visually-hidden">Preço máximo</span>
            <input
              type="number"
              min="0"
              step="0.01"
              placeholder="Máx."
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              onBlur={applyPrices}
              onKeyDown={(e) => e.key === 'Enter' && applyPrices()}
            />
          </label>
        </div>
      </fieldset>

      <fieldset>
        <legend>Prazo</legend>
        <label>
          Preciso até
          <input type="date" value={filters.availableBy} onChange={(e) => set({ availableBy: e.target.value })} />
        </label>
      </fieldset>

      {view.hasActiveFilters && (
        <button type="button" className="mk-button-ghost" onClick={view.clearAll}>
          Limpar filtros
        </button>
      )}
    </div>
  );
}

function ActiveFilters({ view }: { view: MarketplaceView }) {
  const filters = view.serverFilters;
  const chips: { label: string; clear: () => void }[] = [];
  const clearServer = (key: keyof ServerFilters) => view.setServerFilters({ ...filters, [key]: '' });
  if (view.category) chips.push({ label: PRODUCT_CATEGORY_LABELS[view.category], clear: () => view.setCategory('') });
  if (filters.region) chips.push({ label: filters.region, clear: () => view.setServerFilters({ ...filters, region: '', city: '' }) });
  if (filters.city) chips.push({ label: filters.city, clear: () => clearServer('city') });
  if (filters.certificationType) {
    chips.push({
      label: CERTIFICATION_LABELS[filters.certificationType] ?? filters.certificationType,
      clear: () => clearServer('certificationType'),
    });
  }
  if (filters.minPrice) chips.push({ label: `a partir de R$ ${filters.minPrice}`, clear: () => clearServer('minPrice') });
  if (filters.maxPrice) chips.push({ label: `até R$ ${filters.maxPrice}`, clear: () => clearServer('maxPrice') });
  if (filters.availableBy) {
    const [year, month, day] = filters.availableBy.split('-');
    chips.push({ label: `disponível até ${day}/${month}/${year}`, clear: () => clearServer('availableBy') });
  }
  if (chips.length === 0) return null;

  return (
    <ul className="mk-chips" aria-label="Filtros aplicados">
      {chips.map((chip) => (
        <li key={chip.label}>
          <button type="button" className="mk-chip" onClick={chip.clear} aria-label={`Remover filtro ${chip.label}`}>
            {chip.label} <span aria-hidden="true">×</span>
          </button>
        </li>
      ))}
    </ul>
  );
}
