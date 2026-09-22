import { useState, type ChangeEvent } from 'react';
import { ApiError } from '../api/client';
import { FILE_ACCEPT, FILE_MAX_MB, uploadFile, type FilePurpose, type StoredFileResponse } from '../api/files';

// RF04/RF07 — escolhe um arquivo, envia na hora e devolve a URL gerada pelo servidor.
export function FileUploadField({
  label,
  purpose,
  token,
  required,
  uploadedName,
  onUploaded,
}: {
  label: string;
  purpose: FilePurpose;
  token?: string | null;
  required?: boolean;
  uploadedName?: string | null;
  onUploaded: (file: StoredFileResponse) => void;
}) {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    setError(null);
    if (file.size > FILE_MAX_MB[purpose] * 1024 * 1024) {
      setError(`O arquivo deve ter no máximo ${FILE_MAX_MB[purpose]} MB.`);
      return;
    }
    setUploading(true);
    try {
      onUploaded(await uploadFile(file, purpose, token));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível enviar o arquivo.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <label>
      {label}
      <input
        type="file"
        accept={FILE_ACCEPT[purpose]}
        onChange={handleChange}
        disabled={uploading}
        required={required && !uploadedName}
      />
      {uploading && <span className="form-notice">Enviando...</span>}
      {uploadedName && !uploading && <span className="form-notice">Arquivo enviado: {uploadedName}</span>}
      {error && <span className="form-error">{error}</span>}
    </label>
  );
}
