"use client";

import React, { useEffect, useState, useRef, useCallback } from "react";
import { Button } from "primereact/button";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Dialog } from "primereact/dialog";
import { ProgressSpinner } from "primereact/progressspinner";
import { Toast } from "primereact/toast";
import { Toolbar } from "primereact/toolbar";
import { Tooltip } from "primereact/tooltip";
import { DataTableStateEvent } from "primereact/datatable";
import { debounce } from "lodash";
import { saveAs } from "file-saver";
import { CandidatFinis } from "../convocationStore";
import { useCandidatInspectionAcademieStore } from "./candidatInspectionAcademieStore";
import DetailCandidatDialog from "../etablissements/DetailCandidatDialog";

interface CandidatInspectionAcademieListProps {
  onViewCandidat?: (candidat: CandidatFinis) => void;
  onPrintConvocation?: (candidat: CandidatFinis) => void;
}

export default function CandidatInspectionAcademieList({
  onViewCandidat,
  onPrintConvocation,
}: CandidatInspectionAcademieListProps) {
  const toast = useRef<Toast>(null);

  const {
    candidats,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    isLoading,
    error,
    fetchCandidats,
    setPage,
    setPageSize,
    setKeyword,
    exportCandidats,
    isExportingExcel,
    clearError,
  } = useCandidatInspectionAcademieStore();

  const [globalFilter, setGlobalFilter] = useState("");
  const [viewDialogVisible, setViewDialogVisible] = useState(false);
  const [selectedCandidat, setSelectedCandidat] =
    useState<CandidatFinis | null>(null);
  const [isExporting, setIsExporting] = useState(false);

  const first = currentPage * pageSize;

  useEffect(() => {
    fetchCandidats();
  }, [fetchCandidats]);

  const debouncedSetKeyword = useCallback(
    debounce((value: string) => {
      setKeyword(value);
    }, 500),
    [setKeyword],
  );

  useEffect(() => {
    debouncedSetKeyword(globalFilter);
    return () => debouncedSetKeyword.cancel();
  }, [globalFilter, debouncedSetKeyword]);

  useEffect(() => {
    if (error) {
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: error,
        life: 5000,
      });
    }
  }, [error]);

  const handleExportExcel = async () => {
    if (isExporting) return;
    setIsExporting(true);

    try {
      toast.current?.show({
        severity: "info",
        summary: "Export",
        detail: "Génération du fichier en cours, veuillez patienter...",
        life: 3000,
      });

      const blob = await exportCandidats();

      if (blob) {
        const filename = `candidats_inspection_academie_${new Date()
          .toISOString()
          .slice(0, 19)}.xlsx`;
        saveAs(blob, filename);

        toast.current?.show({
          severity: "success",
          summary: "Export réussi",
          detail: `${totalElements} candidats exportés`,
          life: 3000,
        });
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erreur",
          detail: "Aucune donnée à exporter",
          life: 5000,
        });
      }
    } catch (err) {
      console.error("Erreur export:", err);
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Erreur lors de l'export",
        life: 5000,
      });
    } finally {
      setIsExporting(false);
    }
  };

  // ─── Templates ───────────────────────────────────────────────────────────

  const epsTemplate = (rowData: CandidatFinis) => {
    if (!rowData.eps) return "-";
    switch (rowData.eps) {
      case "A":
        return <span className="text-green-600 font-semibold">Apte</span>;
      case "I":
        return <span className="text-red-600 font-semibold">Inapte</span>;
      default:
        return rowData.eps;
    }
  };

  const resultatTemplate = (rowData: CandidatFinis) => {
    if (!rowData.resultat) {
      return <span className="text-500 italic">En attente</span>;
    }

    const resultat = rowData.resultat.toUpperCase();

    if (resultat.includes("ADMIS")) {
      return (
        <span className="text-green-700 font-semibold bg-green-100 px-2 py-1 border-round">
          {rowData.resultat}
        </span>
      );
    }

    if (resultat.includes("REFUS") || resultat.includes("AJOURN")) {
      return (
        <span className="text-red-700 font-semibold bg-red-100 px-2 py-1 border-round">
          {rowData.resultat}
        </span>
      );
    }

    return <span>{rowData.resultat}</span>;
  };

  const mentionTemplate = (rowData: CandidatFinis) => {
    if (!rowData.mention) return "-";
    return (
      <span className="font-medium text-primary">{rowData.mention}</span>
    );
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return "-";
    if (dateString.includes("/")) return dateString;
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString;
      return date.toLocaleDateString("fr-FR");
    } catch {
      return dateString;
    }
  };

  const matieresOptionnellesTemplate = (rowData: CandidatFinis) => {
    const matieres = [rowData.mo1, rowData.mo2, rowData.mo3].filter(Boolean);
    return matieres.length === 0 ? "-" : matieres.join(", ");
  };

  const matiereFacultativeTemplate = (rowData: CandidatFinis) => {
    const facultatives = [];
    if (rowData.ef1) facultatives.push(rowData.ef1);
    if (rowData.ef2) facultatives.push(rowData.ef2);
    return facultatives.length === 0 ? "-" : facultatives.join(", ");
  };

  const centreEcritTemplate = (rowData: CandidatFinis) => {
    return rowData.centreEcritParticulier || rowData.centreEcrit?.code || "-";
  };

  const etablissementTemplate = (rowData: CandidatFinis) => {
    return rowData.etablissement?.name || "-";
  };

  const actionsTemplate = (rowData: CandidatFinis) => (
    <div className="flex gap-2 justify-content-center">
      <Button
        icon="pi pi-eye"
        rounded
        text
        severity="info"
        tooltip="Détails"
        tooltipOptions={{ position: "bottom" }}
        className="p-0 border-circle border-2 flex items-center justify-center bg-blue-100 border-primary"
        style={{ width: "2.4rem", height: "2.4rem", minWidth: "2.4rem" }}
        onClick={() => {
          setSelectedCandidat(rowData);
          setViewDialogVisible(true);
          onViewCandidat?.(rowData);
        }}
        size="small"
      />
    </div>
  );

  const onPageChange = (event: DataTableStateEvent) => {
    const newPage = event.page ?? 0;
    const newRows = event.rows ?? 20;

    if (newRows !== pageSize) {
      setPageSize(newRows);
    } else {
      setPage(newPage);
    }
  };

  const handlePrintConvocation = (candidat: CandidatFinis) => {
    onPrintConvocation?.(candidat);
  };

  if (error && candidats.length === 0) {
    return (
      <div className="card flex flex-column align-items-center p-6">
        <i className="pi pi-exclamation-triangle text-5xl text-orange-500" />
        <h4 className="mt-3 mb-2">Erreur de chargement</h4>
        <p className="text-color-secondary text-center">{error}</p>
        <Button
          label="Réessayer"
          icon="pi pi-refresh"
          severity="success"
          onClick={() => {
            clearError();
            fetchCandidats();
          }}
        />
      </div>
    );
  }

  return (
    <>
      <Toast ref={toast} />
      <Tooltip target=".action-btn" />

      {/* Dialog simple avec spinner pendant l'export */}
      <Dialog
        visible={isExporting}
        header="Export en cours"
        modal
        closable={false}
        style={{ width: "380px" }}
        onHide={() => {}}
      >
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            padding: "16px",
            gap: "12px",
          }}
        >
          <ProgressSpinner style={{ width: "50px", height: "50px" }} strokeWidth="4" />
          <span style={{ fontSize: "14px", color: "#374151" }}>
            Génération du fichier Excel, veuillez patienter...
          </span>
        </div>
      </Dialog>

      <div className="card shadow-4 border-round-xl overflow-hidden">
        {/* Toolbar */}
        <div className="bg-white p-2 border-bottom-1 surface-border">
          <Toolbar
            left={
              <div className="flex align-items-center gap-3 pb-3 flex-wrap">
                <div className="flex align-items-center gap-2 bg-primary-50 px-3 py-2 border-round-lg">
                  <i className="pi pi-users text-primary" />
                  <span className="font-semibold">
                    {totalElements.toLocaleString()} candidats
                  </span>
                </div>
                <div
                  style={{
                    width: "300px",
                    height: "35px",
                    display: "flex",
                    alignItems: "center",
                    padding: "0 10px",
                    gap: "4px",
                    border: "1px solid #3b82f6",
                    borderRadius: "999px",
                    background: "#fff",
                  }}
                >
                  <i
                    className="pi pi-search"
                    style={{ fontSize: "14px", color: "#6b7280" }}
                  />
                  <input
                    type="text"
                    value={globalFilter}
                    onChange={(e) => setGlobalFilter(e.target.value)}
                    placeholder="Rechercher..."
                    style={{
                      flex: 1,
                      border: "none",
                      outline: "none",
                      fontSize: "14px",
                      background: "transparent",
                    }}
                  />
                </div>
              </div>
            }
            className="border-none bg-transparent p-0"
          />
          <div className="flex align-items-center gap-3 flex-wrap">
            <Button
              onClick={handleExportExcel}
              disabled={isExporting || isExportingExcel}
              icon="pi pi-download"
              label="Exporter toute la liste (Excel)"
              className="p-button-primary"
            />
          </div>
        </div>

        {/* DataTable */}
        <div className="p-0">
          <DataTable
            value={candidats}
            paginator
            rows={pageSize}
            first={first}
            totalRecords={totalElements}
            onPage={onPageChange}
            rowsPerPageOptions={[20, 50, 100, 200, 500]}
            className="custom-table p-datatable-sm"
            emptyMessage="Aucun candidat trouvé"
            style={{ fontSize: "0.75rem" }}
            loading={isLoading}
            resizableColumns
            columnResizeMode="fit"
            showGridlines
            stripedRows
            lazy
          >
            <Column
              field="etablissement"
              header="Établissement"
              body={etablissementTemplate}
              style={{ maxWidth: "10rem", wordWrap: "break-word", whiteSpace: "normal" }}
            />
            <Column
              field="centreEcrit"
              header="Crt. écrit"
              body={centreEcritTemplate}
              style={{ maxWidth: "6rem" }}
            />
            <Column field="jury" header="Jury" style={{ maxWidth: "4rem" }} />
            <Column
              field="numeroTable"
              header="N° Table"
              style={{ maxWidth: "5rem" }}
            />
            <Column
              field="resultat"
              header="Résultat"
              body={resultatTemplate}
              style={{ maxWidth: "20rem" }}
            />
            <Column
              field="mention"
              header="Mention"
              body={mentionTemplate}
              style={{ maxWidth: "6rem" }}
            />
            <Column field="serie" header="Série" style={{ maxWidth: "3rem" }} />
            <Column
              header="Mat. opt."
              body={matieresOptionnellesTemplate}
              style={{
                maxWidth: "10rem",
                wordWrap: "break-word",
                whiteSpace: "normal",
              }}
            />
            <Column
              field="prenoms"
              header="Prénom(s)"
              style={{
                maxWidth: "8rem",
                wordWrap: "break-word",
                whiteSpace: "normal",
              }}
            />
            <Column field="nom" header="Nom" style={{ maxWidth: "6rem" }} />
            <Column field="sexe" header="Sexe" style={{ maxWidth: "3rem" }} />
            <Column
              field="dateNaissance"
              header="Date naiss."
              body={(c) => formatDate(c.dateNaissance)}
              style={{ maxWidth: "6rem" }}
            />
            <Column
              field="lieuNaissance"
              header="Lieu naiss."
              style={{
                maxWidth: "9rem",
                wordWrap: "break-word",
                whiteSpace: "normal",
              }}
            />
            <Column
              field="eps"
              header="EPS"
              body={epsTemplate}
              style={{ maxWidth: "3rem" }}
            />
            <Column
              header="Matière fac."
              body={matiereFacultativeTemplate}
              style={{
                maxWidth: "10rem",
                wordWrap: "break-word",
                whiteSpace: "normal",
              }}
            />
            <Column
              header="Actions"
              body={actionsTemplate}
              style={{ maxWidth: "6rem" }}
              align="center"
            />
          </DataTable>
        </div>
      </div>

      <DetailCandidatDialog
        visible={viewDialogVisible}
        candidat={selectedCandidat}
        onHide={() => {
          setViewDialogVisible(false);
          setSelectedCandidat(null);
        }}
        onPrintConvocation={handlePrintConvocation}
      />
    </>
  );
}