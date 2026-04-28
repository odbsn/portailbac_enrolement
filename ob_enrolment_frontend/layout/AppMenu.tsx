import { MenuModal } from "@/types/layout";
import AppSubMenu from "./AppSubMenu";
import { useContext } from "react";
import { UserContext } from "@/app/userContext";

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

const AppMenu = () => {
  const { user } = useContext(UserContext);

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
      //label: 'Dashboards',
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
          // items :
          // [
          //     {
          //         label: 'Profils',
          //         // icon: 'pi pi-fw pi-tags',
          //         to: '/editions-systeme/profils'
          //     },
          //     {
          //         label: 'Accés',
          //         // icon: 'pi pi-fw pi-tags',
          //         to: '/editions-systeme/acces'
          //     }
          // ]
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
              // icon: 'pi pi-fw pi-tags',
              to: "/editions-systeme/localisation/region",
            },
            {
              label: "Département",
              // icon: 'pi pi-fw pi-tags',
              to: "/editions-systeme/localisation/departement",
            },
            {
              label: "Ville",
              // icon: 'pi pi-fw pi-tags',
              to: "/editions-systeme/localisation/ville",
            },
            {
              label: "Centre Etat Civil",
              // icon: 'pi pi-fw pi-tags',
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
              // icon: 'pi pi-fw pi-tags',
              to: "/editions-systeme/structures/etablissement-scolaire",
            },
            {
              label: "Inspection Académie",
              // icon: 'pi pi-fw pi-tags',
              to: "/editions-systeme/structures/inspection-academie",
            },
            // {
            //     label: 'Université',
            //     // icon: 'pi pi-fw pi-tags',
            //     to: '/editions-systeme/structures/universite'
            // },
          ],
        },
        {
          label: "Archives candidats",
          icon: "pi pi-book",
          to: "/editions-systeme/archives-candidats",
        },
        // {
        //     label: 'Statistiques nationales',
        //     icon: 'pi pi-chart-bar',
        //     to: '/editions-systeme/statistiques-nationales'
        // }
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
              // icon: 'pi pi-fw pi-tags',
              to: "/scolarite/suivi-concours-general/tableau-de-bord",
            },
            {
              label: "Suivi des candidatures",
              // icon: 'pi pi-fw pi-tags',
              to: "/scolarite/suivi-concours-general",
            },
            {
              label: "Programmation des centres",
              // icon: 'pi pi-fw pi-tags',
              to: "/scolarite/suivi-concours-general/programmation-centres",
            },
            {
              label: "Répartition et liste des candidats",
              // icon: 'pi pi-fw pi-tags',
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
              // icon: 'pi pi-fw pi-tags',
              to: "/scolarite/droits-dossier-1000",
            },
            {
              label: "Droits d'inscription",
              // icon: 'pi pi-fw pi-tags',
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

  return (
    // <ProtectedRoute allowedRoles={['ADMIN', 'AGENT_DE_SAISIE', 'SCOLARITE', 'CHEF_ETABLISSEMENT']}>
    //   <AppSubMenu model={model} />
    // </ProtectedRoute>
    <AppSubMenu model={model} />
  );
};

export default AppMenu;
