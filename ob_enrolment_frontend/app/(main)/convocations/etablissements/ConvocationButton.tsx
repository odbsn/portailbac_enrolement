// src/components/ConvocationButton.tsx
"use client";

import React from "react";
import { Button } from "primereact/button";
import { useCandidatStore } from "../candidatStore";
interface ConvocationButtonProps {
  numeroTable: string;
  label?: string;
  icon?: string;
  className?: string;
}
export default function ConvocationButton({
  numeroTable,
  label = "PDF",
  icon = "pi pi-file-pdf",
  className = "",
}: ConvocationButtonProps) {
  const { generatePdf, isGeneratingPdf } = useCandidatStore();

  const handleGenerate = async () => {
    try {
      const blob = await generatePdf(numeroTable);

      if (blob) {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = `convocation_${numeroTable}.pdf`;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
      }
    } catch (err) {
      console.error("Erreur lors de la génération du PDF:", err);
    }
  };

  return (
    <Button
      icon={icon}
      label={label}
      onClick={handleGenerate}
      loading={isGeneratingPdf}
      /* disabled={isGeneratingPdf} */
      // disabled={true}
      severity="success"
      rounded
      text
      tooltip="Télécharger la convocation"
      tooltipOptions={{ position: "bottom" }}
      className="p-0 border-circle border-2 flex items-center justify-center bg-green-100 border-green-500 "
      style={{ width: "2.4rem", height: "2.4rem", minWidth: "2.4rem" }}
    />
  );
}
