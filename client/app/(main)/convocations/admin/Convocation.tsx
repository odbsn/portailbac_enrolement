"use client";

import html2canvas from "html2canvas";
import jsPDF from "jspdf";
import { useRef } from "react";
import ProtectedRoute from "@/layout/ProtectedRoute";

interface EpreuveData {
  matiere: string;
  date: string;
  heureDebut: string;
  duree: string;
  coefficient: string;
  nature: string;
  secondGroupe: string;
}

interface ConvocationData {
  prenom: string;
  nom: string;
  dateNaissance: string;
  lieuNaissance: string;
  etablissement: string;
  sexe: string;
  nationalite: string;
  typeCandidat: string;
  numeroJury: number;
  numeroTable: number;
  centreEcrit: string;
  serie: string;
  dateConvocation: string;
  epreuves: EpreuveData[];
}

const data: ConvocationData = {
  prenom: "Khoudia",
  nom: "SYLLA",
  dateNaissance: "12/02/1999",
  lieuNaissance: "RIMBAKH",
  etablissement: "CENTRE ACADEMIQUE D'ORIENTATION",
  sexe: "F",
  nationalite: "SENEGAL",
  typeCandidat: "Individuel",
  numeroJury: 991,
  numeroTable: 99569,
  centreEcrit: "LYCEE TECHNIQUE ANDRE PEYTAVIN",
  serie: "T2",
  dateConvocation: "30/03/2026",
  epreuves: [
    {
      matiere: "Liste A (Des,Cout.,Mus,...)",
      date: "Mer. 14 Mai 2025",
      heureDebut: "14:30",
      duree: "",
      coefficient: "",
      nature: "Ecr./Prat.",
      secondGroupe: "=== NON ===",
    },
    {
      matiere: "Liste B (Langues...)",
      date: "Sam. 17 Mai 2025",
      heureDebut: "07:30",
      duree: "",
      coefficient: "",
      nature: "Ecr./Prat.",
      secondGroupe: "=== NON ===",
    },
    {
      matiere: "Educ. Physique et Sportive",
      date: "Mar. 27 Mai 2025",
      heureDebut: "07:30",
      duree: "",
      coefficient: "",
      nature: "Ecr./Prat.",
      secondGroupe: "=== NON ===",
    },

    {
      matiere: "Mathématiques",
      date: "Lun. 16 Juin 2025",
      heureDebut: "07:30",
      duree: "04:00",
      coefficient: "4,00",
      nature: "Écrit",
      secondGroupe: "OUI / DOMINANTE",
    },
    {
      matiere: "Analyse des Syst. Electriques",
      date: "Lun. 16 Juin 2025",
      heureDebut: "14:30",
      duree: "03:00",
      coefficient: "2,00",
      nature: "Écrit",
      secondGroupe: "Oui / Non-dominante",
    },
    {
      matiere: "Electrotech/Electronique",
      date: "Mar. 17 Juin 2025",
      heureDebut: "07:30",
      duree: "04:00",
      coefficient: "6,00",
      nature: "Écrit",
      secondGroupe: "OUI / DOMINANTE",
    },
    {
      matiere: "Constr. Electromécanique",
      date: "Mar. 17 Juin 2025",
      heureDebut: "14:30",
      duree: "04:00",
      coefficient: "3,00",
      nature: "Écrit",
      secondGroupe: "Oui / Non-dominante",
    },
    {
      matiere: "Schéma Automatique - Informatique",
      date: "Mer. 18 Juin 2025",
      heureDebut: "07:30",
      duree: "04:00",
      coefficient: "4,00",
      nature: "Écrit",
      secondGroupe: "OUI / DOMINANTE",
    },
    {
      matiere: "Constr. Electric. (Cablage)",
      date: "Mer. 18 Juin 2025",
      heureDebut: "14:30",
      duree: "",
      coefficient: "3,00",
      nature: "Oral/TP",
      secondGroupe: "=== NON ===",
    },
    {
      matiere: "Essais et Mesures",
      date: "Mer. 18 Juin 2025",
      heureDebut: "14:30",
      duree: "",
      coefficient: "3,00",
      nature: "Oral/TP",
      secondGroupe: "=== NON ===",
    },
    {
      matiere: "Technique d’Expression et de Communication",
      date: "Jeu. 19 Juin 2025",
      heureDebut: "07:30",
      duree: "04:00",
      coefficient: "3,00",
      nature: "Écrit",
      secondGroupe: "Oui / Non-dominante",
    },
    {
      matiere: "Anglais",
      date: "Jeu. 19 Juin 2025",
      heureDebut: "14:30",
      duree: "02:00",
      coefficient: "2,00",
      nature: "Écrit",
      secondGroupe: "=== NON ===",
    },
    {
      matiere: "Sciences Physiques",
      date: "Ven. 20 Juin 2025",
      heureDebut: "07:30",
      duree: "02:00",
      coefficient: "2,00",
      nature: "Écrit",
      secondGroupe: "=== NON ===",
    },
    {
      matiere: "Sciences Physiques",
      date: "Ven. 20 Juin 2025",
      heureDebut: "07:30",
      duree: "02:00",
      coefficient: "2,00",
      nature: "Écrit",
      secondGroupe: "=== NON ===",
    },
    {
      matiere: "Sciences Physiques",
      date: "Ven. 20 Juin 2025",
      heureDebut: "07:30",
      duree: "02:00",
      coefficient: "2,00",
      nature: "Écrit",
      secondGroupe: "=== NON ===",
    },
    {
      matiere: "Sciences Physiques",
      date: "Ven. 20 Juin 2025",
      heureDebut: "07:30",
      duree: "02:00",
      coefficient: "2,00",
      nature: "Écrit",
      secondGroupe: "=== NON ===",
    },
  ],
};

export default function Page() {
  const pageRef = useRef<HTMLDivElement>(null);

  const handleDownload = async () => {
    if (!pageRef.current) return;

    try {
      const element = pageRef.current;

      const canvas = await html2canvas(element, {
        scale: 2,
        logging: false,
        useCORS: true,
        backgroundColor: "#ffffff",
      });

      const imgData = canvas.toDataURL("image/png");
      const pdf = new jsPDF({
        orientation: "portrait",
        unit: "mm",
        format: "a4",
      });

      const imgWidth = 210;
      const pageHeight = 297;
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      let heightLeft = imgHeight;
      let position = 0;

      pdf.addImage(imgData, "PNG", 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;

      while (heightLeft > 0) {
        position = heightLeft - imgHeight;
        pdf.addPage();
        pdf.addImage(imgData, "PNG", 0, position, imgWidth, imgHeight);
        heightLeft -= pageHeight;
      }

      pdf.save(`convocation_${data.nom}_${data.prenom}.pdf`);
    } catch (error) {
      console.error("Erreur lors de la génération du PDF:", error);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <ProtectedRoute allowedRoles={["ADMIN", "SCOLARITE"]}>
      <div className="toolbar">
        <button onClick={handleDownload} className="btn btn-download">
          📥 Télécharger (PDF)
        </button>
        <button onClick={handlePrint} className="btn btn-print">
          🖨️ Imprimer
        </button>
      </div>
      <div className="container">
        {/* Barre d'outils avec les boutons en haut */}

        {/* Contenu à imprimer/télécharger */}
        <div ref={pageRef} className="page print-content">
          {/* HEADER */}
          <div className="header">
            <div>
              UNIVERSITE CHEIKH ANTA DIOP
              <br />
              DE DAKAR
              <br />
              <b>OFFICE DU BACCALAUREAT</b>
            </div>

            <div className="right">
              REPUBLIQUE DU SENEGAL
              <br />
              Un Peuple - Un But - Une Foi
            </div>
          </div>

          <h6 className="title">
            BACCALAUREAT DE L&apos;ENSEIGNEMENT SECONDAIRE
          </h6>

          <div className="rowTop">
            <div className="convocation underline font-bold">CONVOCATION</div>
            <div className="font-bold">
              <h6 className="p-0 m-0">SESSION NORMALE 2025</h6>
            </div>
            <span className="serie w-3">
              <h6 className="p-0 m-0">Série : {data.serie}</h6>
            </span>
          </div>

          {/* GRID */}
          <div className="grid h-auto py-0">
            {/* LEFT */}
            <div className="left h-auto">
              <div>
                <div className="field">
                  <p>Prénom(s)</p>
                </div>
                <div className="contenu">
                  <span>{data.prenom}</span>
                </div>
              </div>
              <div>
                <div className="field">
                  <p>Nom</p>
                </div>
                <div className="contenu">
                  <span>{data.nom}</span>
                </div>
              </div>
              <div>
                <div className="field">
                  <p>Date de Naissance</p>
                </div>
                <div className="contenu">
                  <span>{data.lieuNaissance}</span>
                </div>
              </div>
              <div>
                <div className="field">
                  <p>Lieu de Naissance</p>
                </div>
                <div className="contenu">
                  <span>{data.lieuNaissance}</span>
                </div>
              </div>
              <div>
                <div className="field">
                  <p>Etablissement fréquenté</p>
                </div>
                <div className="contenu">
                  <span>{data.etablissement}</span>
                </div>
              </div>
              <div>
                <div className="field">
                  <p>Candidat</p>
                </div>
                <div className="contenu">
                  <span>{data.typeCandidat}</span>
                </div>
              </div>
              <div className="mb-0 pb-0">
                <div className="field sexe">
                  <p>Sexe</p>
                  <p>Nationalité</p>
                </div>
                <div className="sexeNationalite py-0 mb-0">
                  <span>{data.sexe}</span>
                  <span>{data.nationalite}</span>
                </div>
              </div>
            </div>

            {/* CENTER */}
            <div className="center">
              <div className="bg-white shadow-lg rounded-lg overflow-hidden">
                <div className="overflow-x-auto">
                  <table className="w-full border-collapse">
                    <thead className="bg-slate-100 ">
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
                          991
                        </td>
                        <td className="p-1 text-center font-semibold">
                          99 569
                        </td>
                        <td className="p-1 text-center font-semibold text-sm">
                          LYCEE TECHNIQUE ANDRE PEYTAVIN
                        </td>
                        <td className="p-1 text-center"></td>
                        <td className="p-1 text-center rounded-br-lg"></td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <div className="flex items-center justify-content-between mt-3">
                <div className="flex flex-column">
                  <span className="font-bold underline">
                    Matière(s) Optionnelle(s):
                  </span>
                  <span className="flex flex-column ml-4">
                    <span>- LV1: Espagnol</span>
                    <span>- LV2: ANglais</span>
                    <span>- LC: Latin</span>
                  </span>
                </div>
                <div className="flex flex-column">
                  <span className="font-bold underline">E.P.S :</span>
                  <span>Apte</span>
                </div>
              </div>
              <div className="flex items-center justify-content-between mt-3">
                <div className="flex flex-column">
                  <span className="font-bold underline">
                    Matière(s) facultative(s):
                  </span>
                  <span className="flex flex-column ml-4">
                    <span>- Liste A(Des., Cout.,Mus.) : Musique </span>
                  </span>
                </div>
              </div>

              <div className="note">
                N.B : Toute information vous paraissant erronée doit être
                signalée au plus tard le Ven. 30 Mai 2025
              </div>
            </div>

            {/* RIGHT */}
            <div className="rightBox">
              <div className="importantTitle">TRES IMPORTANT</div>

              <div
                className="flex flex-column px-3"
                style={{ fontSize: "11px", lineHeight: "0.8rem" }}
              >
                <span>Pendant toute la durée de la session, vous devez :</span>
                <span>
                  - Être en salle, muni de cette convocation et de votre pièce
                  d&apos;identité le matin à 7h 15 et l&apos;après-midi à 14h15.
                  Aucun retardataire ne sera admis en salle.
                </span>

                <span>
                  - Retirer auprès du Président de jury votre relevé de notes,
                  qui est indispensable pour le choix des épreuves du 2ème
                  groupe. Ce choix doit se faire dans la demi journée qui suit
                  la proclamation des résultats.
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
            <div className="flex gap-2 mb-0">
              <div className="epSection flex flex-column md:col-7 border-top-1 border-bottom-1 border-400 border-round-bottom-md border-round-top-md ml-2">
                <span>Epreuves d&apos;Education Physique et Sportive</span>
                <span className="ml-2">A partir du Mardi 27 Mai 2025</span>
                <span className="ml-2">
                  Centre : LYCEE CHARLES DE GAULLE EPS A SAINT LOUIS
                </span>
              </div>

              <div
                className="md:col-5 border-top-1 font-medium border-bottom-1 border-400 border-round-bottom-md border-round-top-md text-start"
                style={{ fontFamily: "Lucida Calligraphy" }}
              >
                Veuillez prendre attache avec ledit centre pour les détails et
                précisions concernant le déroulement de ces épreuves d’EPS
              </div>
            </div>
            <div className="secondGroup">
              En cas d&apos;admissibilité au Second (2nd) groupe
              d&apos;épreuves, vous aurez à choisir trois (03) matières pour la
              recomposition dont{" "}
              <span className="font-bold underline">
                deux (02) dominantes et une (01) non-dominante
              </span>
              . Se référer à la colonne dénommée{" "}
              <b>
                &quot;Matières autorisées au 2nd groupe d&apos;épreuves&quot;
              </b>{" "}
              du planning
            </div>
          </div>

          {/* TABLE */}
          <h5 className="pt-0 mt-1 mb-0 pb-0" style={{ fontSize: "14px" }}>
            <span className=" underline">
              Planning de déroulement de l&apos;examen
            </span>{" "}
            : Série T2, Electrotechnique - Electronique
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
                    border: "0.3px solid #ccc",
                    padding: "4px",
                    fontSize: "12px",
                    background: "#e5e5e5",
                  }}
                >
                  Matière de l&apos;épreuve
                </th>
                <th
                  style={{
                    border: "0.3px solid #ccc",
                    padding: "4px",
                    fontSize: "12px",
                    background: "#e5e5e5",
                  }}
                >
                  Date
                </th>
                <th
                  style={{
                    border: "0.3px solid #ccc",
                    padding: "4px",
                    fontSize: "12px",
                    background: "#e5e5e5",
                  }}
                >
                  Heure
                </th>
                <th
                  style={{
                    border: "0.3px solid #ccc",
                    padding: "4px",
                    fontSize: "12px",
                    background: "#e5e5e5",
                  }}
                >
                  Durée
                </th>
                <th
                  style={{
                    border: "0.3px solid #ccc",
                    padding: "4px",
                    fontSize: "12px",
                    background: "#e5e5e5",
                  }}
                >
                  Coef.
                </th>
                <th
                  style={{
                    border: "0.3px solid #ccc",
                    padding: "4px",
                    fontSize: "12px",
                    background: "#e5e5e5",
                  }}
                >
                  Nature
                </th>
                <th
                  style={{
                    border: "0.3px solid #ccc",
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
              {data.epreuves.map((e, i) => (
                <tr
                  key={i}
                  className={i % 2 === 0 ? "bg-white" : "bg-gray-300"}
                >
                  <td
                    style={{
                      border: "0.3px solid #ccc",
                      padding: "4px",
                      fontSize: "12px",
                    }}
                  >
                    {e.matiere}
                  </td>
                  <td
                    style={{
                      border: "0.3px solid #ccc",
                      padding: "4px",
                      fontSize: "12px",
                    }}
                  >
                    {e.date}
                  </td>
                  <td
                    style={{
                      border: "0.3px solid #ccc",
                      padding: "4px",
                      fontSize: "12px",
                    }}
                  >
                    {e.heureDebut}
                  </td>
                  <td
                    style={{
                      border: "0.3px solid #ccc",
                      padding: "4px",
                      fontSize: "12px",
                    }}
                  >
                    {e.duree}
                  </td>
                  <td
                    style={{
                      border: "0.3px solid #ccc",
                      padding: "4px",
                      fontSize: "12px",
                    }}
                  >
                    {e.coefficient}
                  </td>
                  <td
                    style={{
                      border: "0.3px solid #ccc",
                      padding: "4px",
                      fontSize: "12px",
                    }}
                  >
                    {e.nature}
                  </td>
                  <td
                    style={{
                      border: "0.3px solid #ccc",
                      padding: "4px",
                      fontSize: "12px",
                    }}
                  >
                    {e.secondGroupe}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="footer">SAINT LOUIS, le {data.dateConvocation}</div>
        </div>
      </div>
    </ProtectedRoute>
  );
}
