import { useState, type FormEvent } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { apiClient, ApiError } from '../api/client';

type Step = 'request' | 'reset';

// RF06 — recuperação de acesso em duas etapas: pedir o link por e-mail e depois definir a nova senha.
// A resposta do primeiro passo é sempre a mesma, exista ou não a conta (RN39). O link do e-mail traz o
// token em ?token=..., que já abre direto a segunda etapa.
export function ForgotPasswordPage() {
  const [searchParams] = useSearchParams();
  const tokenFromLink = searchParams.get('token') ?? '';
  const [step, setStep] = useState<Step>(tokenFromLink ? 'reset' : 'request');
  const [email, setEmail] = useState('');
  const [resetToken, setResetToken] = useState(tokenFromLink);
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleRequest = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await apiClient.post('/api/v1/auth/password-reset/request', { email });
      setStep('reset');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível solicitar a recuperação.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleReset = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    if (newPassword !== confirmPassword) {
      setError('As senhas não conferem.');
      return;
    }
    setSubmitting(true);
    try {
      await apiClient.post('/api/v1/auth/password-reset', { resetToken: resetToken.trim(), newPassword });
      navigate('/login', { replace: true, state: { passwordReset: true } });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Link inválido ou expirado. Solicite um novo.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page page-form">
      <h1>Recuperar acesso</h1>
      {step === 'request' ? (
        <form onSubmit={handleRequest}>
          <p>Informe o e-mail da sua conta. Se ele estiver cadastrado, enviaremos um link para criar uma nova senha.</p>
          <label>
            E-mail
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          {error && <p className="form-error">{error}</p>}
          <button type="submit" disabled={submitting}>
            {submitting ? 'Enviando...' : 'Enviar link'}
          </button>
        </form>
      ) : (
        <form onSubmit={handleReset}>
          {tokenFromLink ? (
            <p>Crie sua nova senha.</p>
          ) : (
            <>
              <p className="form-notice">
                Se <strong>{email}</strong> estiver cadastrado, você receberá um e-mail com um link válido por 30
                minutos. Abra o link para continuar ou cole abaixo o código que veio nele.
              </p>
              <label>
                Código de recuperação
                <input value={resetToken} onChange={(e) => setResetToken(e.target.value)} required />
              </label>
            </>
          )}
          <label>
            Nova senha
            <input
              type="password"
              minLength={8}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
          </label>
          <label>
            Confirmar nova senha
            <input
              type="password"
              minLength={8}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </label>
          {error && <p className="form-error">{error}</p>}
          <button type="submit" disabled={submitting}>
            {submitting ? 'Salvando...' : 'Redefinir senha'}
          </button>
          <button type="button" className="admin-action-ghost" onClick={() => setStep('request')} disabled={submitting}>
            Pedir outro link
          </button>
        </form>
      )}
      <p>
        <Link to="/login">Voltar para o login</Link>
      </p>
    </div>
  );
}
