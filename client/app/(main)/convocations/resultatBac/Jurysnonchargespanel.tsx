'use client';

import { useEffect, useMemo, useState } from 'react';
import { useShallow } from 'zustand/react/shallow';
import { useNouveauBachelierStore } from '../nouveauBachelierStore';

type TabKey = 'general' | 'technique';

export default function JurysNonChargesPanel() {
  const [activeTab, setActiveTab] = useState<TabKey>('general');
  const [page, setPage] = useState(0);          // page courante (0-indexée), côté client
  const [pageSize, setPageSize] = useState(10);  // taille de page, comme BacheliersTable
  const [searchTerm, setSearchTerm] = useState(''); // filtre texte, côté client

  const { jurysNonCharges, isLoading, error, fetchJurysNonCharges, clearError } =
    useNouveauBachelierStore(
      useShallow((state) => ({
        jurysNonCharges: state.jurysNonCharges,
        isLoading: state.isLoading,
        error: state.error,
        fetchJurysNonCharges: state.fetchJurysNonCharges,
        clearError: state.clearError,
      }))
    );

  // Recharge la liste complète à chaque changement d'onglet, et réinitialise pagination + filtre
  useEffect(() => {
    fetchJurysNonCharges(activeTab === 'technique');
    setPage(0);
    setSearchTerm('');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  // ── Filtre côté client sur numéro et nom du jury ──────────────────────────
  const filteredJurys = useMemo(() => {
    if (!searchTerm.trim()) return jurysNonCharges;
    const term = searchTerm.toLowerCase();
    return jurysNonCharges.filter(j =>
      j.numero?.toLowerCase().includes(term) ||
      j.name?.toLowerCase().includes(term)
    );
  }, [jurysNonCharges, searchTerm]);

  // ── Pagination côté client : on découpe la liste filtrée ─────────────────
  const totalElements = filteredJurys.length;
  const totalPages = Math.max(1, Math.ceil(totalElements / pageSize));

  const paginatedJurys = useMemo(() => {
    const start = page * pageSize;
    return filteredJurys.slice(start, start + pageSize);
  }, [filteredJurys, page, pageSize]);

  const goToPage = (p: number) => {
    setPage(Math.max(0, Math.min(p, totalPages - 1)));
  };

  const changePageSize = (size: number) => {
    setPageSize(size);
    setPage(0);
  };

  const handleSearchChange = (value: string) => {
    setSearchTerm(value);
    setPage(0); // toute nouvelle recherche repart de la première page
  };

  return (
    <div style={{ padding: 24 }}>
      <h2 style={{ fontSize: 20, fontWeight: 600, margin: '0 0 16px', color: '#111' }}>
        📋 Jurys sans bachelier chargé
      </h2>

      {/* Onglets */}
      <div style={{ display: 'flex', gap: 4, borderBottom: '1px solid #e5e7eb', marginBottom: 20 }}>
        <TabButton
          active={activeTab === 'general'}
          onClick={() => setActiveTab('general')}
          label="🏛️ Général"
        />
        <TabButton
          active={activeTab === 'technique'}
          onClick={() => setActiveTab('technique')}
          label="🔧 Technique"
        />
      </div>

      {/* Error banner */}
      {error && (
        <div style={{
          background: '#fef2f2', border: '1px solid #fecaca',
          borderRadius: 10, padding: '12px 16px', marginBottom: 16,
          display: 'flex', justifyContent: 'space-between', alignItems: 'center'
        }}>
          <span style={{ color: '#dc2626', fontSize: 13 }}>⚠️ {error}</span>
          <button onClick={clearError} style={{ background: 'none', border: 'none', color: '#6b7280', cursor: 'pointer' }}>✕</button>
        </div>
      )}

      {/* Search bar */}
      <div style={{ marginBottom: 16 }}>
        <input
          type="text"
          placeholder="🔍 Rechercher par numéro ou nom de jury..."
          value={searchTerm}
          onChange={(e) => handleSearchChange(e.target.value)}
          style={{
            width: '100%', padding: '10px 16px', borderRadius: 10,
            border: '1px solid #e5e7eb', fontSize: 14, outline: 'none',
            transition: 'all 0.15s'
          }}
          onFocus={(e) => e.currentTarget.style.borderColor = '#16a34a'}
          onBlur={(e) => e.currentTarget.style.borderColor = '#e5e7eb'}
        />
      </div>

      <p style={{ color: '#6b7280', fontSize: 13, marginBottom: 12 }}>
        {totalElements} jury{totalElements > 1 ? 's' : ''} {activeTab === 'technique' ? 'technique' : 'général'}{activeTab === 'technique' && totalElements > 1 ? 's' : ''} sans bachelier chargé
      </p>

      {/* Liste */}
      <div style={{ borderRadius: 12, border: '1px solid #e5e7eb', background: '#fff', overflow: 'hidden' }}>
        {isLoading ? (
          <div style={{ padding: 40, textAlign: 'center', color: '#9ca3af' }}>Chargement...</div>
        ) : totalElements === 0 ? (
          <div style={{ padding: 40, textAlign: 'center', color: '#9ca3af' }}>
            {searchTerm
              ? 'Aucun résultat'
              : `✅ Tous les jurys ${activeTab === 'technique' ? 'techniques' : 'généraux'} ont des bacheliers chargés`}
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
            <thead>
              <tr style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
                <th style={thStyle}>N° Jury</th>
                <th style={thStyle}>Nom</th>
              </tr>
            </thead>
            <tbody>
              {paginatedJurys.map(j => (
                <tr key={j.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                  <td style={tdStyle}><span style={{ fontFamily: 'monospace', fontWeight: 500 }}>{j.numero || '—'}</span></td>
                  <td style={tdStyle}>{j.name || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Pagination client */}
      {totalElements > 0 && (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 20, flexWrap: 'wrap', gap: 12 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <span style={{ fontSize: 13, color: '#6b7280' }}>
              {page * pageSize + 1} -{' '}
              {Math.min((page + 1) * pageSize, totalElements)} sur {totalElements}
            </span>
            <select
              value={pageSize}
              onChange={e => changePageSize(Number(e.target.value))}
              style={{ padding: '6px 10px', borderRadius: 8, border: '1px solid #e5e7eb', fontSize: 13 }}
            >
              {[10, 20, 50, 100].map((size) => (
                <option key={size} value={size}>{size} / page</option>
              ))}
            </select>
          </div>

          <div style={{ display: 'flex', gap: 6 }}>
            <button onClick={() => goToPage(page - 1)} disabled={page <= 0} style={paginationButtonStyle}>
              ◀ Précédent
            </button>
            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
              const p = i + Math.max(0, Math.min(totalPages - 5, page - 2));
              return (
                <button
                  key={p}
                  onClick={() => goToPage(p)}
                  style={{
                    ...paginationButtonStyle,
                    background: page === p ? '#16a34a' : '#fff',
                    color: page === p ? '#fff' : '#374151',
                    borderColor: page === p ? '#16a34a' : '#e5e7eb',
                  }}
                >
                  {p + 1}
                </button>
              );
            })}
            <button onClick={() => goToPage(page + 1)} disabled={page >= totalPages - 1} style={paginationButtonStyle}>
              Suivant ▶
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function TabButton({ active, onClick, label }: { active: boolean; onClick: () => void; label: string }) {
  return (
    <button
      onClick={onClick}
      style={{
        padding: '10px 18px',
        fontSize: 14,
        fontWeight: 600,
        background: 'none',
        border: 'none',
        borderBottom: active ? '2px solid #16a34a' : '2px solid transparent',
        color: active ? '#16a34a' : '#6b7280',
        cursor: 'pointer',
        transition: 'all 0.15s',
      }}
    >
      {label}
    </button>
  );
}

const thStyle: React.CSSProperties = {
  padding: '12px 16px', textAlign: 'left', fontWeight: 600, color: '#374151',
};

const tdStyle: React.CSSProperties = {
  padding: '12px 16px', color: '#1f2937',
};

const paginationButtonStyle: React.CSSProperties = {
  padding: '6px 12px',
  borderRadius: 8,
  border: '1px solid #e5e7eb',
  background: '#fff',
  cursor: 'pointer',
  fontSize: 13,
  transition: 'all 0.1s',
};