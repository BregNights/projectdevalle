import { apiClient } from '../api/client';
import type { SocialLoginResponse } from '../api/types';

export function loginWithGoogle(idToken: string): Promise<SocialLoginResponse> {
  return apiClient.post<SocialLoginResponse>('/api/v1/auth/google', { idToken });
}

// Nunca mostrada ao usuário: contas criadas via login social sempre entram pelo Google,
// mas o cadastro (POST /api/v1/producers|restaurants) exige uma senha por causa do fluxo comum.
export function generateRandomPassword(): string {
  const bytes = new Uint8Array(24);
  crypto.getRandomValues(bytes);
  return btoa(String.fromCharCode(...bytes)).replace(/[^a-zA-Z0-9]/g, '') + 'Aa1!';
}
