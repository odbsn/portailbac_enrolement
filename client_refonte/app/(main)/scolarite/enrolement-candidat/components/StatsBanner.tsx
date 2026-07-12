// app/scolarite/enrolement-candidat/components/StatsBanner.tsx
import React from 'react';

export const StatsBanner: React.FC<{ edition?: number; faeb?: any; nbrTCdts?: number; diffDays?: number; typeEtab?: string }> = 
  ({ edition, faeb, nbrTCdts, diffDays, typeEtab }) => {
  const remaining = faeb ? Number(faeb.count_1000_OB) - Number(nbrTCdts) : 0;
  return (
    <div className="mb-4 p-3 surface-100 border-round-lg flex flex-wrap align-items-center gap-3">
      {typeEtab && <span className="font-bold text-primary">Type : <span className="text-600">{typeEtab}</span></span>}
      {edition && <span className="font-bold text-primary">Édition : <span className="text-600">{edition}</span></span>}
      {faeb ? <span className="font-bold text-red-500">👉 Il vous reste {remaining} vignette{remaining > 1 ? 's' : ''}</span>
        : <span className="font-bold text-red-500">❌ Payez les droits de dossier à 1000 FCFA</span>}
      {diffDays !== undefined && <span className={`font-bold ${diffDays > 0 ? 'text-red-500' : 'text-orange-500'}`}>
        {diffDays > 0 ? `⏳ Il reste ${diffDays} jour${diffDays > 1 ? 's' : ''}` : '⚠️ Période d\'ouverture terminée'}
      </span>}
      <span className="font-bold text-blue-500">📊 {nbrTCdts || 0} candidat{nbrTCdts && nbrTCdts > 1 ? 's' : ''}</span>
    </div>
  );
};