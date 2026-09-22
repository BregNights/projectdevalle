import { ApiError, BASE_URL } from './client';
import type { ProblemDetail } from './types';

// RF04/RF07 — finalidade do arquivo: define tipos aceitos, tamanho máximo e quem pode ver.
export type FilePurpose = 'OFFER_PHOTO' | 'SUPPORTING_DOCUMENT' | 'CERTIFICATION_PROOF';

export interface StoredFileResponse {
  id: string;
  url: string;
  purpose: FilePurpose;
  contentType: string;
  sizeBytes: number;
  originalName: string;
}

export const FILE_ACCEPT: Record<FilePurpose, string> = {
  OFFER_PHOTO: 'image/jpeg,image/png,image/webp',
  SUPPORTING_DOCUMENT: 'application/pdf,image/jpeg,image/png',
  CERTIFICATION_PROOF: 'application/pdf,image/jpeg,image/png',
};

export const FILE_MAX_MB: Record<FilePurpose, number> = {
  OFFER_PHOTO: 5,
  SUPPORTING_DOCUMENT: 10,
  CERTIFICATION_PROOF: 10,
};

export async function uploadFile(file: File, purpose: FilePurpose, token?: string | null): Promise<StoredFileResponse> {
  const body = new FormData();
  body.append('purpose', purpose);
  body.append('file', file);
  const headers: Record<string, string> = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const response = await fetch(`${BASE_URL}/api/v1/files`, { method: 'POST', headers, body });
  const text = await response.text();
  const data = text ? JSON.parse(text) : undefined;
  if (!response.ok) {
    const problem = data as ProblemDetail | undefined;
    throw new ApiError(problem?.detail ?? 'Não foi possível enviar o arquivo.', response.status);
  }
  return data as StoredFileResponse;
}

// Endereço absoluto de um arquivo público (ex.: foto de oferta) para usar em <img>.
export function publicFileSrc(url: string): string {
  return /^https?:\/\//.test(url) ? url : `${BASE_URL}${url}`;
}

// Arquivos privados (documentos, comprovantes) exigem o token, que um <a href> não envia:
// baixa com autenticação e abre o conteúdo em nova aba.
export async function openProtectedFile(url: string, token: string | null): Promise<void> {
  if (/^https?:\/\//.test(url)) {
    window.open(url, '_blank', 'noopener');
    return;
  }
  const newTab = window.open('', '_blank');
  const response = await fetch(`${BASE_URL}${url}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (!response.ok) {
    newTab?.close();
    throw new ApiError('Arquivo não encontrado ou sem permissão.', response.status);
  }
  const objectUrl = URL.createObjectURL(await response.blob());
  if (newTab) {
    newTab.location.href = objectUrl;
  } else {
    window.open(objectUrl, '_blank');
  }
  setTimeout(() => URL.revokeObjectURL(objectUrl), 60_000);
}
