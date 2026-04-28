// src/components/RegenerateConvocationButton.tsx
"use client";

import React from "react";
import { Button } from "primereact/button";
import { useCandidatStore } from "../convocationStore";

interface RegenerateConvocationButtonProps {
  numeroTable: string;
  label?: string;
  icon?: string;
  className?: string;
}

export default function RegenerateConvocationButton({ 
  numeroTable, 
  label = "", 
  icon = "pi pi-refresh",
  className = ""
}: RegenerateConvocationButtonProps) {
  const { regenerateConvocation, isRegenerating } = useCandidatStore();

  const handleRegenerate = async () => {
    try {
      await regenerateConvocation(numeroTable);
    } catch (err) {
      console.error('Erreur lors de la régénération du PDF:', err);
    }
  };

  return (
    <Button
      icon={icon}
      label={label}
      onClick={handleRegenerate}
      loading={isRegenerating}
      disabled={isRegenerating}
      severity="warning"
      rounded
      text
      tooltip="Régénérer la convocation"
      className="p-0 border-circle border-2 flex items-center justify-center bg-orange-100 border-orange-500"
      style={{ width: "2.4rem", height: "2.4rem", minWidth: "2.4rem" }}
    />
  );
}