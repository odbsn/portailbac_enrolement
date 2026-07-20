// AppMenuWithCandidat.tsx
"use client";

import { MenuModal } from "@/types/layout";
import AppSubMenu from "./AppSubMenu";
import { useContext, useState, useRef } from "react";
import { UserContext } from "@/app/userContext";
import { Button } from "primereact/button";
import { Dialog } from "primereact/dialog";
import { InputText } from "primereact/inputtext";
import { useRouter } from "next/navigation";
import { Toast } from "primereact/toast";

type Role =
  | "ADMIN"
  | "AGENT_DE_SAISIE"
  | "SCOLARITE"
  | "CHEF_ETABLISSEMENT"
  | "RECEPTIONNISTE"
  | "AUTORISATION_RECEPTION"
  | "VIGNETTES_COUPONS"
  | "INSPECTEUR_ACADEMIE"
  | "DEMSG"
  | "FINANCE_COMPTA"
  | "PLANIFICATION";

interface User {
  username: string;
  role: Role;
}

// Composant pour le dialogue candidat
const CandidatAccessDialog = ({ visible, onHide, toastRef }: any) => {
  const [formData, setFormData] = useState({
    prenom: "",
    nom: "",
    numeroTable: "",
    jury: "",
  });
  const [errors, setErrors] = useState<{
    prenom?: string;
    nom?: string;
    numeroTable?: string;
    jury?: string;
  }>({});
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    // Effacer l'erreur du champ
    if (errors[e.target.name as keyof typeof errors]) {
      setErrors({
        ...errors,
        [e.target.name]: undefined,
      });
    }
  };

  const validateForm = () => {
    const newErrors: typeof errors = {};

    if (!formData.prenom.trim()) newErrors.prenom = "Le prénom est obligatoire";
    if (!formData.nom.trim()) newErrors.nom = "Le nom est obligatoire";
    if (!formData.numeroTable.trim())
      newErrors.numeroTable = "Le numéro de table est obligatoire";
    if (!formData.jury.trim()) newErrors.jury = "Le jury est obligatoire";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;

    setLoading(true);

    try {
      // Sauvegarder les informations du candidat
      const candidatData = {
        prenom: formData.prenom,
        nom: formData.nom,
        numeroTable: formData.numeroTable,
        jury: formData.jury,
        timestamp: Date.now(),
      };

      localStorage.setItem("candidat_info", JSON.stringify(candidatData));

      toastRef?.current?.show({
        severity: "success",
        summary: "Succès",
        detail: "Authentification réussie, redirection en cours...",
        life: 2000,
      });

      setTimeout(() => {
        onHide();
        router.push("/convocations/candidats");
      }, 1500);
    } catch (error) {
      console.error("Erreur:", error);
      toastRef?.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Une erreur est survenue",
        life: 3000,
      });
    } finally {
      setLoading(false);
    }
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
        label="Accéder à mon espace"
        icon="pi pi-sign-in"
        onClick={handleSubmit}
        loading={loading}
        autoFocus
      />
    </div>
  );

  return (
    <Dialog
      visible={visible}
      header={
        <div className="flex align-items-center gap-2">
          <i className="pi pi-user text-primary text-xl"></i>
          <span className="text-xl font-semibold">Espace Candidat</span>
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
      <div className="p-fluid p-3">
        <div className="text-center mb-4">
          <i className="pi pi-id-card text-4xl text-primary mb-2"></i>
          <p className="text-600">
            Veuillez renseigner vos informations pour accéder à votre espace
            personnel.
          </p>
        </div>

        <div className="field mb-3">
          <label htmlFor="prenom" className="font-semibold block mb-2">
            Prénom <span className="text-red-500">*</span>
          </label>
          <InputText
            id="prenom"
            name="prenom"
            value={formData.prenom}
            onChange={handleChange}
            className={errors.prenom ? "p-invalid" : ""}
            placeholder="Votre prénom"
          />
          {errors.prenom && <small className="p-error">{errors.prenom}</small>}
        </div>

        <div className="field mb-3">
          <label htmlFor="nom" className="font-semibold block mb-2">
            Nom <span className="text-red-500">*</span>
          </label>
          <InputText
            id="nom"
            name="nom"
            value={formData.nom}
            onChange={handleChange}
            className={errors.nom ? "p-invalid" : ""}
            placeholder="Votre nom"
          />
          {errors.nom && <small className="p-error">{errors.nom}</small>}
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
          <label htmlFor="jury" className="font-semibold block mb-2">
            Jury <span className="text-red-500">*</span>
          </label>
          <InputText
            id="jury"
            name="jury"
            value={formData.jury}
            onChange={handleChange}
            className={errors.jury ? "p-invalid" : ""}
            placeholder="Ex: JURY A"
          />
          {errors.jury && <small className="p-error">{errors.jury}</small>}
        </div>
      </div>

      <style jsx global>{`
        .candidat-dialog .p-dialog-header {
          background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
          color: white;
          border-bottom: none;
          padding: 1rem 1.5rem;
        }
        .candidat-dialog .p-dialog-header .p-dialog-title {
          font-weight: 600;
        }
        .candidat-dialog .p-dialog-header-icons .p-dialog-header-icon {
          color: white;
        }
        .candidat-dialog .p-dialog-header-icons .p-dialog-header-icon:hover {
          background: rgba(255, 255, 255, 0.2);
        }
        .candidat-dialog .p-dialog-content {
          padding: 0;
        }
        .candidat-dialog .p-dialog-footer {
          padding: 1rem 1.5rem;
          border-top: 1px solid #e2e8f0;
        }
      `}</style>
    </Dialog>
  );
};

const AppMenu = () => {
  const { user } = useContext(UserContext);
  const [candidatDialogVisible, setCandidatDialogVisible] = useState(false);
  const toastRef = useRef<Toast>(null);

  const isRole = (value: string): value is Role => {
    return [
      "ADMIN",
      "AGENT_DE_SAISIE",
      "SCOLARITE",
      "CHEF_ETABLISSEMENT",
      "PLANIFICATION",
      "RECEPTIONNISTE",
      "VIGNETTES_COUPONS",
      "FINANCE_COMPTA",
      "AUTORISATION_RECEPTION",
      "INSPECTEUR_ACADEMIE",
      "DEMSG",
    ].includes(value);
  };

  const hasAccess = (roles: Role[]): boolean => {
    const roleName = user?.profil?.name;
    return isRole(roleName) && roles.includes(roleName);
  };

  const model: MenuModal[] = [
    hasAccess([
      "AGENT_DE_SAISIE",
      "SCOLARITE",
      "ADMIN",
      "INSPECTEUR_ACADEMIE",
      "DEMSG",
      "FINANCE_COMPTA",
    ]) && {
      icon: "pi pi-home",
      items: [
        {
          label: "Tableau de bord",
          icon: "pi pi-fw pi-home",
          to: "/tableau-de-bord",
        },
      ].filter(Boolean),
    },
    { separator: true },
    hasAccess(["ADMIN"]) && {
      label: "Administration",
      icon: "pi pi-home",
      items: [
        {
          label: "Accés & Profils",
          icon: "pi pi-fw pi-key",
          to: "/editions-systeme/acces",
        },
        {
          label: "Programmation",
          icon: "pi pi-fw pi-calendar",
          to: "/editions-systeme/programmation",
        },
        {
          label: "Paiement FAEB3",
          icon: "pi pi-fw pi-money-bill",
          to: "/editions-systeme/vignettes-numeriques-FAEB3",
        },
        {
          label: "Localisation",
          icon: "pi pi-fw pi-map",
          items: [
            {
              label: "Région",
              to: "/editions-systeme/localisation/region",
            },
            {
              label: "Département",
              to: "/editions-systeme/localisation/departement",
            },
            {
              label: "Ville",
              to: "/editions-systeme/localisation/ville",
            },
            {
              label: "Centre Etat Civil",
              to: "/editions-systeme/localisation/centre-etat-civil",
            },
          ],
        },
        {
          label: "Séries",
          icon: "pi pi-fw pi-megaphone",
          to: "/editions-systeme/series",
        },
        {
          label: "Matières",
          icon: "pi pi-fw pi-tags",
          to: "/editions-systeme/matieres",
        },
        {
          label: "Structures",
          icon: "pi pi-fw pi-building",
          items: [
            {
              label: "Etablissement",
              to: "/editions-systeme/structures/etablissement-scolaire",
            },
            {
              label: "Inspection Académie",
              to: "/editions-systeme/structures/inspection-academie",
            },
          ],
        },
        {
          label: "Archives candidats",
          icon: "pi pi-book",
          to: "/editions-systeme/archives-candidats",
        },
      ].filter(Boolean),
    },
    hasAccess(["SCOLARITE", "ADMIN", "PLANIFICATION"]) && {
      label: "Données et Sorties",
      items: [
        {
          label: "Données",
          icon: "pi pi-fw pi-database",
          to: "/planification/data-and-output",
        },
      ].filter(Boolean),
    },
    hasAccess([
      "SCOLARITE",
      "RECEPTIONNISTE",
      "ADMIN",
      "VIGNETTES_COUPONS",
      "AUTORISATION_RECEPTION",
    ]) && {
      label: "Scolarité",
      icon: "pi pi-home",
      items: [
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Candidats",
          icon: "pi pi-fw pi-history",
          to: "/editions-systeme/audit-dossier-candidats",
        },
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Convocations",
          icon: "pi pi-copy",
          to: "/convocations/admin",
        },
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Contacts Etablissements",
          icon: "pi pi-fw pi-phone",
          to: "/scolarite/contacts-etab",
        },
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Sujets avec candidats",
          icon: "pi pi-fw pi-megaphone",
          to: "/scolarite/sujets-avec-candidats",
        },
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Check Receptionniste",
          icon: "pi pi-fw pi-info-circle",
          to: "/scolarite/check-receptionniste",
        },
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Etablissements flottants",
          icon: "pi pi-fw pi-star",
          to: "/scolarite/check-etab-part-received",
        },
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Etablissements non réceptionnés",
          icon: "pi pi-fw pi-ban",
          to: "/scolarite/check-etab-not-received",
        },
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Incohérence vignette",
          icon: "pi pi-fw pi-thumbs-down",
          to: "/scolarite/check-etab-with-problem",
        },
        hasAccess(["SCOLARITE", "ADMIN", "VIGNETTES_COUPONS"]) && {
          label: "Vignettes & Coupons",
          icon: "pi pi-fw pi-ticket",
          to: "/scolarite/vignettes-coupons",
        },
        hasAccess(["SCOLARITE", "ADMIN", "AUTORISATION_RECEPTION"]) && {
          label: "Autorisation réception",
          icon: "pi pi-fw pi-folder-open",
          to: "/scolarite/autorisation-reception",
        },
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Suivi mandataire",
          icon: "pi pi-fw pi-id-card",
          to: "/scolarite/suivi-mandataires",
        },
        hasAccess(["SCOLARITE", "RECEPTIONNISTE", "ADMIN"]) && {
          label: "Réception BAC",
          icon: "pi pi-fw pi-inbox",
          to: "/scolarite/reception-candidats",
        },
        hasAccess(["RECEPTIONNISTE"]) && {
          label: "Réception CGS",
          icon: "pi pi-fw pi-sort-numeric-up-alt",
          to: "/scolarite/suivi-concours-general",
        },
        hasAccess(["SCOLARITE", "ADMIN"]) && {
          label: "Concours Général",
          icon: "pi pi-fw pi-sort-numeric-up-alt",
          items: [
            {
              label: "Statistiques",
              to: "/scolarite/suivi-concours-general/tableau-de-bord",
            },
            {
              label: "Suivi des candidatures",
              to: "/scolarite/suivi-concours-general",
            },
            {
              label: "Programmation des centres",
              to: "/scolarite/suivi-concours-general/programmation-centres",
            },
            {
              label: "Répartition et liste des candidats",
              to: "/scolarite/suivi-concours-general/repartition-des-candidats",
            },
          ],
        },
        hasAccess(["SCOLARITE", "RECEPTIONNISTE", "ADMIN"]) && {
          label: "Voir receptionniste",
          icon: "pi pi-fw pi-eye",
          to: "/scolarite/obtenir-receptionniste-by-etab",
        },
        hasAccess(["RECEPTIONNISTE"]) && {
          label: "Listing des receptionnés",
          icon: "pi pi-fw pi-file-import",
          to: "/scolarite/listing-deja-receptionner",
        },
        hasAccess(["SCOLARITE"]) && {
          label: "Etablissement",
          icon: "pi pi-fw pi-align-justify",
          to: "/scolarite/etablissement-scolaire",
        },
      ].filter(Boolean),
    },
    hasAccess(["AGENT_DE_SAISIE"]) && {
      label: "Etablissement",
      icon: "pi pi-home",
      items: [
        {
          label: "Versements",
          icon: "pi pi-fw pi-ticket",
          items: [
            {
              label: "Droits de dossier 1000 F",
              to: "/scolarite/droits-dossier-1000",
            },
            {
              label: "Droits d'inscription",
              to: "/scolarite/droits-de-dossiers",
            },
          ],
        },
        {
          label: "Sujet de soutenance",
          icon: "pi pi-fw pi-book",
          to: "/scolarite/sujet-soutenance",
        },
        {
          label: "Enrôlement candidat",
          icon: "pi pi-fw pi-file-edit",
          to: "/scolarite/enrolement-candidat",
        },
        {
          label: "Affectation des sujets",
          icon: "pi pi-fw pi-arrow-right-arrow-left",
          to: "/scolarite/affectation-sujet-candidats",
        },
        {
          label: "Convocations",
          icon: "pi pi-copy",
          to: "/convocations/etablissements",
        },
        { separator: true },
        {
          label: "Concours Général",
          icon: "pi pi-flag-fill",
          to: "/scolarite/enrolement-concours-general",
        },
      ].filter(Boolean),
    },
    hasAccess(["FINANCE_COMPTA"]) && {
      label: "Finance & Compta",
      icon: "pi pi-home",
      items: [
        {
          label: "Paiement FAEB3",
          icon: "pi pi-fw pi-money-bill",
          to: "/editions-systeme/vignettes-numeriques-FAEB3",
        },
        {
          label: "Vignettes & Coupons",
          icon: "pi pi-fw pi-ticket",
          to: "/scolarite/vignettes-coupons",
        },
      ].filter(Boolean),
    },
  ].filter(Boolean) as MenuModal[];

  // Ajout de l'élément Espace Candidat (toujours visible) - Version corrigée sans 'command'
  const candidatMenuItem: MenuModal = {
    label: "Espace Candidat",
    icon: "pi pi-user",
    // Utiliser un élément avec 'to' pour éviter l'erreur
    items: [
      {
        label: "Accéder à mon espace",
        icon: "pi pi-fw pi-sign-in",
        // Au lieu de 'command', on utilise une fonction onClick dans le composant parent
      },
      {
        label: "Consulter mes résultats",
        icon: "pi pi-fw pi-chart-line",
      },
      {
        label: "Télécharger mes convocations",
        icon: "pi pi-fw pi-download",
      },
    ],
  };

  // Ajouter l'élément candidat au début du menu
  const finalModel = [candidatMenuItem, ...model];

  // Fonction pour gérer le clic sur le menu
  const handleMenuItemClick = (event: React.MouseEvent, item: any) => {
    if (
      item.label === "Espace Candidat" ||
      item.label === "Accéder à mon espace" ||
      item.label === "Consulter mes résultats" ||
      item.label === "Télécharger mes convocations"
    ) {
      event.preventDefault();
      setCandidatDialogVisible(true);
    }
  };

  return (
    <>
      <Toast ref={toastRef} />
      <div onClick={(e) => handleMenuItemClick(e, {})}>
        <AppSubMenu model={finalModel} />
      </div>
      <CandidatAccessDialog
        visible={candidatDialogVisible}
        onHide={() => setCandidatDialogVisible(false)}
        toastRef={toastRef}
      />
    </>
  );
};

export default AppMenu;
