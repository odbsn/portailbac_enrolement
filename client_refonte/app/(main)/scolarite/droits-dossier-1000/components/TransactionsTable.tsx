import React, { useRef, useState } from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Tag } from "primereact/tag";

interface Transaction {
  session: string;
  dateTransaction: string;
  phoneNumber: string;
  nbCdtsInscrits: number;
  montantVerser: number;
  paid: boolean;
}

interface TransactionsTableProps {
  transactions: Transaction[];
  loading: boolean;
}

const TransactionsTable: React.FC<TransactionsTableProps> = ({
  transactions,
  loading,
}) => {
  const [globalFilter, setGlobalFilter] = useState<string>("");
  const dt = useRef<any>(null);

  const formatDate = (dateString: string): string => {
    if (!dateString) return "-";
    const date = new Date(dateString.replace(" ", "T"));
    if (isNaN(date.getTime())) return "Date invalide";

    return (
      date.toLocaleDateString("fr-FR", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      }) +
      " à " +
      date.toLocaleTimeString("fr-FR", {
        hour: "2-digit",
        minute: "2-digit",
      })
    );
  };

  const statusBodyTemplate = (rowData: Transaction) => {
    return (
      <Tag
        severity={rowData.paid ? "success" : "danger"}
        value={rowData.paid ? "Validé" : "Non validé"}
        className="text-xs font-medium px-3 py-1 rounded-full"
        icon={rowData.paid ? "pi pi-check-circle" : "pi pi-times-circle"}
      />
    );
  };

  const currencyBodyTemplate = (rowData: Transaction) => {
    return (
      <span className="font-semibold text-gray-700">
        {rowData.montantVerser?.toLocaleString("fr-FR")} FCFA
      </span>
    );
  };

  return (
    <div>
      <div className="transactions-header">
        <div className="transactions-title">
          <div className="transactions-title-icon">
            <i className="pi pi-history"></i>
          </div>
          <div>
            <h2>Historique des transactions</h2>
            {!loading && (
              <p>
                {transactions.length} transaction
                {transactions.length > 1 ? "s" : ""} au total
              </p>
            )}
          </div>
        </div>

        <div className="transactions-search">
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
        ref={dt}
        value={transactions}
        loading={loading}
        paginator
        rows={10}
        rowsPerPageOptions={[10, 20, 50]}
        className="transactions-table text-sm"
        paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
        currentPageReportTemplate="Affichage de {first} à {last} des {totalRecords} enregistrement(s)"
        globalFilter={globalFilter}
        emptyMessage="Aucune transaction trouvée"
        responsiveLayout="scroll"
        sortMode="multiple"
        removableSort
      >
        <Column
          field="session"
          header="Session"
          sortable
          headerClassName="bg-gray-50/80 text-gray-600 font-semibold text-xs uppercase tracking-wider"
          style={{ minWidth: "10rem" }}
        />
        <Column
          field="dateTransaction"
          header="Date"
          sortable
          body={(rowData) => formatDate(rowData.dateTransaction)}
          headerClassName="bg-gray-50/80 text-gray-600 font-semibold text-xs uppercase tracking-wider"
          style={{ minWidth: "12rem" }}
        />
        <Column
          field="phoneNumber"
          header="Téléphone"
          sortable
          headerClassName="bg-gray-50/80 text-gray-600 font-semibold text-xs uppercase tracking-wider"
          style={{ minWidth: "10rem" }}
        />
        <Column
          field="nbCdtsInscrits"
          header="Nombre"
          sortable
          headerClassName="bg-gray-50/80 text-gray-600 font-semibold text-xs uppercase tracking-wider"
          style={{ minWidth: "8rem" }}
        />
        <Column
          field="montantVerser"
          header="Montant"
          sortable
          body={currencyBodyTemplate}
          headerClassName="bg-gray-50/80 text-gray-600 font-semibold text-xs uppercase tracking-wider"
          style={{ minWidth: "12rem" }}
        />
        <Column
          field="paid"
          header="Statut"
          sortable
          body={statusBodyTemplate}
          headerClassName="bg-gray-50/80 text-gray-600 font-semibold text-xs uppercase tracking-wider"
          style={{ minWidth: "10rem" }}
        />
      </DataTable>
    </div>
  );
};

export default TransactionsTable;
