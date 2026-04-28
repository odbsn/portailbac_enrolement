// app/convocations/etablissements/allConvocations/page.tsx
"use client";

import React, { useEffect, useRef, useState } from "react";
import { Button } from "primereact/button";
import { ProgressSpinner } from "primereact/progressspinner";
import { Toast } from "primereact/toast";
import { useSearchParams, useRouter } from "next/navigation";
import {
  useCandidatEtabStore,
  CandidatFinis,
} from "../../convocationEtabStore";
import ConvocationMassivePrint, {
  ConvocationMassivePrintRef,
} from "./ConvocationMassivePrint";
import "../../styles.css";

export default function AllConvocationsPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const toast = useRef<Toast>(null);
  const printRef = useRef<ConvocationMassivePrintRef>(null);
  const serie = searchParams.get("serie");

  const [candidats, setCandidats] = useState<CandidatFinis[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isGeneratingPDF, setIsGeneratingPDF] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { fetchAllCandidatsBySerie } = useCandidatEtabStore();

  useEffect(() => {
    if (!serie) {
      setError("Aucune série sélectionnée");
      setIsLoading(false);
      return;
    }

    const loadCandidats = async () => {
      setIsLoading(true);
      try {
        await fetchAllCandidatsBySerie(serie === "all" ? undefined : serie);
        const { candidats } = useCandidatEtabStore.getState();

        if (candidats.length === 0) {
          setError(
            `Aucun candidat trouvé pour ${
              serie === "all" ? "toutes les séries" : `la série ${serie}`
            }`,
          );
        } else {
          setCandidats(candidats);
        }
      } catch (err) {
        setError("Erreur lors du chargement des candidats");
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    loadCandidats();
  }, [serie, fetchAllCandidatsBySerie]);

  const handlePrint = () => {
    printRef.current?.print();
  };

  const handleDownloadPDF = async () => {
    setIsGeneratingPDF(true);
    try {
      await printRef.current?.downloadPDF();
      toast.current?.show({
        severity: "success",
        summary: "Succès",
        detail: "PDF généré avec succès",
        life: 3000,
      });
    } catch (error) {
      toast.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Erreur lors de la génération du PDF",
        life: 5000,
      });
    } finally {
      setIsGeneratingPDF(false);
    }
  };

  const handleBack = () => {
    router.back();
  };

  if (isLoading) {
    return (
      <div
        className="flex flex-column justify-content-center align-items-center p-6"
        style={{ minHeight: "400px" }}
      >
        <ProgressSpinner />
        <span className="mt-4 text-color-secondary">
          Chargement des convocations pour{" "}
          {serie === "all" ? "toutes les séries" : `la série ${serie}`}...
        </span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card flex flex-column align-items-center p-6">
        <i className="pi pi-exclamation-triangle text-5xl text-orange-500" />
        <h4 className="mt-3 mb-2">Erreur</h4>
        <p className="text-color-secondary text-center">{error}</p>
        <Button
          label="Retour"
          icon="pi pi-arrow-left"
          severity="secondary"
          onClick={handleBack}
        />
      </div>
    );
  }

  return (
    <>
      <Toast ref={toast} />

      <div
        className="fixed top-0 left-0 right-0 bg-white shadow-2 p-3 flex justify-content-between align-items-center no-print"
        style={{ zIndex: 1000, flexWrap: "wrap", gap: "10px" }}
      >
        <div className="flex align-items-center flex-wrap gap-2">
          <Button
            label="Retour"
            icon="pi pi-arrow-left"
            text
            onClick={handleBack}
            severity="secondary"
          />
          <span className="ml-3 font-semibold text-lg">
            Convocation
            {serie !== "all" ? `s - Série ${serie}` : "s - Toutes séries"}
            <span className="ml-2 text-primary">
              ({candidats.length} candidat{candidats.length > 1 ? "s" : ""})
            </span>
          </span>
        </div>
        <div className="flex gap-2">
          <Button
            label="Imprimer"
            icon="pi pi-print"
            severity="success"
            onClick={handlePrint}
          />
          <Button
            label="Télécharger PDF"
            icon="pi pi-download"
            severity="info"
            onClick={handleDownloadPDF}
            loading={isGeneratingPDF}
          />
        </div>
      </div>

      <div className="pt-3">
        <ConvocationMassivePrint
          ref={printRef}
          candidats={candidats}
          serie={serie === "all" ? undefined : serie}
        />
      </div>

      <style jsx global>{`
        @media print {
          .no-print {
            display: none !important;
          }
          body {
            margin: 0;
            padding: 0;
          }
        }
      `}</style>
    </>
  );
}
