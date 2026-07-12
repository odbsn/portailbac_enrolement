// app/scolarite/enrolement-candidat/utils.ts
import * as Yup from "yup";

// ===== FORMATTERS =====
export const formatDateToInput = (isoDateStr?: string): string => {
  if (!isoDateStr) return "";
  try {
    const date = new Date(isoDateStr);
    if (isNaN(date.getTime())) return "";
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  } catch {
    return "";
  }
};

export const formatDateToISO = (dateStr?: string): string => {
  if (!dateStr) return "";
  const parts = dateStr.split("/");
  if (parts.length !== 3) return "";
  const [day, month, year] = parts;
  return `${year}-${month}-${day}`;
};

export const normalizeText = (text?: string): string => {
  if (!text) return "";
  return text
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toUpperCase();
};

export const calculateAgeFromDate = (
  dateStr: string,
  yearRef?: number,
): number | null => {
  if (!dateStr) return null;
  const parts = dateStr.split("/");
  if (parts.length !== 3) return null;
  const [day, month, year] = parts.map(Number);
  if (isNaN(day) || isNaN(month) || isNaN(year)) return null;
  const refYear = yearRef || new Date().getFullYear();
  let age = refYear - year;
  const today = new Date();
  if (
    today.getMonth() < month - 1 ||
    (today.getMonth() === month - 1 && today.getDate() < day)
  )
    age--;
  return age;
};

export const diffDays = (dateEnd: string): number => {
  if (!dateEnd) return 0;
  const today = new Date().getTime();
  const endDate = new Date(dateEnd).getTime();
  return Math.max(0, Math.ceil((endDate - today) / (1000 * 60 * 60 * 24)));
};

// ===== VALIDATORS =====
export const isValidDate = (dateStr: string): boolean => {
  if (!dateStr) return false;
  const regex = /^(0[1-9]|[12][0-9]|3[01])\/(0[1-9]|1[0-2])\/\d{4}$/;
  if (!regex.test(dateStr)) return false;
  const [day, month, year] = dateStr.split("/").map(Number);
  const date = new Date(year, month - 1, day);
  return (
    date.getFullYear() === year &&
    date.getMonth() === month - 1 &&
    date.getDate() === day
  );
};

export const isValidPhone = (phone: string): boolean => {
  if (!phone) return false;
  const digitsOnly = phone.replace(/\s/g, "");
  if (digitsOnly.length !== 9) return false;
  const allowedPrefixes = ["70", "71", "75", "76", "77", "78"];
  return allowedPrefixes.includes(digitsOnly.slice(0, 2));
};

export const isValidEmail = (email: string): boolean => {
  if (!email) return false;
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email.trim());
};

// ===== VALIDATION SCHEMA =====
export const getValidationSchema = (
  user: any,
  candidats: any[],
  isUpdate: boolean,
) => {
  const currentYear = new Date().getFullYear();

  return {
    serie: Yup.object().nullable().required("Série obligatoire"),
    dosNumber: Yup.string()
      .required("Numéro de dossier obligatoire")
      .matches(/^[0-9]+$/, "Numéro dossier invalide")
      .test(
        "unique",
        "Ce numéro existe déjà",
        (value) => !candidats.some((c) => c.dosNumber === value && !isUpdate),
      ),
    centreEtatCivil: Yup.object()
      .nullable()
      .required("Centre d'état civil obligatoire"),
    year_registry_num: Yup.number()
      .required("Année obligatoire")
      .min(1900, "Année > 1900")
      .max(currentYear, `Année ≤ ${currentYear}`),
    registry_num: Yup.string()
      .required("N° acte obligatoire")
      .matches(/^\S.*$/, "Ne peut pas commencer par un espace"),
    firstname: Yup.string()
      .required("Prénom obligatoire")
      .test(
        "no-space",
        "Ne peut pas commencer par un espace",
        (v) => !v?.startsWith(" "),
      ),
    lastname: Yup.string()
      .required("Nom obligatoire")
      .test(
        "no-space",
        "Ne peut pas commencer par un espace",
        (v) => !v?.startsWith(" "),
      ),
    date_birth: Yup.string()
      .required("Date de naissance obligatoire")
      .test("valid-date", "Format JJ/MM/AAAA", isValidDate),
    place_birth: Yup.string()
      .required("Lieu de naissance obligatoire")
      .test(
        "no-space",
        "Ne peut pas commencer par un espace",
        (v) => !v?.startsWith(" "),
      ),
    gender: Yup.string().required("Genre obligatoire"),
    nationality: Yup.object().nullable().required("Nationalité obligatoire"),
    countryBirth: Yup.object()
      .nullable()
      .required("Pays de naissance obligatoire"),
    phone1: Yup.string()
      .required("Téléphone obligatoire")
      .test("valid-phone", "Numéro invalide", isValidPhone),
    email: Yup.string()
      .email("Email invalide")
      .required("Email obligatoire")
      .matches(/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/, "Email invalide"),
    eps: Yup.string().required("EPS obligatoire"),
    type_handicap: Yup.string().required("Handicap obligatoire"),
    hasBac: Yup.string().oneOf(["yes", "no"]).required("Obligatoire"),
    bac_do_count: Yup.number()
      .min(1, "Minimum 1")
      .when("hasBac", {
        is: "yes",
        then: (schema) => schema.required("Obligatoire"),
      }),
    tableNum: Yup.string().when("hasBac", {
      is: "yes",
      then: (schema) =>
        schema
          .required("Obligatoire")
          .matches(/^[0-9]+$/, "Chiffres uniquement"),
    }),
    yearBac: Yup.string().when("hasBac", {
      is: "yes",
      then: (schema) =>
        schema
          .required("Obligatoire")
          .matches(/^[0-9]{4}$/, "4 chiffres")
          .test("valid-year", `Année 1900-${currentYear}`, (v) => {
            if (!v) return false;
            const y = Number(v);
            return y >= 1900 && y <= currentYear;
          }),
    }),
    year_bfem: Yup.string().when("_", {
      is: () =>
        user?.acteur?.etablissement?.typeCandidat?.name !== "Régulier/Officiel",
      then: (schema) =>
        schema
          .required("Année BFEM obligatoire")
          .matches(/^[0-9]+$/, "Année invalide"),
    }),
    centreExamen: Yup.object()
      .nullable()
      .when("_", {
        is: () => user?.acteur?.etablissement?.typeEtablissement?.code === "I",
        then: (schema) => schema.required("Centre d'examen obligatoire"),
      }),
  };
};
