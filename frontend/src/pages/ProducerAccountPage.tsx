import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { apiClient, ApiError } from '../api/client';
import { openProtectedFile } from '../api/files';
import { BANK_ACCOUNT_TYPE_LABELS, CERTIFICATION_LABELS } from '../api/labels';
import type {
  AddressInput,
  AddressResponse,
  BankAccountType,
  CertificationView,
  ProducerAccountResponse,
  ProducerResponse,
  UpdateOriginAddressResponse,
} from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { FileUploadField } from '../components/FileUploadField';

export function ProducerAccountPage() {
  const { token } = useAuth();
  const [account, setAccount] = useState<ProducerAccountResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const data = await apiClient.get<ProducerAccountResponse>('/api/v1/producers/me/account', token);
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
      <ProfileSection name={account.name} token={token} onSaved={load} />
      <OriginAddressSection
        address={account.originAddress}
        geocodingPending={account.geocodingPending}
        token={token}
        onSaved={load}
      />
      <BankDetailsSection bankDetails={account.bankDetails} token={token} onSaved={load} />
      <DeliveryAreaSection municipalities={account.deliveryAreaMunicipalities} token={token} onSaved={load} />
      <CertificationsSection token={token} />
    </div>
  );
}

function ProfileSection({
  name,
  token,
  onSaved,
}: {
  name: string;
  token: string | null;
  onSaved: () => void;
}) {
  const [value, setValue] = useState(name);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSaved(false);
    setSubmitting(true);
    try {
      await apiClient.put('/api/v1/producers/me/profile', { name: value }, token);
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
        <legend>Dados do produtor</legend>
        <label>
          Nome / Razão social
          <input value={value} onChange={(e) => setValue(e.target.value)} required />
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

function OriginAddressSection({
  address,
  geocodingPending,
  token,
  onSaved,
}: {
  address: AddressResponse;
  geocodingPending: boolean;
  token: string | null;
  onSaved: () => void;
}) {
  const [form, setForm] = useState<AddressInput>({ ...address, complement: address.complement ?? '' });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const update = <K extends keyof AddressInput>(key: K, value: AddressInput[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSaved(false);
    setSubmitting(true);
    try {
      await apiClient.put<UpdateOriginAddressResponse>(
        '/api/v1/producers/me/origin-address',
        { address: { ...form, complement: form.complement || undefined } },
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
        <legend>Propriedade / ponto de coleta</legend>
        {geocodingPending && (
          <p className="form-notice">Ainda não localizamos este endereço no mapa.</p>
        )}
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
      </fieldset>
      {error && <p className="form-error">{error}</p>}
      {saved && !error && <p className="form-notice">Salvo com sucesso.</p>}
      <button type="submit" disabled={submitting}>
        {submitting ? 'Salvando...' : 'Salvar'}
      </button>
    </form>
  );
}

function BankDetailsSection({
  bankDetails,
  token,
  onSaved,
}: {
  bankDetails: ProducerAccountResponse['bankDetails'];
  token: string | null;
  onSaved: () => void;
}) {
  const [form, setForm] = useState({
    bankName: bankDetails?.bankName ?? '',
    agency: bankDetails?.agency ?? '',
    account: bankDetails?.account ?? '',
    accountType: (bankDetails?.accountType ?? 'CHECKING') as BankAccountType,
    accountHolder: bankDetails?.accountHolder ?? '',
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
      await apiClient.put('/api/v1/producers/me/bank-details', form, token);
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
        <legend>Dados bancários</legend>
        <label>
          Banco
          <input value={form.bankName} onChange={(e) => update('bankName', e.target.value)} required />
        </label>
        <label>
          Agência
          <input value={form.agency} onChange={(e) => update('agency', e.target.value)} required />
        </label>
        <label>
          Conta
          <input value={form.account} onChange={(e) => update('account', e.target.value)} required />
        </label>
        <label>
          Tipo de conta
          <select value={form.accountType} onChange={(e) => update('accountType', e.target.value as BankAccountType)}>
            {Object.entries(BANK_ACCOUNT_TYPE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>
        <label>
          Titular
          <input value={form.accountHolder} onChange={(e) => update('accountHolder', e.target.value)} required />
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

function DeliveryAreaSection({
  municipalities,
  token,
  onSaved,
}: {
  municipalities: string[];
  token: string | null;
  onSaved: () => void;
}) {
  const [list, setList] = useState<string[]>(municipalities);
  const [newMunicipality, setNewMunicipality] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const addMunicipality = () => {
    const trimmed = newMunicipality.trim();
    if (!trimmed || list.includes(trimmed)) return;
    setList((prev) => [...prev, trimmed]);
    setNewMunicipality('');
  };

  const removeMunicipality = (municipality: string) => {
    setList((prev) => prev.filter((item) => item !== municipality));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSaved(false);
    if (list.length === 0) {
      setError('Adicione ao menos um município.');
      return;
    }
    setSubmitting(true);
    try {
      await apiClient.put('/api/v1/producers/me/delivery-area', { municipalities: list }, token);
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
        <legend>Área de entrega</legend>
        <div className="cert-chips">
          {list.length === 0 ? (
            <span className="admin-empty">Nenhum município adicionado.</span>
          ) : (
            list.map((municipality) => (
              <span className="cert-chip" key={municipality}>
                {municipality}
                <button
                  type="button"
                  onClick={() => removeMunicipality(municipality)}
                  aria-label={`Remover ${municipality}`}
                  className="chip-remove"
                >
                  ×
                </button>
              </span>
            ))
          )}
        </div>
        <label>
          Adicionar município
          <div className="inline-input-group">
            <input
              value={newMunicipality}
              onChange={(e) => setNewMunicipality(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  addMunicipality();
                }
              }}
              placeholder="ex.: Blumenau"
            />
            <button type="button" onClick={addMunicipality}>
              Adicionar
            </button>
          </div>
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

// RF04/RN03 — certificações com comprovante; só ficam visíveis no perfil enquanto estiverem válidas.
function CertificationsSection({ token }: { token: string | null }) {
  const [certifications, setCertifications] = useState<CertificationView[]>([]);
  const [type, setType] = useState('ORGANIC');
  const [validUntil, setValidUntil] = useState('');
  const [proofUrl, setProofUrl] = useState('');
  const [proofName, setProofName] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const load = useCallback(async () => {
    try {
      const producer = await apiClient.get<ProducerResponse>('/api/v1/producers/me', token);
      setCertifications(producer.certifications ?? []);
    } catch {
      // A lista é informativa; o formulário continua utilizável.
    }
  }, [token]);

  useEffect(() => {
    load();
  }, [load]);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSaved(false);
    if (!proofUrl) {
      setError('Envie o comprovante da certificação.');
      return;
    }
    setSubmitting(true);
    try {
      await apiClient.post('/api/v1/producers/me/certifications', { type, proofUrl, validUntil }, token);
      setSaved(true);
      setProofUrl('');
      setProofName(null);
      setValidUntil('');
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível anexar a certificação.');
    } finally {
      setSubmitting(false);
    }
  };

  const openProof = async (url: string) => {
    try {
      await openProtectedFile(url, token);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível abrir o comprovante.');
    }
  };

  return (
    <form className="card-form" onSubmit={handleSubmit}>
      <fieldset>
        <legend>Certificações</legend>
        {certifications.length === 0 ? (
          <span className="admin-empty">Nenhuma certificação anexada.</span>
        ) : (
          <div className="admin-documents">
            {certifications.map((certification) => (
              <span key={`${certification.type}-${certification.proofUrl}`}>
                {CERTIFICATION_LABELS[certification.type] ?? certification.type} — válida até{' '}
                {certification.validUntil.split('-').reverse().join('/')}
                {!certification.valid && ' (expirada, não aparece no perfil)'} ·{' '}
                <button type="button" className="link-button" onClick={() => openProof(certification.proofUrl)}>
                  ver comprovante
                </button>
              </span>
            ))}
          </div>
        )}
        <label>
          Tipo
          <select value={type} onChange={(e) => setType(e.target.value)}>
            {Object.entries(CERTIFICATION_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </label>
        <label>
          Válida até
          <input type="date" value={validUntil} onChange={(e) => setValidUntil(e.target.value)} required />
        </label>
        <FileUploadField
          label="Comprovante (PDF, JPG ou PNG)"
          purpose="CERTIFICATION_PROOF"
          token={token}
          required
          uploadedName={proofName}
          onUploaded={(file) => {
            setProofUrl(file.url);
            setProofName(file.originalName);
          }}
        />
      </fieldset>
      {error && <p className="form-error">{error}</p>}
      {saved && !error && <p className="form-notice">Certificação anexada.</p>}
      <button type="submit" disabled={submitting}>
        {submitting ? 'Anexando...' : 'Anexar certificação'}
      </button>
    </form>
  );
}
