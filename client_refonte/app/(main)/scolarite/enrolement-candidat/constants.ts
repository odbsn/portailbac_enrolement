// app/scolarite/enrolement-candidat/constants.ts

export const SEXE_OPTIONS: { label: string; value: string }[] = [
  { label: 'M', value: 'M' },
  { label: 'F', value: 'F' }
];

export const HANDICAP_OPTIONS: { label: string; value: string }[] = [
  { label: 'Aucun', value: 'Néant' },
  { label: 'Aveugle', value: 'Aveugle' },
  { label: 'Mal Entendant', value: 'Mal Entendant' },
  { label: 'Mal Voyant', value: 'Mal Voyant' },
  { label: 'Sourd-Muet', value: 'Sourd-Muet' },
  { label: 'Moteur', value: 'Moteur' },
  { label: 'Autre', value: 'Autre' }
];

export const EPS_OPTIONS: { label: string; value: string }[] = [
  { label: 'Apte', value: 'Apte' },
  { label: 'Inapte', value: 'Inapte' }
];

export const EF_OPTIONS: { label: string; value: string }[] = [
  { label: 'Dessin', value: 'Dessin' },
  { label: 'Couture', value: 'Couture' },
  { label: 'Musique', value: 'Musique' }
];

export const TYPE_LISTE: { label: string; value: string }[] = [
  { label: 'Provisoire', value: 'notOfficiel' },
  { label: 'Liste des contacts', value: 'callList' }
];

export const CLE_DE_TRIE: { label: string; value: string }[] = [
  { label: 'Ordre Alphabétique', value: 'lastname' },
  { label: 'Numéro de dossier', value: 'dosNumber' }
];

export const OPTION_PRINT: { label: string; value: string }[] = [
  { label: 'Tous les candidats', value: 'allCdt' },
  { label: 'Un candidat', value: 'oneCdt' },
  { label: 'Plage de candidats', value: 'rangeCdt' }
];

export const ORIGINE_OPTIONS: { label: string; value: string }[] = [
  { label: 'National', value: 'National' },
  { label: 'Etranger', value: 'Etranger' }
];