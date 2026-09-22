import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { apiClient, ApiError } from '../api/client';
import { openProtectedFile } from '../api/files';
import { CATEGORY_LABELS, CERTIFICATION_LABELS, PRODUCTION_TYPE_LABELS } from '../api/labels';
import type { ProducerResponse, RegistrationStatus, RestaurantResponse } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { AdminMetrics } from './AdminMetrics';
import { AdminSettings } from './AdminSettings';
import { StatusBadge } from './StatusBadge';

type Kind = 'producer' | 'restaurant';
type Tab = 'overview' | 'queue' | 'settings';
type ReasonMode = 'reject' | 'suspend' | 'remove';

const DOCUMENT_TYPE_LABELS: Record<string, string> = {
  CPF: 'CPF',
  CNPJ: 'CNPJ',
  DAP_CAF: 'DAP/CAF',
  FISHING_LICENSE: 'Registro de pesca',
};

const REASON_PLACEHOLDERS: Record<ReasonMode, string> = {
  reject: 'Motivo da rejeição',
  suspend: 'Motivo da suspensão',
  remove: 'Motivo da remoção (o usuário perderá o acesso à plataforma)',
};

const STATUS_OPTIONS: { value: RegistrationStatus; label: string }[] = [
  { value: 'PENDING', label: 'Pendentes' },
  { value: 'APPROVED', label: 'Aprovados' },
  { value: 'REJECTED', label: 'Rejeitados' },
  { value: 'SUSPENDED', label: 'Suspensos' },
  { value: 'REMOVED', label: 'Removidos' },
];

export function AdminPanel() {
  const { token } = useAuth();
  const [tab, setTab] = useState<Tab>('overview');
  const [status, setStatus] = useState<RegistrationStatus>('PENDING');
  const [producers, setProducers] = useState<ProducerResponse[]>([]);
  const [restaurants, setRestaurants] = useState<RestaurantResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [producerList, restaurantList] = await Promise.all([
        apiClient.get<ProducerResponse[]>(`/api/v1/admin/producers?status=${status}`, token),
        apiClient.get<RestaurantResponse[]>(`/api/v1/admin/restaurants?status=${status}`, token),
      ]);
      setProducers(producerList);
      setRestaurants(restaurantList);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível carregar os cadastros.');
    } finally {
      setLoading(false);
    }
  }, [status, token]);

  useEffect(() => {
    if (tab === 'queue') {
      load();
    }
  }, [load, tab]);

  async function openFile(url: string) {
    try {
      await openProtectedFile(url, token);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível abrir o arquivo.');
    }
  }

  // RF03 — o administrador confere documentos e comprovantes antes de aprovar.
  function producerDocuments(producer: ProducerResponse): ReactNode {
    const documents = producer.supportingDocuments ?? [];
    const certifications = producer.certifications ?? [];
    if (documents.length === 0 && certifications.length === 0) return null;
    return (
      <div className="admin-documents">
        {documents.map((document) => (
          <span key={document.fileUrl}>
            {DOCUMENT_TYPE_LABELS[document.type] ?? document.type}: {document.documentNumber} ·{' '}
            <button type="button" className="link-button" onClick={() => openFile(document.fileUrl)}>
              ver arquivo
            </button>
          </span>
        ))}
        {certifications.map((certification) => (
          <span key={certification.proofUrl}>
            {CERTIFICATION_LABELS[certification.type] ?? certification.type} (até{' '}
            {certification.validUntil.split('-').reverse().join('/')}
            {!certification.valid && ', expirada'}) ·{' '}
            <button type="button" className="link-button" onClick={() => openFile(certification.proofUrl)}>
              ver comprovante
            </button>
          </span>
        ))}
      </div>
    );
  }

  async function handleApprove(kind: Kind, id: string) {
    setBusyId(id);
    setError(null);
    try {
      await apiClient.post(`/api/v1/admin/${kind}s/${id}/approve`, undefined, token);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível aprovar o cadastro.');
    } finally {
      setBusyId(null);
    }
  }

  async function handleReject(kind: Kind, id: string, reason: string) {
    setBusyId(id);
    setError(null);
    try {
      await apiClient.post(`/api/v1/admin/${kind}s/${id}/reject`, { reason }, token);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível rejeitar o cadastro.');
    } finally {
      setBusyId(null);
    }
  }

  async function handleSuspend(kind: Kind, id: string, reason: string) {
    setBusyId(id);
    setError(null);
    try {
      await apiClient.post(`/api/v1/admin/${kind}s/${id}/suspend`, { reason }, token);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível suspender o cadastro.');
    } finally {
      setBusyId(null);
    }
  }

  async function handleReactivate(kind: Kind, id: string) {
    setBusyId(id);
    setError(null);
    try {
      await apiClient.post(`/api/v1/admin/${kind}s/${id}/reactivate`, undefined, token);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível reativar o cadastro.');
    } finally {
      setBusyId(null);
    }
  }

  async function handleRemove(kind: Kind, id: string, reason: string) {
    setBusyId(id);
    setError(null);
    try {
      await apiClient.post(`/api/v1/admin/${kind}s/${id}/remove`, { reason }, token);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Não foi possível remover o cadastro.');
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div className="page admin-panel">
      <h1>Painel de administração</h1>
      <p className="admin-subtitle">Visão geral da plataforma e aprovação de cadastros.</p>

      <div className="admin-filter">
        <button type="button" className={tab === 'overview' ? 'admin-filter-active' : ''} onClick={() => setTab('overview')}>
          Visão geral
        </button>
        <button type="button" className={tab === 'queue' ? 'admin-filter-active' : ''} onClick={() => setTab('queue')}>
          Fila de aprovação
        </button>
        <button type="button" className={tab === 'settings' ? 'admin-filter-active' : ''} onClick={() => setTab('settings')}>
          Configurações
        </button>
      </div>

      {tab === 'overview' && <AdminMetrics />}

      {tab === 'settings' && <AdminSettings />}

      {tab === 'queue' && (
        <>
          <div className="admin-filter">
            {STATUS_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                className={option.value === status ? 'admin-filter-active' : ''}
                onClick={() => setStatus(option.value)}
              >
                {option.label}
              </button>
            ))}
          </div>

          {error && <p className="form-error">{error}</p>}

          {loading ? (
            <p>Carregando...</p>
          ) : (
            <>
              <section className="admin-section">
                <h2>Produtores ({producers.length})</h2>
                {producers.length === 0 && <p className="admin-empty">Nenhum cadastro neste status.</p>}
                <div className="admin-list">
                  {producers.map((producer) => (
                    <RegistrationRow
                      key={producer.id}
                      title={producer.name}
                      subtitle={PRODUCTION_TYPE_LABELS[producer.productionType] ?? producer.productionType}
                      status={producer.status}
                      busy={busyId === producer.id}
                      reason={producer.statusReason}
                      details={producerDocuments(producer)}
                      note={producer.geocodingPending ? 'Endereço ainda não localizado no mapa.' : undefined}
                      onApprove={() => handleApprove('producer', producer.id)}
                      onReject={(reason) => handleReject('producer', producer.id, reason)}
                      onSuspend={(reason) => handleSuspend('producer', producer.id, reason)}
                      onReactivate={() => handleReactivate('producer', producer.id)}
                      onRemove={(reason) => handleRemove('producer', producer.id, reason)}
                    />
                  ))}
                </div>
              </section>

              <section className="admin-section">
                <h2>Restaurantes ({restaurants.length})</h2>
                {restaurants.length === 0 && <p className="admin-empty">Nenhum cadastro neste status.</p>}
                <div className="admin-list">
                  {restaurants.map((restaurant) => (
                    <RegistrationRow
                      key={restaurant.id}
                      title={restaurant.corporateName}
                      subtitle={CATEGORY_LABELS[restaurant.category] ?? restaurant.category}
                      status={restaurant.status}
                      busy={busyId === restaurant.id}
                      reason={restaurant.statusReason}
                      onApprove={() => handleApprove('restaurant', restaurant.id)}
                      onReject={(reason) => handleReject('restaurant', restaurant.id, reason)}
                      onSuspend={(reason) => handleSuspend('restaurant', restaurant.id, reason)}
                      onReactivate={() => handleReactivate('restaurant', restaurant.id)}
                      onRemove={(reason) => handleRemove('restaurant', restaurant.id, reason)}
                    />
                  ))}
                </div>
              </section>
            </>
          )}
        </>
      )}
    </div>
  );
}

function RegistrationRow({
  title,
  subtitle,
  status,
  busy,
  reason: currentReason,
  details,
  note,
  onApprove,
  onReject,
  onSuspend,
  onReactivate,
  onRemove,
}: {
  title: string;
  subtitle: string;
  status: RegistrationStatus;
  busy: boolean;
  reason: string | null;
  details?: ReactNode;
  note?: string;
  onApprove: () => void;
  onReject: (reason: string) => void;
  onSuspend: (reason: string) => void;
  onReactivate: () => void;
  onRemove: (reason: string) => void;
}) {
  const [reasonMode, setReasonMode] = useState<ReasonMode | null>(null);
  const [reason, setReason] = useState('');

  const submitReason = () => {
    if (!reason.trim()) return;
    if (reasonMode === 'reject') onReject(reason.trim());
    if (reasonMode === 'suspend') onSuspend(reason.trim());
    if (reasonMode === 'remove') onRemove(reason.trim());
    setReasonMode(null);
    setReason('');
  };

  return (
    <div className="admin-row">
      <div className="admin-row-info">
        <div>
          <strong>{title}</strong>
          <span className="admin-row-subtitle">{subtitle}</span>
        </div>
        <StatusBadge status={status} />
      </div>

      {note && <p className="form-notice admin-row-note">{note}</p>}
      {currentReason && <p className="form-notice admin-row-note">Motivo: {currentReason}</p>}
      {details}

      {reasonMode ? (
        <div className="admin-reason">
          <textarea
            placeholder={REASON_PLACEHOLDERS[reasonMode]}
            value={reason}
            onChange={(event) => setReason(event.target.value)}
            rows={2}
          />
          <div className="admin-row-actions">
            <button
              type="button"
              className="admin-action-reject"
              onClick={submitReason}
              disabled={busy || !reason.trim()}
            >
              Confirmar
            </button>
            <button type="button" className="admin-action-ghost" onClick={() => setReasonMode(null)} disabled={busy}>
              Cancelar
            </button>
          </div>
        </div>
      ) : (
        <div className="admin-row-actions">
          {status === 'PENDING' && (
            <>
              <button type="button" className="admin-action-approve" onClick={onApprove} disabled={busy}>
                Aprovar
              </button>
              <button type="button" className="admin-action-reject" onClick={() => setReasonMode('reject')} disabled={busy}>
                Rejeitar
              </button>
            </>
          )}
          {status === 'APPROVED' && (
            <button type="button" className="admin-action-reject" onClick={() => setReasonMode('suspend')} disabled={busy}>
              Suspender
            </button>
          )}
          {status === 'SUSPENDED' && (
            <button type="button" className="admin-action-approve" onClick={onReactivate} disabled={busy}>
              Reativar
            </button>
          )}
          {status !== 'REMOVED' && (
            <button type="button" className="admin-action-ghost" onClick={() => setReasonMode('remove')} disabled={busy}>
              Remover
            </button>
          )}
        </div>
      )}
    </div>
  );
}
