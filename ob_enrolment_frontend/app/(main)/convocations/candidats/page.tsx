"use client";

import React, { useEffect, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { TabView, TabPanel } from "primereact/tabview";
import { Card } from "primereact/card";
import { Button } from "primereact/button";
import { ProgressSpinner } from "primereact/progressspinner";
import { Toast } from "primereact/toast";
import { motion } from "framer-motion";
import { useCandidatStore } from "../candidatStore";
import { useSafeNavigation } from "@/app/(full-page)/hooks/useSafeNavigation";
import Marquee from "react-fast-marquee";

interface CandidatInfo {
  prenoms?: string;
  nom?: string;
  dateNaissance?: string;
  lieuNaissance?: string;
  nationalite?: string;
  numeroTable?: string;
  jury?: string;
  serie?: string;
  sexe?: string;
  typeCandidat?: string;
  eps?: string;
  centreCode?: string;
  etablissementName?: string;
  centreEcritName?: string;
  centreEcritParticulier?: string;
  centreActEPSName?: string;
  mo1?: string;
  mo2?: string;
  mo3?: string;
  ef1?: string;
  ef2?: string;
  centreMatFac1?: string;
  libMatFac1?: string;
  centreMatFac2?: string;
  libMatFac2?: string;
  codeEtab?: string;
  timestamp?: number;
}

// Composant InfoRow - libellé et valeur sur la même ligne
const InfoRow = ({ label, value }: { label: string; value?: string }) => (
  <div className="info-row">
    <span className="info-row-label">{label}</span>
    <span className="info-row-value">{value || "-"}</span>
  </div>
);

// Bloc d'informations
const InfoBlock = ({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) => (
  <div className="info-block">
    <div className="info-block-header">
      <span className="info-block-title">{title}</span>
    </div>
    <div className="info-block-content">{children}</div>
  </div>
);

export default function EspaceCandidat() {
  const router = useRouter();
  const { safeNavigate } = useSafeNavigation();
  const [candidatInfo, setCandidatInfo] = useState<CandidatInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [activeIndex, setActiveIndex] = useState(0);
  const toastRef = useRef<Toast>(null);

  const {
    currentCandidat,
    isLoading: isLoadingCandidat,
    error: candidatError,
    downloadConvocation,
    clearCurrentCandidat,
    clearError,
  } = useCandidatStore();

  useEffect(() => {
    const originalError = console.error;
    console.error = (...args) => {
      if (
        typeof args[0] === "string" &&
        args[0].includes("useInsertionEffect must not schedule updates")
      ) {
        return;
      }
      originalError.apply(console, args);
    };

    const storedInfo = localStorage.getItem("candidat_info");

    if (!storedInfo) {
      toastRef.current?.show({
        severity: "warn",
        summary: "Accès non autorisé",
        detail: "Veuillez vous identifier d'abord",
        life: 3000,
      });
      setIsLoading(false);
      setTimeout(() => safeNavigate("/"), 2000);
      return;
    }

    try {
      const info = JSON.parse(storedInfo);
      setCandidatInfo(info);
    } catch (error) {
      console.error("Erreur lors du chargement des données:", error);
      safeNavigate("/");
    } finally {
      setIsLoading(false);
    }

    return () => {
      console.error = originalError;
      clearCurrentCandidat();
      clearError();
    };
  }, [router, safeNavigate]);

  const handleLogout = () => {
    localStorage.removeItem("candidat_info");
    clearCurrentCandidat();
    clearError();
    toastRef.current?.show({
      severity: "success",
      summary: "Déconnexion",
      detail: "Vous avez été déconnecté avec succès",
      life: 2000,
    });
    setTimeout(() => safeNavigate("/"), 1500);
  };

  const handleDownloadConvocation = async () => {
    const centreCode =
      currentCandidat?.centreCode ||
      candidatInfo?.centreCode ||
      candidatInfo?.codeEtab;
    const numeroTable = candidatInfo?.numeroTable;

    if (!numeroTable) {
      toastRef.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Numéro de table non disponible",
        life: 3000,
      });
      return;
    }

    if (!centreCode) {
      toastRef.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Code centre non disponible",
        life: 3000,
      });
      return;
    }

    toastRef.current?.show({
      severity: "info",
      summary: "Téléchargement",
      detail: "Téléchargement de votre convocation en cours...",
      life: 2000,
    });

    const success = await downloadConvocation({
      centreCode: centreCode,
      numeroTable: numeroTable,
    });

    if (success) {
      toastRef.current?.show({
        severity: "success",
        summary: "Succès",
        detail: "Convocation téléchargée avec succès",
        life: 3000,
      });
    } else {
      toastRef.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Impossible de télécharger la convocation",
        life: 3000,
      });
    }
  };

  if (isLoading || isLoadingCandidat) {
    return (
      <div className="flex justify-content-center align-items-center min-h-screen">
        <ProgressSpinner />
      </div>
    );
  }

  if (!candidatInfo) {
    return (
      <div className="flex justify-content-center align-items-center min-h-screen">
        <div className="text-center">
          <i className="pi pi-exclamation-triangle text-4xl text-warning mb-3"></i>
          <p>Aucune information disponible</p>
          <Button label="Retour" onClick={() => safeNavigate("/")} />
        </div>
      </div>
    );
  }

  const candidat = candidatInfo;

  const matieresOptionnelles = [
    candidat?.mo1 && { nom: candidat.mo1 },
    candidat?.mo2 && { nom: candidat.mo2 },
    candidat?.mo3 && { nom: candidat.mo3 },
  ].filter(Boolean);

  const matieresFacultatives = [
    candidat?.ef1 && { nom: candidat.ef1 },
    candidat?.ef2 && { nom: candidat.ef2 },
  ].filter(Boolean);

  return (
    <div className="espace-candidat-container min-h-screen bg-gray-50">
      <Toast ref={toastRef} />

      {/* Header */}
      <motion.div
        className="bg-gradient-header text-white p-2 shadow-2"
        initial={{ y: -100 }}
        animate={{ y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="flex justify-content-between align-items-center flex-wrap gap-3">
          <div className="flex align-items-center gap-3">
            <motion.div
              className="avatar-wrapper"
              whileHover={{ scale: 1.1, rotate: 360 }}
              transition={{ duration: 0.3 }}
            >
              <div className="bg-white w-3rem h-3rem border-circle flex align-items-center justify-content-center shadow-2">
                <i className="pi pi-user text-blue-600 text-xl"></i>
              </div>
            </motion.div>
            <div>
              <motion.h1
                className="text-xl md:text-2xl text-white font-bold m-0"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.2 }}
              >
                {candidat?.prenoms} {candidat?.nom}
              </motion.h1>
              {candidatError && (
                <small className="text-yellow-200">{candidatError}</small>
              )}
            </div>
          </div>
          <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
            <Button
              label="Déconnexion"
              icon="pi pi-sign-out"
              onClick={handleLogout}
              className="p-button-outlined p-button-text-white text-white text-sm"
            />
          </motion.div>
        </div>
      </motion.div>

      {/* Contenu principal */}
      <div className="p-1 md:p-1">
        <motion.div
          initial={{ opacity: 0, y: 50 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
        >
          <div className="shadow-3 border-round-xl mt-0 pt-O">
            <Marquee
              speed={40}
              pauseOnHover={false}
              className="marquee-global pt-0 mt-0"
            >
              <div className="marquee-content-global bg-blue-500 p-2 border-round-lg flex align-items-center text-white">
                <i
                  className="pi pi-info-circle"
                  style={{
                    marginRight: "12px",
                    color: "#fff",
                    fontSize: "1.1rem",
                  }}
                ></i>
                <span style={{ fontWeight: "600" }}>
                  Veuillez revenir ici après le premier tour pour consulter
                  votre résultat
                </span>
              </div>
            </Marquee>
            <TabView
              activeIndex={activeIndex}
              onTabChange={(e) => setActiveIndex(e.index)}
              className="custom-tabview mt-2"
            >
              <TabPanel header="Informations" leftIcon="pi pi-user mr-2">
                <div className="p-1 md:p-2">
                  {/* Bouton Télécharger la convocation */}
                  <div className="flex justify-content-end mb-2">
                    <motion.div
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                    >
                      <Button
                        label="Télécharger ma convocation"
                        icon="pi pi-download"
                        onClick={handleDownloadConvocation}
                        className="p-button-rounded"
                        disabled={true}
                        style={{
                          background:
                            "linear-gradient(135deg, #2196f3 0%, #1565C0 100%)",
                          border: "none",
                          borderRadius: "50px",
                          padding: "0.75rem 1.5rem",
                        }}
                      />
                    </motion.div>
                  </div>

                  {/* Les 3 blocs d'informations */}
                  <div className="three-blocks-container">
                    {/* Bloc 1: Informations personnelles */}
                    <InfoBlock title="INFORMATIONS PERSONNELLES">
                      <InfoRow label="Prénom(s)" value={candidat?.prenoms} />
                      <InfoRow label="Nom" value={candidat?.nom} />
                      <InfoRow
                        label="Date de naissance"
                        value={candidat?.dateNaissance}
                      />
                      <InfoRow
                        label="Lieu de naissance"
                        value={candidat?.lieuNaissance}
                      />
                      <InfoRow label="Sexe" value={candidat?.sexe} />
                      <InfoRow
                        label="Nationalité"
                        value={candidat?.nationalite}
                      />
                    </InfoBlock>

                    {/* Bloc 2: Informations scolaires (avec les matières) */}
                    <InfoBlock title="INFORMATIONS SCOLAIRES">
                      <InfoRow label="Série" value={candidat?.serie} />
                      <InfoRow
                        label="Type de candidat"
                        value={candidat?.typeCandidat}
                      />
                      <InfoRow label="EPS" value={candidat?.eps} />
                      <InfoRow
                        label="Établissement fréquenté"
                        value={candidat?.etablissementName}
                      />
                      {/* Matières optionnelles */}
                      {matieresOptionnelles.map((matiere: any, idx: number) => (
                        <InfoRow
                          key={`mo-${idx}`}
                          label={`Matière optionnelle ${idx + 1}`}
                          value={matiere?.nom}
                        />
                      ))}
                      {/* Matières facultatives */}
                      {matieresFacultatives.map((matiere: any, idx: number) => (
                        <InfoRow
                          key={`ef-${idx}`}
                          label={`Matière facultative ${idx + 1}`}
                          value={matiere?.nom}
                        />
                      ))}
                      {candidat?.centreMatFac1 && candidat?.libMatFac1 && (
                        <InfoRow
                          label={`Centre ${candidat.libMatFac1}`}
                          value={candidat.centreMatFac1}
                        />
                      )}
                      {candidat?.centreMatFac2 && candidat?.libMatFac2 && (
                        <InfoRow
                          label={`Centre ${candidat.libMatFac2}`}
                          value={candidat.centreMatFac2}
                        />
                      )}
                    </InfoBlock>

                    {/* Bloc 3: Informations de la candidature */}
                    <InfoBlock title="INFORMATIONS DE LA CANDIDATURE">
                      <InfoRow
                        label="Numéro de table"
                        value={candidat?.numeroTable}
                      />
                      <InfoRow label="Jury" value={candidat?.jury} />
                      <InfoRow
                        label="Centre EPS"
                        value={candidat?.centreActEPSName}
                      />
                      <InfoRow
                        label="Centre d'écrit"
                        value={
                          candidat?.centreEcritName ||
                          candidat?.centreEcritParticulier
                        }
                      />
                    </InfoBlock>
                  </div>
                </div>
              </TabPanel>

              <TabPanel
                header="Mon résultat au Bac"
                leftIcon="pi pi-chart-line mr-2"
              >
                <div className="p-2 md:p-4">
                  <div className="text-center p-4">
                    <i className="pi pi-chart-line text-4xl text-500 mb-3"></i>
                    <p className="text-600">
                      Les résultats seront disponibles prochainement.
                    </p>
                  </div>
                </div>
              </TabPanel>
            </TabView>
          </div>
        </motion.div>
      </div>

      <style jsx global>{`
        .espace-candidat-container {
          min-height: 100vh;
        }

        .bg-gradient-header {
          background: linear-gradient(
            135deg,
            #1565c0 0%,
            #0d47a1 50%,
            #1565c0 100%
          );
          background-size: 200% 200%;
          animation: gradientShift 5s ease infinite;
        }

        @keyframes gradientShift {
          0%,
          100% {
            background-position: 0% 50%;
          }
          50% {
            background-position: 100% 50%;
          }
        }

        .avatar-wrapper {
          cursor: pointer;
        }

        /* Conteneur des 3 blocs en ligne */
        .three-blocks-container {
          display: flex;
          gap: 1.5rem;
          flex-wrap: wrap;
        }

        .info-block {
          flex: 1;
          min-width: 250px;
          background: white;
          border-radius: 12px;
          border: 1px solid rgba(33, 150, 243, 0.15);
          overflow: hidden;
        }

        .info-block-header {
          background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
          padding: 0.75rem 1rem;
          border-bottom: 1px solid rgba(33, 150, 243, 0.2);
        }

        .info-block-title {
          font-weight: 700;
          font-size: 0.85rem;
          color: #1565c0;
          letter-spacing: 0.5px;
        }

        .info-block-content {
          padding: 0.75rem;
        }

        /* Style pour chaque ligne info - libellé et valeur sur la même ligne */
        .info-row {
          display: flex;
          justify-content: space-between;
          align-items: baseline;
          padding: 0.5rem 0;
          border-bottom: 1px dashed #e9ecef;
          font-size: 0.85rem;
        }

        .info-row:last-child {
          border-bottom: none;
        }

        .info-row-label {
          font-weight: 600;
          color: #495057;
          flex-shrink: 0;
          margin-right: 1rem;
        }

        .info-row-value {
          color: #212529;
          text-align: right;
          word-break: break-word;
        }

        .custom-tabview .p-tabview-nav {
          display: flex;
          flex-wrap: nowrap;
          overflow-x: auto;
          gap: 4px;
          background: transparent;
        }

        .custom-tabview .p-tabview-nav li .p-tabview-nav-link {
          white-space: nowrap;
          border-radius: 50px;
          transition: all 0.3s ease;
          font-weight: 600;
          padding: 0.75rem 1.25rem;
        }

        .custom-tabview .p-tabview-nav li.p-highlight .p-tabview-nav-link {
          background: linear-gradient(135deg, #2196f3 0%, #1565c0 100%);
          color: white;
        }

        @media (max-width: 900px) {
          .three-blocks-container {
            flex-direction: column;
            gap: 1rem;
          }

          .info-block {
            width: 100%;
          }
        }

        ::-webkit-scrollbar {
          width: 6px;
          height: 6px;
        }

        ::-webkit-scrollbar-track {
          background: #f1f1f1;
          border-radius: 10px;
        }

        ::-webkit-scrollbar-thumb {
          background: linear-gradient(135deg, #2196f3 0%, #1565c0 100%);
          border-radius: 10px;
        }
      `}</style>
    </div>
  );
}
