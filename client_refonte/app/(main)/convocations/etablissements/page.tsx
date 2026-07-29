"use client";

import React, { useRef } from "react";
import { Toast } from "primereact/toast";
import ProtectedRoute from "@/layout/ProtectedRoute";
import { CandidatFinis } from "../convocationStore";
import CandidatsTabTableAdmin from "./CandidatList";
import "../styles.css";

export default function AdminCandidatsPage() {
  const toast = useRef<Toast>(null);

  const handleViewCandidat = (candidat: CandidatFinis) => {
    console.log("View candidat:", candidat);
  };

  const handlePrintConvocation = (candidat: CandidatFinis) => {
    // Implémentez la logique d'impression ici
    toast.current?.show({
      severity: "success",
      summary: "Impression",
      detail: `Impression de la convocation pour ${candidat.prenoms} ${candidat.nom}`,
      life: 3000,
    });

    // Vous pouvez ouvrir une nouvelle fenêtre avec la convocation
    // window.open(`/api/convocation/${candidat.id}`, '_blank');
  };

  return (
    <ProtectedRoute allowedRoles={["AGENT_DE_SAISIE", "CHEF_ETABLISSEMENT"]}>
      <div className="candidats-page-fullwidth">
        <Toast ref={toast} />

        <CandidatsTabTableAdmin
          onViewCandidat={handleViewCandidat}
          onPrintConvocation={handlePrintConvocation}
        />
      </div>
    </ProtectedRoute>
  );
}
