import { useState, type FormEvent } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { apiClient, ApiError } from '../api/client';
import { allCoveredCities, usePlatformCoverage } from '../api/platform';
import type {
  ProducerRegistrationResponse,
  ProductionType,
  RegisterProducerRequest,
  SupportingDocumentType,
  TaxDocumentType,
} from '../api/types';
import { generateRandomPassword } from '../auth/socialLogin';
import type { SocialSignupState } from '../auth/socialSignup';
import { FileUploadField } from '../components/FileUploadField';

const PRODUCTION_TYPES: { value: ProductionType; label: string }[] = [
  { value: 'FARMING', label: 'Agricultura' },
  { value: 'FISHING', label: 'Pesca' },
  { value: 'LIVESTOCK', label: 'Pecuária' },
  { value: 'ARTISANAL_PROCESSING', label: 'Processamento artesanal' },
];

const SUPPORTING_DOCUMENT_TYPES: { value: SupportingDocumentType; label: string }[] = [
  { value: 'CPF', label: 'CPF' },
  { value: 'CNPJ', label: 'CNPJ' },
  { value: 'DAP_CAF', label: 'DAP/CAF' },
  { value: 'FISHING_LICENSE', label: 'Registro de pesca' },
];

interface FormState {
  email: string;
  password: string;
  name: string;
  taxDocumentType: TaxDocumentType;
  taxDocumentNumber: string;
  productionType: ProductionType;
  street: string;
  number: string;
  neighborhood: string;
  city: string;
  state: string;
  zipCode: string;
  complement: string;
  documentType: SupportingDocumentType;
  documentNumber: string;
  fileUrl: string;
}

const INITIAL_STATE: FormState = {
  email: '',
  password: '',
  name: '',
  taxDocumentType: 'CPF',
  taxDocumentNumber: '',
  productionType: 'FARMING',
  street: '',
  number: '',
  neighborhood: '',
  city: '',
  state: '',
  zipCode: '',
  complement: '',
  documentType: 'CPF',
  documentNumber: '',
  fileUrl: '',
};

export function RegisterProducerPage() {
  const location = useLocation();
  const socialSignup = location.state as SocialSignupState | undefined;

  const [form, setForm] = useState<FormState>(() => ({
    ...INITIAL_STATE,
    email: socialSignup?.verifiedEmail ?? '',
  }));
  const [socialPassword] = useState(() => (socialSignup ? generateRandomPassword() : ''));
  const [uploadedDocumentName, setUploadedDocumentName] = useState<string | null>(null);
  const coverage = usePlatformCoverage();
  const coveredCities = allCoveredCities(coverage);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<ProducerRegistrationResponse | null>(null);

  const update = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    if (!form.fileUrl) {
      setError('Envie o arquivo do documento comprobatório.');
      return;
    }
    setSubmitting(true);
    try {
      const request: RegisterProducerRequest = {
        email: form.email,
        password: socialSignup ? socialPassword : form.password,
        name: form.name,
        taxDocumentType: form.taxDocumentType,
        taxDocumentNumber: form.taxDocumentNumber,
        productionType: form.productionType,
        originAddress: {
          street: form.street,
          number: form.number,
          neighborhood: form.neighborhood,
          city: form.city,
          state: form.state,
          zipCode: form.zipCode,
          complement: form.complement || undefined,
        },
        supportingDocuments: [
          { type: form.documentType, documentNumber: form.documentNumber, fileUrl: form.fileUrl },
        ],
      };
      const response = await apiClient.post<ProducerRegistrationResponse>('/api/v1/producers', request);
      setResult(response);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível concluir o cadastro.');
    } finally {
      setSubmitting(false);
    }
  };

  if (result) {
    return (
      <div className="page page-form">
        <h1>Cadastro enviado</h1>
        <p>Seu cadastro foi recebido e está pendente de aprovação pela administração.</p>
        {result.geocodingPending && (
          <p className="form-notice">
            Não conseguimos localizar automaticamente seu endereço no mapa agora, mas isso não impede o
            cadastro — a localização será resolvida antes da aprovação.
          </p>
        )}
        <p>
          <Link to="/login">Ir para o login</Link>
        </p>
      </div>
    );
  }

  return (
    <div className="page page-form">
      <h1>Cadastro de produtor</h1>
      <form onSubmit={handleSubmit}>
        <fieldset>
          <legend>Acesso</legend>
          <label>
            E-mail
            <input
              type="email"
              value={form.email}
              onChange={(e) => update('email', e.target.value)}
              readOnly={Boolean(socialSignup)}
              required
            />
          </label>
          {socialSignup ? (
            <p className="form-notice">E-mail verificado via Google.</p>
          ) : (
            <label>
              Senha
              <input
                type="password"
                minLength={8}
                value={form.password}
                onChange={(e) => update('password', e.target.value)}
                required
              />
            </label>
          )}
        </fieldset>

        <fieldset>
          <legend>Dados do produtor</legend>
          <label>
            Nome / Razão social
            <input value={form.name} onChange={(e) => update('name', e.target.value)} required />
          </label>
          <label>
            Tipo de produção
            <select value={form.productionType} onChange={(e) => update('productionType', e.target.value as ProductionType)}>
              {PRODUCTION_TYPES.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
          <label>
            Documento
            <select
              value={form.taxDocumentType}
              onChange={(e) => update('taxDocumentType', e.target.value as TaxDocumentType)}
            >
              <option value="CPF">CPF</option>
              <option value="CNPJ">CNPJ</option>
            </select>
          </label>
          <label>
            Número do documento
            <input
              value={form.taxDocumentNumber}
              onChange={(e) => update('taxDocumentNumber', e.target.value)}
              required
            />
          </label>
        </fieldset>

        <fieldset>
          <legend>Propriedade / ponto de coleta</legend>
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
            <input
              value={form.city}
              onChange={(e) => update('city', e.target.value)}
              list="covered-cities"
              required
            />
            <datalist id="covered-cities">
              {coveredCities.map((city) => (
                <option key={city} value={city} />
              ))}
            </datalist>
            {coveredCities.length > 0 && (
              <span className="form-notice">
                Atendemos: {coverage?.coverageRegions.map((region) => region.name).join(' e ')}. Cadastros de
                outras cidades não podem ser aprovados.
              </span>
            )}
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

        <fieldset>
          <legend>Documento comprobatório</legend>
          <label>
            Tipo
            <select
              value={form.documentType}
              onChange={(e) => update('documentType', e.target.value as SupportingDocumentType)}
            >
              {SUPPORTING_DOCUMENT_TYPES.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
          <label>
            Número do documento
            <input value={form.documentNumber} onChange={(e) => update('documentNumber', e.target.value)} required />
          </label>
          <FileUploadField
            label="Arquivo do documento (PDF, JPG ou PNG)"
            purpose="SUPPORTING_DOCUMENT"
            required
            uploadedName={uploadedDocumentName}
            onUploaded={(file) => {
              update('fileUrl', file.url);
              setUploadedDocumentName(file.originalName);
            }}
          />
        </fieldset>

        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Enviando...' : 'Enviar cadastro'}
        </button>
      </form>
    </div>
  );
}
