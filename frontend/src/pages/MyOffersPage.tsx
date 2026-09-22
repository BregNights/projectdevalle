import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { apiClient, ApiError } from '../api/client';
import { publicFileSrc } from '../api/files';
import { usePlatformCoverage } from '../api/platform';
import {
  DAY_OF_WEEK_LABELS,
  MEASUREMENT_UNIT_LABELS,
  PERISHABLE_CATEGORIES,
  PRODUCT_CATEGORY_LABELS,
} from '../api/labels';
import type {
  DayOfWeekName,
  MeasurementUnit,
  OfferResponse,
  ProductCategory,
  PublishOfferRequest,
  RecurrenceType,
} from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { FileUploadField } from '../components/FileUploadField';
import { OfferStatusBadge } from '../components/OfferStatusBadge';

const CATEGORY_OPTIONS = Object.keys(PRODUCT_CATEGORY_LABELS) as ProductCategory[];
const MAX_PHOTOS = 5;
const UNIT_OPTIONS = Object.keys(MEASUREMENT_UNIT_LABELS) as MeasurementUnit[];
const DAY_OPTIONS = Object.keys(DAY_OF_WEEK_LABELS) as DayOfWeekName[];

// RN48 — a validade não pode estar no passado; usa a data local para não errar perto da meia-noite.
function todayIsoDate(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}

interface FormState {
  productName: string;
  category: ProductCategory;
  unit: MeasurementUnit;
  price: string;
  quantityAvailable: string;
  recurrenceType: RecurrenceType;
  recurrenceDayOfWeek: DayOfWeekName;
  availabilityFrom: string;
  availabilityUntil: string;
  photoUrls: string[];
}

const INITIAL_STATE: FormState = {
  productName: '',
  category: 'VEGETABLES',
  unit: 'KILOGRAM',
  price: '',
  quantityAvailable: '',
  recurrenceType: 'ONE_TIME',
  recurrenceDayOfWeek: 'MONDAY',
  availabilityFrom: '',
  availabilityUntil: '',
  photoUrls: [],
};

export function MyOffersPage() {
  const { token, claims } = useAuth();
  const [offers, setOffers] = useState<OfferResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState<string | null>(null);
  const [form, setForm] = useState<FormState>(INITIAL_STATE);
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [busyId, setBusyId] = useState<string | null>(null);

  const isPerishable = PERISHABLE_CATEGORIES.has(form.category);
  const coverage = usePlatformCoverage();
  // RF43 — só categorias habilitadas pela administração aparecem para novas ofertas.
  const categoryOptions = coverage
    ? CATEGORY_OPTIONS.filter((option) => coverage.enabledProductCategories.includes(option))
    : CATEGORY_OPTIONS;

  useEffect(() => {
    if (!coverage || coverage.enabledProductCategories.length === 0) return;
    setForm((prev) =>
      coverage.enabledProductCategories.includes(prev.category)
        ? prev
        : { ...prev, category: coverage.enabledProductCategories[0] },
    );
  }, [coverage]);

  const update = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const load = useCallback(async () => {
    setLoading(true);
    setListError(null);
    try {
      const list = await apiClient.get<OfferResponse[]>('/api/v1/offers/mine', token);
      setOffers(list);
    } catch (err) {
      setListError(err instanceof ApiError ? err.message : 'Não foi possível carregar suas ofertas.');
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    load();
  }, [load]);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setFormError(null);
    setSubmitting(true);
    try {
      const request: PublishOfferRequest = {
        productName: form.productName,
        category: form.category,
        unit: form.unit,
        price: Number(form.price),
        quantityAvailable: Number(form.quantityAvailable),
        recurrenceType: form.recurrenceType,
        recurrenceDayOfWeek: form.recurrenceType === 'RECURRING' ? form.recurrenceDayOfWeek : undefined,
        availabilityFrom: form.availabilityFrom || undefined,
        availabilityUntil: form.availabilityUntil || undefined,
        photoUrls: form.photoUrls.length > 0 ? form.photoUrls : undefined,
      };
      const created = await apiClient.post<OfferResponse>('/api/v1/offers', request, token);
      setOffers((prev) => [created, ...prev]);
      setForm({ ...INITIAL_STATE, category: categoryOptions[0] ?? INITIAL_STATE.category });
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : 'Não foi possível publicar a oferta.');
    } finally {
      setSubmitting(false);
    }
  };

  const runAction = async (id: string, action: () => Promise<void>) => {
    setBusyId(id);
    setListError(null);
    try {
      await action();
      await load();
    } catch (err) {
      setListError(err instanceof ApiError ? err.message : 'Não foi possível atualizar a oferta.');
    } finally {
      setBusyId(null);
    }
  };

  const handlePause = (id: string) => runAction(id, () => apiClient.post(`/api/v1/offers/${id}/pause`, undefined, token));
  const handleResume = (id: string) => runAction(id, () => apiClient.post(`/api/v1/offers/${id}/resume`, undefined, token));
  const handleRemove = (id: string) => runAction(id, () => apiClient.post(`/api/v1/offers/${id}/remove`, undefined, token));

  const handleQuantityChange = (id: string, quantityAvailable: string) => {
    const parsed = Number(quantityAvailable);
    if (Number.isNaN(parsed) || parsed < 0) return;
    runAction(id, () => apiClient.put(`/api/v1/offers/${id}/quantity`, { quantityAvailable: parsed }, token));
  };

  if (claims && claims.role !== 'PRODUCER') {
    return (
      <div className="page">
        <h1>Minhas ofertas</h1>
        <p>Esta página é exclusiva para contas de produtor.</p>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>Minhas ofertas</h1>
      <p className="admin-subtitle">Publique produtos disponíveis e gerencie suas ofertas ativas.</p>

      <form className="card-form" onSubmit={handleSubmit}>
        <fieldset>
            <legend>Nova oferta</legend>
            <label>
              Produto
              <input value={form.productName} onChange={(e) => update('productName', e.target.value)} required />
            </label>
            <label>
              Categoria
              <select value={form.category} onChange={(e) => update('category', e.target.value as ProductCategory)}>
                {categoryOptions.map((option) => (
                  <option key={option} value={option}>
                    {PRODUCT_CATEGORY_LABELS[option]}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Unidade de medida
              <select value={form.unit} onChange={(e) => update('unit', e.target.value as MeasurementUnit)}>
                {UNIT_OPTIONS.map((option) => (
                  <option key={option} value={option}>
                    {MEASUREMENT_UNIT_LABELS[option]}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Preço (R$)
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={form.price}
                onChange={(e) => update('price', e.target.value)}
                required
              />
            </label>
            <label>
              Quantidade disponível
              <input
                type="number"
                min="0.001"
                step="0.001"
                value={form.quantityAvailable}
                onChange={(e) => update('quantityAvailable', e.target.value)}
                required
              />
            </label>
            <label>
              Recorrência
              <select
                value={form.recurrenceType}
                onChange={(e) => update('recurrenceType', e.target.value as RecurrenceType)}
              >
                <option value="ONE_TIME">Pontual (ex.: safra, pescado do dia)</option>
                <option value="RECURRING">Recorrente (toda semana)</option>
              </select>
            </label>
            {form.recurrenceType === 'RECURRING' && (
              <label>
                Dia da semana
                <select
                  value={form.recurrenceDayOfWeek}
                  onChange={(e) => update('recurrenceDayOfWeek', e.target.value as DayOfWeekName)}
                >
                  {DAY_OPTIONS.map((day) => (
                    <option key={day} value={day}>
                      {DAY_OF_WEEK_LABELS[day]}
                    </option>
                  ))}
                </select>
              </label>
            )}
            <label>
              Disponível a partir de (opcional)
              <input
                type="date"
                value={form.availabilityFrom}
                onChange={(e) => update('availabilityFrom', e.target.value)}
              />
            </label>
            <label>
              Válido até {isPerishable ? '(obrigatório para esta categoria)' : '(opcional)'}
              <input
                type="date"
                value={form.availabilityUntil}
                onChange={(e) => update('availabilityUntil', e.target.value)}
                min={todayIsoDate()}
                required={isPerishable}
              />
            </label>
            {form.photoUrls.length < MAX_PHOTOS && (
              <FileUploadField
                label={`Fotos (opcional, até ${MAX_PHOTOS})`}
                purpose="OFFER_PHOTO"
                token={token}
                onUploaded={(file) => update('photoUrls', [...form.photoUrls, file.url])}
              />
            )}
            {form.photoUrls.length > 0 && (
              <div className="offer-photos">
                {form.photoUrls.map((url) => (
                  <span className="offer-photo" key={url}>
                    <img src={publicFileSrc(url)} alt="Foto da oferta" />
                    <button
                      type="button"
                      className="chip-remove"
                      aria-label="Remover foto"
                      onClick={() => update('photoUrls', form.photoUrls.filter((item) => item !== url))}
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
            )}
          </fieldset>

        {formError && <p className="form-error">{formError}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Publicando...' : 'Publicar oferta'}
        </button>
      </form>

      {listError && <p className="form-error">{listError}</p>}

      {loading ? (
        <p>Carregando...</p>
      ) : offers.length === 0 ? (
        <p className="admin-empty">Você ainda não publicou nenhuma oferta.</p>
      ) : (
        <div className="admin-list">
          {offers.map((offer) => (
            <div className="admin-row" key={offer.id}>
              <div className="admin-row-info">
                {offer.photoUrls.length > 0 && (
                  <img className="offer-thumb" src={publicFileSrc(offer.photoUrls[0])} alt={offer.productName} />
                )}
                <div>
                  <strong>{offer.productName}</strong>
                  <span className="admin-row-subtitle">
                    {PRODUCT_CATEGORY_LABELS[offer.category] ?? offer.category} · R$ {offer.price.toFixed(2)} /{' '}
                    {MEASUREMENT_UNIT_LABELS[offer.unit] ?? offer.unit}
                  </span>
                </div>
                <OfferStatusBadge status={offer.status} />
              </div>

              <div className="offer-quantity-field">
                <label>
                  Quantidade disponível
                  <input
                    type="number"
                    min="0"
                    step="0.001"
                    defaultValue={offer.quantityAvailable}
                    disabled={busyId === offer.id || offer.status === 'REMOVED'}
                    onBlur={(e) => handleQuantityChange(offer.id, e.target.value)}
                  />
                </label>
              </div>

              <div className="admin-row-actions">
                {offer.status === 'ACTIVE' && (
                  <button type="button" className="admin-action-reject" onClick={() => handlePause(offer.id)} disabled={busyId === offer.id}>
                    Pausar
                  </button>
                )}
                {(offer.status === 'PAUSED' || offer.status === 'SOLD_OUT') && (
                  <button type="button" className="admin-action-approve" onClick={() => handleResume(offer.id)} disabled={busyId === offer.id}>
                    Retomar
                  </button>
                )}
                {offer.status !== 'REMOVED' && (
                  <button type="button" className="admin-action-reject" onClick={() => handleRemove(offer.id)} disabled={busyId === offer.id}>
                    Remover
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
