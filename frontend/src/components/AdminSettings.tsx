import { useEffect, useState, type FormEvent } from 'react';
import { apiClient, ApiError } from '../api/client';
import { PRODUCT_CATEGORY_LABELS } from '../api/labels';
import type { PlatformSettings } from '../api/platform';
import type { ProductCategory } from '../api/types';
import { useAuth } from '../auth/AuthContext';

const ALL_CATEGORIES = Object.keys(PRODUCT_CATEGORY_LABELS) as ProductCategory[];

interface RegionDraft {
  name: string;
  citiesText: string;
}

// RF43 — parâmetros globais: comissão (RN13), regiões/municípios atendidos (RN02) e categorias habilitadas.
export function AdminSettings() {
  const { token } = useAuth();
  const [commission, setCommission] = useState('');
  const [penalty, setPenalty] = useState('');
  const [regions, setRegions] = useState<RegionDraft[]>([]);
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const apply = (settings: PlatformSettings) => {
    setCommission(String(settings.commissionPercentage));
    setPenalty(String(settings.cancellationPenaltyPercentage));
    setRegions(settings.coverageRegions.map((region) => ({ name: region.name, citiesText: region.cities.join('\n') })));
    setCategories(settings.enabledProductCategories);
  };

  useEffect(() => {
    apiClient
      .get<PlatformSettings>('/api/v1/admin/settings', token)
      .then(apply)
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Não foi possível carregar as configurações.'))
      .finally(() => setLoading(false));
  }, [token]);

  const updateRegion = (index: number, patch: Partial<RegionDraft>) => {
    setRegions((prev) => prev.map((region, i) => (i === index ? { ...region, ...patch } : region)));
  };

  const toggleCategory = (category: ProductCategory) => {
    setCategories((prev) =>
      prev.includes(category) ? prev.filter((item) => item !== category) : [...prev, category],
    );
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSaved(false);
    setSubmitting(true);
    try {
      const updated = await apiClient.put<PlatformSettings>(
        '/api/v1/admin/settings',
        {
          commissionPercentage: Number(commission),
          cancellationPenaltyPercentage: Number(penalty),
          coverageRegions: regions.map((region) => ({
            name: region.name.trim(),
            cities: region.citiesText
              .split('\n')
              .map((city) => city.trim())
              .filter(Boolean),
          })),
          enabledProductCategories: categories,
        },
        token,
      );
      apply(updated);
      setSaved(true);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível salvar as configurações.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <p>Carregando...</p>;
  }

  return (
    <form className="card-form" onSubmit={handleSubmit}>
      <fieldset>
        <legend>Comissão da plataforma</legend>
        <label>
          Percentual sobre cada transação concluída (%)
          <input
            type="number"
            min="0"
            max="99.99"
            step="0.01"
            value={commission}
            onChange={(e) => setCommission(e.target.value)}
            required
          />
        </label>
      </fieldset>

      <fieldset>
        <legend>Política de cancelamento</legend>
        <label>
          Multa do restaurante que cancela depois de o produtor iniciar o preparo (% do pedido)
          <input
            type="number"
            min="0"
            max="99.99"
            step="0.01"
            value={penalty}
            onChange={(e) => setPenalty(e.target.value)}
            required
          />
        </label>
      </fieldset>

      <fieldset>
        <legend>Regiões e municípios atendidos</legend>
        <p className="form-notice">
          Produtores fora destes municípios não podem ser aprovados e deixam de aparecer no catálogo.
        </p>
        {regions.map((region, index) => (
          <div className="settings-region" key={index}>
            <label>
              Nome da região
              <input value={region.name} onChange={(e) => updateRegion(index, { name: e.target.value })} required />
            </label>
            <label>
              Municípios (um por linha)
              <textarea
                rows={5}
                value={region.citiesText}
                onChange={(e) => updateRegion(index, { citiesText: e.target.value })}
                required
              />
            </label>
            {regions.length > 1 && (
              <button
                type="button"
                className="admin-action-ghost"
                onClick={() => setRegions((prev) => prev.filter((_, i) => i !== index))}
              >
                Remover região
              </button>
            )}
          </div>
        ))}
        <button
          type="button"
          className="admin-action-ghost"
          onClick={() => setRegions((prev) => [...prev, { name: '', citiesText: '' }])}
        >
          Adicionar região
        </button>
      </fieldset>

      <fieldset>
        <legend>Categorias de produto habilitadas</legend>
        <p className="form-notice">Categorias desabilitadas não aceitam novas ofertas e saem do catálogo.</p>
        <div className="settings-categories">
          {ALL_CATEGORIES.map((category) => (
            <label key={category}>
              <input
                type="checkbox"
                checked={categories.includes(category)}
                onChange={() => toggleCategory(category)}
              />
              {PRODUCT_CATEGORY_LABELS[category]}
            </label>
          ))}
        </div>
      </fieldset>

      {error && <p className="form-error">{error}</p>}
      {saved && !error && <p className="form-notice">Configurações salvas.</p>}
      <button type="submit" disabled={submitting}>
        {submitting ? 'Salvando...' : 'Salvar configurações'}
      </button>
    </form>
  );
}
