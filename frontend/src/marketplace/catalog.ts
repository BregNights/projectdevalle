import { useEffect, useState } from 'react';
import { apiClient, ApiError } from '../api/client';
import { MEASUREMENT_UNIT_LABELS, PRODUCT_CATEGORY_LABELS } from '../api/labels';
import { formatDate, formatQuantity } from '../api/orders';
import type { CatalogEntryResponse, OfferResponse, ProductCategory } from '../api/types';
import { useAuth } from '../auth/AuthContext';

// RF10 — filtros aplicados pelo servidor (dependem de dados que o catálogo não devolve, como certificações).
// Categoria, produtor e busca por texto são aplicados no navegador, para trocar de prateleira sem nova busca.
export interface ServerFilters {
  region: string;
  city: string;
  certificationType: string;
  minPrice: string;
  maxPrice: string;
  availableBy: string;
}

export const EMPTY_SERVER_FILTERS: ServerFilters = {
  region: '',
  city: '',
  certificationType: '',
  minPrice: '',
  maxPrice: '',
  availableBy: '',
};

export interface CatalogResult {
  entries: CatalogEntryResponse[];
  loading: boolean;
  error: string | null;
}

// RN47 — o catálogo é público; com token de restaurante, o servidor também calcula a distância (RN33).
export function useCatalog(filters: ServerFilters): CatalogResult {
  const { token } = useAuth();
  const [result, setResult] = useState<CatalogResult>({ entries: [], loading: true, error: null });
  const query = new URLSearchParams(Object.entries(filters).filter(([, value]) => value !== '')).toString();

  useEffect(() => {
    let cancelled = false;
    setResult((prev) => ({ ...prev, loading: true, error: null }));
    apiClient
      .get<CatalogEntryResponse[]>(`/api/v1/catalog${query ? `?${query}` : ''}`, token)
      .then((entries) => {
        if (!cancelled) setResult({ entries, loading: false, error: null });
      })
      .catch((err) => {
        if (!cancelled) {
          setResult({
            entries: [],
            loading: false,
            error: err instanceof ApiError ? err.message : 'Não foi possível carregar o catálogo.',
          });
        }
      });
    return () => {
      cancelled = true;
    };
  }, [query, token]);

  return result;
}

function normalize(text: string): string {
  return text.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase();
}

// Busca por texto em produto, produtor, cidade e categoria, sem diferenciar acentos ou maiúsculas.
export function matchesSearch(entry: CatalogEntryResponse, search: string): boolean {
  const terms = normalize(search).split(/\s+/).filter(Boolean);
  if (terms.length === 0) return true;
  const haystack = normalize(
    [
      entry.offer.productName,
      entry.producerName ?? '',
      entry.producerCity ?? '',
      PRODUCT_CATEGORY_LABELS[entry.offer.category] ?? '',
    ].join(' '),
  );
  return terms.every((term) => haystack.includes(term));
}

export type SortOption = 'relevance' | 'price-asc' | 'price-desc' | 'distance' | 'name';

export const SORT_LABELS: Record<SortOption, string> = {
  relevance: 'Relevância',
  'price-asc': 'Menor preço',
  'price-desc': 'Maior preço',
  distance: 'Mais perto',
  name: 'Nome (A–Z)',
};

export function sortEntries(entries: CatalogEntryResponse[], sort: SortOption): CatalogEntryResponse[] {
  const sorted = [...entries];
  switch (sort) {
    case 'price-asc':
      return sorted.sort((a, b) => a.offer.price - b.offer.price);
    case 'price-desc':
      return sorted.sort((a, b) => b.offer.price - a.offer.price);
    case 'distance':
      return sorted.sort((a, b) => (a.distanceKilometers ?? Infinity) - (b.distanceKilometers ?? Infinity));
    case 'name':
      return sorted.sort((a, b) => a.offer.productName.localeCompare(b.offer.productName, 'pt-BR'));
    default:
      return sorted;
  }
}

export const CATEGORY_ORDER: ProductCategory[] = [
  'VEGETABLES',
  'FRUITS',
  'FISH',
  'MEAT_POULTRY',
  'DAIRY',
  'GRAINS_CEREALS',
  'PROCESSED',
  'OTHER',
];

// Ícone e cor de fundo da categoria, usados quando a oferta não tem foto.
export const CATEGORY_META: Record<ProductCategory, { icon: string; tint: string }> = {
  VEGETABLES: { icon: '🥬', tint: '#e3efd9' },
  FRUITS: { icon: '🍓', tint: '#f8e1d4' },
  FISH: { icon: '🐟', tint: '#dcebf0' },
  MEAT_POULTRY: { icon: '🍗', tint: '#f3dcd4' },
  DAIRY: { icon: '🧀', tint: '#f6edcf' },
  GRAINS_CEREALS: { icon: '🌾', tint: '#efe6cf' },
  PROCESSED: { icon: '🫙', tint: '#e9e2f0' },
  OTHER: { icon: '🧺', tint: '#ece8df' },
};

const SHORT_WEEKDAYS: Record<string, string> = {
  MONDAY: 'segunda',
  TUESDAY: 'terça',
  WEDNESDAY: 'quarta',
  THURSDAY: 'quinta',
  FRIDAY: 'sexta',
  SATURDAY: 'sábado',
  SUNDAY: 'domingo',
};

export function unitLabel(offer: OfferResponse): string {
  return MEASUREMENT_UNIT_LABELS[offer.unit] ?? offer.unit;
}

const UNIT_PLURALS: Record<string, string> = { UNIT: 'unidades', DOZEN: 'dúzias', BOX: 'caixas' };

export function stockLabel(offer: OfferResponse): string {
  const unit = offer.quantityAvailable === 1 ? unitLabel(offer) : (UNIT_PLURALS[offer.unit] ?? unitLabel(offer));
  return `${formatQuantity(offer.quantityAvailable)} ${unit} disponíveis`;
}

// RF08 — oferta recorrente (semanal) ou pontual, com a janela de disponibilidade.
export function availabilityLabel(offer: OfferResponse): string | null {
  if (offer.recurrenceType === 'RECURRING' && offer.recurrenceDayOfWeek) {
    return `Toda ${SHORT_WEEKDAYS[offer.recurrenceDayOfWeek] ?? offer.recurrenceDayOfWeek}`;
  }
  if (offer.availabilityFrom && offer.availabilityUntil) {
    return `${formatDate(offer.availabilityFrom).slice(0, 5)} a ${formatDate(offer.availabilityUntil).slice(0, 5)}`;
  }
  if (offer.availabilityUntil) return `Até ${formatDate(offer.availabilityUntil).slice(0, 5)}`;
  if (offer.availabilityFrom) return `A partir de ${formatDate(offer.availabilityFrom).slice(0, 5)}`;
  return null;
}

export function distanceLabel(entry: CatalogEntryResponse): string | null {
  if (entry.distanceKilometers == null) return null;
  const km = entry.distanceKilometers.toLocaleString('pt-BR', { maximumFractionDigits: 1 });
  return entry.distanceDurationMinutes != null ? `${km} km · ${entry.distanceDurationMinutes} min` : `${km} km`;
}

export function initials(name: string | null): string {
  if (!name) return '?';
  const parts = name.trim().split(/\s+/);
  return (parts[0][0] + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase();
}

export function countLabel(count: number, singular: string, plural: string): string {
  return `${count.toLocaleString('pt-BR')} ${count === 1 ? singular : plural}`;
}
