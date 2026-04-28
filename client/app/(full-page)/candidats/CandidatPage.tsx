"use client";

import React, { useState, useRef, useEffect } from "react";
import { Dialog } from "primereact/dialog";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { OverlayPanel } from "primereact/overlaypanel";
import { Toast } from "primereact/toast";
import Marquee from "react-fast-marquee";

interface CandidatDialogProps {
  visible: boolean;
  onHide: () => void;
  toastRef: React.RefObject<Toast>;
}

interface DateSelection {
  jour: number | null;
  mois: number | null;
  annee: number | null;
}

export default function CandidatDialog({
  visible,
  onHide,
  toastRef,
}: CandidatDialogProps) {
  const [formData, setFormData] = useState({
    codeEtablissement: "",
    numeroTable: "",
    dateNaissance: {
      jour: null as number | null,
      mois: null as number | null,
      annee: null as number | null,
    },
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<{
    codeEtablissement?: string;
    numeroTable?: string;
    dateNaissance?: string;
  }>({});

  // Refs pour les OverlayPanels
  const jourPanelRef = useRef<OverlayPanel>(null);
  const moisPanelRef = useRef<OverlayPanel>(null);
  const anneePanelRef = useRef<OverlayPanel>(null);
  const jourInputRef = useRef<HTMLInputElement>(null);
  const moisInputRef = useRef<HTMLInputElement>(null);
  const anneeInputRef = useRef<HTMLInputElement>(null);

  // Liste des mois
  const monthList = [
    { value: 1, label: "Janvier" },
    { value: 2, label: "Février" },
    { value: 3, label: "Mars" },
    { value: 4, label: "Avril" },
    { value: 5, label: "Mai" },
    { value: 6, label: "Juin" },
    { value: 7, label: "Juillet" },
    { value: 8, label: "Août" },
    { value: 9, label: "Septembre" },
    { value: 10, label: "Octobre" },
    { value: 11, label: "Novembre" },
    { value: 12, label: "Décembre" },
  ];

  const getDayList = (): number[] => {
    const maxDays = getMaxDays();
    return Array.from({ length: maxDays }, (_, i) => i + 1);
  };

  const getYearList = (): number[] => {
    const currentYear = new Date().getFullYear();
    return Array.from(
      { length: currentYear - 1900 + 1 },
      (_, i) => currentYear - i,
    );
  };

  const getMaxDays = () => {
    const { mois, annee } = formData.dateNaissance;
    if (!mois || !annee) return 31;
    if ([4, 6, 9, 11].includes(mois)) return 30;
    if (mois === 2) {
      if ((annee % 4 === 0 && annee % 100 !== 0) || annee % 400 === 0)
        return 29;
      return 28;
    }
    return 31;
  };

  useEffect(() => {
    const maxDays = getMaxDays();
    const currentDay = formData.dateNaissance.jour;
    if (currentDay && currentDay > maxDays) {
      setFormData((prev) => ({
        ...prev,
        dateNaissance: { ...prev.dateNaissance, jour: maxDays },
      }));
    }
  }, [formData.dateNaissance.mois, formData.dateNaissance.annee]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    if (errors[e.target.name as keyof typeof errors]) {
      setErrors({
        ...errors,
        [e.target.name]: undefined,
      });
    }
  };

  const handleDateSelect = (field: keyof DateSelection, value: number) => {
    setFormData({
      ...formData,
      dateNaissance: {
        ...formData.dateNaissance,
        [field]: value,
      },
    });
    if (errors.dateNaissance) {
      setErrors({
        ...errors,
        dateNaissance: undefined,
      });
    }
    // Fermer le panel après sélection
    if (field === "jour") jourPanelRef.current?.hide();
    if (field === "mois") moisPanelRef.current?.hide();
    if (field === "annee") anneePanelRef.current?.hide();
  };

  const centerPanel = (
    panelElement: HTMLElement | null,
    inputElement: HTMLInputElement | null,
  ) => {
    if (panelElement && inputElement) {
      const inputRect = inputElement.getBoundingClientRect();
      const panelRect = panelElement.getBoundingClientRect();

      const left = inputRect.left + inputRect.width / 2 - panelRect.width / 2;
      const top = inputRect.top + inputRect.height / 2 - panelRect.height / 2;

      panelElement.style.position = "fixed";
      panelElement.style.top = `${top}px`;
      panelElement.style.left = `${left}px`;
      panelElement.style.margin = "0";
    }
  };

  const handleInputClick = (
    event: React.MouseEvent<HTMLInputElement>,
    field: "jour" | "mois" | "annee",
  ) => {
    if (field === "jour") {
      jourPanelRef.current?.toggle(event);
      setTimeout(() => {
        const panel = document.querySelector(
          ".jour-panel .p-overlaypanel-content",
        )?.parentElement;
        centerPanel(panel as HTMLElement, jourInputRef.current);
      }, 10);
    }
    if (field === "mois") {
      moisPanelRef.current?.toggle(event);
      setTimeout(() => {
        const panel = document.querySelector(
          ".mois-panel .p-overlaypanel-content",
        )?.parentElement;
        centerPanel(panel as HTMLElement, moisInputRef.current);
      }, 10);
    }
    if (field === "annee") {
      anneePanelRef.current?.toggle(event);
      setTimeout(() => {
        const panel = document.querySelector(
          ".annee-panel .p-overlaypanel-content",
        )?.parentElement;
        centerPanel(panel as HTMLElement, anneeInputRef.current);
      }, 10);
    }
  };

  const getDisplayValue = (field: "jour" | "mois" | "annee") => {
    const value = formData.dateNaissance[field];
    if (!value) return "";
    if (field === "jour") return value.toString().padStart(2, "0");
    if (field === "mois") {
      const month = monthList.find((m) => m.value === value);
      return month ? month.label : value.toString().padStart(2, "0");
    }
    return value.toString();
  };

  const validateForm = () => {
    const newErrors: typeof errors = {};

    if (!formData.codeEtablissement.trim())
      newErrors.codeEtablissement = "Le code établissement est obligatoire";
    if (!formData.numeroTable.trim())
      newErrors.numeroTable = "Le numéro de table est obligatoire";

    const { jour, mois, annee } = formData.dateNaissance;
    if (!jour || !mois || !annee) {
      newErrors.dateNaissance = "La date de naissance est obligatoire";
    } else {
      const date = new Date(annee, mois - 1, jour);
      if (
        date.getFullYear() !== annee ||
        date.getMonth() !== mois - 1 ||
        date.getDate() !== jour
      ) {
        newErrors.dateNaissance = "Date invalide";
      } else if (date > new Date()) {
        newErrors.dateNaissance = "La date ne peut pas être dans le futur";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const formatDate = (): string => {
    const { jour, mois, annee } = formData.dateNaissance;
    if (!jour || !mois || !annee) return "";
    const jourStr = jour.toString().padStart(2, "0");
    const moisStr = mois.toString().padStart(2, "0");
    return `${jourStr}/${moisStr}/${annee}`;
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;

    setLoading(true);

    try {
      const candidatData = {
        codeEtablissement: formData.codeEtablissement,
        numeroTable: formData.numeroTable,
        dateNaissance: formatDate(),
        timestamp: Date.now(),
      };

      localStorage.setItem("candidat_info", JSON.stringify(candidatData));

      toastRef.current?.show({
        severity: "success",
        summary: "Succès",
        detail: "Authentification réussie, redirection en cours...",
        life: 2000,
      });

      onHide();

      setTimeout(() => {
        window.location.href = "/convocations/candidats";
      }, 500);
    } catch (error) {
      console.error("Erreur:", error);
      toastRef.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Une erreur est survenue",
        life: 3000,
      });
      setLoading(false);
    }
  };

  const renderOptions = (items: number[], field: "jour" | "mois" | "annee") => {
    return (
      <div className="options-container">
        <div className="options-header">
          <span>
            Sélectionnez{" "}
            {field === "jour"
              ? "le jour"
              : field === "mois"
              ? "le mois"
              : "l'année"}
          </span>
        </div>
        <div className="options-list">
          {items.map((item) => (
            <div
              key={item}
              className={`option-item ${
                formData.dateNaissance[field] === item ? "selected" : ""
              }`}
              onClick={() => handleDateSelect(field, item)}
            >
              {field === "mois"
                ? monthList.find((m) => m.value === item)?.label
                : field === "jour"
                ? item.toString().padStart(2, "0")
                : item.toString()}
            </div>
          ))}
        </div>
      </div>
    );
  };

  const footer = (
    <div className="flex justify-content-end gap-2">
      <Button
        label="Annuler"
        icon="pi pi-times"
        className="p-button-text"
        onClick={onHide}
        disabled={loading}
      />
      <Button
        label="Accéder"
        icon="pi pi-check"
        onClick={handleSubmit}
        loading={loading}
        autoFocus
        style={{ backgroundColor: "#2e7d32", borderColor: "#2e7d32" }}
      />
    </div>
  );

  return (
    <Dialog
      visible={visible}
      header={
        <div className="flex align-items-center gap-2">
          <i className="pi pi-user" style={{ color: "#2e7d32", fontSize: "1.25rem" }}></i>
          <span className="text-xl font-semibold" style={{ color: "#1e3c72" }}>
            Espace Candidat
          </span>
        </div>
      }
      modal
      style={{ width: "500px" }}
      footer={footer}
      onHide={onHide}
      draggable={false}
      resizable={false}
      className="candidat-dialog"
    >
      <div className="p-fluid">
        <p className="mb-4 text-600">
          Veuillez renseigner vos informations pour accéder à votre espace candidat.
        </p>

        <div className="field mb-3">
          <label htmlFor="codeEtablissement" className="font-semibold block mb-2">
            Code Établissement <span className="text-red-500">*</span>
          </label>
          <InputText
            id="codeEtablissement"
            name="codeEtablissement"
            value={formData.codeEtablissement}
            onChange={handleChange}
            className={errors.codeEtablissement ? "p-invalid" : ""}
            placeholder="Ex: LSLL"
          />
          {errors.codeEtablissement && (
            <small className="p-error">{errors.codeEtablissement}</small>
          )}
        </div>

        <div className="field mb-3">
          <label htmlFor="numeroTable" className="font-semibold block mb-2">
            Numéro de table <span className="text-red-500">*</span>
          </label>
          <InputText
            id="numeroTable"
            name="numeroTable"
            value={formData.numeroTable}
            onChange={handleChange}
            className={errors.numeroTable ? "p-invalid" : ""}
            placeholder="Ex: 1234"
          />
          {errors.numeroTable && (
            <small className="p-error">{errors.numeroTable}</small>
          )}
        </div>

        <div className="field mb-2">
          <label className="font-semibold block mb-2">
            Date de naissance <span className="text-red-500">*</span>
          </label>

          <div className="date-picker-container">
            <div className="date-column">
              <label className="date-label">Jour</label>
              <input
                ref={jourInputRef}
                type="text"
                className={`custom-date-input ${
                  errors.dateNaissance ? "p-invalid" : ""
                }`}
                value={getDisplayValue("jour")}
                onClick={(e) => handleInputClick(e, "jour")}
                readOnly
                placeholder="JJ"
              />
              <OverlayPanel
                ref={jourPanelRef}
                showCloseIcon
                style={{ width: "120px", padding: 0 }}
                className="jour-panel"
              >
                {renderOptions(getDayList(), "jour")}
              </OverlayPanel>
            </div>

            <div className="date-column">
              <label className="date-label">Mois</label>
              <input
                ref={moisInputRef}
                type="text"
                className={`custom-date-input ${
                  errors.dateNaissance ? "p-invalid" : ""
                }`}
                value={getDisplayValue("mois")}
                onClick={(e) => handleInputClick(e, "mois")}
                readOnly
                placeholder="MM"
              />
              <OverlayPanel
                ref={moisPanelRef}
                showCloseIcon
                style={{ width: "150px", padding: 0 }}
                className="mois-panel"
              >
                {renderOptions(
                  monthList.map((m) => m.value),
                  "mois",
                )}
              </OverlayPanel>
            </div>

            <div className="date-column">
              <label className="date-label">Année</label>
              <input
                ref={anneeInputRef}
                type="text"
                className={`custom-date-input ${
                  errors.dateNaissance ? "p-invalid" : ""
                }`}
                value={getDisplayValue("annee")}
                onClick={(e) => handleInputClick(e, "annee")}
                readOnly
                placeholder="AAAA"
              />
              <OverlayPanel
                ref={anneePanelRef}
                showCloseIcon
                style={{ width: "120px", padding: 0 }}
                className="annee-panel"
              >
                {renderOptions(getYearList(), "annee")}
              </OverlayPanel>
            </div>
          </div>

          {errors.dateNaissance && (
            <small className="p-error">{errors.dateNaissance}</small>
          )}
        </div>
      </div>

      <style jsx global>{`
        .candidat-dialog .p-dialog-header {
          background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
          border-bottom: 1px solid #e2e8f0;
          padding: 1rem 1.5rem;
        }
        .candidat-dialog .p-dialog-content {
          padding: 1.5rem;
        }
        .candidat-dialog .p-dialog-footer {
          padding: 1rem 1.5rem;
          border-top: 1px solid #e2e8f0;
        }

        .date-picker-container {
          display: flex;
          gap: 1rem;
          justify-content: space-between;
        }

        .date-column {
          flex: 1;
          text-align: center;
        }

        .date-label {
          display: block;
          font-size: 0.75rem;
          color: #6c757d;
          margin-bottom: 0.5rem;
          font-weight: 500;
          text-align: left;
        }

        .custom-date-input {
          width: 100%;
          padding: 0.75rem;
          border: 1px solid #ced4da;
          border-radius: 6px;
          font-size: 1rem;
          text-align: center;
          cursor: pointer;
          background-color: white;
          transition: all 0.2s;
        }

        .custom-date-input:hover {
          border-color: #2e7d32;
        }

        .custom-date-input:focus {
          outline: none;
          border-color: #2e7d32;
          box-shadow: 0 0 0 2px rgba(46, 125, 50, 0.2);
        }

        .custom-date-input.p-invalid {
          border-color: #f44336;
        }

        .options-container {
          background: white;
          border-radius: 6px;
          overflow: hidden;
        }

        .options-header {
          padding: 0.75rem;
          background: #f8f9fa;
          border-bottom: 1px solid #dee2e6;
          font-weight: 600;
          font-size: 0.875rem;
          text-align: center;
          color: #1e3c72;
        }

        .options-list {
          max-height: 250px;
          overflow-y: auto;
        }

        .option-item {
          padding: 0.75rem;
          cursor: pointer;
          text-align: center;
          transition: background 0.2s;
          border-bottom: 1px solid #f0f0f0;
        }

        .option-item:hover {
          background: #e8f5e9;
        }

        .option-item.selected {
          background: #2e7d32;
          color: white;
        }

        .option-item:last-child {
          border-bottom: none;
        }

        @media (max-width: 550px) {
          .date-picker-container {
            flex-direction: column;
            gap: 0.75rem;
          }

          .date-column {
            width: 100%;
          }
        }
      `}</style>
    </Dialog>
  );
}