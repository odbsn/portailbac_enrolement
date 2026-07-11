// app/scolarite/enrolement-candidat/components/StatsCards.tsx
import React from 'react';

const Card: React.FC<{ title: string; value: number | string; icon: string; color: string }> = 
  ({ title, value, icon, color }) => (
  <div className={`p-3 border-round-lg shadow-1 ${color}`}>
    <div className="flex align-items-center justify-content-between">
      <div><div className="text-500 text-sm font-medium">{title}</div><div className="text-2xl font-bold mt-1">{value}</div></div>
      <i className={`${icon} text-3xl opacity-60`} />
    </div>
  </div>
);

export const StatsCards: React.FC<{ stats: { total: number; validated: number; rejected: number; pending: number }; edition?: number }> = 
  ({ stats, edition }) => (
  <div className="grid mb-3">
    <div className="col-12 md:col-3"><Card title="Total" value={stats.total} icon="pi pi-users" color="bg-blue-50 text-blue-700" /></div>
    <div className="col-12 md:col-3"><Card title="Validés" value={stats.validated} icon="pi pi-check-circle" color="bg-green-50 text-green-700" /></div>
    <div className="col-12 md:col-3"><Card title="Rejetés" value={stats.rejected} icon="pi pi-times-circle" color="bg-red-50 text-red-700" /></div>
    <div className="col-12 md:col-3"><Card title="En attente" value={stats.pending} icon="pi pi-clock" color="bg-yellow-50 text-yellow-700" /></div>
    {edition && <div className="col-12 text-center text-500 text-sm">Édition {edition}</div>}
  </div>
);