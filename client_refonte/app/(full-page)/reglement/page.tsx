"use client";

import React, { useRef } from "react";
import { Button } from "primereact/button";
import { Card } from "primereact/card";
import jsPDF from "jspdf";
import html2canvas from "html2canvas";

export default function ReglementPage() {
  const contentRef = useRef<HTMLDivElement>(null);

  const handleDownloadPDF = async () => {
    if (!contentRef.current) return;

    const element = contentRef.current;
    const canvas = await html2canvas(element, {
      scale: 2,
      backgroundColor: "#ffffff",
      logging: false,
    });

    const imgData = canvas.toDataURL("image/png");
    const pdf = new jsPDF({
      orientation: "portrait",
      unit: "mm",
      format: "a4",
    });

    const imgWidth = 210; // A4 width in mm
    const pageHeight = 297; // A4 height in mm
    const imgHeight = (canvas.height * imgWidth) / canvas.width;
    let heightLeft = imgHeight;
    let position = 0;

    pdf.addImage(imgData, "PNG", 0, position, imgWidth, imgHeight);
    heightLeft -= pageHeight;

    while (heightLeft >= 0) {
      position = heightLeft - imgHeight;
      pdf.addPage();
      pdf.addImage(imgData, "PNG", 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;
    }

    pdf.save("reglement_baccalaureat.pdf");
  };

  return (
    <div className="reglement-page min-h-screen bg-gray-50 py-4 md:py-6">
      {/* Bouton PDF flottant */}
      <div className="fixed bottom-4 right-4 z-50">
        <Button
          label="Télécharger PDF"
          icon="pi pi-file-pdf"
          onClick={handleDownloadPDF}
          className="p-button-rounded p-button-danger shadow-3"
          style={{ borderRadius: "50px", padding: "0.75rem 1.5rem" }}
        />
      </div>

      <Card className="max-w-4xl mx-auto shadow-3 border-round-xl">
        <div ref={contentRef} className="reglement-content p-4 md:p-6">
          {/* En-tête */}
          <div className="text-center mb-6 pb-4 border-bottom-1 border-300">
            <h1 className="text-xl md:text-2xl font-bold text-primary m-0">
              UNIVERSITÉ CHEIKH ANTA DIOP DE DAKAR
            </h1>
            <h2 className="text-lg md:text-xl font-semibold text-primary mt-1 mb-0">
              OFFICE DU BACCALAURÉAT
            </h2>
            <div className="h-1 w-24 bg-primary mx-auto mt-3"></div>
          </div>

          <h1 className="text-xl md:text-2xl font-bold text-center text-primary mb-6">
            Discipline et règlement intérieur du Centre d'Examen
          </h1>

          {/* Section 1 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">1. Fraude et tentative de fraude</h3>
            <p className="section-text">
              Toute fraude ou tentative de fraude entraînera des sanctions
              disciplinaires sévères, notamment :
            </p>
            <ul className="section-list">
              <li>
                <strong>L'exclusion immédiate</strong> de la session d'examen,
              </li>
              <li>
                La traduction devant la Commission de Discipline de
                l'Université,
              </li>
              <li>Et le cas échéant, des poursuites pénales.</li>
            </ul>
          </div>

          {/* Section 2 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">2. Comportements délictueux</h3>
            <p className="section-text">
              Tout comportement délictueux ou perturbateur, dans l'enceinte ou
              aux abords du centre d'examen, sera pareillement sanctionné. Tout
              candidat perturbant le bon déroulement de l'épreuve sera
              immédiatement exclu.
            </p>
          </div>

          {/* Section 3 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">3. Pièces obligatoires</h3>
            <p className="section-text">
              Le candidat doit être en mesure de présenter, à toute réquisition
              : sa pièce d'identité ou carte d'identité scolaire, sa convocation
              à l'examen.
            </p>
          </div>

          {/* Section 4 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">
              4. Placement et durée minimale de présence
            </h3>
            <p className="section-text">
              Le candidat doit obligatoirement s'installer à la place qui lui
              est assignée, et y rester pendant toute la durée de l'épreuve. Il
              n'est autorisé à quitter la salle qu'après au moins une (01) heure
              à compter du début de l'épreuve.
            </p>
          </div>

          {/* Section 5 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">
              5. Cas de malaise ou incident médical
            </h3>
            <p className="section-text">
              En cas de malaise grave, le candidat peut demander à sortir, à
              condition :
            </p>
            <ul className="section-list">
              <li>D'avoir remis sa copie et ses brouillons,</li>
              <li>D'être accompagné d'un surveillant.</li>
            </ul>
            <p className="section-text mt-2">
              L'incident sera consigné au procès-verbal et sur la copie du
              candidat, laquelle ne pourra lui être restituée qu'après décision
              du Président du jury.
            </p>
          </div>

          {/* Section 6 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">6. Présentation de la copie</h3>
            <p className="section-text">
              Le candidat doit commencer sa rédaction sur la première page de la
              feuille. Il est tenu d'utiliser uniquement le matériel fourni :
              feuilles de copie officielles, intercalaires, brouillons mis à
              disposition par le centre.
            </p>
          </div>

          {/* Section 7 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">7. Objets interdits</h3>
            <p className="section-text">
              Il est formellement interdit de conserver, au sol ou à portée de
              main, tout manuscrit, imprimé, livre, ouvrage ou document, ainsi
              que tout objet communiquant avec un autre individu.
            </p>
            <p className="section-text mt-2">
              Conformément à la circulaire n° 0032 du 07 janvier 2012,{" "}
              <strong>
                l'usage et la possession de téléphones portables, ainsi que de
                tout autre terminal de communication électronique, sont
                strictement interdits
              </strong>{" "}
              dans l'enceinte du centre d'examen.
            </p>
          </div>

          {/* Section 8 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">8. Matériel autorisé</h3>
            <p className="section-text">
              L'usage du dictionnaire n'est autorisé que pour les épreuves de
              Grec, Latin et Arabe classique. Pour les épreuves de Mathématiques
              et de Sciences Physiques, l'utilisation de règles, cercles à
              calcul et tables numériques (logarithmes, statistiques,
              financières) est permise, à condition que ces instruments ne
              comportent aucune annotation ou indication susceptible d'aider le
              candidat.
            </p>
          </div>

          {/* Section 9 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">9. Communication interdite</h3>
            <p className="section-text">
              Toute forme de communication ou tentative de communication, entre
              candidats ou avec toute personne extérieure à la salle, est
              strictement interdite. Les complices seront sanctionnés au même
              titre que les fraudeurs.
            </p>
          </div>

          {/* Section 10 */}
          <div className="regulation-section mb-5">
            <h3 className="section-title">
              10. Remise des copies et relevés de notes
            </h3>
            <p className="section-text">
              À la fin de l'épreuve, chaque candidat doit remettre une copie,
              même blanche avec en-tête dûment rempli, avant de signer la liste
              de présence. À l'issue du premier groupe, les candidats sont
              invités à se rendre au secrétariat du jury pour retirer leurs
              relevés de notes.
            </p>
            <div className="subsections mt-3 ml-4">
              <p className="section-text">
                <strong>a)</strong> Les candidats admissibles, autorisés à subir
                les épreuves du second groupe au vu des notes obtenues, font
                connaître, dans la demi-journée qui suit la proclamation des
                résultats, les trois (03) disciplines sur lesquelles ils
                désirent composer au second groupe ;
              </p>
              <p className="section-text mt-2">
                <strong>b)</strong> À la fin de l'examen, les relevés de notes
                non retirés resteront disponibles auprès du Chef de centre.
              </p>
            </div>
          </div>
        </div>
      </Card>

      <style jsx global>{`
        .reglement-page {
          background: linear-gradient(135deg, #f5f7fa 0%, #eef2f7 100%);
        }

        .regulation-section {
          margin-bottom: 1.5rem;
        }

        .section-title {
          font-size: 1.1rem;
          font-weight: 700;
          color: #1e3c72;
          margin-bottom: 0.75rem;
          border-left: 4px solid #1e3c72;
          padding-left: 0.75rem;
        }

        .section-text {
          font-size: 0.95rem;
          line-height: 1.6;
          color: #2c3e50;
          text-align: justify;
          margin: 0.5rem 0;
        }

        .section-list {
          margin: 0.5rem 0 0.5rem 1.5rem;
          padding-left: 0;
          list-style-type: none;
        }

        .section-list li {
          font-size: 0.95rem;
          line-height: 1.6;
          color: #2c3e50;
          margin-bottom: 0.25rem;
          position: relative;
          padding-left: 1.25rem;
        }

        .section-list li::before {
          content: "•";
          color: #1e3c72;
          font-weight: bold;
          position: absolute;
          left: 0;
        }

        .subsections {
          margin-top: 0.75rem;
        }

        .border-bottom-1 {
          border-bottom: 1px solid #dee2e6;
        }

        .border-300 {
          border-color: #ced4da;
        }

        .text-primary {
          color: #1e3c72;
        }

        .bg-primary {
          background-color: #1e3c72;
        }

        @media print {
          .fixed {
            display: none;
          }

          .reglement-page {
            background: white;
            padding: 0;
          }

          .shadow-3 {
            box-shadow: none;
          }
        }

        @media (max-width: 768px) {
          .reglement-content {
            padding: 1rem;
          }

          .section-title {
            font-size: 1rem;
          }

          .section-text {
            font-size: 0.85rem;
          }

          .section-list li {
            font-size: 0.85rem;
          }
        }
      `}</style>
    </div>
  );
}
