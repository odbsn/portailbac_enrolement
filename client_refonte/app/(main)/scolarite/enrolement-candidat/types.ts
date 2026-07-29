// app/scolarite/enrolement-candidat/types.ts

export interface Candidat {
  id?: number;
  dosNumber: string;
  firstname: string;
  lastname: string;
  date_birth: string;
  place_birth: string;
  adresse?: string;
  gender: string;
  phone1: string;
  phone2?: string;
  email: string;
  year_registry_num: number;
  registry_num: string;
  bac_do_count: number;
  year_bfem?: number;
  origine_bfem?: string;
  subject?: string;
  handicap: boolean;
  type_handicap: string;
  eps: string;
  decision: number;
  etablissement?: any;
  centreEtatCivil?: any;
  centreExamen?: any;
  typeCandidat?: any;
  serie?: any;
  matiere1?: any;
  matiere2?: any;
  matiere3?: any;
  matiere4?: any;
  nationality?: any;
  countryBirth?: any;
  eprFacListA?: string;
  eprFacListB?: any;
  session: number;
  rejets?: any[];
  codeEnrolementEC?: string;
  hasBac?: 'yes' | 'no';
  tableNum?: string;
  yearBac?: string;
  codeCentre?: string;
  concoursGeneral?: any;
}

// DTO compatible avec le service existant
export interface CandidatDTO {
  dosNumber: string;
  session: number;
  firstname: string;
  lastname: string;
  date_birth: string;
  place_birth: string;
  adresse: string;
  gender: string;
  phone1: string;
  phone2?: string;
  email: string;
  year_registry_num: number;
  registry_num: string;
  bac_do_count: number;
  year_bfem: number;
  origine_bfem: string;
  subject: string;
  handicap: boolean;
  type_handicap: string;
  eps: string;
  alreadyBac: boolean;
  decision: number;
  etablissement: any;
  centreEtatCivil: any;
  centreExamen?: any;
  typeCandidat?: any;
  serie: any;
  matiere1?: any;
  matiere2?: any;
  matiere3?: any;
  nationality: any;
  countryBirth: any;
  eprFacListA: string;
  eprFacListB?: any;
  concoursGeneral?: any;
  codeEnrolementEC?: string;
}

export type FormMode = 'create' | 'edit' | 'view';

export interface DialogState {
  form: { visible: boolean; mode: FormMode };
  print: boolean;
  confirm: { visible: boolean; title: string; message: string; onConfirm: () => void };
  result: { visible: boolean; data: any; loading: boolean };
}

export interface SelectOption {
  label: string;
  value: string;
}