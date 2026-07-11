// app/scolarite/enrolement-candidat/components/CandidatureTable.tsx
import React, { useState } from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { StatusBadge } from "./Common";

interface CandidatureTableProps {
  candidats: any[];
  loading: boolean;
  onEdit: (c: any) => void;
  onDelete: (c: any) => void;
  onView: (c: any) => void;
  diffDays: number;
}

export const CandidatureTable: React.FC<CandidatureTableProps> = ({
  candidats,
  loading,
  onEdit,
  onDelete,
  onView,
  diffDays,
}) => {
  const [filter, setFilter] = useState("");

  const actionBody = (row: any) => {
    if (diffDays <= 0) {
      return (
        <Button
          icon="pi pi-eye"
          rounded
          severity="success"
          onClick={() => onView(row)}
        />
      );
    }
    if (row.decision === 1) {
      return (
        <div className="flex items-center gap-2 text-green-600">
          <span>✅</span>
          <span className="text-sm font-semibold">Validé</span>
        </div>
      );
    }
    return (
      <div className="flex gap-2">
        <Button
          icon="pi pi-user-edit"
          rounded
          severity="warning"
          onClick={() => onEdit(row)}
        />
        {row.decision !== 2 && (
          <Button
            icon="pi pi-trash"
            rounded
            severity="danger"
            onClick={() => onDelete(row)}
          />
        )}
      </div>
    );
  };

  const dateBody = (row: any) => {
    if (!row.date_birth) return "-";
    const d = new Date(row.date_birth);
    return `${String(d.getDate()).padStart(2, "0")}/${String(
      d.getMonth() + 1,
    ).padStart(2, "0")}/${d.getFullYear()}`;
  };

  const header = (
    <div className="flex flex-wrap align-items-center justify-content-between gap-2">
      <span className="text-xl font-semibold">Liste des candidats</span>
      <span className="p-input-icon-left">
        <i className="pi pi-search" />
        <InputText
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          placeholder="Recherche..."
          className="p-inputtext-sm"
        />
      </span>
    </div>
  );

  return (
    <DataTable
      value={candidats}
      loading={loading}
      paginator
      rows={10}
      rowsPerPageOptions={[5, 10, 25, 50]}
      className="p-datatable-sm"
      globalFilter={filter}
      emptyMessage="Aucun candidat trouvé"
      header={header}
      rowClassName={(r) => {
        if (r.decision === 1) return "accepted-row";
        if (r.decision === 2) return "rejected-row";
        return "";
      }}
    >
      <Column
        field="dosNumber"
        header="N° dossier"
        sortable
        headerStyle={{ minWidth: "6rem" }}
      />
      <Column
        field="firstname"
        header="Prénom(s)"
        sortable
        headerStyle={{ minWidth: "12rem" }}
      />
      <Column
        field="lastname"
        header="Nom"
        sortable
        headerStyle={{ minWidth: "10rem" }}
      />
      <Column
        field="date_birth"
        header="Date naiss."
        body={dateBody}
        sortable
        headerStyle={{ minWidth: "8rem" }}
      />
      <Column
        field="place_birth"
        header="Lieu naiss."
        sortable
        headerStyle={{ minWidth: "10rem" }}
      />
      <Column
        field="gender"
        header="Sexe"
        sortable
        headerStyle={{ minWidth: "4rem" }}
      />
      <Column
        field="decision"
        header="Statut"
        body={(r) => <StatusBadge decision={r.decision} />}
        sortable
        headerStyle={{ minWidth: "8rem" }}
      />
      <Column
        body={actionBody}
        header="Actions"
        headerStyle={{ minWidth: "8rem" }}
        style={{ textAlign: "center" }}
      />
    </DataTable>
  );
};
