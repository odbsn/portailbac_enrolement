// src/components/ConvocationButton.tsx
"use client";

import React from "react";
import { Button } from "primereact/button";

interface ConvocationButtonProps {
  numeroTable: string;
  label?: string;
  icon?: string;
  className?: string;
  circular?: boolean;
}

// ⚠️ Temporairement désactivé : la génération de convocation n'est pas
// encore à jour côté établissement. À réactiver (restaurer generatePdf/
// handleGenerate/onClick) quand ce sera prêt.
export default function ConvocationButton({
  label = "PDF",
  icon = "pi pi-file-pdf",
  className = "",
  circular = true,
}: ConvocationButtonProps) {
  if (circular) {
    return (
      <Button
        icon={icon}
        label={label}
        disabled
        severity="secondary"
        rounded
        text
        tooltip="Téléchargement temporairement indisponible (mise à jour en cours)"
        tooltipOptions={{ position: "bottom" }}
        className={`p-0 border-circle border-2 flex items-center justify-center bg-gray-100 border-gray-400 ${className}`}
        style={{ width: "2.4rem", height: "2.4rem", minWidth: "2.4rem" }}
      />
    );
  }

  return (
    <Button
      label={label}
      icon={icon}
      disabled
      severity="secondary"
      tooltip="Téléchargement temporairement indisponible (mise à jour en cours)"
      tooltipOptions={{ position: "bottom" }}
      className={className}
    />
  );
}
