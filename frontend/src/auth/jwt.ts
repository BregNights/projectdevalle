export interface JwtClaims {
  sub: string;
  role: string;
  exp: number;
}

export function decodeJwt(token: string): JwtClaims | null {
  try {
    const [, payload] = token.split('.');
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = atob(normalized);
    return JSON.parse(json) as JwtClaims;
  } catch {
    return null;
  }
}

export function isExpired(claims: JwtClaims): boolean {
  return claims.exp * 1000 < Date.now();
}
