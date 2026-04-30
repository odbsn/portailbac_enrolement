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
import { useCandidatEtabStore, CandidatFinis } from "../convocationEtabStore";
import { useSerieStore } from "./serieStore";
import DetailCandidatDialog from "./DetailCandidatDialog";
import ConvocationMassivePrint, {
  ConvocationMassivePrintRef,
} from "./allConvocations/ConvocationMassivePrint";
import ConvocationButton from "./ConvocationButton";

// Composant de dialogue avec barre de progression
interface ExportProgressDialogProps {
  visible: boolean;
  title: string;
  progress: number;
  status: string;
  onHide?: () => void;
}

const ExportProgressDialog: React.FC<ExportProgressDialogProps> = ({
  visible,
  title,
  progress,
  status,
  onHide,
}) => {
  return (
    <Dialog
      visible={visible}
      header={title}
      modal
      closable={false}
      showHeader={true}
      style={{ width: "450px" }}
      onHide={onHide || (() => {})}
      footer={
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            gap: "8px",
            width: "100%",
          }}
        >
          <button
            onClick={onHide}
            disabled={progress === 100}
            style={{
              padding: "8px 16px",
              background: "transparent",
              border: "none",
              color: progress === 100 ? "#9ca3af" : "#6b7280",
              cursor: progress === 100 ? "not-allowed" : "pointer",
              fontSize: "14px",
            }}
          >
            Annuler
          </button>
          {progress === 100 && (
            <button
              onClick={onHide}
              autoFocus
              style={{
                padding: "8px 16px",
                background: "#3b82f6",
                border: "none",
                color: "white",
                borderRadius: "6px",
                cursor: "pointer",
                fontSize: "14px",
              }}
            >
              Fermer
            </button>
          )}
        </div>
      }
    >
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          padding: "16px",
          gap: "16px",
        }}
      >
        {/* Icône animée pendant le chargement */}
        {progress < 100 && (
          <div style={{ position: "relative" }}>
            <div
              style={{
                width: "56px",
                height: "56px",
                borderRadius: "50%",
                border: "4px solid #bfdbfe",
                borderTopColor: "#3b82f6",
                animation: "spin 1s linear infinite",
              }}
            />
            <i
              className="pi pi-download"
              style={{
                position: "absolute",
                top: "50%",
                left: "50%",
                transform: "translate(-50%, -50%)",
                color: "#3b82f6",
                fontSize: "20px",
              }}
            />
          </div>
        )}

        {progress === 100 && (
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              backgroundColor: "#dcfce7",
              borderRadius: "50%",
              width: "64px",
              height: "64px",
            }}
          >
            <i
              className="pi pi-check-circle"
              style={{ color: "#22c55e", fontSize: "32px" }}
            />
          </div>
        )}

        <div style={{ width: "100%" }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              marginBottom: "8px",
            }}
          >
            <span
              style={{ fontSize: "14px", fontWeight: "500", color: "#374151" }}
            >
              {status}
            </span>
            <span
              style={{ fontSize: "14px", fontWeight: "bold", color: "#3b82f6" }}
            >
              {Math.round(progress)}%
            </span>
          </div>

          {/* Barre de progression */}
          <div
            style={{
              width: "100%",
              backgroundColor: "#e5e7eb",
              borderRadius: "9999px",
              height: "12px",
              overflow: "hidden",
            }}
          >
            <div
              style={{
                width: `${progress}%`,
                backgroundColor: "#3b82f6",
                height: "12px",
                borderRadius: "9999px",
                transition: "width 500ms cubic-bezier(0, 0, 0.2, 1)",
              }}
            />
          </div>
        </div>

        {/* Message additionnel pour les gros fichiers */}
        {progress > 0 && progress < 100 && (
          <div
            style={{ textAlign: "center", fontSize: "12px", color: "#6b7280" }}
          >
            <i className="pi pi-info-circle" style={{ marginRight: "4px" }}></i>
            Veuillez patienter, téléchargement en cours...
          </div>
        )}
      </div>

      <style jsx>{`
        @keyframes spin {
          from {
            transform: rotate(0deg);
          }
          to {
            transform: rotate(360deg);
          }
        }
      `}</style>
    </Dialog>
  );
};

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

  // États pour les dialogues d'export
  const [exportDialogVisible, setExportDialogVisible] = useState(false);
  const [exportProgress, setExportProgress] = useState(0);
  const [exportStatus, setExportStatus] = useState("");
  const [exportTitle, setExportTitle] = useState("");
  const [currentExportType, setCurrentExportType] = useState<
    "all" | "serie" | "convocation" | null
  >(null);

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
    exportPdfBySerie,
    exportZipBySeries,
    isExportingPdfBySerie,
    isExportingZipBySeries,
  } = useCandidatEtabStore();

  const { mySeries, isLoadingMy, errorMy, fetchMySeries } = useSerieStore();

  const [globalFilter, setGlobalFilter] = useState("");
  const [convocationDialogVisible, setConvocationDialogVisible] =
    useState(false);
  const [selectedCandidatForConvocation, setSelectedCandidatForConvocation] =
    useState<CandidatFinis | null>(null);
  const [viewDialogVisible, setViewDialogVisible] = useState(false);
  const [selectedCandidat, setSelectedCandidat] =
    useState<CandidatFinis | null>(null);
  const [selectedSerieCode, setSelectedSerieCode] = useState<string>("");
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

  const first = currentPage * pageSize;

  // Options pour le dropdown des convocations
  const convocationOptions = [
    { label: "Toutes les séries", value: "all" },
    ...(mySeries || []).map((serie: string) => ({
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
    fetchMySeries();
  }, [fetchMySeries]);

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

  // Filtre par série
  const handleSerieClick = (serieCode: string) => {
    const newSerie = serieCode === selectedSerieCode ? "" : serieCode;
    setSelectedSerieCode(newSerie);
    setFilters({ serie: newSerie, page: 0 });
  };

  // Simulation de progression pour l'export
  const simulateProgress = () => {
    let progress = 0;
    const interval = setInterval(() => {
      progress += 5;
      if (progress >= 95) {
        clearInterval(interval);
      }
      setExportProgress(progress);
    }, 100);

    return () => clearInterval(interval);
  };

  // Export via API avec barre de progression
  const handleExportWithProgress = async (
    exportFn: () => Promise<Blob | null | void>,
    title: string,
    successMessage: string,
  ) => {
    if (isExporting) return;

    setExportTitle(title);
    setExportStatus("Préparation de l'export...");
    setExportProgress(0);
    setExportDialogVisible(true);
    setIsExporting(true);

    let clearProgress: (() => void) | undefined;

    try {
      // Démarrer la simulation de progression
      let progress = 0;
      const interval = setInterval(() => {
        progress += 3;
        if (progress >= 90) {
          clearInterval(interval);
        }
        setExportProgress(progress);
        if (progress === 30) {
          setExportStatus("Génération du fichier...");
        } else if (progress === 60) {
          setExportStatus("Téléchargement en cours...");
        }
      }, 200);

      // Exécuter l'export
      const result = await exportFn();

      // Nettoyer l'intervalle
      clearInterval(interval);

      // Passer à 100%
      setExportProgress(100);
      setExportStatus("Export terminé !");

      toast.current?.show({
        severity: "success",
        summary: "Succès",
        detail: successMessage,
        life: 3000,
      });

      // Fermer le dialogue après 1.5 secondes
      setTimeout(() => {
        setExportDialogVisible(false);
        setExportProgress(0);
      }, 1500);
    } catch (error) {
      console.error("Erreur export:", error);
      setExportStatus("Erreur lors de l'export");
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Erreur lors de l'export",
        life: 5000,
      });
      setTimeout(() => {
        setExportDialogVisible(false);
        setExportProgress(0);
      }, 2000);
    } finally {
      setIsExporting(false);
    }
  };

  const handleExportAllCandidats = async () => {
    await handleExportWithProgress(
      async () => {
        const filters = {
          serie: selectedSerieCode || undefined,
          keyword: globalFilter || undefined,
        };

        const blob = await exportCandidats(filters);

        if (blob) {
          const filename = `candidats_${
            selectedSerieCode || "tous"
          }_${new Date().toISOString().slice(0, 19)}.xlsx`;
          saveAs(blob, filename);
          return blob;
        }
        throw new Error("Aucune donnée à exporter");
      },
      "Export de la liste des candidats",
      `${totalElements} candidats exportés avec succès`,
    );
  };

  const handleExportBySerie = async () => {
    await handleExportWithProgress(
      async () => {
        const blob = await exportZipBySerie();
        if (blob) {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = "listes_candidats_par_serie.zip";
          a.click();
          window.URL.revokeObjectURL(url);
          return blob;
        }
        throw new Error("Erreur lors de la génération du ZIP");
      },
      "Export des listes par série",
      "Les listes par série ont été exportées avec succès",
    );
  };

  const handleExportAllConvocations = async () => {
    await handleExportWithProgress(
      async () => {
        const blob = await exportZipBySeries();
        if (blob) {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = `convocations_toutes_series_${new Date()
            .toISOString()
            .slice(0, 19)}.zip`;
          a.click();
          window.URL.revokeObjectURL(url);
          return blob;
        }
        throw new Error("Erreur lors de la génération des convocations");
      },
      "Export des convocations",
      "Les convocations ont été exportées avec succès",
    );
  };

  // Filtre par série
  /* const handleSerieClick = (serieCode: string) => {
    const newSerie = serieCode === selectedSerieCode ? "" : serieCode;
    setSelectedSerieCode(newSerie);
    setFilters({ serie: newSerie, page: 0 });
  }; */

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

  const handleExportPdfBySerie = async (serieCode: string) => {
    if (!serieCode) {
      toast.current?.show({
        severity: "warn",
        summary: "Attention",
        detail: "Veuillez sélectionner une série",
        life: 3000,
      });
      return;
    }

    try {
      toast.current?.show({
        severity: "info",
        summary: "Génération",
        detail: `Génération du PDF pour la série ${serieCode}...`,
        life: 2000,
      });

      const blob = await exportPdfBySerie(serieCode);

      if (blob) {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `convocations_serie_${serieCode}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);

        toast.current?.show({
          severity: "success",
          summary: "Succès",
          detail: `PDF généré pour la série ${serieCode}`,
          life: 3000,
        });
      }
    } catch (error) {
      console.error("Erreur:", error);
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Erreur lors de la génération du PDF",
        life: 5000,
      });
    }
  };

  const handleExportZipBySeries = async () => {
    try {
      toast.current?.show({
        severity: "info",
        summary: "Génération",
        detail: "Génération du ZIP contenant un PDF par série...",
        life: 2000,
      });

      const blob = await exportZipBySeries();

      if (blob) {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `convocations_toutes_series_${new Date()
          .toISOString()
          .slice(0, 19)}.zip`;
        a.click();
        window.URL.revokeObjectURL(url);

        toast.current?.show({
          severity: "success",
          summary: "Succès",
          detail: "ZIP généré avec succès",
          life: 3000,
        });
      }
    } catch (error) {
      console.error("Erreur:", error);
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Erreur lors de la génération du ZIP",
        life: 5000,
      });
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

      const { candidats: loadedCandidats } = useCandidatEtabStore.getState();

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
      <ConvocationButton
        numeroTable={rowData.numeroTable}
        label=""
        icon="pi pi-file-pdf"
      />
      {/* <Button
        icon="pi pi-print"
        rounded
        text
        severity="success"
        tooltip="Imprimer"
        tooltipOptions={{ position: "bottom" }}
        className="p-0 border-circle border-2 flex items-center justify-center bg-green-100 border-green-400"
        style={{ width: "2.4rem", height: "2.4rem", minWidth: "2.4rem" }}
        onClick={() => {
          setSelectedCandidatForConvocation(rowData);
          setConvocationDialogVisible(true);
          onPrintConvocation?.(rowData);
        }}
        size="small"
      /> */}
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

      {/* Dialogue de progression d'export */}
      <ExportProgressDialog
        visible={exportDialogVisible}
        title={exportTitle}
        progress={exportProgress}
        status={exportStatus}
        onHide={() => {
          if (exportProgress !== 100) {
            setExportDialogVisible(false);
            setExportProgress(0);
          }
        }}
      />

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
                    style={{
                      fontSize: "14px",
                      color: "#6b7280",
                    }}
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
              onClick={handleExportAllCandidats}
              disabled={isExporting || isExportingPdf}
              icon="pi pi-download"
              label="Exporter toute la liste des candidats (Excel)"
              className="p-button-primary"
            />
            <Button
              onClick={handleExportBySerie}
              disabled={isExporting || isExportingZip}
              icon="pi pi-table"
              label="Exporter la liste par série (PDF)"
              className="p-button-secondary"
            />

            <Button
              onClick={handleExportAllConvocations}
              /* disabled={isExporting || isExportingZipBySeries} */
              icon="pi pi-file-pdf"
              label="Exporter toutes les convocations"
              className="p-button-info"
              disabled={true}
            />
          </div>
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
        {mySeries && mySeries.length > 0 && (
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
                Toutes les séries de l'établissement
              </button>
              {mySeries.map((serieCode: string) => (
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
              field="nationalite"
              header="Nationalité"
              style={{ maxWidth: "6rem" }}
            />
            <Column field="eps" header="EPS" style={{ maxWidth: "3rem" }} />

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

      <style jsx global>{`
        .massive-print-container {
          background: white;
          padding: 20px;
        }

        .fullscreen-dialog .p-dialog-content {
          padding: 0 !important;
          margin: 0 !important;
          height: calc(100vh - 60px) !important;
          overflow: auto !important;
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

        /* Style responsive */
        @media (max-width: 768px) {
          .fullscreen-dialog .p-dialog {
            width: 95vw !important;
          }

          .fullscreen-dialog .p-dialog-content {
            height: calc(100vh - 50px) !important;
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

        /* Styles pour le dialogue d'export */
        .export-progress-dialog .p-dialog-content {
          padding: 1rem !important;
        }

        .export-progress-dialog .p-dialog-header {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          border-radius: 8px 8px 0 0;
        }

        .export-progress-dialog .p-dialog-header .p-dialog-title {
          color: white;
          font-weight: 600;
        }

        .export-progress-dialog .p-dialog-header .p-dialog-header-icon {
          color: white;
        }

        .bg-blue-500 {
          background-color: #3b82f6;
        }

        .transition-all {
          transition-property: all;
        }

        .duration-300 {
          transition-duration: 300ms;
        }

        .ease-out {
          transition-timing-function: cubic-bezier(0, 0, 0.2, 1);
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
