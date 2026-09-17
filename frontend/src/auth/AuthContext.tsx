import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import { decodeJwt, isExpired, type JwtClaims } from './jwt';

const STORAGE_KEY = 'projectdevalle.accessToken';

interface AuthState {
  token: string | null;
  claims: JwtClaims | null;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

function readStoredToken(): string | null {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (!stored) return null;
  const claims = decodeJwt(stored);
  if (!claims || isExpired(claims)) {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
  return stored;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => readStoredToken());

  const login = (newToken: string) => {
    localStorage.setItem(STORAGE_KEY, newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem(STORAGE_KEY);
    setToken(null);
  };

  const claims = useMemo(() => (token ? decodeJwt(token) : null), [token]);

  return <AuthContext.Provider value={{ token, claims, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
