// app/scolarite/enrolement-candidat/page.tsx
'use client';

import React, { useState, useEffect, useContext, useRef, useCallback } from 'react';
import { Toast } from 'primereact/toast';
import { UserContext } from '@/app/userContext';
import { CandidatureService } from './services';
import { useCandidatData, useDialogs } from './hooks';
import { CandidatureList, CandidatureForm, StatsBanner, ConfirmDialog, ResultDialog, PrintDialog } from './components';
import { diffDays } from './utils';
import ProtectedRoute from '@/layout/ProtectedRoute';
import './style.css';

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
    prog?.edition
  );

  useEffect(() => {
    const loadStatic = async () => {
      const [p, s, c, cx, pa, m] = await Promise.all([
        CandidatureService.getLastProg(),
        CandidatureService.getSeries(),
        CandidatureService.getCentreEtatCivils(),
        CandidatureService.getCentreExamen(),
        CandidatureService.getPays(),
        CandidatureService.getMatiereOptions()
      ]);
      setProg(p); setSeries(s); setCecs(c); setCexam(cx); setPays(pa); setMatieres(m);
    };
    loadStatic();
  }, []);

  useEffect(() => {
    if (user?.acteur?.etablissement?.id && prog?.edition) {
      CandidatureService.getCentreExamForI(user.acteur.etablissement.id, Number(prog.edition))
        .then(setCentreExamenEtab);
    }
  }, [user, prog]);

  const daysLeft = diffDays(prog?.date_end);
  const canAdd = faeb && Number(nbrTCdts) < Number(faeb?.count_1000_OB) && daysLeft > 0;

  const handleSuccess = useCallback(() => {
    reloadData();
    toast.current?.show({ severity: 'success', summary: 'Office du Bac', detail: 'Opération réussie', life: 3000 });
  }, [reloadData]);

  const handleDelete = useCallback((candidat: any) => {
    dialogs.openConfirm('Confirmation', `Supprimer le dossier ${candidat.dosNumber} ?`, async () => {
      try {
        await CandidatureService.deleteCandidat(candidat.id, user?.login);
        toast.current?.show({ severity: 'success', summary: 'Office du Bac', detail: 'Dossier supprimé', life: 3000 });
        reloadData();
      } catch (err) {
        toast.current?.show({ severity: 'error', summary: 'Erreur', detail: 'Suppression impossible', life: 3000 });
      }
    });
  }, [user, reloadData]);

  return (
    <ProtectedRoute allowedRoles={['ADMIN', 'AGENT_DE_SAISIE']}>
      <div className="grid crud-demo"><div className="col-12"><div className="card">
        <Toast ref={toast} />
        <StatsBanner edition={prog?.edition} faeb={faeb} nbrTCdts={nbrTCdts} diffDays={daysLeft} typeEtab={user?.acteur?.etablissement?.typeEtablissement?.code} />
        
        <CandidatureList
          groupedCdts={groupedCdts} loading={loading}
          onEdit={(c) => { setSelectedCandidat(c); dialogs.openForm('edit'); }}
          onDelete={handleDelete}
          onView={(c) => { setSelectedCandidat(c); dialogs.openForm('view'); }}
          onAdd={() => { setSelectedCandidat(null); dialogs.openForm('create'); }}
          onPrint={dialogs.openPrint}
          diffDays={daysLeft} canAdd={canAdd}
        />
      </div></div></div>

      <CandidatureForm
        visible={dialogs.form.visible} onHide={dialogs.closeForm} mode={dialogs.form.mode}
        user={user} prog={prog} candidats={[]} cecs={cecs} pays={pays} series={series}
        matieres={matieres} cexam={cexam} centreExamenEtab={centreExamenEtab}
        onSuccess={handleSuccess} initialData={selectedCandidat} onShowResult={dialogs.openResult}
      />

      <PrintDialog
        visible={dialogs.print} onHide={dialogs.closePrint}
        user={user} prog={prog} series={series}
        etabId={user?.acteur?.etablissement?.id} etabName={user?.acteur?.etablissement?.name}
      />

      <ConfirmDialog
        visible={dialogs.confirm.visible} onHide={dialogs.closeConfirm}
        onConfirm={dialogs.confirm.onConfirm} title={dialogs.confirm.title} message={dialogs.confirm.message}
      />

      <ResultDialog
        visible={dialogs.result.visible} onHide={dialogs.closeResult}
        data={dialogs.result.data} loading={dialogs.result.loading}
        prog={prog} onPreload={() => {}}
      />
    </ProtectedRoute>
  );
};

export default EnrolementCandidat;