// app/scolarite/enrolement-candidat/components/CandidatureList.tsx
import React from 'react';
import { TabView, TabPanel } from 'primereact/tabview';
import { Toolbar } from 'primereact/toolbar';
import { Button } from 'primereact/button';
import { CandidatureTable } from './CandidatureTable';

export const CandidatureList: React.FC<{ groupedCdts: any[]; loading: boolean; onEdit: (c: any) => void; onDelete: (c: any) => void; onView: (c: any) => void; onAdd: () => void; onPrint: () => void; diffDays: number; canAdd: boolean }> = 
  ({ groupedCdts, loading, onEdit, onDelete, onView, onAdd, onPrint, diffDays, canAdd }) => {

  const leftToolbar = () => <div className="my-1">
    {canAdd && <Button severity="success" label="Ajouter" icon="pi pi-plus" className="p-button-sm mr-2" onClick={onAdd} />}
    <Button label="Imprimer" icon="pi pi-print" className="p-button-sm" onClick={onPrint} />
  </div>;

  if (groupedCdts?.length > 0 && 'serieName' in groupedCdts[0]) {
    return (
      <>
        <Toolbar className="mb-4" left={leftToolbar} />
        <TabView>
          {groupedCdts.map(({ serieName, cdts }) => (
            <TabPanel key={serieName} header={serieName}>
              <CandidatureTable candidats={cdts} loading={loading} onEdit={onEdit} onDelete={onDelete} onView={onView} diffDays={diffDays} />
            </TabPanel>
          ))}
        </TabView>
      </>
    );
  }

  return (
    <>
      <Toolbar className="mb-4" left={leftToolbar} />
      <CandidatureTable candidats={groupedCdts} loading={loading} onEdit={onEdit} onDelete={onDelete} onView={onView} diffDays={diffDays} />
    </>
  );
};