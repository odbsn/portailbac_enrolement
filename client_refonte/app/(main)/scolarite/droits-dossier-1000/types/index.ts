export interface ProgramData {
  edition?: number;
  bfem_IfEPI?: number;
  bfem_IfI?: number;
  date_end?: string;
}

export interface Transaction {
  session: string;
  dateTransaction: string;
  phoneNumber: string;
  nbCdtsInscrits: number;
  montantVerser: number;
  paid: boolean;
}

export interface PaymentFormValues {
  nbCdtsInscrits: number;
  phoneNumber: string;
}