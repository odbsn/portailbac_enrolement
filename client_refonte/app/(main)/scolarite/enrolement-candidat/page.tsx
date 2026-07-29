"use client";

import React, {
  useState,
  useEffect,
  useContext,
  useRef,
  useCallback,
} from "react";
import { Toast } from "primereact/toast";
import { UserContext } from "@/app/userContext";
import { CandidatureService } from "./services";
import { useCandidatData, useDialogs } from "./hooks";
import {
  CandidatureList,
  CandidatureForm,
  ConfirmDialog,
  ResultDialog,
  PrintDialog,
} from "./components";
import { diffDays } from "./utils";
import ProtectedRoute from "@/layout/ProtectedRoute";
import "./style.css";
import { Button } from "primereact/button";

const EnrolementCandidat = () => {
  const { user } = useContext(UserContext);
  const toast = useRef<Toast>(null);
  const [prog, setProg] = useState<any>(null);
  const [series, setSeries] = useState([]);
  const [cecs, setCecs] = useState([]);
  const [cexam, setCexam] = useState([]);
  const [pays, setPays] = useState([]);
  const [matieres, setMatieres] = useState([]);
  const [selectedCandidat, setSelectedCandidat] = useState<any>(null);
  const [centreExamenEtab, setCentreExamenEtab] = useState([]);

  const dialogs = useDialogs();
  const { groupedCdts, faeb, nbrTCdts, loading, reloadData } = useCandidatData(
    user?.acteur?.etablissement?.id,
    prog?.edition,
  );

  useEffect(() => {
    const loadStatic = async () => {
      const [p, s, c, cx, pa, m] = await Promise.all([
        CandidatureService.getLastProg(),
        CandidatureService.getSeries(),
        CandidatureService.getCentreEtatCivils(),
        CandidatureService.getCentreExamen(),
        CandidatureService.getPays(),
        CandidatureService.getMatiereOptions(),
      ]);
      setProg(p);
      setSeries(s);
      setCecs(c);
      setCexam(cx);
      setPays(pa);
      setMatieres(m);
    };
    loadStatic();
  }, []);

  useEffect(() => {
    if (user?.acteur?.etablissement?.id && prog?.edition) {
      CandidatureService.getCentreExamForI(
        user.acteur.etablissement.id,
        Number(prog.edition),
      ).then(setCentreExamenEtab);
    }
  }, [user, prog]);

  const daysLeft = diffDays(prog?.date_end);
  const canAdd =
    faeb && Number(nbrTCdts) < Number(faeb?.count_1000_OB) && daysLeft > 0;

  // Calcul des statistiques
  const totalCandidats =
    groupedCdts?.reduce((acc, group) => acc + (group.cdts?.length || 0), 0) ||
    0;
  const vignettesRestantes = faeb
    ? Number(faeb.count_1000_OB) - Number(nbrTCdts)
    : 0;

  const handleSuccess = useCallback(() => {
    reloadData();
    toast.current?.show({
      severity: "success",
      summary: "Office du Bac",
      detail: "Opération réussie",
      life: 3000,
    });
  }, [reloadData]);

  const handleDelete = useCallback(
    (candidat: any) => {
      dialogs.openConfirm(
        "Confirmation",
        `Supprimer le dossier ${candidat.dosNumber} ?`,
        async () => {
          try {
            await CandidatureService.deleteCandidat(candidat.id, user?.login);
            toast.current?.show({
              severity: "success",
              summary: "Office du Bac",
              detail: "Dossier supprimé",
              life: 3000,
            });
            reloadData();
          } catch (err) {
            toast.current?.show({
              severity: "error",
              summary: "Erreur",
              detail: "Suppression impossible",
              life: 3000,
            });
          }
        },
      );
    },
    [user, reloadData],
  );

  const isOpen = daysLeft > 0;

  return (
    <ProtectedRoute allowedRoles={["ADMIN", "AGENT_DE_SAISIE"]}>
      <div className="page-container">
        <Toast ref={toast} />

        <div className="max-w-7xl mx-auto">
          <div className="page-card">
            {/* Header avec statistiques intégrées */}
            <div className="page-header page-header-with-stats">
              <div className="page-header-content">
                <div className="page-header-left">
                  <div className="page-header-badge">
                    <div className="page-header-icon">
                      <i className="pi pi-users"></i>
                    </div>
                    <span
                      className={`status-badge ${
                        isOpen ? "status-badge-open" : "status-badge-closed"
                      }`}
                    >
                      {isOpen ? "● En cours" : "● Fermé"}
                    </span>
                  </div>
                  <h1 className="page-header-title">
                    Enrôlement des candidats
                  </h1>
                </div>
                <div className="page-header-action">
                  {isOpen ? (
                    <div className="flex gap-3">
                      <Button
                        label="Ajouter un candidat"
                        icon="pi pi-plus"
                        className="btn-primary-gradient"
                        onClick={() => {
                          setSelectedCandidat(null);
                          dialogs.openForm("create");
                        }}
                        disabled={!canAdd}
                      />
                      <Button
                        label="Imprimer"
                        icon="pi pi-print"
                        className="btn-secondary-gradient"
                        onClick={dialogs.openPrint}
                      />
                    </div>
                  ) : (
                    <div className="page-header-closed">
                      <div className="page-header-closed-icon">
                        <i className="pi pi-exclamation-triangle"></i>
                      </div>
                      <span className="page-header-closed-text">
                        La période d'enrôlement est fermée
                      </span>
                    </div>
                  )}
                </div>
              </div>

              {/* Stats intégrées dans le header */}
              <div className="page-header-stats">
                <div className="page-header-stat">
                  <span className="page-header-stat-label">Type</span>
                  <span className="page-header-stat-value">
                    {user?.acteur?.etablissement?.typeEtablissement?.code ||
                      "-"}
                  </span>
                </div>
                <div className="page-header-stat-divider"></div>
                <div className="page-header-stat">
                  <span className="page-header-stat-label">Édition</span>
                  <span className="page-header-stat-value">
                    {prog?.edition || "-"}
                  </span>
                </div>
                <div className="page-header-stat-divider"></div>
                <div className="page-header-stat">
                  <span className="page-header-stat-label">
                    <i className="pi pi-tags"></i> Vignettes restantes
                  </span>
                  <span
                    className={`page-header-stat-value ${
                      vignettesRestantes <= 0 ? "text-red-400" : ""
                    }`}
                  >
                    {vignettesRestantes}
                  </span>
                </div>
                <div className="page-header-stat-divider"></div>
                <div className="page-header-stat">
                  <span className="page-header-stat-label">
                    <i className="pi pi-clock"></i> Jours restants
                  </span>
                  <span className="page-header-stat-value">
                    {daysLeft > 0 ? daysLeft : 0}
                  </span>
                </div>
                <div className="page-header-stat-divider"></div>
                <div className="page-header-stat">
                  <span className="page-header-stat-label">
                    <i className="pi pi-users"></i> Candidats
                  </span>
                  <span className="page-header-stat-value">
                    {totalCandidats}
                  </span>
                </div>
              </div>
            </div>

            {/* Table - sans StatsBanner */}
            <div className="p-0 md:px-4 pt-0">
              <CandidatureList
                groupedCdts={groupedCdts}
                loading={loading}
                onEdit={(c) => {
                  setSelectedCandidat(c);
                  dialogs.openForm("edit");
                }}
                onDelete={handleDelete}
                onView={(c) => {
                  setSelectedCandidat(c);
                  dialogs.openForm("view");
                }}
                onAdd={() => {
                  setSelectedCandidat(null);
                  dialogs.openForm("create");
                }}
                onPrint={dialogs.openPrint}
                diffDays={daysLeft}
                canAdd={canAdd}
              />
            </div>
          </div>
        </div>

        {/* Dialogs */}
        <CandidatureForm
          visible={dialogs.form.visible}
          onHide={dialogs.closeForm}
          mode={dialogs.form.mode}
          user={user}
          prog={prog}
          candidats={[]}
          cecs={cecs}
          pays={pays}
          series={series}
          matieres={matieres}
          cexam={cexam}
          centreExamenEtab={centreExamenEtab}
          onSuccess={handleSuccess}
          initialData={selectedCandidat}
          onShowResult={dialogs.openResult}
        />

        <PrintDialog
          visible={dialogs.print}
          onHide={dialogs.closePrint}
          user={user}
          prog={prog}
          series={series}
          etabId={user?.acteur?.etablissement?.id}
          etabName={user?.acteur?.etablissement?.name}
        />

        <ConfirmDialog
          visible={dialogs.confirm.visible}
          onHide={dialogs.closeConfirm}
          onConfirm={dialogs.confirm.onConfirm}
          title={dialogs.confirm.title}
          message={dialogs.confirm.message}
        />

        <ResultDialog
          visible={dialogs.result.visible}
          onHide={dialogs.closeResult}
          data={dialogs.result.data}
          loading={dialogs.result.loading}
          prog={prog}
          onPreload={() => {}}
        />
      </div>
    </ProtectedRoute>
  );
};

export default EnrolementCandidat;
