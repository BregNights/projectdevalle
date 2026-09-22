import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { apiClient, ApiError } from '../api/client';
import type { TokenResponse } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { SocialLoginButtons } from '../auth/SocialLoginButtons';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const passwordReset = (location.state as { passwordReset?: boolean } | null)?.passwordReset === true;

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const response = await apiClient.post<TokenResponse>('/api/v1/auth/login', { email, password });
      login(response.accessToken);
      navigate('/dashboard');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível entrar. Tente novamente.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page page-form">
      <h1>Entrar</h1>
      {passwordReset && <p className="form-notice">Senha redefinida. Entre com a nova senha.</p>}
      <form onSubmit={handleSubmit}>
        <label>
          E-mail
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          Senha
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {error && <p className="form-error">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Entrando...' : 'Entrar'}
        </button>
      </form>
      <p>
        <Link to="/recuperar-senha">Esqueci minha senha</Link>
      </p>
      <SocialLoginButtons />
    </div>
  );
}
