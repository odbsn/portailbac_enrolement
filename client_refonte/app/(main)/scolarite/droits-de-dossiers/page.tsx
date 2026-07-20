"use client";

import React, { useContext, useEffect, useRef, useState } from "react";
import { UserContext } from "@/app/userContext";
import { CandidatureService } from "@/demo/service/CandidatureService";
import { FileService } from "@/demo/service/FileService";
import { Toast } from "primereact/toast";
import { Button } from "primereact/button";
import { Dialog } from "primereact/dialog";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { FileUpload } from "primereact/fileupload";
import dynamic from "next/dynamic";
import "primereact/resources/themes/lara-light-blue/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "react-pdf/dist/Page/AnnotationLayer.css";
import "react-pdf/dist/Page/TextLayer.css";
import "./styles.css";

const PdfViewer = dynamic(() => import("../../pdfViewer"), {
  ssr: false,
});

interface ProgramData {
  edition?: number;
  date_end?: string;
}

interface Versement {
  id: number;
  session: string;
  date_deposit: string;
  count_5000: number;
  count_1000_EF: number;
  file_id: number;
  invalid_file: boolean;
}

const DroitsDossiersPage: React.FC = () => {
  const { user } = useContext(UserContext);
  const toast = useRef<Toast>(null);

  const [program, setProgram] = useState<ProgramData | null>(null);
  const [versements, setVersements] = useState<Versement[]>([]);
  const [loading, setLoading] = useState(false);
  const [reloadTrigger, setReloadTrigger] = useState(false);

  // Dialog states
  const [uploadDialogVisible, setUploadDialogVisible] = useState(false);
  const [pdfDialogVisible, setPdfDialogVisible] = useState(false);

  // Upload states
  const [file, setFile] = useState<File | null>(null);
  const [fileUrl, setFileUrl] = useState<string | null>(null);
  const [fileId, setFileId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [globalFilter, setGlobalFilter] = useState("");
  const dt = useRef<any>(null);

  // Load program
  useEffect(() => {
    CandidatureService.getLastProg().then((response) => {
      setProgram(response);
    });
  }, []);

  // Load versements
  useEffect(() => {
    if (user?.acteur?.etablissement?.id && program?.edition) {
      loadVersements();
    }
  }, [reloadTrigger, user, program]);

  const loadVersements = async () => {
    setLoading(true);
    try {
      const response = await CandidatureService.filterEtatsVersements_(
        user?.acteur?.etablissement?.id,
        program?.edition,
      );
      setVersements(response || []);
    } catch (err) {
      console.error("❌ Erreur chargement versements:", err);
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Impossible de charger les versements",
        life: 4000,
      });
    } finally {
      setLoading(false);
    }
  };

  // File upload handlers
  const handleFileChange = (e: any) => {
    const fileOK = e.files?.[0];
    if (fileOK) {
      if (fileOK.size > 5242880) {
        setErrorMessage(
          "❌ Le fichier dépasse la taille maximale autorisée de 5 Mo.",
        );
        setFile(null);
      } else {
        setFile(fileOK);
        setErrorMessage("");
      }
    }
  };

  const handleUpload = async () => {
    if (!file) {
      setErrorMessage("⚠️ Veuillez d'abord charger un fichier PDF valide.");
      return;
    }

    try {
      const code = 1;
      await FileService.uploadFile(
        file,
        Number(program?.edition),
        user?.acteur?.etablissement?.id,
        code,
      );
      toast.current?.show({
        severity: "success",
        summary: "Succès",
        detail: "Fichier chargé avec succès",
        life: 4000,
      });
      setUploadDialogVisible(false);
      setFile(null);
      setErrorMessage("");
      setReloadTrigger((prev) => !prev);
    } catch (error) {
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Impossible de téléverser le fichier",
        life: 4000,
      });
    }
  };

  // View PDF
  const handleViewPDF = async (rowData: Versement) => {
    if (rowData.file_id) {
      setFileId(rowData.file_id);
      const response = await FileService.getViewUrl(rowData.file_id);
      if (response) {
        setFileUrl(response);
        setPdfDialogVisible(true);
      }
    }
  };

  // Delete file
  const handleDeleteFile = async (rowData: Versement) => {
    try {
      if (rowData.file_id) {
        await FileService.deleteFile(rowData.file_id);
        toast.current?.show({
          severity: "success",
          summary: "Succès",
          detail: "Fichier supprimé avec succès",
          life: 4000,
        });
        setReloadTrigger((prev) => !prev);
      }
    } catch (error) {
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Impossible de supprimer le fichier",
        life: 4000,
      });
    }
  };

  // Format date
  const formatDate = (dateString: string) => {
    if (!dateString) return "-";
    const date = new Date(dateString.replace(" ", "T"));
    if (isNaN(date.getTime())) return "Date invalide";
    return (
      date.toLocaleDateString("fr-FR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      }) +
      " " +
      date.toLocaleTimeString("fr-FR", {
        hour: "2-digit",
        minute: "2-digit",
      })
    );
  };

  // Table column templates
  const statusBodyTemplate = (rowData: Versement) => {
    const hasData =
      Number(rowData?.count_5000 ?? 0) > 0 ||
      Number(rowData?.count_1000_EF ?? 0) > 0;

    if (rowData.invalid_file) {
      return (
        <span className="status-badge status-badge-error">
          <i className="pi pi-times-circle"></i>
          Fichier invalide
        </span>
      );
    } else if (!hasData) {
      return (
        <span className="status-badge status-badge-processing">
          <i className="pi pi-spin pi-spinner"></i>
          En traitement
        </span>
      );
    } else {
      return (
        <span className="status-badge status-badge-success">
          <i className="pi pi-check-circle"></i>
          Vignettes attribuées
        </span>
      );
    }
  };

  const v5000BodyTemplate = (rowData: Versement) => {
    return (
      <span className="vignette-badge vignette-badge-yellow">
        {rowData.count_5000 || 0}
      </span>
    );
  };

  const v1000BodyTemplate = (rowData: Versement) => {
    return (
      <span className="vignette-badge vignette-badge-green">
        {rowData.count_1000_EF || 0}
      </span>
    );
  };

  const actionBodyTemplate = (rowData: Versement) => {
    const hasData =
      Number(rowData?.count_5000 ?? 0) > 0 ||
      Number(rowData?.count_1000_EF ?? 0) > 0;

    return (
      <div className="action-buttons">
        <Button
          icon="pi pi-eye"
          rounded
          tooltip="Ouvrir le reçu"
          tooltipOptions={{ position: "bottom" }}
          className="btn-action btn-action-view"
          onClick={() => handleViewPDF(rowData)}
        />
        {!hasData && (
          <Button
            icon="pi pi-trash"
            rounded
            tooltip="Supprimer le fichier"
            tooltipOptions={{ position: "bottom" }}
            className="btn-action btn-action-delete"
            onClick={() => handleDeleteFile(rowData)}
          />
        )}
      </div>
    );
  };

  // Get remaining days
  const getRemainingDays = (): number | null => {
    if (!program?.date_end) return null;
    const today = new Date().getTime();
    const endDate = new Date(program.date_end).getTime();
    return Math.max(0, Math.ceil((endDate - today) / (1000 * 60 * 60 * 24)));
  };

  const remainingDays = getRemainingDays();
  const isOpen = remainingDays !== null && remainingDays > 0;

  return (
    <div className="page-container">
      <Toast ref={toast} />

      <div className="max-w-7xl mx-auto">
        <div className="page-card">
          {/* ============================================
                        HEADER - STYLE IDENTIQUE À droits-dossier-1000
                    ============================================ */}
          <div className="page-header">
            <div className="page-header-content">
              <div className="page-header-left">
                <div className="page-header-badge">
                  <div className="page-header-icon">
                    <i className="pi pi-file-pdf"></i>
                  </div>
                  <span
                    className={`status-badge ${
                      isOpen ? "status-badge-open" : "status-badge-closed"
                    }`}
                  >
                    {isOpen ? "● En cours" : "● Fermé"}
                  </span>
                </div>
                <h1 className="page-header-title">
                  Droits d'inscription des candidats
                </h1>
                <p className="page-header-subtitle">
                  5 000 FCFA et 1 000 FCFA (pour épreuve facultative) versés au
                  Trésor Public
                </p>
                {isOpen && remainingDays !== null && (
                  <div className="page-header-days">
                    <div className="page-header-days-box">
                      <i className="pi pi-clock"></i>
                      <span>
                        <strong>{remainingDays}</strong> jour
                        {remainingDays > 1 ? "s" : ""} restant
                        {remainingDays > 1 ? "s" : ""}
                      </span>
                    </div>
                  </div>
                )}
              </div>
              <div className="page-header-action">
                {isOpen ? (
                  <Button
                    label="Déposer un état de versement"
                    icon="pi pi-plus"
                    className="btn-primary-gradient"
                    onClick={() => setUploadDialogVisible(true)}
                  />
                ) : (
                  <div className="page-header-closed">
                    <div className="page-header-closed-icon">
                      <i className="pi pi-exclamation-triangle"></i>
                    </div>
                    <span className="page-header-closed-text">
                      La période de dépôt est fermée
                    </span>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* ============================================
                        TABLE - STYLE IDENTIQUE À droits-dossier-1000
                    ============================================ */}
          <div className="p-4 md:p-8">
            <div className="table-header">
              <div className="table-title">
                <div className="table-title-icon">
                  <i className="pi pi-list"></i>
                </div>
                <div>
                  <h2>Historique des versements</h2>
                  {!loading && (
                    <p>
                      {versements.length} versement
                      {versements.length > 1 ? "s" : ""} au total
                    </p>
                  )}
                </div>
              </div>
              <div className="table-search">
                <i className="pi pi-search"></i>
                <input
                  type="text"
                  placeholder="Rechercher..."
                  value={globalFilter}
                  onChange={(e) => setGlobalFilter(e.target.value)}
                />
              </div>
            </div>

            <DataTable
              ref={dt}
              value={versements}
              loading={loading}
              paginator
              rows={10}
              rowsPerPageOptions={[10, 20, 50]}
              className="custom-datatable"
              paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
              currentPageReportTemplate="Affichage de {first} à {last} des {totalRecords} enregistrement(s)"
              globalFilter={globalFilter}
              emptyMessage="Aucun versement trouvé"
              responsiveLayout="scroll"
              sortMode="multiple"
              removableSort
            >
              <Column
                field="id"
                header="ID"
                sortable
                headerClassName="table-header-cell"
                style={{ minWidth: "6rem" }}
              />
              <Column
                field="session"
                header="Session"
                sortable
                headerClassName="table-header-cell"
                style={{ minWidth: "8rem" }}
              />
              <Column
                field="date_deposit"
                header="Date du dépôt"
                sortable
                body={(rowData) => formatDate(rowData.date_deposit)}
                headerClassName="table-header-cell"
                style={{ minWidth: "10rem" }}
              />
              <Column
                field="count_5000"
                header="Vignettes 5000 FCFA"
                body={v5000BodyTemplate}
                headerClassName="table-header-cell"
                style={{ minWidth: "8rem" }}
              />
              <Column
                field="count_1000_EF"
                header="Vignettes 1000 FCFA (Épr. Fac.)"
                body={v1000BodyTemplate}
                headerClassName="table-header-cell"
                style={{ minWidth: "10rem" }}
              />
              <Column
                field="status"
                header="Statut"
                body={statusBodyTemplate}
                headerClassName="table-header-cell"
                style={{ minWidth: "10rem" }}
              />
              <Column
                body={actionBodyTemplate}
                headerClassName="table-header-cell"
                style={{ minWidth: "8rem" }}
              />
            </DataTable>
          </div>
        </div>
      </div>

      {/* ============================================
                DIALOG - PDF VIEWER
            ============================================ */}
      <Dialog
        visible={pdfDialogVisible}
        style={{ width: "90vw", maxWidth: "1000px" }}
        header="État de versement"
        modal
        className="custom-dialog"
        onHide={() => setPdfDialogVisible(false)}
      >
        {fileUrl ? (
          <PdfViewer fileUrl={fileUrl} />
        ) : (
          <p>Chargement du PDF...</p>
        )}
      </Dialog>

      {/* ============================================
                DIALOG - UPLOAD
            ============================================ */}
      <Dialog
        visible={uploadDialogVisible}
        style={{ width: "90vw", maxWidth: "800px" }}
        header="Dépôt d'un état de versement"
        modal
        className="custom-dialog"
        onHide={() => {
          setUploadDialogVisible(false);
          setFile(null);
          setErrorMessage("");
        }}
      >
        <div className="upload-dialog-content">
          <div className="upload-info-box">
            <div className="upload-info-item">
              <span className="upload-info-number">1</span>
              <p>
                Remplir et signer l'état de versement des droits d'inscription
              </p>
            </div>
            <div className="upload-info-item">
              <span className="upload-info-number">2</span>
              <p>
                Agrafer la <strong>quittance numérique</strong> délivrée par le
                Trésor en haut de l'état
              </p>
            </div>
            <div className="upload-info-item">
              <span className="upload-info-number">3</span>
              <p>
                Scanner clairement (en couleur) l'état de versement, quittance
                comprise, au format PDF
              </p>
            </div>
            <div className="upload-info-item">
              <span className="upload-info-number">4</span>
              <p>
                La taille du fichier ne doit pas dépasser <strong>5 Mo</strong>
              </p>
            </div>
            <div className="upload-info-item">
              <span className="upload-info-number">5</span>
              <p>Charger et téléverser le fichier sur la plateforme</p>
            </div>
            <div className="upload-info-item upload-info-warning">
              <span className="upload-info-number">⚠</span>
              <p>
                <strong>Aucun remboursement</strong> ne sera effectué une fois
                le paiement validé par le trésor
              </p>
            </div>
            <div className="upload-info-item upload-info-warning">
              <span className="upload-info-number">📌</span>
              <p>
                La présentation des documents originaux en version physique sera
                exigée lors de la réception
              </p>
            </div>
          </div>

          <div className="upload-area">
            <FileUpload
              mode="basic"
              accept="application/pdf"
              customUpload
              name="pdf"
              chooseLabel="Charger le PDF généré"
              onSelect={handleFileChange}
              className="upload-file-input"
            />
            {errorMessage && <div className="upload-error">{errorMessage}</div>}
            <Button
              label="Téléverser l'état de versement"
              icon="pi pi-upload"
              className="btn-upload"
              onClick={handleUpload}
            />
          </div>
        </div>
      </Dialog>
    </div>
  );
};

export default DroitsDossiersPage;
