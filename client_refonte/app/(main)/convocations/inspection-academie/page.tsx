"use client";

import React, { useRef } from "react";
import { Toast } from "primereact/toast";
import ProtectedRoute from "@/layout/ProtectedRoute";
import "./styles.css";
import { CandidatFinis } from "../convocationStore";
import CandidatInspectionAcademieList from "./CandidatInspectionAcademieList";

export default function AdminCandidatsInspectionAcademiePage() {
  const toast = useRef<Toast>(null);

  const handleViewCandidat = (candidat: CandidatFinis) => {
    console.log("View candidat:", candidat);
  };

  const handlePrintConvocation = (candidat: CandidatFinis) => {
    toast.current?.show({
      severity: "success",
      summary: "Impression",
      detail: `Impression de la convocation pour ${candidat.prenoms} ${candidat.nom}`,
      life: 3000,
    });
  };
  return (
    <ProtectedRoute allowedRoles={["INSPECTEUR_ACADEMIE"]}>
      <div className="candidats-page-fullwidth">
        <Toast ref={toast} />
        <CandidatInspectionAcademieList
          onViewCandidat={handleViewCandidat}
          onPrintConvocation={handlePrintConvocation}
        />
      </div>
    </ProtectedRoute>
  );
}