'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import {
  createColumnHelper,
  flexRender,
  getCoreRowModel,
  useReactTable,
} from '@tanstack/react-table';
import { useShallow } from 'zustand/react/shallow';
import { NouveauBachelierResponse, useNouveauBachelierStore } from '../nouveauBachelierStore';
import ImportExcelModal from './ImportExcelModal';
import JurysNonChargesPanel from './Jurysnonchargespanel';
import ImportExcelMultipleModal from './Importexcelmultiplemodal';
import ImportZipModal from './Importzipmodal';
import ImportNumeroDiplomeModal from './ImportNumeroDiplomeModal';

// ─── Composants UI ───────────────────────────────────────────────────────────

function StatusBadge({ resultat }: { resultat: string }) {
  const styles: Record<string, { bg: string; text: string; label: string }> = {
    ADMIS: { bg: '#dcfce7', text: '#166534', label: '✅ Admis' },
    ADMIS_MENTION: { bg: '#fef9c3', text: '#854d0e', label: '⭐ Admis mention' },
    AJOURNE: { bg: '#fee2e2', text: '#991b1b', label: '❌ Ajourné' },
    ABSENT: { bg: '#f3f4f6', text: '#4b5563', label: '⏳ Absent' },
  };
  const style = styles[resultat] || { bg: '#f3f4f6', text: '#4b5563', label: resultat };

  return (
    <span style={{
      background: style.bg,
      color: style.text,
      padding: '4px 10px',
      borderRadius: 20,
      fontSize: 12,
      fontWeight: 500,
      display: 'inline-block',
    }}>
      {style.label}
    </span>
  );
}

function LoadingSpinner() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: 40 }}>
      <div style={{
        width: 40, height: 40, border: '3px solid #e5e7eb',
        borderTopColor: '#16a34a', borderRadius: '50%',
        animation: 'spin 0.8s linear infinite'
      }} />
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}

// ─── Onglets de premier niveau ────────────────────────────────────────────────

type MainTabKey = 'bacheliers' | 'jurysNonCharges';

export default function BacheliersPage() {
  const [activeMainTab, setActiveMainTab] = useState<MainTabKey>('bacheliers');

  return (
    <div style={{ maxWidth: 1400, margin: '0 auto' }}>
      {/* Onglets principaux */}
      <div style={{
        display: 'flex', gap: 4, borderBottom: '1px solid #e5e7eb',
        padding: '0 24px', marginTop: 24
      }}>
        <MainTabButton
          active={activeMainTab === 'bacheliers'}
          onClick={() => setActiveMainTab('bacheliers')}
          label="📚 Bacheliers"
        />
        <MainTabButton
          active={activeMainTab === 'jurysNonCharges'}
          onClick={() => setActiveMainTab('jurysNonCharges')}
          label="📋 Jurys non chargés"
        />
      </div>

      {activeMainTab === 'bacheliers' ? <BacheliersTable /> : <JurysNonChargesPanel />}
    </div>
  );
}

function MainTabButton({ active, onClick, label }: { active: boolean; onClick: () => void; label: string }) {
  return (
    <button
      onClick={onClick}
      style={{
        padding: '12px 20px',
        fontSize: 14,
        fontWeight: 600,
        background: 'none',
        border: 'none',
        borderBottom: active ? '2px solid #2563eb' : '2px solid transparent',
        color: active ? '#2563eb' : '#6b7280',
        cursor: 'pointer',
        transition: 'all 0.15s',
      }}
    >
      {label}
    </button>
  );
}

// ─── Composant Bacheliers (anciennement BacheliersPage) ──────────────────────

function BacheliersTable() {
  const {
    bacheliers,
    isLoading,
    isSubmitting,
    error,
    searchTerm,
    currentPage,
    pageSize,
    totalElements,
    totalPages,
    fetchPage,
    delete: deleteBachelier,
    setSearchTerm,
    setCurrentPage,
    setPageSize,
    clearError,
  } = useNouveauBachelierStore(
    useShallow((state) => ({
      bacheliers: state.bacheliers,
      isLoading: state.isLoading,
      isSubmitting: state.isSubmitting,
      error: state.error,
      searchTerm: state.searchTerm,
      currentPage: state.currentPage,
      pageSize: state.pageSize,
      totalElements: state.totalElements,
      totalPages: state.totalPages,
      fetchPage: state.fetchPage,
      delete: state.delete,
      setSearchTerm: state.setSearchTerm,
      setCurrentPage: state.setCurrentPage,
      setPageSize: state.setPageSize,
      clearError: state.clearError,
    }))
  );

  const [showImportModal, setShowImportModal] = useState(false);
  const [showImportMultipleModal, setShowImportMultipleModal] = useState(false);
  const [showImportZipModal, setShowImportZipModal] = useState(false);
  const [showImportDiplomeModal, setShowImportDiplomeModal] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null);
  const [searchInput, setSearchInput] = useState(searchTerm);

  // ── Chargement initial ──────────────────────────────────────────────────
  useEffect(() => { fetchPage(0); }, []);

  // ── Debounce de la recherche : on ne tape pas une requête serveur à chaque frappe ──
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      setSearchTerm(searchInput);
      fetchPage(0, pageSize, searchInput);
    }, 400);
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchInput]);

  // ─── Colonnes du tableau ───────────────────────────────────────────────────
  const columnHelper = createColumnHelper<NouveauBachelierResponse>();

  const columns = useMemo(() => [
    columnHelper.accessor('numeroTable', {
      header: 'N° Table',
      cell: info => <span style={{ fontFamily: 'monospace', fontWeight: 500 }}>{info.getValue()}</span>,
    }),
    columnHelper.accessor('numeroDiplome', {
      header: 'N° Diplôme',
      cell: info => (
        <span style={{ fontFamily: 'monospace' }}>
          {info.getValue() || '—'}
        </span>
      ),
    }),
    columnHelper.accessor('nom', {
      header: 'Nom',
      cell: info => <span style={{ fontWeight: 500 }}>{info.getValue()}</span>,
    }),
    columnHelper.accessor('prenoms', {
      header: 'Prénoms',
    }),
    columnHelper.accessor('telephone', {
      header: 'Téléphone',
      cell: info => info.getValue() || '—',
    }),
    columnHelper.accessor('resultat', {
      header: 'Résultat',
      cell: info => <StatusBadge resultat={info.getValue()} />,
    }),
    columnHelper.accessor('mention', {
      header: 'Mention',
      cell: info => info.getValue() || '—',
    }),
    columnHelper.accessor('jury', {
      header: 'Jury',
      // ✅ CORRECTION : l'entité Jury n'a pas de champ "code", seulement "numero" et "name"
      cell: info => info.getValue()?.numero || '—',
    }),
    columnHelper.accessor('dateCreation', {
      header: 'Date création',
      cell: info => new Date(info.getValue()).toLocaleDateString('fr-FR'),
    }),
    columnHelper.display({
      id: 'actions',
      header: 'Actions',
      cell: ({ row }) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <button
            onClick={() => window.location.href = `/bacheliers/${row.original.id}`}
            style={actionButtonStyle('#3b82f6')}
          >
            ✏️
          </button>
          <button
            onClick={() => setDeleteConfirm(row.original.id)}
            style={actionButtonStyle('#ef4444')}
          >
            🗑️
          </button>
        </div>
      ),
    }),
  ], []);

  // ─── Table (le tri/filtrage/pagination sont désormais gérés par le serveur) ─
  const table = useReactTable({
    data: bacheliers,
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
    manualFiltering: true,
    pageCount: totalPages,
  });

  // ─── Handlers ──────────────────────────────────────────────────────────────
  const handleDelete = async (id: string) => {
    await deleteBachelier(id);
    setDeleteConfirm(null);
  };

  const goToPage = (page: number) => {
    setCurrentPage(page);
    fetchPage(page, pageSize, searchTerm);
  };

  const changePageSize = (size: number) => {
    setPageSize(size);
    fetchPage(0, size, searchTerm);
  };

  // ─── Render ────────────────────────────────────────────────────────────────
  if (isLoading && bacheliers.length === 0) return <LoadingSpinner />;

  return (
    <div style={{ padding: 24 }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, flexWrap: 'wrap', gap: 16 }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 600, margin: 0, color: '#111' }}>
            📚 Gestion des Bacheliers
          </h1>
          <p style={{ margin: '4px 0 0', color: '#6b7280', fontSize: 14 }}>
            {totalElements.toLocaleString('fr-FR')} bachelier{totalElements > 1 ? 's' : ''}
          </p>
        </div>

        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <button
            onClick={() => setShowImportModal(true)}
            style={buttonStyle('#16a34a')}
          >
            📥 Importer Excel
          </button>
          <button
            onClick={() => setShowImportMultipleModal(true)}
            style={buttonStyle('#2563eb')}
          >
            🗂️ Importer plusieurs fichiers
          </button>
          <button
            onClick={() => setShowImportZipModal(true)}
            style={buttonStyle('#f97316')}
          >
            🗜️ Importer des ZIP
          </button>
          <button
            onClick={() => setShowImportDiplomeModal(true)}
            style={buttonStyle('#7c3aed')}
          >
            🎓 Importer N° Diplôme
          </button>
          <button
            onClick={() => window.location.href = '/bacheliers/ajouter'}
            style={buttonStyle('#2563eb')}
          >
            + Ajouter
          </button>
        </div>
      </div>

      {/* Error banner */}
      {error && (
        <div style={{
          background: '#fef2f2', border: '1px solid #fecaca',
          borderRadius: 10, padding: '12px 16px', marginBottom: 20,
          display: 'flex', justifyContent: 'space-between', alignItems: 'center'
        }}>
          <span style={{ color: '#dc2626', fontSize: 13 }}>⚠️ {error}</span>
          <button onClick={clearError} style={{ background: 'none', border: 'none', color: '#6b7280', cursor: 'pointer' }}>✕</button>
        </div>
      )}

      {/* Search bar */}
      <div style={{ marginBottom: 20 }}>
        <input
          type="text"
          placeholder="🔍 Rechercher par nom, prénom, numéro table, téléphone ou numéro de jury..."
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          style={{
            width: '100%', padding: '10px 16px', borderRadius: 10,
            border: '1px solid #e5e7eb', fontSize: 14, outline: 'none',
            transition: 'all 0.15s'
          }}
          onFocus={(e) => e.currentTarget.style.borderColor = '#16a34a'}
          onBlur={(e) => e.currentTarget.style.borderColor = '#e5e7eb'}
        />
      </div>

      {/* Table */}
      <div style={{ overflowX: 'auto', borderRadius: 12, border: '1px solid #e5e7eb', background: '#fff', position: 'relative' }}>
        {isLoading && (
          <div style={{
            position: 'absolute', inset: 0, background: 'rgba(255,255,255,0.6)',
            display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 5
          }}>
            <LoadingSpinner />
          </div>
        )}
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
          <thead>
            {table.getHeaderGroups().map(headerGroup => (
              <tr key={headerGroup.id} style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
                {headerGroup.headers.map(header => (
                  <th
                    key={header.id}
                    style={{
                      padding: '12px 16px', textAlign: 'left', fontWeight: 600,
                      color: '#374151', userSelect: 'none'
                    }}
                  >
                    {flexRender(header.column.columnDef.header, header.getContext())}
                  </th>
                ))}
              </tr>
            ))}
          </thead>
          <tbody>
            {table.getRowModel().rows.length === 0 ? (
              <tr>
                <td colSpan={columns.length} style={{ textAlign: 'center', padding: 48, color: '#9ca3af' }}>
                  {searchTerm ? 'Aucun résultat' : 'Aucun bachelier'}
                </td>
              </tr>
            ) : (
              table.getRowModel().rows.map(row => (
                <tr key={row.id} style={{ borderBottom: '1px solid #f3f4f6', transition: 'background 0.1s' }}>
                  {row.getVisibleCells().map(cell => (
                    <td key={cell.id} style={{ padding: '12px 16px', color: '#1f2937' }}>
                      {flexRender(cell.column.columnDef.cell, cell.getContext())}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination serveur */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 20, flexWrap: 'wrap', gap: 12 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <span style={{ fontSize: 13, color: '#6b7280' }}>
            {totalElements === 0 ? 0 : currentPage * pageSize + 1} -{' '}
            {Math.min((currentPage + 1) * pageSize, totalElements)} sur {totalElements.toLocaleString('fr-FR')}
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
          <button onClick={() => goToPage(currentPage - 1)} disabled={currentPage <= 0} style={paginationButtonStyle}>
            ◀ Précédent
          </button>
          {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
            const page = i + Math.max(0, Math.min(totalPages - 5, currentPage - 2));
            return (
              <button
                key={page}
                onClick={() => goToPage(page)}
                style={{
                  ...paginationButtonStyle,
                  background: currentPage === page ? '#16a34a' : '#fff',
                  color: currentPage === page ? '#fff' : '#374151',
                  borderColor: currentPage === page ? '#16a34a' : '#e5e7eb',
                }}
              >
                {page + 1}
              </button>
            );
          })}
          <button onClick={() => goToPage(currentPage + 1)} disabled={currentPage >= totalPages - 1} style={paginationButtonStyle}>
            Suivant ▶
          </button>
        </div>
      </div>

      {/* Delete confirmation modal */}
      {deleteConfirm && (
        <div style={modalOverlayStyle}>
          <div style={modalContentStyle}>
            <h3 style={{ margin: '0 0 8px' }}>Confirmer la suppression</h3>
            <p style={{ margin: 0, color: '#6b7280', fontSize: 14 }}>
              Êtes-vous sûr de vouloir supprimer ce bachelier ?
            </p>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 24 }}>
              <button onClick={() => setDeleteConfirm(null)} style={{ ...buttonStyle('#9ca3af'), padding: '8px 16px' }}>
                Annuler
              </button>
              <button onClick={() => handleDelete(deleteConfirm)} style={{ ...buttonStyle('#dc2626'), padding: '8px 16px' }} disabled={isSubmitting}>
                {isSubmitting ? 'Suppression...' : 'Supprimer'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Import Modal (fichier unique) */}
      <ImportExcelModal
        open={showImportModal}
        onClose={() => setShowImportModal(false)}
        onSuccess={() => { fetchPage(0); setShowImportModal(false); }}
      />

      {/* Import Modal (plusieurs fichiers) */}
      <ImportExcelMultipleModal
        open={showImportMultipleModal}
        onClose={() => setShowImportMultipleModal(false)}
        onSuccess={() => { fetchPage(0); }}
      />

      {/* Import Modal (fichiers ZIP) */}
      <ImportZipModal
        open={showImportZipModal}
        onClose={() => setShowImportZipModal(false)}
        onSuccess={() => { fetchPage(0); }}
      />

      {/* Import Modal (mise à jour numeroDiplome, asynchrone) */}
      <ImportNumeroDiplomeModal
        open={showImportDiplomeModal}
        onClose={() => setShowImportDiplomeModal(false)}
        onSuccess={() => { fetchPage(0); }}
      />
    </div>
  );
}

// ─── Styles ──────────────────────────────────────────────────────────────────

const buttonStyle = (bg: string) => ({
  background: bg,
  color: '#fff',
  border: 'none',
  padding: '9px 18px',
  borderRadius: 8,
  fontSize: 13,
  fontWeight: 600,
  cursor: 'pointer',
  transition: 'opacity 0.15s',
});

const actionButtonStyle = (color: string) => ({
  background: 'none',
  border: 'none',
  fontSize: 16,
  cursor: 'pointer',
  padding: '4px 8px',
  borderRadius: 6,
  transition: 'background 0.1s',
  color,
});

const paginationButtonStyle = {
  padding: '6px 12px',
  borderRadius: 8,
  border: '1px solid #e5e7eb',
  background: '#fff',
  cursor: 'pointer',
  fontSize: 13,
  transition: 'all 0.1s'
};

const modalOverlayStyle = {
  position: 'fixed' as const,
  top: 0, left: 0, right: 0, bottom: 0,
  background: 'rgba(0,0,0,0.5)',
  display: 'flex', justifyContent: 'center', alignItems: 'center',
  zIndex: 1000
};

const modalContentStyle = {
  background: '#fff',
  borderRadius: 16,
  padding: 24,
  width: '90%',
  maxWidth: 400,
  boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)'
};