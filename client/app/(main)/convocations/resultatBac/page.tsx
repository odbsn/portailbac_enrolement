'use client';

import { useMemo, useState, useEffect } from 'react'; // ← Ajouter useEffect
import {
  createColumnHelper,
  flexRender,
  getCoreRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  useReactTable,
  SortingState,
} from '@tanstack/react-table';
import { useShallow } from 'zustand/react/shallow';
import { NouveauBachelierResponse, useNouveauBachelierStore } from '../nouveauBachelierStore';
import ImportExcelModal from './ImportExcelModal';

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

// ─── Composant principal ─────────────────────────────────────────────────────

export default function BacheliersPage() {
  const {
    bacheliers,
    isLoading,
    isSubmitting,
    error,
    searchTerm,
    fetchAll,
    delete: deleteBachelier,
    setSearchTerm,
    clearError,
  } = useNouveauBachelierStore(
    useShallow((state) => ({
      bacheliers: state.bacheliers,
      isLoading: state.isLoading,
      isSubmitting: state.isSubmitting,
      error: state.error,
      searchTerm: state.searchTerm,
      fetchAll: state.fetchAll,
      delete: state.delete,
      setSearchTerm: state.setSearchTerm,
      clearError: state.clearError,
    }))
  );

  const [sorting, setSorting] = useState<SortingState>([]);
  const [showImportModal, setShowImportModal] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState<string | null>(null);

  // Chargement initial - CORRECTION : utiliser useEffect au lieu de useState
  useEffect(() => { fetchAll(); }, []);

  // ─── Colonnes du tableau ───────────────────────────────────────────────────
  const columnHelper = createColumnHelper<NouveauBachelierResponse>();
  
  const columns = useMemo(() => [
    columnHelper.accessor('numeroTable', {
      header: 'N° Table',
      cell: info => <span style={{ fontFamily: 'monospace', fontWeight: 500 }}>{info.getValue()}</span>,
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
      cell: info => info.getValue()?.code || '—',
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

  // ─── Filtrage côté client ──────────────────────────────────────────────────
  const filteredData = useMemo(() => {
    if (!searchTerm.trim()) return bacheliers;
    const term = searchTerm.toLowerCase();
    return bacheliers.filter(b => 
      b.nom?.toLowerCase().includes(term) ||
      b.prenoms?.toLowerCase().includes(term) ||
      b.numeroTable?.toLowerCase().includes(term) ||
      b.telephone?.includes(term)
    );
  }, [bacheliers, searchTerm]);

  // ─── Table ─────────────────────────────────────────────────────────────────
  const table = useReactTable({
    data: filteredData,
    columns,
    state: { sorting, globalFilter: searchTerm },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    initialState: { pagination: { pageSize: 10 } },
  });

  // ─── Handlers ──────────────────────────────────────────────────────────────
  const handleDelete = async (id: string) => {
    await deleteBachelier(id);
    setDeleteConfirm(null);
  };

  // ─── Render ────────────────────────────────────────────────────────────────
  if (isLoading && bacheliers.length === 0) return <LoadingSpinner />;

  return (
    <div style={{ padding: 24, maxWidth: 1400, margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, flexWrap: 'wrap', gap: 16 }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 600, margin: 0, color: '#111' }}>
            📚 Gestion des Bacheliers
          </h1>
          <p style={{ margin: '4px 0 0', color: '#6b7280', fontSize: 14 }}>
            {filteredData.length} bachelier{filteredData.length > 1 ? 's' : ''}
          </p>
        </div>
        
        <div style={{ display: 'flex', gap: 12 }}>
          <button
            onClick={() => setShowImportModal(true)}
            style={buttonStyle('#16a34a')}
          >
            📥 Importer Excel
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
          placeholder="🔍 Rechercher par nom, prénom, numéro table ou téléphone..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
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
      <div style={{ overflowX: 'auto', borderRadius: 12, border: '1px solid #e5e7eb', background: '#fff' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
          <thead>
            {table.getHeaderGroups().map(headerGroup => (
              <tr key={headerGroup.id} style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
                {headerGroup.headers.map(header => (
                  <th
                    key={header.id}
                    onClick={header.column.getToggleSortingHandler()}
                    style={{
                      padding: '12px 16px', textAlign: 'left', fontWeight: 600,
                      color: '#374151', cursor: header.column.getCanSort() ? 'pointer' : 'default',
                      userSelect: 'none'
                    }}
                  >
                    {flexRender(header.column.columnDef.header, header.getContext())}
                    {header.column.getIsSorted() === 'asc' && ' 🔼'}
                    {header.column.getIsSorted() === 'desc' && ' 🔽'}
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

      {/* Pagination - CORRECTION ici */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 20, flexWrap: 'wrap', gap: 12 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <span style={{ fontSize: 13, color: '#6b7280' }}>
            {table.getState().pagination.pageIndex * table.getState().pagination.pageSize + 1} -{' '}
            {Math.min((table.getState().pagination.pageIndex + 1) * table.getState().pagination.pageSize, filteredData.length)} sur {filteredData.length}
          </span>
          <select
            value={table.getState().pagination.pageSize}
            onChange={e => table.setPageSize(Number(e.target.value))}
            style={{ padding: '6px 10px', borderRadius: 8, border: '1px solid #e5e7eb', fontSize: 13 }}
          >
            {[5, 10, 20, 50].map((size) => (
              <option key={size} value={size}>{size} / page</option>
            ))}
          </select>
        </div>
        
        <div style={{ display: 'flex', gap: 6 }}>
          <button onClick={() => table.previousPage()} disabled={!table.getCanPreviousPage()} style={paginationButtonStyle}>
            ◀ Précédent
          </button>
          {Array.from({ length: Math.min(5, table.getPageCount()) }, (_, i) => {
            const page = i + Math.max(0, Math.min(table.getPageCount() - 5, table.getState().pagination.pageIndex - 2));
            return (
              <button
                key={page}
                onClick={() => table.setPageIndex(page)}
                style={{
                  ...paginationButtonStyle,
                  background: table.getState().pagination.pageIndex === page ? '#16a34a' : '#fff',
                  color: table.getState().pagination.pageIndex === page ? '#fff' : '#374151',
                  borderColor: table.getState().pagination.pageIndex === page ? '#16a34a' : '#e5e7eb',
                }}
              >
                {page + 1}
              </button>
            );
          })}
          <button onClick={() => table.nextPage()} disabled={!table.getCanNextPage()} style={paginationButtonStyle}>
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

      {/* Import Modal */}
      <ImportExcelModal
        open={showImportModal}
        onClose={() => setShowImportModal(false)}
        onSuccess={() => { fetchAll(); setShowImportModal(false); }}
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
  ':hover': { opacity: 0.9 }
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
  ':hover': { background: '#f3f4f6' }
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