import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import type { SocialSignupState } from '../auth/socialSignup';

export function SocialSignupChooserPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as SocialSignupState | undefined;

  useEffect(() => {
    if (!state?.verifiedEmail) {
      navigate('/login', { replace: true });
    }
  }, [state, navigate]);

  if (!state?.verifiedEmail) {
    return null;
  }

  return (
    <div className="page page-form">
      <h1>Como você quer se cadastrar?</h1>
      <p>
        Você verificou o e-mail <strong>{state.verifiedEmail}</strong> via Google. Não encontramos uma conta
        com esse e-mail — escolha um tipo de perfil para continuar o cadastro.
      </p>
      <div className="social-signup-choices">
        <button type="button" onClick={() => navigate('/cadastro/produtor', { state })}>
          Sou Produtor
        </button>
        <button type="button" onClick={() => navigate('/cadastro/restaurante', { state })}>
          Sou Restaurante
        </button>
      </div>
    </div>
  );
}
