import { PRODUCT_CATEGORY_LABELS } from '../../api/labels';
import { useAuth } from '../../auth/AuthContext';
import { CATEGORY_META, CATEGORY_ORDER, countLabel } from '../catalog';
import type { MarketplaceView } from '../MarketplacePage';
import { DistanceHint, ResultsGrid, SortSelect } from '../parts';

// Layout 1 — "Feira": grade de produtos no estilo de app de delivery, com categorias em destaque no topo.
export function FeiraLayout({ view }: { view: MarketplaceView }) {
  const { token } = useAuth();
  const producers = new Set(view.entries.map((entry) => entry.producerId)).size;
  const cities = new Set(view.entries.map((entry) => entry.producerCity).filter(Boolean)).size;
  const categories = CATEGORY_ORDER.filter((category) =>
    view.entries.some((entry) => entry.offer.category === category),
  );

  return (
    <div className="feira">
      <section className="feira-hero">
        <div className="mk-inner feira-hero-inner">
          <div className="feira-hero-copy">
            <span className="mk-eyebrow">Vale do Itajaí e litoral norte de SC</span>
            <h1>Do produtor para a sua cozinha, sem atravessador</h1>
            <p>
              Hortifruti, pescado, carnes e laticínios de quem produz na região.
              {!token && ' Monte o carrinho à vontade: a conta só é pedida na hora de fechar o pedido.'}
            </p>
          </div>
          <dl className="feira-stats">
            <div>
              <dt>{view.loading ? '—' : view.entries.length}</dt>
              <dd>ofertas ativas</dd>
            </div>
            <div>
              <dt>{view.loading ? '—' : producers}</dt>
              <dd>produtores</dd>
            </div>
            <div>
              <dt>{view.loading ? '—' : cities}</dt>
              <dd>cidades</dd>
            </div>
          </dl>
        </div>
      </section>

      <nav className="mk-inner feira-categories" aria-label="Categorias">
        <button
          type="button"
          className="feira-category"
          aria-pressed={view.category === ''}
          onClick={() => view.setCategory('')}
        >
          <span className="feira-category-icon" style={{ background: 'var(--primary-light)' }} aria-hidden="true">
            🌱
          </span>
          Tudo
        </button>
        {categories.map((category) => (
          <button
            key={category}
            type="button"
            className="feira-category"
            aria-pressed={view.category === category}
            onClick={() => view.setCategory(view.category === category ? '' : category)}
          >
            <span className="feira-category-icon" style={{ background: CATEGORY_META[category].tint }} aria-hidden="true">
              {CATEGORY_META[category].icon}
            </span>
            {PRODUCT_CATEGORY_LABELS[category]}
          </button>
        ))}
      </nav>

      <div className="mk-inner mk-toolbar">
        <div>
          <h2 className="mk-toolbar-title">
            {view.category ? PRODUCT_CATEGORY_LABELS[view.category] : 'Ofertas da semana'}
          </h2>
          <p className="mk-toolbar-count">
            {countLabel(view.results.length, 'oferta', 'ofertas')}
            {view.search && <> para “{view.search}”</>}
            {view.hasActiveFilters && (
              <>
                {' · '}
                <button type="button" className="mk-link" onClick={view.clearAll}>
                  limpar filtros
                </button>
              </>
            )}
          </p>
        </div>
        <SortSelect view={view} />
      </div>

      <div className="mk-inner">
        <DistanceHint />
        <ResultsGrid view={view} />
      </div>
    </div>
  );
}
