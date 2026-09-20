import { GoogleLogin, type CredentialResponse } from '@react-oauth/google';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useAuth } from './AuthContext';
import { loginWithGoogle } from './socialLogin';
import type { SocialSignupState } from './socialSignup';

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID ?? '';

export function SocialLoginButtons() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  if (!GOOGLE_CLIENT_ID) {
    return null;
  }

  const handleSuccess = (credentialResponse: CredentialResponse) => {
    if (!credentialResponse.credential) return;

    loginWithGoogle(credentialResponse.credential)
      .then((response) => {
        if (response.status === 'AUTHENTICATED' && response.accessToken) {
          login(response.accessToken);
          navigate('/dashboard');
          return;
        }
        const state: SocialSignupState = {
          verifiedEmail: response.email ?? '',
          displayName: response.displayName ?? '',
        };
        navigate('/cadastro/escolher-tipo', { state });
      })
      .catch(() => setError('Não foi possível entrar com essa conta. Tente novamente.'));
  };

  return (
    <div className="social-login">
      <p className="social-login-divider">ou continue com</p>
      <GoogleLogin
        onSuccess={handleSuccess}
        onError={() => setError('Não foi possível entrar com essa conta. Tente novamente.')}
        text="continue_with"
        width="100%"
      />
      {error && <p className="form-error">{error}</p>}
    </div>
  );
}
