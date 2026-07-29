// app/scolarite/enrolement-candidat/components/Dialogs.tsx
import React, { useState } from 'react';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';
import { ProgressSpinner } from 'primereact/progressspinner';
import { CandidatureService } from '../services';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';

// ===== ConfirmDialog =====
export const ConfirmDialog: React.FC<{ visible: boolean; onHide: () => void; onConfirm: () => void; title: string; message: string }> = 
  ({ visible, onHide, onConfirm, title, message }) => (
  <Dialog visible={visible} header={title} modal onHide={onHide} style={{ width: '500px' }}
    footer={<><Button label="Annuler" icon="pi pi-times" text onClick={onHide} /><Button label="Confirmer" icon="pi pi-check" severity="danger" onClick={onConfirm} /></>}>
    <div className="flex align-items-center gap-3"><i className="pi pi-exclamation-triangle text-4xl text-yellow-500" /><span className="text-lg">{message}</span></div>
  </Dialog>
);

// ===== ResultDialog =====
export const ResultDialog: React.FC<{ visible: boolean; onHide: () => void; data: any; loading: boolean; onPreload?: () => void; prog?: any }> = 
  ({ visible, onHide, data, loading, onPreload, prog }) => {
  const canPreload = data?.exclusionDuree === 0 || (data?.exclusionDuree + 1 + data?.exYearBac <= (prog?.edition || 0));
  return (
    <Dialog visible={visible} header="Contrôle des informations" modal onHide={onHide} style={{ width: '600px' }}
      footer={<>
        {data && canPreload && onPreload && <Button label="Précharger" icon="pi pi-upload" className="p-button-success mr-2" onClick={onPreload} />}
        <Button label="Fermer" icon="pi pi-times" onClick={onHide} />
      </>}>
      <div className="py-2">
        {loading ? <div className="flex flex-column align-items-center"><ProgressSpinner /><span className="mt-3">Recherche en cours...</span></div>
        : !data ? <span className="text-green-600 font-bold">Informations transmises avec succès à l'Office du Bac.</span>
        : canPreload ? <div className="space-y-1">
            <p><strong>Prénom(s) :</strong> {data.firstname?.toUpperCase()}</p>
            <p><strong>Nom :</strong> {data.lastname?.toUpperCase()}</p>
            <p><strong>Date naissance :</strong> {data.date_birth}</p>
            <p><strong>Lieu :</strong> {data.place_birth}</p>
            <p><strong>Sexe :</strong> {data.gender}</p>
            <p><strong>Pays :</strong> {data.countryBirth?.name || data.countryBirth}</p>
            <p><strong>N° fois :</strong> {data.bac_do_count}</p>
            <p className="text-red-600 font-bold mt-2">Ce candidat a déjà fait le BAC, saisie à partir du relevé de note.</p>
          </div>
        : <div>
            <p>Exclu pour <span className="text-red-600 font-bold">{data.exclusionDuree} année(s)</span></p>
            <p>Déjà présenté en <span className="text-blue-600 font-bold">{data.exYearBac}</span></p>
            <p>Pourra faire le BAC en <span className="text-green-600 font-bold">{data.exYearBac + data.exclusionDuree + 1}</span></p>
          </div>}
      </div>
    </Dialog>
  );
};

// ===== PrintDialog =====
export const PrintDialog: React.FC<{ visible: boolean; onHide: () => void; user: any; prog: any; series: any[]; etabId: string; etabName: string }> = 
  ({ visible, onHide, user, prog, series, etabId, etabName }) => {
  const [loading, setLoading] = useState(false);
  const [serie, setSerie] = useState<string | null>(null);
  const [type, setType] = useState<string | null>(null);
  const [tri, setTri] = useState<string | null>(null);
  const [option, setOption] = useState<string | null>(null);
  const [debut, setDebut] = useState('');
  const [fin, setFin] = useState('');

  const handleDownload = async () => {
    setLoading(true);
    try {
      if (type === 'notOfficiel') {
        await CandidatureService.getListByEtab(etabId, prog?.edition, etabName, user?.login, serie, tri, option, debut, fin);
      } else if (type === 'callList') {
        await CandidatureService.getListContactsByEtab(etabId, prog?.edition, etabName, user?.login);
      }
    } catch (err) { console.error(err); } finally { setLoading(false); }
  };

  const serieOpts = [{ label: 'Toutes', value: 'all' }, ...(series?.map((s: any) => ({ label: s.code, value: s.code })) || [])];
  const typeOpts = [{ label: 'Provisoire', value: 'notOfficiel' }, { label: 'Contacts', value: 'callList' }];
  const triOpts = [{ label: 'Alphabétique', value: 'lastname' }, { label: 'N° dossier', value: 'dosNumber' }];
  const optOpts = [{ label: 'Tous', value: 'allCdt' }, { label: 'Un', value: 'oneCdt' }, { label: 'Plage', value: 'rangeCdt' }];

  return (
    <Dialog visible={visible} header="Impression de liste" modal onHide={onHide} style={{ width: '500px' }}>
      <div className="p-fluid">
        <div className="field"><label>Type de liste</label><Dropdown value={type} options={typeOpts} onChange={(e) => setType(e.value)} placeholder="Sélectionner" className="w-full" /></div>
        {type !== 'callList' && <>
          <div className="field"><label>Série</label><Dropdown value={serie} options={serieOpts} onChange={(e) => setSerie(e.value)} placeholder="Sélectionner" className="w-full" /></div>
          {serie !== 'all' && <>
            <div className="field"><label>Option</label><Dropdown value={option} options={optOpts} onChange={(e) => setOption(e.value)} placeholder="Sélectionner" className="w-full" /></div>
            {(option === 'oneCdt' || option === 'rangeCdt') && <>
              <div className="field"><InputText value={debut} onChange={(e) => setDebut(e.target.value.replace(/\D/g, ''))} placeholder="Début" className="w-full" /></div>
              {option === 'rangeCdt' && <div className="field"><InputText value={fin} onChange={(e) => setFin(e.target.value.replace(/\D/g, ''))} placeholder="Fin" className="w-full" /></div>}
            </>}
          </>}
          {type === 'notOfficiel' && <div className="field"><label>Clé de tri</label><Dropdown value={tri} options={triOpts} onChange={(e) => setTri(e.value)} placeholder="Sélectionner" className="w-full" /></div>}
        </>}
        <Button label={loading ? 'Téléchargement...' : 'Télécharger'} icon="pi pi-download" onClick={handleDownload} disabled={loading} className="mt-2" />
      </div>
    </Dialog>
  );
};