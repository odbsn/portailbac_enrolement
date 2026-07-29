import React from "react";
import { Dialog } from "primereact/dialog";
import { Button } from "primereact/button";
import { Chip } from "primereact/chip";
import { Tag } from "primereact/tag";
import { Badge } from "primereact/badge";
import { Divider } from "primereact/divider";
import { CandidatFinis } from "../convocationEtabStore";
import ConvocationButton from "./ConvocationButton";

interface DetailCandidatDialogProps {
  visible: boolean;
  candidat: CandidatFinis | null;
  onHide: () => void;
  onPrintConvocation?: (candidat: CandidatFinis) => void;
}

const formatDate = (dateString?: string) => {
  if (!dateString) return "-";
  if (dateString.includes("/")) return dateString;
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    return date.toLocaleDateString("fr-FR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  } catch {
    return dateString;
  }
};

const getStatutTag = (statut?: string) => {
  const statutMap: Record<
    string,
    {
      severity: "success" | "danger" | "warning" | "info" | "secondary";
      label: string;
    }
  > = {
    "0": { severity: "warning", label: "En attente" },
    "1": { severity: "success", label: "Admis" },
    "2": { severity: "danger", label: "Non admis" },
    "3": { severity: "info", label: "Absent" },
  };
  return (
    statutMap[statut || "0"] || {
      severity: "secondary",
      label: statut || "Inconnu",
    }
  );
};

export default function DetailCandidatDialog({
  visible,
  candidat,
  onHide,
  onPrintConvocation,
}: DetailCandidatDialogProps) {
  if (!candidat) return null;

  const statutInfo = getStatutTag(candidat.statutResultat);

  const matieresOptionnelles = [
    candidat.mo1,
    candidat.mo2,
    candidat.mo3,
  ].filter(Boolean);

  const matieresFacultatives = [candidat.ef1, candidat.ef2].filter(Boolean);

  const footer = (
    <div className="flex justify-content-end gap-3">
      <Button
        label="Fermer"
        icon="pi pi-times"
        onClick={onHide}
        text
        className="p-button-text"
      />
      <ConvocationButton
        numeroTable={candidat.numeroTable}
        label="Télécharger la convocation"
        icon="pi pi-file-pdf"
        className="p-button-text"
        circular={false}
      />
    </div>
  );

  const header = (
    <div className="flex align-items-center gap-3">
      <div className="bg-primary w-3rem h-3rem border-round-circle flex align-items-center justify-content-center shadow-2">
        <i className="pi pi-user text-white text-xl" />
      </div>
      <div>
        <div className="text-xl font-bold">
          {candidat.prenoms} {candidat.nom}
        </div>
        <div className="flex align-items-center gap-3 text-sm text-500 mt-1">
          <span>
            <i className="pi pi-id-card mr-1" /> N° Dossier:{" "}
            {candidat.numeroDossier}
          </span>
        </div>
      </div>
    </div>
  );

  return (
    <Dialog
      visible={visible}
      header={header}
      footer={footer}
      modal
      style={{ width: "90%", maxWidth: "950px" }}
      onHide={onHide}
      dismissableMask
      closeOnEscape
      className="professional-dialog"
    >
      <div className="p-4">
        {/* Section Identité */}
        <div className="section py-3">
          <div className="section-title">
            <i className="pi pi-id-card mr-2" />
            Informations personnelles
          </div>
          <div className="grid flex gap-4">
            <div className="flex align-items-center gap-2 border-right-2 ">
              <span className="">Sexe :</span>
              <span className="info-data">
                <Chip
                  label={
                    candidat.sexe === "M"
                      ? "Masculin"
                      : candidat.sexe === "F"
                      ? "Féminin"
                      : candidat.sexe || "-"
                  }
                  icon={
                    candidat.sexe === "M"
                      ? "pi pi-mars"
                      : candidat.sexe === "F"
                      ? "pi pi-venus"
                      : "pi pi-user"
                  }
                  className="bg-primary-50"
                />
              </span>
            </div>
            <div className="flex align-items-center gap-2 border-right-2  pr-4">
              <span className="">Date de naissance :</span>
              <span className="info-data">
                {formatDate(candidat.dateNaissance)}
              </span>
            </div>
            <div className="flex align-items-center gap-2 border-right-2  pr-4">
              <span className="">Lieu de naissance :</span>
              <span className="info-data">{candidat.lieuNaissance || "-"}</span>
            </div>
            <div className="flex align-items-center gap-2 border-right-2  pr-4">
              <span className="">Nationalité :</span>
              <span className="info-data">{candidat.nationalite || "-"}</span>
            </div>
            <div className="flex align-items-center gap-2 border-right-2  pr-4">
              <span className="">Téléphone :</span>
              <span className="info-data">
                {candidat.telephone || "Non renseigné"}
              </span>
            </div>
          </div>
        </div>

        <Divider className="my-3" />

        {/* Section Examen */}
        <div className="section py-3">
          <div className="section-title">
            <i className="pi pi-graduation-cap mr-2" />
            Informations d'examen
          </div>
          <div className="grid flex gap-6">
            <div className="flex align-items-center gap-3">
              <span className="">Série</span>
              <span className="info-data">
                <span className="bg-green-200 px-4 py-1 border-round-xl">
                  {candidat.serie || "-"}
                </span>
              </span>
            </div>
            <div className="flex align-items-center gap-3">
              <span className="">Jury</span>
              <span className="info-data">
                <span className="bg-green-200 px-4 py-1 border-round-xl">
                  {candidat.jury || "-"}
                </span>
              </span>
            </div>
            <div className="flex align-items-center gap-3">
              <span className="">Numero de table :</span>
              <span className="info-data">
                <span className="bg-green-200 px-4 py-1 border-round-xl">
                  {candidat.numeroTable}
                </span>
              </span>
            </div>

            <div className="flex align-items-center gap-3">
              <span className="">EPS</span>
              <span className="info-data">
                <Tag
                  severity={
                    candidat.eps === "A"
                      ? "success"
                      : candidat.eps === "I"
                      ? "danger"
                      : "secondary"
                  }
                  value={
                    candidat.eps === "A"
                      ? "Apte"
                      : candidat.eps === "I"
                      ? "Inapte"
                      : candidat.eps || "-"
                  }
                  rounded
                  icon={
                    candidat.eps === "A"
                      ? "pi pi-check"
                      : candidat.eps === "I"
                      ? "pi pi-times"
                      : "pi pi-question"
                  }
                />
              </span>
            </div>
          </div>
        </div>

        <Divider className="my-3" />

        {/* Section Centres */}
        <div className="section py-3">
          <div className="section-title">
            <i className="pi pi-building mr-2" />
            Centres d'examen
          </div>
          <div className="grid flex gap-4">
            <div className="flex align-items-center gap-3 border-right-2 pr-4">
              <span className="">Centre écrit :</span>
              <span className="info-data">
                {candidat.centreEcrit?.name && (
                  <div className="">{candidat.centreEcrit.name}</div>
                )}
              </span>
            </div>
            <div className="flex align-items-center gap-3 border-right-2 pr-4">
              <span className="">Centre EPS :</span>
              <span className="info-data">
                {candidat.centreActEPS?.name || "-"}
              </span>
            </div>
            <div className="flex align-items-center gap-3 border-right-2 pr-4">
              <span className="">Établissement :</span>
              <span className="info-data">
                {candidat.etablissement?.name || "-"}
                {candidat.etablissement?.code && (
                  <span className="text-400 ml-1">
                    ({candidat.etablissement.code})
                  </span>
                )}
              </span>
            </div>
          </div>
        </div>

        {/* Section Matières optionnelles */}
        {matieresOptionnelles.length > 0 && (
          <>
            <Divider className="my-3" />
            <div className="section py-3">
              <div className="section-title">
                <i className="pi pi-star mr-2" />
                Matières optionnelles
              </div>
              <div className="flex flex-wrap gap-2 mt-2">
                {matieresOptionnelles.map((matiere, index) => (
                  <Chip
                    key={index}
                    label={matiere}
                    icon="pi pi-check"
                    className="bg-primary-50"
                  />
                ))}
              </div>
            </div>
          </>
        )}

        {/* Section Matières facultatives - Maintenant comme les optionnelles */}
        {matieresFacultatives.length > 0 && (
          <>
            <Divider className="my-3" />
            <div className="section py-3">
              <div className="section-title">
                <i className="pi pi-plus-circle mr-2" />
                Matières facultatives
              </div>
              <div className="flex flex-wrap gap-2 mt-2">
                {matieresFacultatives.map((matiere, index) => (
                  <Chip
                    key={index}
                    label={matiere}
                    icon="pi pi-check"
                    className="bg-primary-50"
                  />
                ))}
              </div>
            </div>
          </>
        )}
      </div>

      <style jsx>{`
        :global(.professional-dialog .p-dialog-header) {
          padding: 1.25rem 1.5rem;
          border-bottom: 1px solid var(--surface-200);
          background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
        }
        :global(.professional-dialog .p-dialog-content) {
          padding: 0;
          max-height: 70vh;
          overflow-y: auto;
        }
        :global(.professional-dialog .p-dialog-footer) {
          padding: 1rem 1.5rem;
          border-top: 1px solid var(--surface-200);
          background: #f8fafc;
        }
        .section {
          margin-bottom: 0.5rem;
        }
        .section-title {
          font-size: 1.25rem;
          font-weight: 800;
          color: var(--primary-700);
          margin-bottom: 1rem;
          display: flex;
          align-items: center;
          border-left: 3px solid var(--primary-500);
          padding-left: 0.75rem;
        }
        .info-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 1rem 1.5rem;
        }
        .info-item {
          display: flex;
          align-items: flex-start;
          gap: 0.5rem;
        }
        .info-label {
          min-width: 110px;
          font-size: 0.8rem;
          font-weight: 600;
          color: var(--surface-500);
          text-transform: uppercase;
          letter-spacing: 0.3px;
        }
        .info-data {
          flex: 1;
          font-size: 0.9rem;
          font-weight: 800;
          /* color: var(--surface-700); */
          word-break: break-word;
        }
        :global(.professional-dialog .p-badge) {
          font-size: 0.75rem;
          padding: 0.25rem 0.6rem;
        }
        :global(.professional-dialog .p-chip) {
          background: var(--primary-50);
          color: var(--primary-700);
          font-size: 0.85rem;
          padding: 0.35rem 0.75rem;
        }
        :global(.professional-dialog .p-tag) {
          font-size: 0.8rem;
          padding: 0.25rem 0.6rem;
        }
        :global(.professional-dialog .p-divider) {
          margin: 0;
        }
        @media (max-width: 640px) {
          .info-grid {
            grid-template-columns: 1fr;
            gap: 0.75rem;
          }
          .info-item {
            flex-direction: column;
            gap: 0.25rem;
          }
          .info-label {
            min-width: auto;
          }
        }
      `}</style>
    </Dialog>
  );
}
