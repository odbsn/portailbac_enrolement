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
  const [globalFilter, setGlobalFilter] = useState("");

  const actionBody = (row: any) => {
    // Si période fermée, uniquement voir
    if (diffDays <= 0) {
      return (
        <Button
          icon="pi pi-eye"
          rounded
          className="btn-action btn-action-view"
          onClick={() => onView(row)}
        />
      );
    }

    // Si déjà validé
    if (row.decision === 1) {
      return (
        <div className="status-badge status-badge-success">
          <i className="pi pi-check-circle"></i>
          Validé
        </div>
      );
    }

    return (
      <div className="action-buttons">
        <Button
          icon="pi pi-user-edit"
          rounded
          className="btn-action btn-action-edit"
          onClick={() => onEdit(row)}
        />
        {row.decision !== 2 && (
          <Button
            icon="pi pi-trash"
            rounded
            className="btn-action btn-action-delete"
            onClick={() => onDelete(row)}
          />
        )}
        <Button
          icon="pi pi-eye"
          rounded
          className="btn-action btn-action-view"
          onClick={() => onView(row)}
        />
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

  return (
    <div className="candidature-table-wrapper">
      <div className="table-header">
        <div className="table-title">
          <div className="table-title-icon">
            <i className="pi pi-list"></i>
          </div>
          <div>
            <h2>Liste des candidats</h2>
            {!loading && (
              <p>
                {candidats.length} candidat{candidats.length > 1 ? "s" : ""} au
                total
              </p>
            )}
          </div>
        </div>
        <div className="table-search">
          <i className="pi pi-search"></i>
          <input
            type="text"
            placeholder="Rechercher..."
            value={globalFilter}
            onChange={(e) => setGlobalFilter(e.target.value)}
          />
        </div>
      </div>

      <DataTable
        value={candidats}
        loading={loading}
        paginator
        rows={10}
        rowsPerPageOptions={[10, 20, 50]}
        className="custom-datatable"
        globalFilter={globalFilter}
        emptyMessage="Aucun candidat trouvé"
        responsiveLayout="scroll"
        sortMode="multiple"
        removableSort
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
          headerClassName="table-header-cell"
          style={{ minWidth: "8rem" }}
        />
        <Column
          field="firstname"
          header="Prénom(s)"
          sortable
          headerClassName="table-header-cell"
          style={{ minWidth: "12rem" }}
        />
        <Column
          field="lastname"
          header="Nom"
          sortable
          headerClassName="table-header-cell"
          style={{ minWidth: "10rem" }}
        />
        <Column
          field="date_birth"
          header="Date naiss."
          body={dateBody}
          sortable
          headerClassName="table-header-cell"
          style={{ minWidth: "8rem" }}
        />
        <Column
          field="place_birth"
          header="Lieu naiss."
          sortable
          headerClassName="table-header-cell"
          style={{ minWidth: "10rem" }}
        />
        <Column
          field="gender"
          header="Sexe"
          sortable
          headerClassName="table-header-cell"
          style={{ minWidth: "5rem" }}
        />
        <Column
          field="decision"
          header="Statut"
          body={(r) => <StatusBadge decision={r.decision} />}
          sortable
          headerClassName="table-header-cell"
          style={{ minWidth: "8rem" }}
        />
        <Column
          body={actionBody}
          header="Actions"
          headerClassName="table-header-cell"
          style={{ minWidth: "10rem", textAlign: "center" }}
        />
      </DataTable>
    </div>
  );
};
