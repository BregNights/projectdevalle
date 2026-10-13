import { PRODUCT_CATEGORY_LABELS } from '../../api/labels';
import type { CatalogEntryResponse, ProductCategory } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';
import { CATEGORY_META, CATEGORY_ORDER, countLabel, initials } from '../catalog';
import type { MarketplaceView } from '../MarketplacePage';
import { DistanceHint, ResultsGrid, SortSelect } from '../parts';
import { ProductCard, ProductCardSkeleton } from '../ProductCard';

const SHELF_SIZE = 12;

interface ProducerSummary {
  producerId: string;
  name: string | null;
  city: string | null;
  offers: number;
}

function summarizeProducers(entries: CatalogEntryResponse[]): ProducerSummary[] {
  const byProducer = new Map<string, ProducerSummary>();
  entries.forEach((entry) => {
    const current = byProducer.get(entry.producerId);
    byProducer.set(entry.producerId, {
      producerId: entry.producerId,
      name: entry.producerName,
      city: entry.producerCity,
      offers: (current?.offers ?? 0) + 1,
    });
  });
  return [...byProducer.values()].sort((a, b) => b.offers - a.offers);
}

// Layout 2 — "Vitrine": prateleiras por categoria (rolagem horizontal) e produtores em destaque, como a home
// de um grande marketplace. Ao filtrar, vira uma grade de resultados.
export function VitrineLayout({ view }: { view: MarketplaceView }) {
  const { token } = useAuth();
  const shelves = CATEGORY_ORDER.map((category) => ({
    category,
    entries: view.entries.filter((entry) => entry.offer.category === category),
  })).filter((shelf) => shelf.entries.length > 0);
  // A faixa ignora o filtro de produtor, para dar para trocar de um produtor para outro.
  const producers = summarizeProducers(view.searchMatches);
  const browsing = view.category !== '' || view.search !== '' || view.producerId !== '';
  const highlights = shelves.slice(0, 2).map((shelf) => shelf.category);

  return (
    <div className="vitrine">
      <section className="mk-inner vitrine-hero">
        <div className="vitrine-hero-main">
          <span className="mk-eyebrow">Feito no Vale</span>
          <h1>Ingredientes da região, direto de quem produz</h1>
          <p>Compare produtores, monte o carrinho e feche o pedido com quem colhe e pesca perto de você.</p>
          {highlights.length > 0 && (
            <div className="vitrine-hero-actions">
              {highlights.map((category) => (
                <button key={category} type="button" className="mk-button" onClick={() => view.setCategory(category)}>
                  {CATEGORY_META[category].icon} Ver {PRODUCT_CATEGORY_LABELS[category].toLowerCase()}
                </button>
              ))}
            </div>
          )}
        </div>
        <div className="vitrine-hero-side">
          {token ? (
            <div className="vitrine-promo vitrine-promo-a">
              <strong>Compare antes de comprar</strong>
              <span>Veja ofertas do mesmo produto lado a lado: preço, prazo, origem e distância.</span>
            </div>
          ) : (
            <div className="vitrine-promo vitrine-promo-a">
              <strong>Compre sem cadastro</strong>
              <span>Monte o carrinho agora. A conta só é pedida para fechar o pedido.</span>
            </div>
          )}
          <div className="vitrine-promo vitrine-promo-b">
            <strong>Pedido toda semana</strong>
            <span>Transforme o carrinho em um pedido recorrente e esqueça a reposição.</span>
          </div>
        </div>
      </section>

      {producers.length > 0 && (
        <section className="mk-inner vitrine-producers" aria-label="Produtores da região">
          <h2 className="vitrine-section-title">Produtores da região</h2>
          <div className="vitrine-producer-row">
            {producers.map((producer) => (
              <button
                key={producer.producerId}
                type="button"
                className="vitrine-producer"
                aria-pressed={view.producerId === producer.producerId}
                onClick={() => view.setProducerId(view.producerId === producer.producerId ? '' : producer.producerId)}
              >
                <span className="mk-avatar" aria-hidden="true">
                  {initials(producer.name)}
                </span>
                <span className="vitrine-producer-text">
                  <strong>{producer.name ?? 'Produtor'}</strong>
                  <span>
                    {producer.city ?? 'Região'} · {countLabel(producer.offers, 'oferta', 'ofertas')}
                  </span>
                </span>
              </button>
            ))}
          </div>
        </section>
      )}

      {browsing ? (
        <section className="mk-inner vitrine-results">
          <div className="mk-toolbar">
            <div>
              <h2 className="mk-toolbar-title">
                {view.category ? PRODUCT_CATEGORY_LABELS[view.category] : 'Resultados'}
              </h2>
              <p className="mk-toolbar-count">
                {countLabel(view.results.length, 'oferta', 'ofertas')}
                {view.search && <> para “{view.search}”</>}
                {' · '}
                <button type="button" className="mk-link" onClick={view.clearAll}>
                  voltar para a vitrine
                </button>
              </p>
            </div>
            <SortSelect view={view} />
          </div>
          <DistanceHint />
          <ResultsGrid view={view} />
        </section>
      ) : view.loading ? (
        <section className="mk-inner shelf">
          <div className="shelf-row">
            {Array.from({ length: 6 }, (_, index) => (
              <div className="shelf-item" key={index}>
                <ProductCardSkeleton />
              </div>
            ))}
          </div>
        </section>
      ) : view.error || shelves.length === 0 ? (
        <section className="mk-inner">
          <ResultsGrid view={view} />
        </section>
      ) : (
        <>
          <div className="mk-inner">
            <DistanceHint />
          </div>
          {shelves.map((shelf) => (
            <Shelf key={shelf.category} category={shelf.category} entries={shelf.entries} view={view} />
          ))}
        </>
      )}
    </div>
  );
}

function Shelf({
  category,
  entries,
  view,
}: {
  category: ProductCategory;
  entries: CatalogEntryResponse[];
  view: MarketplaceView;
}) {
  return (
    <section className="mk-inner shelf" aria-label={PRODUCT_CATEGORY_LABELS[category]}>
      <header className="shelf-header">
        <h2 className="vitrine-section-title">
          <span className="shelf-icon" style={{ background: CATEGORY_META[category].tint }} aria-hidden="true">
            {CATEGORY_META[category].icon}
          </span>
          {PRODUCT_CATEGORY_LABELS[category]}
        </h2>
        <button type="button" className="mk-link" onClick={() => view.setCategory(category)}>
          Ver todas ({entries.length}) →
        </button>
      </header>
      <div className="shelf-row">
        {entries.slice(0, SHELF_SIZE).map((entry) => (
          <div className="shelf-item" key={entry.offer.id}>
            <ProductCard entry={entry} onCompare={view.onCompare} />
          </div>
        ))}
      </div>
    </section>
  );
}
