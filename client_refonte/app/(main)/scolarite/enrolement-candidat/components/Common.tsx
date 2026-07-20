// app/scolarite/enrolement-candidat/components/Common.tsx
import React, { useMemo } from "react";
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";
import { InputMask } from "primereact/inputmask";
import { Button } from "primereact/button";

// ===== FormField =====
interface FormFieldProps {
  label: string;
  required?: boolean;
  error?: any;
  col?: number;
  children: React.ReactNode;
}

export const FormField: React.FC<FormFieldProps> = ({
  label,
  required = false,
  error,
  col = 12,
  children,
}) => {
  return (
    <div className={`col-${col}`}>
      <div className="field">
        <label className="block text-sm font-medium text-gray-700 mb-1">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
        {children}
        {error && <small className="p-error block mt-1">{error}</small>}
      </div>
    </div>
  );
};

// ===== CustomFieldset =====
interface CustomFieldsetProps {
  legend: string;
  children: React.ReactNode;
}

export const CustomFieldset: React.FC<CustomFieldsetProps> = ({
  legend,
  children,
}) => {
  return (
    <fieldset className="custom-fieldset">
      <legend>{legend}</legend>
      {children}
    </fieldset>
  );
};

// ===== StatusBadge =====
interface StatusBadgeProps {
  decision: number;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ decision }) => {
  if (decision === 1) {
    return (
      <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-semibold">
        Validé
      </span>
    );
  }
  if (decision === 2) {
    return (
      <span className="px-3 py-1 bg-red-100 text-red-700 rounded-full text-sm font-semibold">
        Rejeté
      </span>
    );
  }
  return (
    <span className="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-sm font-semibold">
      En attente
    </span>
  );
};

// ===== CountrySelect avec drapeaux =====
interface CountrySelectProps {
  formik: any;
  fieldName: string;
  label: string;
  pays: any[];
  required?: boolean;
  disabled?: boolean;
  col?: number;
}

const getCountryCode = (countryName: string): string => {
  const countryMap: { [key: string]: string } = {
    Sénégal: "SN",
    France: "FR",
    Mali: "ML",
    Guinée: "GN",
    "Côte d'Ivoire": "CI",
    "Burkina Faso": "BF",
    Bénin: "BJ",
    Niger: "NE",
    Togo: "TG",
    Mauritanie: "MR",
    Algérie: "DZ",
    Maroc: "MA",
    Tunisie: "TN",
    Égypte: "EG",
    Cameroun: "CM",
    Nigeria: "NG",
    Ghana: "GH",
    Congo: "CG",
    "République démocratique du Congo": "CD",
    Gabon: "GA",
    Canada: "CA",
    "États-Unis": "US",
    Belgique: "BE",
    Suisse: "CH",
    "Royaume-Uni": "GB",
    Allemagne: "DE",
    Espagne: "ES",
    Portugal: "PT",
    Italie: "IT",
  };
  return countryMap[countryName] || "";
};

const countryOptionTemplate = (option: any) => {
  if (!option) return null;
  const code = option.code || option.iso2 || getCountryCode(option.name);
  return (
    <div className="flex align-items-center gap-2">
      {code && (
        <img
          src={`https://flagcdn.com/w20/${code.toLowerCase()}.png`}
          srcSet={`https://flagcdn.com/w40/${code.toLowerCase()}.png 2x`}
          width="20"
          height="15"
          alt={option.name}
          loading="lazy"
          className="mr-2"
          style={{ objectFit: "cover" }}
        />
      )}
      <span>{option.name}</span>
    </div>
  );
};

const selectedCountryTemplate = (option: any) => {
  if (!option) return null;
  const code = option.code || option.iso2 || getCountryCode(option.name);
  return (
    <div className="flex align-items-center gap-2">
      {code && (
        <img
          src={`https://flagcdn.com/w20/${code.toLowerCase()}.png`}
          srcSet={`https://flagcdn.com/w40/${code.toLowerCase()}.png 2x`}
          width="20"
          height="15"
          alt={option.name}
          loading="lazy"
          style={{ objectFit: "cover" }}
        />
      )}
      <span>{option.name}</span>
    </div>
  );
};

export const CountrySelect: React.FC<CountrySelectProps> = ({
  formik,
  fieldName,
  label,
  pays,
  required = false,
  disabled = false,
  col = 6,
}) => {
  const error = formik.touched[fieldName] && formik.errors[fieldName];

  const sortedPays = useMemo(() => {
    return [...pays].sort((a, b) => a.name.localeCompare(b.name));
  }, [pays]);

  return (
    <FormField label={label} required={required} error={error} col={col}>
      <Dropdown
        id={fieldName}
        name={fieldName}
        value={formik.values[fieldName]}
        onChange={(e) => formik.setFieldValue(fieldName, e.value)}
        options={sortedPays}
        optionLabel="name"
        placeholder={`Sélectionner ${label}`}
        disabled={disabled}
        filter
        filterMatchMode="contains"
        showFilterClear
        scrollHeight="250px"
        virtualScrollerOptions={{ itemSize: 38 }}
        itemTemplate={countryOptionTemplate}
        valueTemplate={selectedCountryTemplate}
        className={`p-inputtext-sm w-full ${error ? "p-invalid" : ""}`}
      />
    </FormField>
  );
};

// ===== CentreEtatCivilSelect =====
interface CentreEtatCivilSelectProps {
  formik: any;
  fieldName: string;
  label: string;
  cecs: any[];
  required?: boolean;
  disabled?: boolean;
  col?: number;
}

const centreOptionTemplate = (option: any) => {
  if (!option) return null;
  return (
    <div className="flex align-items-center gap-2">
      <span className="font-bold text-blue-600">{option.code}</span>
      <span className="text-gray-400">-</span>
      <span>{option.name}</span>
    </div>
  );
};

const selectedCentreTemplate = (option: any) => {
  if (!option) return <span>Sélectionner un centre</span>;
  return (
    <div className="flex align-items-center gap-2">
      <span className="font-bold text-blue-600">{option.code}</span>
      <span className="text-gray-400">-</span>
      <span>{option.name}</span>
    </div>
  );
};

export const CentreEtatCivilSelect: React.FC<CentreEtatCivilSelectProps> = ({
  formik,
  fieldName,
  label,
  cecs,
  required = false,
  disabled = false,
  col = 6,
}) => {
  const error = formik.touched[fieldName] && formik.errors[fieldName];

  const sortedCecs = useMemo(() => {
    if (!cecs || !Array.isArray(cecs)) return [];
    return [...cecs].sort((a, b) => a.name?.localeCompare(b.name) || 0);
  }, [cecs]);

  return (
    <FormField label={label} required={required} error={error} col={col}>
      <Dropdown
        id={fieldName}
        name={fieldName}
        value={formik.values[fieldName]}
        onChange={(e) => {
          formik.setFieldValue(fieldName, e.value);
          formik.setFieldValue("codeCentre", e.value?.code || "");
        }}
        options={sortedCecs}
        optionLabel="name"
        placeholder="Sélectionner un centre d'état civil"
        disabled={disabled}
        filter
        filterMatchMode="contains"
        showFilterClear
        scrollHeight="250px"
        virtualScrollerOptions={{ itemSize: 38 }}
        itemTemplate={centreOptionTemplate}
        valueTemplate={selectedCentreTemplate}
        className={`p-inputtext-sm w-full ${error ? "p-invalid" : ""}`}
      />
    </FormField>
  );
};

// ===== PhoneInput =====
interface PhoneInputProps {
  formik: any;
  fieldName: string;
  label: string;
  required?: boolean;
  disabled?: boolean;
  col?: number;
}

export const PhoneInput: React.FC<PhoneInputProps> = ({
  formik,
  fieldName,
  label,
  required = false,
  disabled = false,
  col = 12,
}) => {
  const error = formik.touched[fieldName] && formik.errors[fieldName];

  return (
    <FormField label={label} required={required} error={error} col={col}>
      <InputMask
        id={fieldName}
        name={fieldName}
        value={formik.values[fieldName] || ""}
        onChange={(e) => formik.setFieldValue(fieldName, e.value)}
        onBlur={formik.handleBlur}
        disabled={disabled}
        mask="99 999 99 99"
        placeholder="77 777 77 77"
        autoClear={false}
        className={`p-inputtext-sm w-full ${error ? "p-invalid" : ""}`}
      />
    </FormField>
  );
};
