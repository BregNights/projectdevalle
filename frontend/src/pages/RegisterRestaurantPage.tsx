import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { apiClient, ApiError } from '../api/client';
import type { EstablishmentCategory, RegisterRestaurantRequest, RestaurantRegistrationResponse } from '../api/types';

const CATEGORIES: { value: EstablishmentCategory; label: string }[] = [
  { value: 'FINE_DINING', label: 'Alta gastronomia' },
  { value: 'BISTRO', label: 'Bistrô' },
  { value: 'CHAIN', label: 'Rede' },
  { value: 'OTHER', label: 'Outro' },
];

interface FormState {
  email: string;
  password: string;
  corporateName: string;
  cnpj: string;
  category: EstablishmentCategory;
  contactName: string;
  contactRole: string;
  contactPhone: string;
  contactEmail: string;
  addressLabel: string;
  street: string;
  number: string;
  neighborhood: string;
  city: string;
  state: string;
  zipCode: string;
  complement: string;
}

const INITIAL_STATE: FormState = {
  email: '',
  password: '',
  corporateName: '',
  cnpj: '',
  category: 'BISTRO',
  contactName: '',
  contactRole: '',
  contactPhone: '',
  contactEmail: '',
  addressLabel: 'Matriz',
  street: '',
  number: '',
  neighborhood: '',
  city: '',
  state: '',
  zipCode: '',
  complement: '',
};

export function RegisterRestaurantPage() {
  const [form, setForm] = useState<FormState>(INITIAL_STATE);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<RestaurantRegistrationResponse | null>(null);

  const update = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const request: RegisterRestaurantRequest = {
        email: form.email,
        password: form.password,
        corporateName: form.corporateName,
        cnpj: form.cnpj,
        category: form.category,
        contact: {
          name: form.contactName,
          role: form.contactRole || undefined,
          phone: form.contactPhone || undefined,
          email: form.contactEmail || undefined,
        },
        initialAddressLabel: form.addressLabel,
        initialAddress: {
          street: form.street,
          number: form.number,
          neighborhood: form.neighborhood,
          city: form.city,
          state: form.state,
          zipCode: form.zipCode,
          complement: form.complement || undefined,
        },
      };
      const response = await apiClient.post<RestaurantRegistrationResponse>('/api/v1/restaurants', request);
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
        <p>
          <Link to="/login">Ir para o login</Link>
        </p>
      </div>
    );
  }

  return (
    <div className="page page-form">
      <h1>Cadastro de restaurante</h1>
      <form onSubmit={handleSubmit}>
        <fieldset>
          <legend>Acesso</legend>
          <label>
            E-mail
            <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required />
          </label>
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
        </fieldset>

        <fieldset>
          <legend>Dados do estabelecimento</legend>
          <label>
            Razão social
            <input value={form.corporateName} onChange={(e) => update('corporateName', e.target.value)} required />
          </label>
          <label>
            CNPJ
            <input value={form.cnpj} onChange={(e) => update('cnpj', e.target.value)} required />
          </label>
          <label>
            Categoria
            <select value={form.category} onChange={(e) => update('category', e.target.value as EstablishmentCategory)}>
              {CATEGORIES.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
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

        <fieldset>
          <legend>Endereço de entrega</legend>
          <label>
            Identificação (ex.: Matriz, Filial Centro)
            <input value={form.addressLabel} onChange={(e) => update('addressLabel', e.target.value)} required />
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
        </fieldset>

        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Enviando...' : 'Enviar cadastro'}
        </button>
      </form>
    </div>
  );
}
