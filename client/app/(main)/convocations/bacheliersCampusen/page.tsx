'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import {
  createColumnHelper,
  flexRender,
  getCoreRowModel,
  useReactTable,
} from '@tanstack/react-table';
import { useShallow } from 'zustand/react/shallow';
import { BacheliersToCampusen, useBacheliersToCampusenStore } from '../bacheliersToCampusenStore';
import ImportBacheliersModal from './ImportBacheliersModal';

// ─── Composants UI ───────────────────────────────────────────────────────────

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

export default function BacheliersCampusenPage() {
  const {
    bacheliers,
    isLoading,
    error,
    searchTerm,
    currentPage,
    pageSize,
    totalElements,
    totalPages,
    fetchPage,
    setSearchTerm,
    setCurrentPage,
    setPageSize,
    clearError,
  } = useBacheliersToCampusenStore(
    useShallow((state) => ({
      bacheliers: state.bacheliers,
      isLoading: state.isLoading,
      error: state.error,
      searchTerm: state.searchTerm,
      currentPage: state.currentPage,
      pageSize: state.pageSize,
      totalElements: state.totalElements,
      totalPages: state.totalPages,
      fetchPage: state.fetchPage,
      setSearchTerm: state.setSearchTerm,
      setCurrentPage: state.setCurrentPage,
      setPageSize: state.setPageSize,
      clearError: state.clearError,
    }))
  );

  const [showImportModal, setShowImportModal] = useState(false);
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
  const columnHelper = createColumnHelper<BacheliersToCampusen>();

  // ── Tous les champs du modèle sont affichés (demande explicite : "afficher
  //    toutes les informations dans la liste") ────────────────────────────────
  const FIELDS: { key: keyof BacheliersToCampusen; label: string; bold?: boolean; mono?: boolean }[] = [
    { key: 'numeroTable', label: 'N° Table', mono: true },
    { key: 'serie', label: 'Série' },
    { key: 'nom', label: 'Nom', bold: true },
    { key: 'prenoms', label: 'Prénoms', bold: true },
    { key: 'annee', label: 'Année' },
    { key: 'dateNaissance', label: 'Date naissance' },
    { key: 'anneeNaissance', label: 'Année naissance' },
    { key: 'lieuNaissance', label: 'Lieu naissance' },
    { key: 'paysNaissance', label: 'Pays naissance' },
    { key: 'sexe', label: 'Sexe' },
    { key: 'telephone', label: 'Téléphone' },
    { key: 'nationalite', label: 'Nationalité' },
    { key: 'etsProvenance', label: 'Établissement provenance' },
    { key: 'academieProvenance', label: 'Académie provenance' },
    { key: 'typeCandidature', label: 'Type candidature' },
    { key: 'residence', label: 'Résidence' },
    { key: 'centreEcrit', label: "Centre d'écrit" },
    { key: 'numeroJury', label: 'N° Jury' },
    { key: 'nombreFois', label: 'Nbre fois' },
    { key: 'matiereOptionnelle1', label: 'Mat. opt. 1' },
    { key: 'matiereOptionnelle2', label: 'Mat. opt. 2' },
    { key: 'matiereOptionnelle3', label: 'Mat. opt. 3' },
    { key: 'epreuveFacultativeListeA', label: 'Épr. fac. liste A' },
    { key: 'epreuveFacultativeListeB', label: 'Épr. fac. liste B' },
    { key: 'noteEpreuveFacultativeA', label: 'Note EF A' },
    { key: 'noteEpreuveFacultativeB', label: 'Note EF B' },
    { key: 'noteEps', label: 'Note EPS' },
    { key: 'present', label: 'Présent' },
    { key: 'resultat', label: 'Résultat' },
    { key: 'mention', label: 'Mention' },
    { key: 'groupeResultat', label: 'Groupe résultat' },
    { key: 'dateDeliberation', label: 'Date délibération' },
    { key: 'cec', label: 'CEC' },
    { key: 'numeroAec', label: 'N° AEC' },
    { key: 'anneeExtraitEc', label: "Année extrait EC" },
    { key: 'typeAec', label: 'Type AEC' },
    { key: 'moyenneSeconde', label: 'Moy. 2nde' },
    { key: 'moyennePremiere', label: 'Moy. 1ère' },
    { key: 'moyenneS1Terminale', label: 'Moy. S1 term.' },
    { key: 'moyenneS2Terminale', label: 'Moy. S2 term.' },
    { key: 'totalPointsGroupe1', label: 'Tot. pts grp 1' },
    { key: 'moyenneGroupe1', label: 'Moy. grp 1' },
    { key: 'totalPointsG1G2', label: 'Tot. pts G1+G2' },
    { key: 'moyenneGenerale', label: 'Moy. générale' },
    { key: 'moyenneMatieresFondamentales', label: 'Moy. mat. fond.' },
    { key: 'moyenneRetenue', label: 'Moy. retenue' },
    { key: 'moyenneDefinitive', label: 'Moy. définitive' },
  ];

  const columns = useMemo(() => [
    ...FIELDS.map(({ key, label, bold, mono }) =>
      columnHelper.accessor(key as any, {
        header: label,
        cell: (info: any) => {
          const value = info.getValue();
          const display = value === null || value === undefined || value === '' ? '—' : String(value);
          if (!bold && !mono) return display;
          return (
            <span style={{ fontWeight: bold ? 500 : undefined, fontFamily: mono ? 'monospace' : undefined }}>
              {display}
            </span>
          );
        },
      })
    ),
    columnHelper.accessor('notes', {
      header: 'Notes (matières spécifiques)',
      cell: info => {
        const notes = info.getValue();
        if (!notes || Object.keys(notes).length === 0) return '—';
        return Object.entries(notes).map(([k, v]) => `${k}: ${v}`).join(' · ');
      },
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
  ], []);

  // ─── Table (le tri/filtrage/pagination sont gérés par le serveur) ─────────
  const table = useReactTable({
    data: bacheliers,
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
    manualFiltering: true,
    pageCount: totalPages,
  });

  // ─── Handlers ──────────────────────────────────────────────────────────────
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
    <div style={{ padding: 24, maxWidth: 1400, margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, flexWrap: 'wrap', gap: 16 }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 600, margin: 0, color: '#111' }}>
            🎓 Bacheliers Campusen
          </h1>
          <p style={{ margin: '4px 0 0', color: '#6b7280', fontSize: 14 }}>
            {totalElements.toLocaleString('fr-FR')} bachelier{totalElements > 1 ? 's' : ''}
          </p>
        </div>

        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <button onClick={() => setShowImportModal(true)} style={buttonStyle('#16a34a')}>
            📥 Importer Excel
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
        <table style={{ width: 'max-content', minWidth: '100%', borderCollapse: 'collapse', fontSize: 13 }}>
          <thead>
            {table.getHeaderGroups().map(headerGroup => (
              <tr key={headerGroup.id} style={{ background: '#f9fafb', borderBottom: '1px solid #e5e7eb' }}>
                {headerGroup.headers.map(header => (
                  <th
                    key={header.id}
                    style={{ padding: '10px 14px', textAlign: 'left', fontWeight: 600, color: '#374151', userSelect: 'none', whiteSpace: 'nowrap' }}
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
                    <td key={cell.id} style={{ padding: '10px 14px', color: '#1f2937', whiteSpace: 'nowrap' }}>
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

      {/* Import Modal */}
      <ImportBacheliersModal
        open={showImportModal}
        onClose={() => setShowImportModal(false)}
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

const paginationButtonStyle = {
  padding: '6px 12px',
  borderRadius: 8,
  border: '1px solid #e5e7eb',
  background: '#fff',
  cursor: 'pointer',
  fontSize: 13,
  transition: 'all 0.1s'
};
