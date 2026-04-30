"use client";

import React, {
  useRef,
  forwardRef,
  useImperativeHandle,
  useState,
  useEffect,
} from "react";
import {
  useCandidatEtabStore,
  CandidatFinis,
} from "../../convocationEtabStore";
import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import "../../styles.css";

interface ConvocationMassivePrintProps {
  candidats: CandidatFinis[];
  serie?: string;
}

export interface ConvocationMassivePrintRef {
  downloadPDF: () => Promise<void>;
  print: () => void;
}

const ConvocationMassivePrint = forwardRef<
  ConvocationMassivePrintRef,
  ConvocationMassivePrintProps
>(({ candidats, serie }, ref) => {
  const printRef = useRef<HTMLDivElement>(null);
  const [downloadProgress, setDownloadProgress] = useState<number>(0);
  const [isDownloading, setIsDownloading] = useState<boolean>(false);

  const formatDate = (dateString?: string) => {
    if (!dateString) return "-";

    try {
      let date: Date | null = null;

      // 1. Si c'est déjà un timestamp numérique
      if (typeof dateString === "number" || /^\d+$/.test(dateString)) {
        const timestamp = parseInt(dateString as string, 10);
        date = new Date(timestamp);
      }
      // 2. Format ISO: 2025-03-15 ou 2025-03-15T00:00:00.000Z
      else if (/^\d{4}-\d{2}-\d{2}/.test(dateString)) {
        date = new Date(dateString);
      }
      // 3. Format DD/MM/YYYY (français)
      else if (/^\d{2}\/\d{2}\/\d{4}/.test(dateString)) {
        const [day, month, year] = dateString.split("/");
        date = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
      }
      // 4. Format DD-MM-YYYY
      else if (/^\d{2}-\d{2}-\d{4}/.test(dateString)) {
        const [day, month, year] = dateString.split("-");
        date = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
      }
      // 5. Format avec heure: 15/03/2025 14:30
      else if (dateString.includes(" ") && dateString.includes("/")) {
        const [datePart, timePart] = dateString.split(" ");
        const [day, month, year] = datePart.split("/");
        const [hour, minute] = timePart.split(":");
        date = new Date(
          parseInt(year),
          parseInt(month) - 1,
          parseInt(day),
          parseInt(hour),
          parseInt(minute),
        );
      }
      // 6. Format DD MMM YYYY (ex: 15 Mar 2025)
      else {
        date = new Date(dateString);
      }

      // Vérifier si la date est valide
      if (!date || isNaN(date.getTime())) {
        console.warn("Date invalide:", dateString);
        return dateString;
      }

      // Formater la date sans le jour de la semaine
      let formattedDate = date.toLocaleDateString("fr-FR", {
        day: "2-digit",
        month: "long",
        year: "numeric",
      });

      // Mettre la première lettre du mois en majuscule
      // Solution plus robuste qui capitalise seulement la première lettre du mois
      const parts = formattedDate.split(" ");
      if (parts.length >= 2) {
        const month = parts[1];
        const capitalizedMonth = month.charAt(0).toUpperCase() + month.slice(1);
        parts[1] = capitalizedMonth;
        formattedDate = parts.join(" ");
      }

      return formattedDate;
    } catch (error) {
      console.error("Erreur formatage date:", error, "Valeur:", dateString);
      return dateString;
    }
  };
  const { jourEPS, fetchJourEPS, isLoading } = useCandidatEtabStore();
  useEffect(() => {
    fetchJourEPS();
  }, []);
  const formatDateFromJour = (jour?: any) => {
    if (!jour) return "-";
    return jour.name;
  };

  const formatHeure = (heure?: any) => {
    if (!heure) return "-";
    return heure.heure || "";
  };

  const getDominante = (estDominant?: boolean) => {
    if (estDominant !== undefined) {
      return estDominant ? "DOMINANTE" : "Non-dominante";
    }
    return "-";
  };

  const getAutorisation = (autorisation?: boolean) => {
    if (autorisation !== undefined) {
      return autorisation ? "OUI" : "NON";
    }
    return "-";
  };

  const hasMatieresOptionnelles = (candidat: CandidatFinis) => {
    return !!(
      candidat?.mo1 ||
      candidat?.mo2 ||
      candidat?.mo3 ||
      candidat?.ef1 ||
      candidat?.ef2
    );
  };

  const hasMatieresFacultatives = (candidat: CandidatFinis) => {
    return !!(candidat?.centreMatFac1 || candidat?.centreMatFac2);
  };

  const getMatieresOptionnelles = (candidat: CandidatFinis) => {
    const options = [];
    if (candidat?.mo1) options.push(`- ${candidat.mo1}`);
    if (candidat?.mo2) options.push(`- ${candidat.mo2}`);
    if (candidat?.mo3) options.push(`- ${candidat.mo3}`);
    if (candidat?.ef1) options.push(`- ${candidat.ef1}`);
    if (candidat?.ef2) options.push(`- ${candidat.ef2}`);
    return options;
  };

  const getMatieresFacultatives = (candidat: CandidatFinis) => {
    const facultatives = [];
    if (candidat?.centreMatFac1) {
      facultatives.push(
        `- ${candidat.libMatFac1 || "Matière 1"} : ${candidat.centreMatFac1}`,
      );
    }
    if (candidat?.centreMatFac2) {
      facultatives.push(
        `- ${candidat.libMatFac2 || "Matière 2"} : ${candidat.centreMatFac2}`,
      );
    }
    return facultatives.length > 0
      ? facultatives
      : ["Pas de matière optionnelle choisie"];
  };

  const downloadPDF = async () => {
    if (!printRef.current) return;

    setIsDownloading(true);
    setDownloadProgress(0);

    try {
      const pages = printRef.current.querySelectorAll(".page");
      const totalPages = pages.length;
      const pdf = new jsPDF({
        orientation: "portrait",
        unit: "mm",
        format: "a4",
      });

      for (let i = 0; i < totalPages; i++) {
        const page = pages[i] as HTMLElement;

        // Mettre à jour la progression
        const progress = Math.round(((i + 1) / totalPages) * 100);
        setDownloadProgress(progress);

        const canvas = await html2canvas(page, {
          scale: 2,
          logging: false,
          useCORS: true,
          backgroundColor: "#ffffff",
        });

        const imgData = canvas.toDataURL("image/png");
        const imgWidth = 210;
        const imgHeight = (canvas.height * imgWidth) / canvas.width;

        if (i > 0) {
          pdf.addPage();
        }

        pdf.addImage(imgData, "PNG", 0, 0, imgWidth, imgHeight);
      }

      const fileName = `convocations_${
        serie === "all" || !serie ? "toutes_series" : `serie_${serie}`
      }_${new Date().toISOString().slice(0, 19).replace(/:/g, "-")}.pdf`;
      pdf.save(fileName);

      setDownloadProgress(100);

      // Réinitialiser après un court délai
      setTimeout(() => {
        setIsDownloading(false);
        setDownloadProgress(0);
      }, 2000);

      return Promise.resolve();
    } catch (error) {
      console.error("Erreur lors de la génération du PDF:", error);
      setIsDownloading(false);
      setDownloadProgress(0);
      return Promise.reject(error);
    }
  };

  const handlePrint = () => {
    const printContent = printRef.current;
    if (!printContent) return;

    const originalTitle = document.title;
    document.title = "";

    const printWindow = window.open("", "_blank");
    if (printWindow) {
      printWindow.document.write(`
          <html>
            <head>
              <style>
              /* Supprime les en-têtes et pieds de page par défaut du navigateur */
            @page {
              margin: 0;
              size: A4;
            }
            
            /* Cache les informations d'URL et de date dans les marges */
            @page :first {
              margin: 0;
            }
            
            /* Pour Chrome/Edge - cache l'en-tête et le pied de page */
            @media print {
              @page {
                margin: 0cm 0cm 0cm 0cm;
              }
              
              /* Cache le numéro de page */
              .page-break, .page-number, .page-marker {
                display: none !important;
              }
              
              /* Supprime les marges par défaut */
              body {
                margin: 0 !important;
                padding: 0 !important;
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
              }
              
              /* Supprime le contenu des marges */
              html, body {
                margin: 0;
                padding: 0;
              }
            }
            
            /* Votre CSS existant */
            * {
              margin: 0;
              padding: 0;
              box-sizing: border-box;
            }
              * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
              }
              body {
                background: white;
                margin: 0;
                padding: 20px;
                color: #000;
                font-family: "Arial", sans-serif;
              }
              .flex {
                display: flex !important;
              }

              .flex-column {
                flex-direction: column !important;
              }

              .items-center {
                align-items: center !important;
              }

              .justify-content-between {
                justify-content: space-between !important;
              }

              .mt-3 {
                margin-top: 0.75rem !important;
              }

              .ml-4 {
                margin-left: 1rem !important;
              }

              .font-bold {
                font-weight: bold !important;
              }

              .underline {
                text-decoration: underline !important;
              }
              .page {
                width: 210mm;
                min-height: 295mm;
                background: white;
                padding: 10mm;
                font-size: 12px;
                margin: 0 auto;
                page-break-after: always;
                position: relative;
              }
              .page-marker {
                display: none;
              }
              .header {
                display: flex;
                justify-content: space-between;
                margin-bottom: 8px;
              }
              .header .right {
                text-align: right;
              }
              .leftTitle {
                text-align: center;
                margin-bottom: 0;
              }
              .title {
                text-align: center;
                margin-top: 5px;
                font-size: 14px;
                font-weight: bold;
                margin-bottom: 0;
              }
              .soustitle {
                font-size: 14px;
                font-weight: bold;
                margin-bottom: 0;
              }
              
              .rowTop {
                display: flex;
                justify-content: space-between;
                margin-top: 10px;
              }
              .convocation {
                font-style: italic;
                font-size: 18px;
              }
              .serie {
                border: 1px solid black;
                border-radius: 8px;
                width: 150px;
                text-align: center;
                justify-content: center;
                padding: 3px 10px;
              }
              .serie h6{
                font-size: 16px;
                text-align: center;
                justify-content: center;
                font-weight: bold;
              }
              .grid {
                display: grid;
                grid-template-columns: 30% 45% 25%;
                gap: 8px;
                margin-top: 8px;
              }
              /* Bordure uniquement pour LEFT */
              .bordered-left {
                border: 2px solid #000 !important;
                border-radius: 13px !important;
              }
              .field {
                border: 1px solid #000;
                text-align: center;
                border-radius: 12px;
                padding: 0 !important;
                margin: 0 !important;
              }
              .field p {
                padding: 4px 8px;
                margin: 0;
              }
              .contenu {
                text-align: center;
                font-weight: bold;
                padding: 4px;
              }
              .sexe-field {
                border: 1px solid #000;
                display: flex !important;
                justify-content: space-between;
                padding: 4px !important;
              }
              .sectionDouble {
                display: flex !important;
                justify-content: space-between;
                padding: 4px !important;
                gap: 15px;
              }
              .sectionDouble .epSection p{
                font-weight: bold;
                font-size: 13px;
              }
              .sectionDouble .epSection{
                width: 55%;
                display: block;
                padding: 4px 10px;
                border-top: 1px solid #000;
                border-bottom: 1px solid #000;
                border-radius: 8px;
              }
              .sectionDouble .epSection .sect{
                display: block !important;    
                line-height: 1.2rem;
                margin-left: 7px;            
              }
              .sectionDouble .infoSection{
                width: 45%;
                padding: 4px 10px;
                border-top: 1px solid #000;
                border-bottom: 1px solid #000;
                border-radius: 8px;
              }
              .sexe-field p {
                padding: 8px;
                margin: 0;
              }
              .sexeNationalite {
                display: flex;
                justify-content: space-between;
                padding: 4px 8px;
                font-weight: bold;
              }
              .note {
                padding: 5px;
                margin-top: 20px;
                font-weight: bold;
                margin-left: 10px;
              }
              .rightBox {
                padding: 5px;
                font-size: 11px;
                text-align: justify;
              }
              .rightBox .flex.flex-column {
                display: flex;
                flex-direction: column;
                gap: 8px;  
              }
              .importantTitle {
                text-align: center;
                font-weight: bold;
                margin-bottom: 5px;
              }
              .textImportant{
                font-size: 11px;
                line-height: 1.2;
                display: block;
                  
              }
              .middleSection {
                margin-top: 10px;
              }
              .rappel {
                margin-top: 6px;
                margin-bottom: 6px;
                font-size: 15px;
                font-weight: bold;
                text-align: justify;
              }
              .secondGroup {
                margin-top: 6px;
                margin-bottom: 6px;
                font-size: 13px;
                font-weight: bold;
                text-align: justify;
              }
              .planning {
                font-weight: bold;
                margin-bottom: 6px;
                font-size: 14px;
              }
              table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 10px;
              }
              th, td {
                border: 0.3px solid #000;
                padding: 4px;
                font-size: 12px;
              }
              th {
                background: #e5e5e5;
              }
              .footer {
                margin-top: 5px;
                text-align: right;
              }
              @media print {
                body {
                  padding: 0;
                }
                .page {
                  page-break-after: always;
                  page-break-inside: avoid;
                }
                .page-marker {
                  display: none;
                }
                .bordered-left {
                  border: 1px solid #000 !important;
                  print-color-adjust: exact;
                  -webkit-print-color-adjust: exact;
                }
              }
            </style>
            </head>
            <body>
              ${printContent.outerHTML}
            </body>
          </html>
        `);
      printWindow.document.close();
      printWindow.print();
      printWindow.close();
    }

    document.title = originalTitle;
  };

  useImperativeHandle(ref, () => ({
    downloadPDF,
    print: handlePrint,
  }));

  const renderConvocation = (candidat: CandidatFinis, index: number) => (
    <div
      key={candidat.id || index}
      className="page"
      style={{
        pageBreakAfter: index < candidats.length - 1 ? "always" : "auto",
        position: "relative",
      }}
    >
      {/* HEADER */}
      <div className="header">
        <div className="leftTitle" style={{ textAlign: "center" }}>
          UNIVERSITE CHEIKH ANTA DIOP
          <br />
          DE DAKAR
          <div
            style={{
              borderTop: "2px dashed #000",
              width: "70px",
              margin: "6px auto",
              opacity: 0.8,
            }}
          />
          <b>OFFICE DU BACCALAUREAT</b>
        </div>
        <div className="right">
          REPUBLIQUE DU SENEGAL
          <br />
          Un Peuple - Un But - Une Foi
        </div>
      </div>

      <div className="title">
        <h6 className="title">
          BACCALAUREAT DE L&apos;ENSEIGNEMENT SECONDAIRE
        </h6>
        <h6 className="p-0 m-0 font-bold title">SESSION NORMALE 2025</h6>
      </div>

      <div className="rowTop">
        <div className="convocation underline font-bold">CONVOCATION</div>
        {/* <div className="font-bold">
          <h6 className="p-0 m-0 soustitre">SESSION NORMALE 2025</h6>
        </div> */}
        <span className="serie w-3">
          <h6 className="p-0 m-0">Série : {candidat.serie || "-"}</h6>
        </span>
      </div>

      {/* GRID */}
      <div className="grid h-auto py-0 bordered-container">
        {/* LEFT */}
        <div className="left h-auto bordered-left">
          <div>
            <div className="field">
              <p>Prénom(s)</p>
            </div>
            <div className="contenu">
              <span>{candidat.prenoms || "-"}</span>
            </div>
          </div>
          <div>
            <div className="field">
              <p>Nom</p>
            </div>
            <div className="contenu">
              <span>{candidat.nom || "-"}</span>
            </div>
          </div>
          <div>
            <div className="field">
              <p>Date de Naissance</p>
            </div>
            <div className="contenu">
              <span>{formatDate(candidat.dateNaissance)}</span>
            </div>
          </div>
          <div>
            <div className="field">
              <p>Lieu de Naissance</p>
            </div>
            <div className="contenu">
              <span>{candidat.lieuNaissance || "-"}</span>
            </div>
          </div>
          <div>
            <div className="field">
              <p>Etablissement fréquenté</p>
            </div>
            <div className="contenu">
              <span>{candidat.etablissement?.name || "-"}</span>
            </div>
          </div>
          <div>
            <div className="field">
              <p>Candidat</p>
            </div>
            <div className="contenu">
              <span>{candidat.typeCandidat || "-"}</span>
            </div>
          </div>
          <div>
            <div>
              <div className="field sexe-field">
                <div className="sexe-header">Sexe</div>
                <div className="nationalite-header">Nationalité</div>
              </div>
              <div className="sexeNationalite px-4 py-1 mb-0">
                <span>{candidat.sexe || "-"}</span>
                <span>{candidat.nationalite || "-"}</span>
              </div>
            </div>
          </div>
        </div>

        {/* CENTER */}
        <div className="center">
          <div className="bg-white shadow-lg rounded-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead className="bg-slate-100">
                  <tr>
                    <th className="p-1 text-center font-bold text-xs rounded-tl-lg">
                      N° Jury
                    </th>
                    <th className="w-2 p-1 text-center font-bold text-xs">
                      N° table
                    </th>
                    <th className="p-1 text-center font-bold text-xs">
                      Centre d&apos;écrit
                    </th>
                    <th className="p-1 text-center font-bold text-xs">
                      N° Bât.
                    </th>
                    <th className="p-1 text-center font-bold text-xs rounded-tr-lg">
                      N° Salle
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td className="p-1 text-center font-semibold rounded-bl-lg">
                      {candidat.jury || "-"}
                    </td>
                    <td className="p-1 text-center font-semibold">
                      {candidat.numeroTable || "-"}
                    </td>
                    <td className="p-1 text-center font-semibold text-sm">
                      {candidat.centreEcritParticulier ||
                        candidat.centreEcrit?.name ||
                        "-"}
                    </td>
                    <td className="p-1 text-center">-</td>
                    <td className="p-1 text-center rounded-br-lg">-</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          {/* Première ligne */}
          <div className="flex items-center justify-content-between mt-3">
            {/* À gauche: Optionnelles si existent, sinon Facultatives */}
            <div className="flex flex-column">
              {hasMatieresOptionnelles(candidat) ? (
                <>
                  <span className="font-bold underline">
                    Matière(s) Optionnelle(s):
                  </span>
                  <div className="flex flex-column ml-4">
                    {getMatieresOptionnelles(candidat).map((opt, idx) => (
                      <span key={idx}>{opt}</span>
                    ))}
                  </div>
                </>
              ) : (
                <>
                  <span className="font-bold underline">
                    Matière(s) facultative(s):
                  </span>
                  <div className="flex flex-column ml-4">
                    {hasMatieresFacultatives(candidat) ? (
                      getMatieresFacultatives(candidat).map((fac, idx) => (
                        <span key={idx}>{fac}</span>
                      ))
                    ) : (
                      <span>- Aucune matière facultative choisie</span>
                    )}
                  </div>
                </>
              )}
            </div>

            {/* EPS à droite */}
            <div className="flex flex-column">
              <span className="font-bold underline">E.P.S :</span>
              {candidat.eps === "A"
                ? "Apte"
                : candidat.eps === "I"
                ? "Inapte"
                : candidat.eps || "-"}
            </div>
          </div>

          {/* Deuxième ligne: Matières facultatives (uniquement si des optionnelles existent) */}
          {hasMatieresOptionnelles(candidat) && (
            <div className="flex items-center justify-content-between mt-3">
              <div className="flex flex-column">
                <span className="font-bold underline">
                  Matière(s) facultative(s):
                </span>
                <div className="flex flex-column ml-4">
                  {hasMatieresFacultatives(candidat) ? (
                    getMatieresFacultatives(candidat).map((fac, idx) => (
                      <span key={idx}>{fac}</span>
                    ))
                  ) : (
                    <span>- Aucune matière facultative choisie</span>
                  )}
                </div>
              </div>
            </div>
          )}

          <div className="note">
            N.B : Toute information vous paraissant erronée doit être signalée
            au plus tard le Ven. 30 Mai 2025
          </div>
        </div>

        {/* RIGHT */}
        <div className="rightBox">
          <div className="importantTitle">TRES IMPORTANT</div>
          <div
            className="flex flex-column px-3 textImportant"
            style={{ fontSize: "11px", lineHeight: "0.8rem" }}
          >
            <span>Pendant toute la durée de la session, vous devez :</span>
            <span>
              - Être en salle, muni de cette convocation et de votre pièce
              d&apos;identité le matin à 7h15 et l&apos;après-midi à 14h15.
              Aucun retardataire ne sera admis en salle.
            </span>
            <span>
              - Retirer auprès du Président de jury votre relevé de notes, qui
              est indispensable pour le choix des épreuves du 2ème groupe. Ce
              choix doit se faire dans la demi journée qui suit la proclamation
              des résultats.
            </span>
            <span>
              - Retirer votre diplôme à l&apos;Inspection d&apos;Académie de
              votre région ou à l&apos;Office du Baccalauréat à partir
              d&apos;une date qui sera communiquée après l&apos;examen.
            </span>
          </div>
        </div>
      </div>

      <div className="middleSection">
        <div className="rappel">
          <p className="rappel">
            Rappel: Le téléphone portable et autres appareils assimilés sont
            formellement interdits dans les centres d&apos;examen. Tout
            contrevenant sera exclu de l&apos;ensemble de l&apos;examen et
            traduit devant le Conseil de Discipline.
          </p>
        </div>
        <div className="flex gap-2 mb-0 sectionDouble">
          <div className="epSection flex flex-column md:col-7 border-top-1 border-bottom-1 border-400 border-round-bottom-md border-round-top-md ml-2">
            <p className="eps">
              Epreuves d&apos;Education Physique et Sportive
            </p>
            <span className="ml-2 sect">
              A partir du :{" "}
              {jourEPS?.date ? formatDate(jourEPS.date) : "date à définir"}
            </span>
            <span className="ml-2 sect">
              Centre : {candidat.centreActEPS?.name || "-"}
            </span>
          </div>
          <div
            className="md:col-5 infoSection border-top-1 font-medium border-bottom-1 border-400 border-round-bottom-md border-round-top-md text-start"
            style={{ fontFamily: "Lucida Calligraphy" }}
          >
            Veuillez prendre attache avec ledit centre pour les détails et
            précisions concernant le déroulement de ces épreuves d&apos;EPS
          </div>
        </div>
        <div className="secondGroup">
          En cas d&apos;admissibilité au Second (2nd) groupe d&apos;épreuves,
          vous aurez à choisir trois (03) matières pour la recomposition dont{" "}
          <span className="font-bold underline">
            deux (02) dominantes et une (01) non-dominante
          </span>
          . Se référer à la colonne dénommée{" "}
          <b>&quot;Matières autorisées au 2nd groupe d&apos;épreuves&quot;</b>{" "}
          du planning
        </div>
      </div>

      {/* TABLEAU DES ÉPREUVES */}
      <h5 className="pt-0 mt-1 mb-0 pb-0 planning" style={{ fontSize: "14px" }}>
        <span className="underline">
          Planning de déroulement de l&apos;examen
        </span>{" "}
        : Série {candidat.serie || "-"}
      </h5>

      <table
        style={{
          marginTop: "0.2rem",
          borderCollapse: "collapse",
          width: "100%",
        }}
      >
        <thead>
          <tr>
            <th
              style={{
                border: "0.3px solid #000",
                padding: "4px",
                fontSize: "12px",
                background: "#e5e5e5",
              }}
            >
              Matière de l&apos;épreuve
            </th>
            <th
              style={{
                border: "0.3px solid #000",
                padding: "4px",
                fontSize: "12px",
                background: "#e5e5e5",
              }}
            >
              Date
            </th>
            <th
              style={{
                border: "0.3px solid #000",
                padding: "4px",
                fontSize: "12px",
                background: "#e5e5e5",
              }}
            >
              Heure
            </th>
            <th
              style={{
                border: "0.3px solid #000",
                padding: "4px",
                fontSize: "12px",
                background: "#e5e5e5",
              }}
            >
              Durée
            </th>
            <th
              style={{
                border: "0.3px solid #000",
                padding: "4px",
                fontSize: "12px",
                background: "#e5e5e5",
              }}
            >
              Coef.
            </th>
            <th
              style={{
                border: "0.3px solid #000",
                padding: "4px",
                fontSize: "12px",
                background: "#e5e5e5",
              }}
            >
              Nature
            </th>
            <th
              style={{
                border: "0.3px solid #000",
                padding: "4px",
                fontSize: "12px",
                background: "#e5e5e5",
              }}
            >
              2nd groupe
            </th>
          </tr>
        </thead>
        <tbody>
          {candidat.epreuves && candidat.epreuves.length > 0 ? (
            candidat.epreuves.map((epreuve, idx) => (
              <tr
                key={idx}
                style={{ backgroundColor: idx % 2 === 0 ? "#fff" : "#f3f4f6" }}
              >
                <td
                  style={{
                    border: "0.3px solid #ccc",
                    padding: "4px",
                    fontSize: "12px",
                  }}
                >
                  {epreuve.estDominant ? (
                    <strong>{epreuve.matiere?.name || "-"}</strong>
                  ) : (
                    epreuve.matiere?.name || "-"
                  )}
                </td>
                <td
                  style={{
                    border: "0.3px solid #000",
                    padding: "4px",
                    fontSize: "12px",
                  }}
                >
                  {formatDateFromJour(epreuve.jourDebut)}
                </td>
                <td
                  style={{
                    border: "0.3px solid #000",
                    padding: "4px",
                    fontSize: "12px",
                  }}
                >
                  {formatHeure(epreuve.heureDebut)}
                </td>
                <td
                  style={{
                    border: "0.3px solid #000",
                    padding: "4px",
                    fontSize: "12px",
                  }}
                >
                  {epreuve.duree || "-"}
                </td>
                <td
                  style={{
                    border: "0.3px solid #000",
                    padding: "4px",
                    fontSize: "12px",
                  }}
                >
                  {epreuve.coefficient?.toString() || "-"}
                </td>
                <td
                  style={{
                    border: "0.3px solid #000",
                    padding: "4px",
                    fontSize: "12px",
                  }}
                >
                  <strong>{epreuve.type}</strong>
                </td>
                <td
                  style={{
                    border: "0.3px solid #ccc",
                    padding: "4px",
                    fontSize: "12px",
                    textAlign: "center",
                  }}
                >
                  {epreuve.type === "Écrit" || epreuve.type === "Ecrit" ? (
                    <strong>{`${getAutorisation(
                      epreuve.autorisation,
                    )} / ${getDominante(epreuve.estDominant)}`}</strong>
                  ) : (
                    "===NON==="
                  )}
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={7} style={{ textAlign: "center", padding: "20px" }}>
                Aucune épreuve trouvée
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <div className="footer">
        {candidat.etablissement.ville.name}, le{" "}
        {formatDate(new Date().toISOString())}
      </div>

      {/* Indicateur de fin de page */}
      {/* <div className="page-marker page-end">
        <div className="marker-content">
          <i className="pi pi-arrow-up"></i> PAGE {index + 1}/{candidats.length}
          <i className="pi pi-arrow-up"></i>
        </div>
      </div> */}
    </div>
  );

  return (
    <div ref={printRef} className="convocations-massive-print">
      {candidats.map((candidat, index) => renderConvocation(candidat, index))}

      <style jsx global>{`
        .convocations-massive-print {
          background: #f0f2f5;
          padding: 20px;
        }

        .convocations-massive-print .page {
          width: 100%;
          max-width: 210mm;
          min-height: 295mm;
          background: white;
          padding: 10mm !important;
          font-size: 12px;
          color: #000;
          margin: 0 auto 20px auto !important;
          font-family: "Arial", sans-serif;
          page-break-after: always;
          page-break-inside: avoid;
          position: relative;
        }

        /* Bordure uniquement autour de la section LEFT */
        .convocations-massive-print .bordered-left {
          border: 1px solid #000 !important;
          border-radius: 13px !important;
        }

        

  }

        /* HEADER */
        .convocations-massive-print .header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 10px;
        }

        .convocations-massive-print .header .right {
          text-align: right;
        }

        /* TITRES */
        .convocations-massive-print .title {
          text-align: center;
          margin-top: 10px;
        }

        .convocations-massive-print .rowTop {
          display: flex;
          justify-content: space-between;
          margin-top: 10px;
        }

        .convocations-massive-print .convocation {
          font-style: italic;
          font-size: 18px;
        }

        .convocations-massive-print .serie {
          border: 1px solid black;
          border-radius: 8px;
          text-align: center;
          font-weight: bold;
          padding: 3px 10px;
        }

        /* GRID */
        .convocations-massive-print .grid {
          display: grid;
          grid-template-columns: 30% 45% 25%;
          gap: 8px;
          margin-top: 8px;
        }

        .convocations-massive-print .field {
          border: 0.3px solid #000;
          border-radius: 12px;
          padding: 0 !important;
          margin: 0 !important;
        }

        .convocations-massive-print .field p {
          padding: 4px 8px;
          margin: 0;
        }

        .convocations-massive-print .contenu {
          text-align: center;
          font-weight: bold;
          padding: 4px;
        }

        .convocations-massive-print .sexe {
          border: 0.3px solid #aaa;
          display: flex;
          justify-content: space-between !important;
        }

        .convocations-massive-print .sexe p {
          padding: 4px 8px;
          margin: 0;
        }

        .convocations-massive-print .sexeNationalite {
          display: flex;
          justify-content: space-between;
          padding: 4px 8px;
          font-weight: bold;
        }

        .convocations-massive-print .note {
          padding: 5px;
          margin-top: 20px;
          font-weight: bold;
          margin-left: 10px;
        }

        .convocations-massive-print .importantTitle {
          text-align: center;
          font-weight: bold;
          margin-bottom: 5px;
        }

        .convocations-massive-print .middleSection {
          margin-top: 10px;
        }

        .convocations-massive-print .rappel {
          margin-top: 5px;
          font-size: 11px;
        }


        .convocations-massive-print .secondGroup {
          margin-top: 6px;
          margin-bottom: 6px;
          font-weight: bold;
          font-size: 13px;
          text-align: justify;
        }

        .convocations-massive-print table {
          width: 100%;
          border-collapse: collapse;
          margin-top: 10px;
        }

        .convocations-massive-print th,
        .convocations-massive-print td {
          border: 0.3px solid #000;
          padding: 4px;
          font-size: 12px;
        }

        .convocations-massive-print th {
          background: #e5e5e5;
        }

        .convocations-massive-print .footer {
          margin-top: 15px;
          text-align: right;
        }

        /* Supprimer les marqueurs de page et séparateurs */
        .convocations-massive-print .page-marker {
          display: none !important;
        }

        .convocations-massive-print .page + .page::after {
          display: none !important;
        }

        .convocations-massive-print .page::before {
          display: none !important;
        }

        @media print {
          .convocations-massive-print {
            background: white;
            padding: 0;
          }

          .convocations-massive-print .page {
            border: none !important;
            box-shadow: none !important;
            margin: 0 !important;
            padding: 10mm !important;
            page-break-after: always;
            page-break-inside: avoid;
          }

          .convocations-massive-print .bordered-left {
            border: 2px solid #000 !important;
            print-color-adjust: exact;
            -webkit-print-color-adjust: exact;
          }
        }
      `}</style>

      {/* Overlay de progression pour le téléchargement PDF */}
      {isDownloading && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: "rgba(0, 0, 0, 0.7)",
            zIndex: 9999,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            backdropFilter: "blur(3px)",
          }}
        >
          <div
            style={{
              backgroundColor: "white",
              padding: "30px",
              borderRadius: "12px",
              textAlign: "center",
              minWidth: "350px",
              boxShadow: "0 10px 40px rgba(0,0,0,0.2)",
            }}
          >
            <div style={{ marginBottom: "20px" }}>
              <i
                className="pi pi-spin pi-spinner"
                style={{ fontSize: "40px", color: "#3b82f6" }}
              ></i>
            </div>
            <h3 style={{ marginBottom: "15px", color: "#1e293b" }}>
              Génération du PDF en cours...
            </h3>

            {/* Barre de progression */}
            <div
              style={{
                width: "100%",
                height: "24px",
                backgroundColor: "#e2e8f0",
                borderRadius: "12px",
                overflow: "hidden",
                marginBottom: "10px",
              }}
            >
              <div
                style={{
                  width: `${downloadProgress}%`,
                  height: "100%",
                  backgroundColor: "#3b82f6",
                  transition: "width 0.3s ease",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  color: "white",
                  fontSize: "12px",
                  fontWeight: "bold",
                }}
              >
                {downloadProgress > 10 && `${downloadProgress}%`}
              </div>
            </div>

            <p style={{ color: "#64748b", marginTop: "10px" }}>
              {downloadProgress}% - Veuillez patienter...
            </p>

            <div
              style={{ marginTop: "15px", fontSize: "12px", color: "#94a3b8" }}
            >
              Génération de {candidats.length} convocation(s)
            </div>
          </div>
        </div>
      )}
    </div>
  );
});

ConvocationMassivePrint.displayName = "ConvocationMassivePrint";

export default ConvocationMassivePrint;
