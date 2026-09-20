import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { apiClient, ApiError } from '../api/client';
import { CATEGORY_LABELS } from '../api/labels';
import type {
  AddressInput,
  DeliveryAddressResponse,
  EstablishmentCategory,
  RestaurantAccountResponse,
} from '../api/types';
import { useAuth } from '../auth/AuthContext';

const INITIAL_ADDRESS: AddressInput = {
  street: '',
  number: '',
  neighborhood: '',
  city: '',
  state: '',
  zipCode: '',
  complement: '',
};

export function RestaurantAccountPage() {
  const { token } = useAuth();
  const [account, setAccount] = useState<RestaurantAccountResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const data = await apiClient.get<RestaurantAccountResponse>('/api/v1/restaurants/me/account', token);
      setAccount(data);
    } catch (err) {
      setLoadError(err instanceof ApiError ? err.message : 'Não foi possível carregar seus dados.');
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    load();
  }, [load]);

  if (loading) {
    return (
      <div className="page">
        <p>Carregando...</p>
      </div>
    );
  }

  if (loadError || !account) {
    return (
      <div className="page">
        <p className="form-error">{loadError ?? 'Cadastro não encontrado.'}</p>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>Editar cadastro</h1>
      <ProfileSection account={account} token={token} onSaved={load} />
      <DeliveryAddressesSection addresses={account.deliveryAddresses} token={token} onSaved={load} />
    </div>
  );
}

function ProfileSection({
  account,
  token,
  onSaved,
}: {
  account: RestaurantAccountResponse;
  token: string | null;
  onSaved: () => void;
}) {
  const [form, setForm] = useState({
    corporateName: account.corporateName,
    category: account.category,
    contactName: account.contact.name,
    contactRole: account.contact.role ?? '',
    contactPhone: account.contact.phone ?? '',
    contactEmail: account.contact.email ?? '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const update = <K extends keyof typeof form>(key: K, value: (typeof form)[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSaved(false);
    setSubmitting(true);
    try {
      await apiClient.put(
        '/api/v1/restaurants/me/profile',
        {
          corporateName: form.corporateName,
          category: form.category,
          contact: {
            name: form.contactName,
            role: form.contactRole || undefined,
            phone: form.contactPhone || undefined,
            email: form.contactEmail || undefined,
          },
        },
        token,
      );
      setSaved(true);
      onSaved();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível salvar.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="card-form" onSubmit={handleSubmit}>
      <fieldset>
        <legend>Dados do estabelecimento</legend>
        <p className="admin-row-subtitle">CNPJ: {account.cnpj}</p>
        <label>
          Razão social
          <input value={form.corporateName} onChange={(e) => update('corporateName', e.target.value)} required />
        </label>
        <label>
          Categoria
          <select value={form.category} onChange={(e) => update('category', e.target.value as EstablishmentCategory)}>
            {Object.entries(CATEGORY_LABELS)
              .filter(([value]) => value !== 'OTHER' || value === form.category)
              .map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
          </select>
        </label>
      </fieldset>

      <fieldset>
        <legend>Responsável pelas compras</legend>
        <label>
          Nome
          <input value={form.contactName} onChange={(e) => update('contactName', e.target.value)} required />
        </label>
        <label>
          Cargo (opcional)
          <input value={form.contactRole} onChange={(e) => update('contactRole', e.target.value)} />
        </label>
        <label>
          Telefone (opcional)
          <input value={form.contactPhone} onChange={(e) => update('contactPhone', e.target.value)} />
        </label>
        <label>
          E-mail (opcional)
          <input type="email" value={form.contactEmail} onChange={(e) => update('contactEmail', e.target.value)} />
        </label>
      </fieldset>

      {error && <p className="form-error">{error}</p>}
      {saved && !error && <p className="form-notice">Salvo com sucesso.</p>}
      <button type="submit" disabled={submitting}>
        {submitting ? 'Salvando...' : 'Salvar'}
      </button>
    </form>
  );
}

function DeliveryAddressesSection({
  addresses,
  token,
  onSaved,
}: {
  addresses: DeliveryAddressResponse[];
  token: string | null;
  onSaved: () => void;
}) {
  const [busyId, setBusyId] = useState<string | null>(null);
  const [listError, setListError] = useState<string | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [form, setForm] = useState({ label: '', ...INITIAL_ADDRESS, primary: false });
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  const update = <K extends keyof typeof form>(key: K, value: (typeof form)[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSetPrimary = async (addressId: string) => {
    setBusyId(addressId);
    setListError(null);
    try {
      await apiClient.put(`/api/v1/restaurants/me/delivery-addresses/${addressId}/primary`, undefined, token);
      onSaved();
    } catch (err) {
      setListError(err instanceof ApiError ? err.message : 'Não foi possível atualizar o endereço.');
    } finally {
      setBusyId(null);
    }
  };

  const handleRemove = async (addressId: string) => {
    setBusyId(addressId);
    setListError(null);
    try {
      await apiClient.delete(`/api/v1/restaurants/me/delivery-addresses/${addressId}`, token);
      onSaved();
    } catch (err) {
      setListError(err instanceof ApiError ? err.message : 'Não foi possível remover o endereço.');
    } finally {
      setBusyId(null);
    }
  };

  const handleAddSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setFormError(null);
    setSubmitting(true);
    try {
      const { label, primary, ...address } = form;
      await apiClient.post(
        '/api/v1/restaurants/me/delivery-addresses',
        { label, address: { ...address, complement: address.complement || undefined }, primary },
        token,
      );
      setForm({ label: '', ...INITIAL_ADDRESS, primary: false });
      setShowAddForm(false);
      onSaved();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : 'Não foi possível adicionar o endereço.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <h2>Endereços de entrega</h2>
      {listError && <p className="form-error">{listError}</p>}

      <div className="admin-list">
        {addresses.map((address) => (
          <div className="admin-row" key={address.id}>
            <div className="admin-row-info">
              <div>
                <strong>{address.label}</strong>
                <span className="admin-row-subtitle">
                  {address.address.street}, {address.address.number} — {address.address.city}/{address.address.state}
                </span>
              </div>
              {address.primary && <span className="status-badge status-approved">Principal</span>}
            </div>
            {address.geocodingPending && (
              <p className="form-notice admin-row-note">Ainda não localizamos este endereço no mapa.</p>
            )}
            <div className="admin-row-actions">
              {!address.primary && (
                <button
                  type="button"
                  className="admin-action-approve"
                  onClick={() => handleSetPrimary(address.id)}
                  disabled={busyId === address.id}
                >
                  Tornar principal
                </button>
              )}
              {addresses.length > 1 && (
                <button
                  type="button"
                  className="admin-action-reject"
                  onClick={() => handleRemove(address.id)}
                  disabled={busyId === address.id}
                >
                  Remover
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {showAddForm ? (
        <form className="card-form section-spaced" onSubmit={handleAddSubmit}>
          <fieldset>
            <legend>Novo endereço de entrega</legend>
            <label>
              Identificação (ex.: Filial Centro)
              <input value={form.label} onChange={(e) => update('label', e.target.value)} required />
            </label>
            <label>
              Rua
              <input value={form.street} onChange={(e) => update('street', e.target.value)} required />
            </label>
            <label>
              Número
              <input value={form.number} onChange={(e) => update('number', e.target.value)} required />
            </label>
            <label>
              Bairro
              <input value={form.neighborhood} onChange={(e) => update('neighborhood', e.target.value)} required />
            </label>
            <label>
              Cidade
              <input value={form.city} onChange={(e) => update('city', e.target.value)} required />
            </label>
            <label>
              Estado (UF)
              <input value={form.state} maxLength={2} onChange={(e) => update('state', e.target.value)} required />
            </label>
            <label>
              CEP
              <input value={form.zipCode} onChange={(e) => update('zipCode', e.target.value)} required />
            </label>
            <label>
              Complemento (opcional)
              <input value={form.complement} onChange={(e) => update('complement', e.target.value)} />
            </label>
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={form.primary}
                onChange={(e) => update('primary', e.target.checked)}
              />
              Definir como principal
            </label>
          </fieldset>
          {formError && <p className="form-error">{formError}</p>}
          <div className="admin-row-actions">
            <button type="submit" disabled={submitting}>
              {submitting ? 'Adicionando...' : 'Adicionar endereço'}
            </button>
            <button type="button" className="admin-action-ghost" onClick={() => setShowAddForm(false)}>
              Cancelar
            </button>
          </div>
        </form>
      ) : (
        <button type="button" className="section-spaced" onClick={() => setShowAddForm(true)}>
          Adicionar endereço
        </button>
      )}
    </div>
  );
}
