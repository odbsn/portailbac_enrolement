"use client";

import React, { useEffect, useState, useRef, useCallback } from "react";
import { Button } from "primereact/button";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Dialog } from "primereact/dialog";
import { InputText } from "primereact/inputtext";
import { Toast } from "primereact/toast";
import { ProgressSpinner } from "primereact/progressspinner";
import { Toolbar } from "primereact/toolbar";
import { Tooltip } from "primereact/tooltip";
import { DataTableStateEvent } from "primereact/datatable";
import { Dropdown } from "primereact/dropdown";
import { debounce } from "lodash";
import { saveAs } from "file-saver";
import { useCandidatStore, CandidatFinis } from "../convocationStore";
import CandidatDialog, { CandidatDialogRef } from "./CandidatDialog";
import DetailCandidatDialog from "./DetailCandidatDialog";
import ConvocationMassivePrint, {
  ConvocationMassivePrintRef,
} from "./allConvocations/ConvocationMassivePrint";
import { useSerieStore } from "../etablissements/serieStore";
import RegenerateConvocationButton from "./RegenerateConvocationButton";
import ConvocationButton from "./ConvocationButtonAdmin";
import { Divide } from "lucide-react";

interface CandidatsTabTableProps {
  onViewCandidat?: (candidat: CandidatFinis) => void;
  onPrintConvocation?: (candidat: CandidatFinis) => void;
}

export default function CandidatsTabTable({
  onViewCandidat,
  onPrintConvocation,
}: CandidatsTabTableProps) {
  const toast = useRef<Toast>(null);
  const massivePrintRef = useRef<ConvocationMassivePrintRef>(null);
  const candidatDialogRef = useRef<CandidatDialogRef>(null);
  const {
    candidats,
    totalElements,
    totalPages,
    currentPage,
    pageSize,
    isLoading: isLoadingCandidats,
    error,
    fetchCandidats,
    setFilters,
    setPage,
    setPageSize,
    setKeyword,
    exportCandidats,
    filters,
    fetchAllCandidatsBySerie,
    exportPdf,
    exportZipBySerie,
    isExportingZip,
    isExportingPdf,
    regenerateConvocation,
  } = useCandidatStore();

  const { allSeries, isLoadingMy, errorMy, fetchAllSeries } = useSerieStore();

  const [globalFilter, setGlobalFilter] = useState("");
  const [convocationDialogVisible, setConvocationDialogVisible] =
    useState(false);
  const [selectedCandidatForConvocation, setSelectedCandidatForConvocation] =
    useState<CandidatFinis | null>(null);
  const [viewDialogVisible, setViewDialogVisible] = useState(false);
  const [selectedCandidat, setSelectedCandidat] =
    useState<CandidatFinis | null>(null);
  const [selectedSerieCode, setSelectedSerieCode] = useState<string>("");
  const [etablissementCodeFilter, setEtablissementCodeFilter] =
    useState<string>("");
  const [numeroDossierFilter, setNumeroDossierFilter] = useState<string>("");
  const [rows, setRows] = useState(20);
  const [isExporting, setIsExporting] = useState(false);
  const [convocationSerie, setConvocationSerie] = useState<string>("");
  const [convocationDropdownVisible, setConvocationDropdownVisible] =
    useState(false);
  const [massivePrintDialogVisible, setMassivePrintDialogVisible] =
    useState(false);
  const [massivePrintCandidats, setMassivePrintCandidats] = useState<
    CandidatFinis[]
  >([]);
  const [isLoadingMassiveCandidats, setIsLoadingMassiveCandidats] =
    useState(false);
  const [selectedSerieForPrint, setSelectedSerieForPrint] =
    useState<string>("");
  const [candidatDialogVisible, setCandidatDialogVisible] = useState(false);
  const [editingCandidatId, setEditingCandidatId] = useState<string | null>(
    null,
  );

  const first = currentPage * pageSize;

  // Options pour le dropdown des convocations
  const convocationOptions = [
    { label: "Toutes les séries", value: "all" },
    ...(allSeries || []).map((serie: string) => ({
      label: `Série ${serie}`,
      value: serie,
    })),
  ];

  // Chargement initial des candidats
  useEffect(() => {
    fetchCandidats();
  }, [fetchCandidats]);

  // Chargement des séries de l'établissement connecté (une seule fois)
  useEffect(() => {
    fetchAllSeries();
  }, [fetchAllSeries]);

  // Debounce pour la recherche
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

  const debouncedSetEtablissementCode = useCallback(
    debounce((value: string) => {
      setFilters({ etablissementCode: value, page: 0 });
    }, 500),
    [setFilters],
  );
  const debouncedSetNumeroDossier = useCallback(
    debounce((value: string) => {
      setFilters({ numeroDossier: value, page: 0 });
    }, 500),
    [setFilters],
  );

  useEffect(() => {
    debouncedSetNumeroDossier(numeroDossierFilter);
    return () => debouncedSetNumeroDossier.cancel();
  }, [numeroDossierFilter, debouncedSetNumeroDossier]);

  useEffect(() => {
    debouncedSetEtablissementCode(etablissementCodeFilter);
    return () => debouncedSetEtablissementCode.cancel();
  }, [etablissementCodeFilter, debouncedSetEtablissementCode]);

  // Filtre par série
  const handleSerieClick = (serieCode: string) => {
    const newSerie = serieCode === selectedSerieCode ? "" : serieCode;
    setSelectedSerieCode(newSerie);
    setFilters({ serie: newSerie, page: 0 });
  };

  // Ouvrir le dialogue d'ajout
  const handleAddCandidat = () => {
    setEditingCandidatId(null);
    setCandidatDialogVisible(true);
    candidatDialogRef.current?.open();
  };

  // Ouvrir le dialogue de modification
  const handleEditCandidat = (candidat: CandidatFinis) => {
    setEditingCandidatId(candidat.id || null);
    setCandidatDialogVisible(true);
    candidatDialogRef.current?.open(candidat.id);
  };

  // Gérer le succès après ajout/modification
  const handleCandidatSuccess = () => {
    // Rafraîchir la liste des candidats
    fetchCandidats();
    toast.current?.show({
      severity: "success",
      summary: "Succès",
      detail: editingCandidatId
        ? "Candidat modifié avec succès"
        : "Candidat ajouté avec succès",
      life: 3000,
    });
    setCandidatDialogVisible(false);
    setEditingCandidatId(null);
  };

  // Export via API
  const handleExport = async () => {
    if (isExporting) return;

    setIsExporting(true);
    try {
      toast.current?.show({
        severity: "info",
        summary: "Export",
        detail: "Préparation de l'export...",
        life: 2000,
      });

      const filters = {
        serie: selectedSerieCode || undefined,
        keyword: globalFilter || undefined,
      };

      const blob = await exportCandidats(filters);

      if (blob) {
        const filename = `candidats_${selectedSerieCode || "tous"}_${new Date()
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
    } catch (error) {
      console.error("Erreur export:", error);
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

  // Générer les convocations
  const handleGenerateConvocations = () => {
    setConvocationDropdownVisible(true);
  };

  const handleConvocationConfirm = async () => {
    setConvocationDropdownVisible(false);

    if (!convocationSerie) {
      toast.current?.show({
        severity: "warn",
        summary: "Attention",
        detail: "Veuillez sélectionner une série",
        life: 3000,
      });
      return;
    }

    setIsLoadingMassiveCandidats(true);
    setMassivePrintDialogVisible(true);

    try {
      // Charger tous les candidats de la série sélectionnée
      const serieToLoad =
        convocationSerie === "all" ? undefined : convocationSerie;
      await fetchAllCandidatsBySerie(serieToLoad);

      const { candidats: loadedCandidats } = useCandidatStore.getState();

      if (loadedCandidats.length === 0) {
        toast.current?.show({
          severity: "warn",
          summary: "Attention",
          detail: `Aucun candidat trouvé pour ${
            convocationSerie === "all"
              ? "toutes les séries"
              : `la série ${convocationSerie}`
          }`,
          life: 3000,
        });
        setMassivePrintDialogVisible(false);
      } else {
        setMassivePrintCandidats(loadedCandidats);
        setSelectedSerieForPrint(convocationSerie);
        toast.current?.show({
          severity: "success",
          summary: "Succès",
          detail: `${loadedCandidats.length} candidat(s) chargé(s) pour ${
            convocationSerie === "all"
              ? "toutes les séries"
              : `la série ${convocationSerie}`
          }`,
          life: 3000,
        });
      }
    } catch (error) {
      console.error("Erreur lors du chargement des candidats:", error);
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Erreur lors du chargement des candidats",
        life: 5000,
      });
      setMassivePrintDialogVisible(false);
    } finally {
      setIsLoadingMassiveCandidats(false);
    }
  };

  // Ajoutez cette fonction après les autres handle
  const handleRegenerateConvocation = async (numeroTable: string) => {
    try {
      const result = await regenerateConvocation(numeroTable);
      if (result) {
        toast.current?.show({
          severity: "success",
          summary: "Succès",
          detail: "Convocation régénérée avec succès",
          life: 3000,
        });
      } else {
        toast.current?.show({
          severity: "error",
          summary: "Erreur",
          detail: "Erreur lors de la régénération",
          life: 5000,
        });
      }
    } catch (error) {
      console.error("Erreur lors de la régénération:", error);
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Erreur lors de la régénération de la convocation",
        life: 5000,
      });
    }
  };

  // Fonctions pour l'impression massive
  const handleMassivePrint = () => {
    massivePrintRef.current?.print();
  };

  const handlePrintConvocation = (candidat: CandidatFinis) => {
    setSelectedCandidatForConvocation(candidat);
    setConvocationDialogVisible(true);
  };

  const handleMassiveDownloadPDF = async () => {
    try {
      await massivePrintRef.current?.downloadPDF();
      toast.current?.show({
        severity: "success",
        summary: "Succès",
        detail: "PDF généré avec succès",
        life: 3000,
      });
    } catch (error) {
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Erreur lors de la génération du PDF",
        life: 5000,
      });
    }
  };
  const handleExportZip = async () => {
    const blob = await exportZipBySerie();
    if (blob) {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "listes_candidats_par_serie.zip";
      a.click();
      window.URL.revokeObjectURL(url);
    }
  };

  useEffect(() => {
    if (error) {
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: error,
        life: 5000,
      });
    }
    if (errorMy) {
      toast.current?.show({
        severity: "warn",
        summary: "Attention",
        detail: errorMy,
        life: 5000,
      });
    }
  }, [error, errorMy]);

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

    // Matières facultatives simples (ef1, ef2)
    if (rowData.ef1) facultatives.push(rowData.ef1);
    if (rowData.ef2) facultatives.push(rowData.ef2);

    // Matières facultatives avec détails
    if (rowData.nbMatFacult && rowData.nbMatFacult > 0) {
      if (rowData.centreMatFac1) {
        facultatives.push(
          `${rowData.libMatFac1 || "Matière 1"} (${rowData.centreMatFac1})`,
        );
      }
      if (rowData.centreMatFac2) {
        facultatives.push(
          `${rowData.libMatFac2 || "Matière 2"} (${rowData.centreMatFac2})`,
        );
      }
    }

    return facultatives.length === 0 ? "-" : facultatives.join(", ");
  };

  const centreEcritTemplate = (rowData: CandidatFinis) => {
    return rowData.centreEcritParticulier || rowData.centreEcrit?.code || "-";
  };

  const getPrintButtonLabel = () => {
    if (selectedSerieForPrint === "all") {
      return "Imprimer toutes les convocations (toutes séries)";
    }
    if (selectedSerieForPrint) {
      return `Imprimer toutes les convocations de la série ${selectedSerieForPrint}`;
    }
    return "Imprimer toutes les convocations";
  };

  const actionsTemplate = (rowData: CandidatFinis) => (
    <div className="flex gap-2 justify-content-center">
      {/* Détails */}
      <Button
        icon="pi pi-eye"
        rounded
        text
        severity="info"
        tooltip="Détails"
        tooltipOptions={{ position: "bottom" }}
        className="p-0 border-circle border-2 bg-blue-100 border-primary"
        style={{ width: "2.4rem", height: "2.4rem", minWidth: "2.4rem" }}
        onClick={() => {
          setSelectedCandidat(rowData);
          setViewDialogVisible(true);
          onViewCandidat?.(rowData);
        }}
        size="small"
      />

      {/* Modifier */}
      <Button
        icon="pi pi-pencil"
        rounded
        text
        severity="warning"
        tooltip="Modifier"
        tooltipOptions={{ position: "bottom" }}
        className="p-0 border-circle border-2 bg-yellow-100 border-yellow-500"
        style={{ width: "2.4rem", height: "2.4rem", minWidth: "2.4rem" }}
        onClick={() => handleEditCandidat(rowData)}
        size="small"
      />

      {/* Générer/Télécharger (existant) */}
      <ConvocationButton
        numeroTable={rowData.numeroTable || ""}
        label=""
        icon="pi pi-file-pdf"
      />

      {/* Régénérer (nouveau) */}
      <RegenerateConvocationButton
        numeroTable={rowData.numeroTable || ""}
        label=""
        icon="pi pi-refresh"
        onSuccess={() => {
          toast.current?.show({
            severity: "success",
            summary: "Succès",
            detail: `Convocation régénérée avec succès pour ${rowData.prenoms} ${rowData.nom}`,
            life: 3000,
          });
        }}
        onError={() => {
          toast.current?.show({
            severity: "error",
            summary: "Erreur",
            detail: `Erreur lors de la régénération de la convocation pour ${rowData.prenoms} ${rowData.nom}`,
            life: 5000,
          });
        }}
      />
      {/* <RegenerateConvocationButton 
      numeroTable={rowData.numeroTable || ""} 
      label=""
      icon="pi pi-refresh"
    /> */}
    </div>
  );
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

  const onPageChange = (event: DataTableStateEvent) => {
    const newPage = event.page ?? 0;
    const newRows = event.rows ?? 20;

    if (newRows !== pageSize) {
      setPageSize(newRows);
    } else {
      setPage(newPage);
    }
  };

  const handleRowsPerPageChange = (value: number) => {
    setPageSize(value);
    setPage(0);
  };

  const isLoading = isLoadingCandidats || isLoadingMy;

  /* if (isLoading && candidats.length === 0) {
    return (
      <div
        className="flex flex-column justify-content-center align-items-center p-6"
        style={{ minHeight: "400px" }}
      >
        <ProgressSpinner />
        <span className="mt-4 text-color-secondary">
          Chargement des données...
        </span>
      </div>
    );
  } */

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
          onClick={() => fetchCandidats()}
        />
      </div>
    );
  }

  return (
    <>
      <Toast ref={toast} />
      <Tooltip target=".action-btn" />

      <div className="card shadow-4 border-round-xl overflow-hidden">
        {/* Toolbar */}
        <div className="bg-white p-4 border-bottom-1 surface-border">
          <Toolbar
            left={
              <div className="flex align-items-center gap-3 flex-wrap">
                <div className="flex align-items-center gap-2 bg-primary-50 px-3 py-2 border-round-lg">
                  <i className="pi pi-users text-primary" />
                  <span className="font-semibold">
                    {totalElements.toLocaleString()} candidats
                  </span>
                </div>

                <div
                  style={{
                    width: "220px",
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
                    className="pi pi-building"
                    style={{ fontSize: "14px", color: "#6b7280" }}
                  />
                  <input
                    type="text"
                    value={etablissementCodeFilter}
                    onChange={(e) => setEtablissementCodeFilter(e.target.value)}
                    placeholder="Code établissement..."
                    style={{
                      flex: 1,
                      border: "none",
                      outline: "none",
                      fontSize: "14px",
                      background: "transparent",
                    }}
                  />
                  {etablissementCodeFilter && (
                    <i
                      className="pi pi-times"
                      style={{
                        fontSize: "12px",
                        color: "#6b7280",
                        cursor: "pointer",
                      }}
                      onClick={() => setEtablissementCodeFilter("")}
                    />
                  )}
                </div>

                <div
                  style={{
                    width: "220px",
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
                    className="pi pi-folder-open"
                    style={{ fontSize: "14px", color: "#6b7280" }}
                  />
                  <input
                    type="text"
                    value={numeroDossierFilter}
                    onChange={(e) => setNumeroDossierFilter(e.target.value)}
                    placeholder="N° Dossier..."
                    style={{
                      flex: 1,
                      border: "none",
                      outline: "none",
                      fontSize: "14px",
                      background: "transparent",
                    }}
                  />
                  {numeroDossierFilter && (
                    <i
                      className="pi pi-times"
                      style={{
                        fontSize: "12px",
                        color: "#6b7280",
                        cursor: "pointer",
                      }}
                      onClick={() => setNumeroDossierFilter("")}
                    />
                  )}
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
            right={
              <Button
                label="Nouveau candidat"
                icon="pi pi-plus"
                severity="success"
                onClick={handleAddCandidat}
              />
            }
            className="border-none bg-transparent p-0"
          />
        </div>

        {/* Dialog pour choisir la série de convocation */}
        <Dialog
          visible={convocationDropdownVisible}
          header="Générer les convocations"
          modal
          style={{ width: "450px" }}
          contentStyle={{ padding: "0" }}
          className="compact-dialog"
          onHide={() => setConvocationDropdownVisible(false)}
          footer={
            <div className="flex justify-content-end gap-2">
              <Button
                label="Annuler"
                icon="pi pi-times"
                className="p-button-text"
                onClick={() => setConvocationDropdownVisible(false)}
              />
              <Button
                label="Imprimer"
                icon="pi pi-print"
                className="p-button-primary"
                onClick={handleConvocationConfirm}
              />
            </div>
          }
        >
          <div className="p-3">
            <div className="flex flex-column gap-3">
              <label htmlFor="serieConvocation" className="font-semibold">
                Choisir la série
              </label>
              <Dropdown
                id="serieConvocation"
                value={convocationSerie}
                options={convocationOptions}
                onChange={(e) => setConvocationSerie(e.value)}
                placeholder="Sélectionner une série"
                className="w-full"
              />
              <div className="text-sm text-500 mt-2">
                <i className="pi pi-info-circle mr-1"></i>
                {convocationSerie === "all"
                  ? "Imprimera les convocations pour tous les candidats de toutes les séries."
                  : convocationSerie
                  ? `Imprimera les convocations pour tous les candidats de la série ${convocationSerie}.`
                  : "Veuillez sélectionner une série pour générer les convocations."}
              </div>
            </div>
          </div>
        </Dialog>

        {/* Dialogue d'ajout/modification des candidats */}
        <CandidatDialog
          ref={candidatDialogRef}
          visible={candidatDialogVisible}
          candidatId={editingCandidatId}
          onHide={() => {
            setCandidatDialogVisible(false);
            setEditingCandidatId(null);
          }}
          onSuccess={handleCandidatSuccess}
        />

        {/* Dialog pour l'impression massive des convocations */}
        <Dialog
          visible={massivePrintDialogVisible}
          modal
          maximizable
          footer={null}
          style={{
            width: "90vw",
            height: "100vh",
            maxWidth: "90vw",
            maxHeight: "100vh",
            margin: 0,
            padding: 0,
          }}
          contentStyle={{
            padding: 0,
            margin: 0,
            overflow: "auto",
            height: "calc(100vh - 60px)",
          }}
          className="fullscreen-dialog"
          onHide={() => {
            setMassivePrintDialogVisible(false);
            setMassivePrintCandidats([]);
          }}
          header={
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                width: "100%",
                padding: "0 10px",
                margin: 0,
                backgroundColor: "#fff",
                /* borderBottom: "1px solid #e2e8f0", */
              }}
            >
              <span>
                Convocations -{" "}
                {selectedSerieForPrint === "all"
                  ? "Toutes séries"
                  : `Série ${selectedSerieForPrint}`}{" "}
                ({massivePrintCandidats.length} candidat
                {massivePrintCandidats.length > 1 ? "s" : ""})
              </span>
              <div style={{ display: "flex", gap: "8px" }}>
                <Button
                  label={getPrintButtonLabel()}
                  icon="pi pi-print"
                  severity="success"
                  size="large"
                  style={{
                    fontSize: "1.1rem",
                    padding: "0.25rem 1rem",
                    borderRadius: "12px",
                  }}
                  onClick={handleMassivePrint}
                />
                {/* <Button
                  label="Télécharger PDF"
                  icon="pi pi-download"
                  severity="info"
                  size="small"
                  onClick={handleMassiveDownloadPDF}
                /> */}
              </div>
            </div>
          }
        >
          {isLoadingMassiveCandidats ? (
            <div
              className="flex flex-column justify-content-center align-items-center p-6"
              style={{ height: "100%" }}
            >
              <ProgressSpinner />
              <span className="mt-4 text-color-secondary">
                Chargement des convocations...
              </span>
            </div>
          ) : massivePrintCandidats.length > 0 ? (
            <div
              className="massive-print-container"
              style={{
                height: "100%",
                overflowY: "auto",
                padding: 0,
                margin: 0,
                backgroundColor: "#f5f5f5",
              }}
            >
              <ConvocationMassivePrint
                ref={massivePrintRef}
                candidats={massivePrintCandidats}
                serie={
                  selectedSerieForPrint === "all"
                    ? undefined
                    : selectedSerieForPrint
                }
              />
            </div>
          ) : (
            <div className="flex flex-column align-items-center p-6">
              <i className="pi pi-inbox text-5xl text-400" />
              <p className="mt-3">Aucun candidat trouvé</p>
            </div>
          )}
        </Dialog>

        {/* Tabs par série */}
        {allSeries && allSeries.length > 0 && (
          <div
            className="series-tabs-container"
            style={{ padding: "0 1rem", borderBottom: "1px solid #e0e0e0" }}
          >
            <div className="flex flex-wrap gap-2 py-2">
              <button
                onClick={() => handleSerieClick("")}
                className={`px-3 py-2 border-round font-medium transition-all cursor-pointer ${
                  selectedSerieCode === ""
                    ? "bg-primary text-white"
                    : "bg-transparent text-700 border-transparent hover:bg-gray-100"
                }`}
                style={{ border: "none", borderRadius: "8px" }}
              >
                Toutes les séries
              </button>
              {allSeries.map((serieCode: string) => (
                <button
                  key={serieCode}
                  onClick={() => handleSerieClick(serieCode)}
                  className={`px-3 py-2 border-round font-medium transition-all cursor-pointer ${
                    selectedSerieCode === serieCode
                      ? "bg-primary text-white"
                      : "bg-transparent text-700 border-transparent hover:bg-gray-100"
                  }`}
                  style={{ border: "none", borderRadius: "8px" }}
                >
                  {serieCode}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* DataTable avec conteneur responsive pour scroll horizontal */}
        <div className="datatable-responsive-container">
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
            style={{ fontSize: "0.75rem", minWidth: "1200px" }}
            loading={isLoading}
            resizableColumns
            columnResizeMode="fit"
            showGridlines
            stripedRows
            lazy
            scrollable
            scrollHeight="flex"
          >
            <Column
              field="centreEcrit"
              header="Crt. écrit"
              body={centreEcritTemplate}
              style={{ maxWidth: "6rem", minWidth: "5rem" }}
            />
            <Column
              field="jury"
              header="Jury"
              style={{ maxWidth: "4rem", minWidth: "3.5rem" }}
            />
            <Column
              field="numeroDossier"
              header="N° dossier"
              style={{ maxWidth: "4rem", minWidth: "3.5rem" }}
            />
            <Column
              field="numeroTable"
              header="N° Table"
              style={{ maxWidth: "5rem", minWidth: "4rem" }}
            />
            <Column
              field="serie"
              header="Série"
              style={{ maxWidth: "3rem", minWidth: "2.5rem" }}
            />
            {/* <Column
              header="Mat. opt."
              body={matieresOptionnellesTemplate}
              style={{
                maxWidth: "10rem",
                minWidth: "8rem",
                wordWrap: "break-word",
                whiteSpace: "normal",
              }}
            /> */}
            <Column
              field="prenoms"
              header="Prénom(s)"
              style={{
                maxWidth: "8rem",
                minWidth: "7rem",
                wordWrap: "break-word",
                whiteSpace: "normal",
              }}
            />
            <Column
              field="nom"
              header="Nom"
              style={{ maxWidth: "6rem", minWidth: "5rem" }}
            />
            <Column
              field="sexe"
              header="Sexe"
              style={{ maxWidth: "3rem", minWidth: "2.5rem" }}
            />
            <Column
              field="dateNaissance"
              header="Date naiss."
              body={(c) => formatDate(c.dateNaissance)}
              style={{ maxWidth: "6rem", minWidth: "5rem" }}
            />
            <Column
              field="lieuNaissance"
              header="Lieu naiss."
              style={{
                maxWidth: "9rem",
                minWidth: "7rem",
                wordWrap: "break-word",
                whiteSpace: "normal",
              }}
            />
            <Column
              field="etablissement.code"
              header="Etablissement"
              style={{ maxWidth: "6rem", minWidth: "5rem" }}
            />
            <Column
              field="eps"
              header="EPS"
              body={epsTemplate} // ← Utilisez le template au lieu du field direct
              style={{ maxWidth: "3rem" }}
            />
            {/* <Column
              header="Matière fac."
              body={matiereFacultativeTemplate}
              style={{
                maxWidth: "10rem",
                minWidth: "8rem",
                wordWrap: "break-word",
                whiteSpace: "normal",
              }}
            /> */}
            <Column
              header="Actions"
              body={actionsTemplate}
              style={{ maxWidth: "15rem", minWidth: "6rem" }}
              align="center"
              className="action-frozen-column"
            />
          </DataTable>
        </div>
      </div>

      <style jsx global>{`
        .massive-print-container {
          background: white;
          padding: 20px;
        }

        .p-datatable-scrollable-body::-webkit-scrollbar {
          height: 10px;
        }

        .p-datatable-scrollable-body table td:last-child,
        .p-datatable-scrollable-body table th:last-child {
          position: sticky !important;
          right: 0 !important;
          background-color: white;
          z-index: 1;
        }

        /* Pour le header */
        .p-datatable-scrollable-body table th:last-child {
          background-color: #f9fafb !important;
          z-index: 2;
        }

        /* Pour les lignes alternées */
        .p-datatable-tbody > tr:nth-child(even) td:last-child {
          background-color: #fafafa !important;
        }

        /* Pour le hover */
        .p-datatable-tbody > tr:hover td:last-child {
          background-color: #f3f4f6 !important;
        }

        /* Ajouter une ombre pour l'effet de profondeur */
        .p-datatable-scrollable-body table td:last-child {
          box-shadow: -2px 0 5px -2px rgba(0, 0, 0, 0.1);
        }

        .fullscreen-dialog .p-dialog-content {
          padding: 0 !important;
          margin: 0 !important;
          height: calc(100vh - 60px) !important;
          overflow: auto !important;
        }

        .fullscreen-dialog .p-dialog-header {
          padding: 0 !important;
          margin: 0 !important;
          border-bottom: 1px solid #e2e8f0;
          border-radius: 0 !important;
        }

        .fullscreen-dialog .p-dialog {
          margin: 0 auto !important;
          padding: 0 !important;
          border-radius: 8px !important;
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15) !important;
        }

        .fullscreen-dialog .p-dialog-header-icons {
          padding-right: 24px;
        }

        /* Centrer le dialogue */
        .p-dialog-mask {
          display: flex !important;
          align-items: center !important;
          justify-content: center !important;
        }

        /* Supprimer tous les padding du conteneur de convocation massive */
        .massive-print-container {
          padding: 0 !important;
          margin: 0 !important;
        }

        .massive-print-container .convocations-massive-print {
          padding: 20px !important;
          margin: 0 !important;
          background: #f5f5f5;
        }

        /* Ajuster les pages de convocation */
        .convocations-massive-print .page {
          width: 100%;
          max-width: 210mm;
          margin: 20px auto !important;
          padding: 5mm !important;
          box-sizing: border-box;
        }
        .p-paginator {
          display: flex !important;
          justify-content: center !important;
          align-items: center;
          gap: 0.5rem;
          flex-wrap: nowrap !important;
          width: auto !important;
          margin: 0 auto;
        }
        .p-paginator-left,
        .p-paginator-right,
        .p-paginator-pages {
          display: flex;
          align-items: center;
        }
        .p-paginator-right {
          margin-left: 1rem;
        }
        .p-dropdown {
          height: 32px;
          font-size: 0.85rem;
        }

        .p-paginator-page.p-highlight {
          background: #e5e7eb;
          color: #111827;
          border-radius: 6px;
        }
        /* Conteneur responsive avec scroll horizontal pour le DataTable */
        .datatable-responsive-container {
          width: 100%;
          overflow-x: auto;
          overflow-y: visible;
          -webkit-overflow-scrolling: touch;
          position: relative;
        }

        /* Personnalisation de la barre de scroll */
        .datatable-responsive-container::-webkit-scrollbar {
          height: 10px;
          background-color: #f1f5f9;
          border-radius: 5px;
        }

        .datatable-responsive-container::-webkit-scrollbar-track {
          background: #f1f5f9;
          border-radius: 5px;
        }

        .datatable-responsive-container::-webkit-scrollbar-thumb {
          background: #cbd5e1;
          border-radius: 5px;
          transition: background 0.2s;
        }

        .datatable-responsive-container::-webkit-scrollbar-thumb:hover {
          background: #94a3b8;
        }

        /* Indicateur de scroll horizontal */
        .datatable-responsive-container {
          position: relative;
        }

        .custom-table .p-datatable-thead > tr > th {
          border: 1px solid #e5e7eb !important; /* gris clair */
          background: #f9fafb; /* léger fond */
        }

        .custom-table .p-datatable-tbody > tr > td {
          border: 1px solid #e5e7eb !important;
        }

        /* Bordure externe */
        .custom-table .p-datatable-wrapper {
          border: 1px solid #e5e7eb;
          border-radius: 8px;
          overflow: hidden;
        }

        /* Header plus propre */
        .custom-table .p-datatable-thead > tr > th {
          font-weight: 600;
          color: #374151;
        }

        /* Hover propre */
        .custom-table .p-datatable-tbody > tr:hover {
          background: #f3f4f6;
        }

        /* lignes alternées (déjà activé mais on améliore) */
        .custom-table .p-datatable-tbody > tr:nth-child(even) {
          background: #fafafa;
        }

        .datatable-responsive-container::after {
          content: "";
          position: sticky;
          right: 0;
          top: 0;
          height: 100%;
          width: 40px;
          background: linear-gradient(
            to right,
            transparent,
            rgba(255, 255, 255, 0.9)
          );
          pointer-events: none;
          opacity: 0;
          transition: opacity 0.3s;
          z-index: 1;
        }

        .datatable-responsive-container:hover::after {
          opacity: 1;
        }

        /* Style pour le DataTable scrollable */
        .p-datatable-scrollable {
          position: relative;
        }

        .p-datatable-scrollable .p-datatable-scrollable-body {
          overflow-x: auto !important;
        }

        /* Style responsive pour petits écrans */
        @media (max-width: 768px) {
          .fullscreen-dialog .p-dialog {
            width: 95vw !important;
          }

          .fullscreen-dialog .p-dialog-content {
            height: calc(100vh - 50px) !important;
          }

          .datatable-responsive-container .p-datatable {
            font-size: 0.7rem;
          }
        }

        @media print {
          .fullscreen-dialog {
            display: none !important;
          }

          .massive-print-container {
            padding: 0 !important;
          }

          .convocations-massive-print .page {
            padding: 0 !important;
            margin: 0 !important;
            border: none !important;
          }
        }

        .massive-print-container .page {
          page-break-after: always;
          page-break-inside: avoid;
          margin-bottom: 20px;
        }

        .massive-print-container .page:last-child {
          margin-bottom: 0;
        }

        /* Supprimer tout padding du Dialog */
        .p-dialog .p-dialog-content {
          padding: 0 !important;
        }

        .p-dialog .p-dialog-header {
          padding: 1rem 1.5rem !important;
        }

        /* Pour que le Dialog prenne toute la hauteur */
        .p-dialog {
          max-height: 90vh !important;
        }

        .full-height-dialog .p-dialog-content {
          padding: 0 !important;
          margin: 0 !important;
          height: calc(95vh - 60px) !important;
          max-height: calc(95vh - 60px) !important;
          overflow: auto !important;
        }

        .full-height-dialog .p-dialog-header {
          padding: 0 !important;
          margin: 0 !important;
          border-bottom: 1px solid #e2e8f0;
        }

        .full-height-dialog .p-dialog-header-icons {
          padding-right: 24px;
        }

        .full-height-dialog .p-dialog-title {
          padding-left: 24px;
        }

        .full-height-dialog .p-dialog {
          margin: 0 !important;
          padding: 0 !important;
        }

        /* Supprimer l'espace entre le header et le contenu */
        .p-dialog .p-dialog-header {
          border-bottom-left-radius: 0 !important;
          border-bottom-right-radius: 0 !important;
        }

        .p-dialog .p-dialog-content:first-of-type {
          border-top-left-radius: 0 !important;
          border-top-right-radius: 0 !important;
        }

        /* Ajuster le conteneur d'impression massive */
        .massive-print-container {
          height: 100%;
          overflow-y: auto;
          background: #f5f5f5;
        }

        .massive-print-container .convocations-massive-print {
          padding: 0;
          background: transparent;
        }

        /* Pour que chaque page de convocation prenne bien toute la largeur */
        .convocations-massive-print .page {
          width: 100%;
          max-width: 210mm;
          margin: 20px auto;
          box-sizing: border-box;
        }

        /* Style responsive pour petits écrans */
        @media (max-width: 768px) {
          .full-height-dialog .p-dialog {
            width: 95vw !important;
            height: 95vh !important;
          }

          .full-height-dialog .p-dialog-content {
            height: calc(95vh - 50px) !important;
          }
        }

        @media print {
          .massive-print-container {
            padding: 0;
          }

          .massive-print-container .page {
            page-break-after: always;
            page-break-inside: avoid;
          }
        }
      `}</style>

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
