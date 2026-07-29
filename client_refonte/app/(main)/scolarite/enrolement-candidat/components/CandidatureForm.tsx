// app/scolarite/enrolement-candidat/components/CandidatureForm.tsx
import React, { useEffect, useState, useMemo, useCallback } from "react";
import { Dialog } from "primereact/dialog";
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";
import { InputMask, InputMaskChangeEvent } from "primereact/inputmask";
import { Button } from "primereact/button";
import { RadioButton } from "primereact/radiobutton";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Skeleton } from "primereact/skeleton";
import { useCandidatureForm, useFormikLocalStorage } from "../hooks";
import {
  CustomFieldset,
  FormField,
  CountrySelect,
  PhoneInput,
  StatusBadge,
  CentreEtatCivilSelect,
} from "./Common";
import {
  SEXE_OPTIONS,
  HANDICAP_OPTIONS,
  EPS_OPTIONS,
  EF_OPTIONS,
} from "../constants";
import { calculateAgeFromDate } from "../utils";

// Clés localStorage utilisées par useFormikLocalStorage pour la série/matières.
const AUTOSAVE_KEYS = ["serie", "matiere1", "matiere2", "matiere3"];

// Composant Dropdown optimisé avec skeleton
const OptimizedDropdown = React.memo(
  ({
    options,
    optionLabel,
    placeholder,
    value,
    onChange,
    disabled,
    filter,
    className,
    loading,
    id,
    name,
    ...props
  }: any) => {
    if (loading) {
      return <Skeleton width="100%" height="38px" className="p-inputtext-sm" />;
    }

    return (
      <Dropdown
        id={id}
        name={name}
        value={value || null}
        onChange={onChange}
        options={options || []}
        optionLabel={optionLabel}
        placeholder={placeholder}
        disabled={disabled}
        filter={filter}
        className={className}
        filterMatchMode="contains"
        showFilterClear
        scrollHeight="250px"
        {...props}
      />
    );
  },
);

OptimizedDropdown.displayName = "OptimizedDropdown";

const SectionTitle: React.FC<{ icon: string; label: string }> = ({
  icon,
  label,
}) => (
  <div className="candidature-section-title">
    <div className="section-title-icon-wrapper">
      <i className={`pi ${icon}`} />
    </div>
    <span>{label}</span>
  </div>
);

export const CandidatureForm: React.FC<{
  visible: boolean;
  onHide: () => void;
  user: any;
  prog: any;
  candidats: any[];
  cecs: any[];
  pays: any[];
  series: any[];
  matieres: any[];
  cexam: any[];
  centreExamenEtab: any[];
  onSuccess: () => void;
  mode: "create" | "edit" | "view";
  initialData?: any;
  onShowResult: (d: any, l: boolean) => void;
}> = ({
  visible,
  onHide,
  user,
  prog,
  candidats,
  cecs,
  pays,
  series,
  matieres,
  cexam,
  centreExamenEtab,
  onSuccess,
  mode,
  initialData,
  onShowResult,
}) => {
  const isView = mode === "view";

  // Optimisation des options avec useMemo
  const memoizedPays = useMemo(() => pays, [pays]);
  const memoizedSeries = useMemo(() => series, [series]);
  const memoizedCecs = useMemo(() => cecs, [cecs]);
  const memoizedMatieres = useMemo(() => matieres, [matieres]);
  const memoizedCexam = useMemo(() => cexam, [cexam]);
  const memoizedSexeOptions = useMemo(() => SEXE_OPTIONS, []);
  const memoizedHandicapOptions = useMemo(() => HANDICAP_OPTIONS, []);
  const memoizedEpsOptions = useMemo(() => EPS_OPTIONS, []);
  const memoizedEfOptions = useMemo(() => EF_OPTIONS, []);

  const {
    formik,
    age,
    loading,
    isSaving,
    baseMorte,
    rejets,
    loadCandidatForEdit,
    resetForm,
    handleSearchBac,
    setBaseMorte,
    calculateAge,
  } = useCandidatureForm(
    user,
    prog,
    candidats,
    cecs,
    matieres,
    onSuccess,
    onShowResult,
  );

  useEffect(() => {
    if (initialData && mode !== "create") loadCandidatForEdit(initialData);
  }, [initialData, mode]);

  const handleClose = () => {
    if (mode === "create") {
      AUTOSAVE_KEYS.forEach((k) => localStorage.removeItem(k));
    }
    resetForm();
    onHide();
  };

  const handleSerieChange = useFormikLocalStorage(formik, "serie", [
    "matiere1",
    "matiere2",
    "matiere3",
    "matiere4",
  ]);
  const handleMatiere1Change = useFormikLocalStorage(formik, "matiere1");
  const handleMatiere2Change = useFormikLocalStorage(formik, "matiere2");
  const handleMatiere3Change = useFormikLocalStorage(formik, "matiere3");

  // Optimisation des fonctions avec useCallback
  const getAvailableOptions = useCallback(
    (
      code: string,
      order: number,
      exclude1?: string,
      exclude2?: string,
      exclude3?: string,
    ) => {
      if (!code) return [];
      const all = memoizedMatieres.filter(
        (m: any) => m.serie.code === code && m.order === order,
      );
      const unique = new Map();
      all.forEach((m: any) => {
        if (
          !unique.has(m.name) &&
          m.name !== exclude1 &&
          m.name !== exclude2 &&
          m.name !== exclude3
        )
          unique.set(m.name, m);
      });
      return Array.from(unique.values());
    },
    [memoizedMatieres],
  );

  const getAvailableOptions2 = useCallback(
    (
      code: string,
      order: number,
      exclude1?: string,
      exclude2?: string,
      exclude3?: string,
    ) => {
      if (!code) return [];
      const excluded = [
        "SN",
        "ECONOMIE",
        "GENIE MECANIQUE",
        "GENIE ELECTRIQUE",
        "FRANCAIS",
      ];
      return memoizedMatieres.filter((m: any) => {
        if (m.order !== order) return false;
        const isExcluded =
          excluded.includes(m.name) ||
          m.name === exclude1 ||
          m.name === exclude2 ||
          m.name === exclude3;
        const isSTEG =
          code === "STEG" && ["ANGLAIS", "ESPAGNOL"].includes(m.name);
        const isLangue =
          ["L1A", "S1A", "S2A", "LA", "L-AR", "S1AR", "S2AR"].includes(code) &&
          ["ARABE MODERNE", "ARABE CLASSIQUE"].includes(m.name);
        const isAnglais =
          [
            "S1A",
            "S2A",
            "S1",
            "S2",
            "S3",
            "S4",
            "S5",
            "T1",
            "T2",
            "STIDD",
            "F6",
          ].includes(code) && m.name === "ANGLAIS";
        const isLatin = ["L1A", "L1B"].includes(code) && m.name === "LATIN";
        return !isExcluded && !isSTEG && !isLangue && !isAnglais && !isLatin;
      });
    },
    [memoizedMatieres],
  );

  const handleDateChange = (e: InputMaskChangeEvent) => {
    const value = e.value || "";
    formik.setFieldValue("date_birth", value);
    if (value) {
      const a = calculateAgeFromDate(value, prog?.edition);
      if (a !== null) calculateAge(value);
    }
  };

  const handleHandicapChange = (value: string) => {
    formik.setFieldValue("type_handicap", value);
    if (value === "Néant") {
      formik.setFieldValue("eps", "Apte");
      formik.setFieldValue("handicap", false);
    } else {
      formik.setFieldValue("handicap", true);
    }
  };

  const disabled = isView || !!baseMorte;

  const showMatieresOptionnelles =
    formik.values.serie?.code &&
    ![
      "F6",
      "L-AR",
      "LA",
      "S1",
      "S2",
      "S1A",
      "S2A",
      "S3",
      "S4",
      "S5",
      "T1",
      "T2",
      "STEG",
      "S1AR",
      "S2AR",
    ].includes(formik.values.serie.code);

  return (
    <Dialog
      visible={visible}
      style={{ width: "98vw", maxHeight: "98vh", height: "98vh" }}
      header={
        <span className="candidature-dialog-title">
          <i
            className={`pi ${
              isView
                ? "pi-eye"
                : mode === "edit"
                ? "pi-user-edit"
                : "pi-user-plus"
            }`}
          />
          {isView
            ? "Consulter un dossier"
            : mode === "edit"
            ? "Modifier le dossier"
            : "Nouveau dossier de candidature"}
        </span>
      }
      modal
      className="candidature-dialog p-fluid"
      onHide={handleClose}
      contentStyle={{
        maxHeight: "98vh",
        height: "calc(98vh - 80px)",
        display: "flex",
        flexDirection: "column",
        padding: 0,
      }}
    >
      {age !== null && age <= 17 && (
        <div className="candidature-alert candidature-alert--warning">
          <i className="pi pi-exclamation-triangle" />
          Attention, justifier son cursus à la réception !
        </div>
      )}

      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (!isView) formik.handleSubmit(e);
        }}
        className="candidature-form-body"
      >
        <div className="candidature-columns-wrapper">
          <div className="candidature-columns">
            {/* Colonne gauche : Scolarité + Acte naissance + Identité + Contact */}
            <div className="candidature-col candidature-col--left">
              {/* Scolarité */}
              {/* <SectionTitle
                icon="pi-graduation-cap"
                label="Informations personnelles & Scolaires"
              /> */}
              <CustomFieldset legend="Informations scolaires">
                <div className="grid">
                  <FormField
                    label="Série"
                    required
                    error={formik.touched.serie && formik.errors.serie}
                    col={5}
                  >
                    <OptimizedDropdown
                      id="serie"
                      name="serie"
                      value={formik.values.serie}
                      onChange={(e) => handleSerieChange(e.value)}
                      options={memoizedSeries}
                      optionLabel="code"
                      placeholder="Sélectionner"
                      filter
                      disabled={disabled}
                      loading={loading}
                      className={`p-inputtext-sm w-full ${
                        formik.touched.serie && formik.errors.serie
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <FormField
                    label="N° dossier"
                    required
                    error={formik.touched.dosNumber && formik.errors.dosNumber}
                    col={4}
                  >
                    <InputText
                      id="dosNumber"
                      name="dosNumber"
                      value={formik.values.dosNumber}
                      onChange={(e) => {
                        const v = e.target.value;
                        if (/^\d*$/.test(v) && !/^0\d*/.test(v))
                          formik.setFieldValue("dosNumber", v);
                      }}
                      onBlur={formik.handleBlur}
                      disabled={disabled}
                      className={`p-inputtext-sm w-full ${
                        formik.touched.dosNumber && formik.errors.dosNumber
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <FormField label="Nbre passages" required col={3}>
                    <InputText
                      id="bac_do_count"
                      name="bac_do_count"
                      value={formik.values.bac_do_count}
                      disabled
                      className="p-inputtext-sm w-full"
                    />
                  </FormField>
                </div>
                <div className="grid mt-2">
                  <FormField label="Déjà fait le BAC ?" required col={4}>
                    <div className="flex gap-4 mt-2">
                      {["yes", "no"].map((v) => (
                        <div key={v} className="flex align-items-center gap-2">
                          <RadioButton
                            inputId={`hasBac-${v}`}
                            name="hasBac"
                            value={v}
                            onChange={(e) =>
                              formik.setFieldValue("hasBac", e.value)
                            }
                            checked={formik.values.hasBac === v}
                            disabled={disabled}
                          />
                          <label htmlFor={`hasBac-${v}`}>
                            {v === "yes" ? "Oui" : "Non"}
                          </label>
                        </div>
                      ))}
                    </div>
                  </FormField>
                  {formik.values.hasBac === "yes" && (
                    <>
                      <FormField
                        label="N° table"
                        required
                        error={
                          formik.touched.tableNum && formik.errors.tableNum
                        }
                        col={3}
                      >
                        <InputText
                          id="tableNum"
                          name="tableNum"
                          value={formik.values.tableNum}
                          disabled={disabled}
                          onChange={(e) =>
                            formik.setFieldValue(
                              "tableNum",
                              e.target.value.replace(/\D/g, ""),
                            )
                          }
                          className={`p-inputtext-sm w-full ${
                            formik.touched.tableNum && formik.errors.tableNum
                              ? "p-invalid"
                              : ""
                          }`}
                        />
                      </FormField>
                      <FormField
                        label="Année BAC"
                        required
                        error={formik.touched.yearBac && formik.errors.yearBac}
                        col={3}
                      >
                        <InputText
                          id="yearBac"
                          name="yearBac"
                          value={formik.values.yearBac}
                          disabled={disabled}
                          onChange={(e) =>
                            formik.setFieldValue(
                              "yearBac",
                              e.target.value.replace(/\D/g, "").slice(0, 4),
                            )
                          }
                          className={`p-inputtext-sm w-full ${
                            formik.touched.yearBac && formik.errors.yearBac
                              ? "p-invalid"
                              : ""
                          }`}
                        />
                      </FormField>
                      <div
                        className="col-2 flex align-items-center"
                        style={{ paddingTop: "1.5rem" }}
                      >
                        <Button
                          icon="pi pi-search"
                          className="p-button-rounded"
                          tooltip="Rechercher"
                          tooltipOptions={{ position: "top" }}
                          onClick={() =>
                            handleSearchBac(
                              formik.values.tableNum,
                              formik.values.yearBac,
                            )
                          }
                          disabled={disabled}
                        />
                      </div>
                    </>
                  )}
                </div>
              </CustomFieldset>

              {/* Référence de l'acte de naissance */}
              <CustomFieldset legend="Référence de l'acte de naissance">
                <div className="grid">
                  <FormField
                    label="Code"
                    required
                    error={
                      formik.touched.codeCentre && formik.errors.codeCentre
                    }
                    col={2}
                  >
                    <InputText
                      id="codeCentre"
                      value={formik.values.codeCentre || ""}
                      disabled={disabled}
                      onChange={(e) => {
                        const code = e.target.value;
                        formik.setFieldValue("codeCentre", code);
                        formik.setFieldValue(
                          "centreEtatCivil",
                          cecs.find((c) => c.code === code) || null,
                        );
                      }}
                      maxLength={4}
                      className={`p-inputtext-sm w-full ${
                        formik.touched.codeCentre && formik.errors.codeCentre
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <CentreEtatCivilSelect
                    formik={formik}
                    fieldName="centreEtatCivil"
                    label="Centre État Civil"
                    cecs={memoizedCecs}
                    required
                    disabled={disabled}
                    col={4}
                  />
                  <FormField
                    label="Année"
                    required
                    error={
                      formik.touched.year_registry_num &&
                      formik.errors.year_registry_num
                    }
                    col={3}
                  >
                    <InputText
                      id="year_registry_num"
                      name="year_registry_num"
                      value={formik.values.year_registry_num}
                      disabled={disabled}
                      onChange={(e) =>
                        formik.setFieldValue(
                          "year_registry_num",
                          e.target.value.replace(/\D/g, "").slice(0, 4),
                        )
                      }
                      className={`p-inputtext-sm w-full ${
                        formik.touched.year_registry_num &&
                        formik.errors.year_registry_num
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <FormField
                    label="N° acte"
                    required
                    error={
                      formik.touched.registry_num && formik.errors.registry_num
                    }
                    col={3}
                  >
                    <InputText
                      id="registry_num"
                      name="registry_num"
                      value={formik.values.registry_num}
                      disabled={disabled}
                      onChange={formik.handleChange}
                      className={`p-inputtext-sm w-full ${
                        formik.touched.registry_num &&
                        formik.errors.registry_num
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                </div>
              </CustomFieldset>

              {user?.acteur?.etablissement?.typeCandidat?.name !==
                "Régulier/Officiel" && (
                <CustomFieldset legend="Diplôme d'accès">
                  <div className="grid">
                    <FormField
                      label="Année BFEM"
                      required
                      error={
                        formik.touched.year_bfem && formik.errors.year_bfem
                      }
                      col={
                        user?.acteur?.etablissement?.typeEtablissement?.code ===
                        "I"
                          ? 5
                          : 12
                      }
                    >
                      <InputText
                        id="year_bfem"
                        name="year_bfem"
                        value={formik.values.year_bfem}
                        disabled={disabled}
                        onChange={formik.handleChange}
                        className={`p-inputtext-sm w-full ${
                          formik.touched.year_bfem && formik.errors.year_bfem
                            ? "p-invalid"
                            : ""
                        }`}
                      />
                    </FormField>
                    {user?.acteur?.etablissement?.typeEtablissement?.code ===
                      "I" && (
                      <FormField
                        label="Centre d'examen"
                        required
                        error={
                          formik.touched.centreExamen &&
                          formik.errors.centreExamen
                        }
                        col={7}
                      >
                        <OptimizedDropdown
                          id="centreExamen"
                          name="centreExamen"
                          value={formik.values.centreExamen}
                          onChange={(e) =>
                            formik.setFieldValue("centreExamen", e.value)
                          }
                          options={memoizedCexam}
                          optionLabel="name"
                          placeholder="Sélectionner"
                          disabled={disabled}
                          filter
                          loading={loading}
                          className={`p-inputtext-sm w-full ${
                            formik.touched.centreExamen &&
                            formik.errors.centreExamen
                              ? "p-invalid"
                              : ""
                          }`}
                        />
                      </FormField>
                    )}
                  </div>
                </CustomFieldset>
              )}

              {/* Identité */}
              <CustomFieldset legend="Identité">
                <div className="grid">
                  <FormField
                    label="Prénom(s)"
                    required
                    error={formik.touched.firstname && formik.errors.firstname}
                    col={4}
                  >
                    <InputText
                      id="firstname"
                      name="firstname"
                      value={formik.values.firstname}
                      disabled={disabled}
                      onChange={(e) =>
                        formik.setFieldValue(
                          "firstname",
                          e.target.value
                            .toUpperCase()
                            .normalize("NFD")
                            .replace(/[\u0300-\u036f]/g, ""),
                        )
                      }
                      className={`p-inputtext-sm w-full ${
                        formik.touched.firstname && formik.errors.firstname
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <FormField
                    label="Nom"
                    required
                    error={formik.touched.lastname && formik.errors.lastname}
                    col={2}
                  >
                    <InputText
                      id="lastname"
                      name="lastname"
                      value={formik.values.lastname}
                      disabled={disabled}
                      onChange={(e) =>
                        formik.setFieldValue(
                          "lastname",
                          e.target.value
                            .toUpperCase()
                            .normalize("NFD")
                            .replace(/[\u0300-\u036f]/g, ""),
                        )
                      }
                      className={`p-inputtext-sm w-full ${
                        formik.touched.lastname && formik.errors.lastname
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <FormField
                    label="Sexe"
                    required
                    error={formik.touched.gender && formik.errors.gender}
                    col={2}
                  >
                    <OptimizedDropdown
                      id="gender"
                      name="gender"
                      value={formik.values.gender}
                      onChange={(e) => formik.setFieldValue("gender", e.value)}
                      options={memoizedSexeOptions}
                      placeholder="Sexe"
                      disabled={disabled}
                      className={`p-inputtext-sm w-full ${
                        formik.touched.gender && formik.errors.gender
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <FormField
                    label="Date naissance"
                    required
                    error={
                      formik.touched.date_birth && formik.errors.date_birth
                    }
                    col={2}
                  >
                    <InputMask
                      id="date_birth"
                      name="date_birth"
                      value={formik.values.date_birth}
                      disabled={disabled}
                      onChange={handleDateChange}
                      onBlur={() => {
                        if (formik.values.date_birth) {
                          const a = calculateAgeFromDate(
                            formik.values.date_birth,
                            prog?.edition,
                          );
                          if (a !== null)
                            calculateAge(formik.values.date_birth);
                        }
                      }}
                      mask="99/99/9999"
                      placeholder="JJ/MM/AAAA"
                      className={`p-inputtext-sm w-full ${
                        formik.touched.date_birth && formik.errors.date_birth
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <FormField
                    label="Lieu naissance"
                    required
                    error={
                      formik.touched.place_birth && formik.errors.place_birth
                    }
                    col={2}
                  >
                    <InputText
                      id="place_birth"
                      name="place_birth"
                      value={formik.values.place_birth}
                      disabled={disabled}
                      onChange={(e) =>
                        formik.setFieldValue(
                          "place_birth",
                          e.target.value.toUpperCase(),
                        )
                      }
                      className={`p-inputtext-sm w-full ${
                        formik.touched.place_birth && formik.errors.place_birth
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <CountrySelect
                    formik={formik}
                    fieldName="nationality"
                    label="Nationalité"
                    pays={memoizedPays}
                    required
                    disabled={disabled}
                    col={6}
                  />
                  <CountrySelect
                    formik={formik}
                    fieldName="countryBirth"
                    label="Pays naissance"
                    pays={memoizedPays}
                    required
                    disabled={disabled}
                    col={6}
                  />
                </div>
              </CustomFieldset>

              {/* Contact */}
              <CustomFieldset legend="Contact">
                <div className="grid">
                  <PhoneInput
                    formik={formik}
                    fieldName="phone1"
                    label="Téléphone"
                    required
                    disabled={disabled}
                    col={4}
                  />
                  <FormField
                    label="Email"
                    required
                    error={formik.touched.email && formik.errors.email}
                    col={4}
                  >
                    <InputText
                      id="email"
                      name="email"
                      value={formik.values.email}
                      disabled={disabled}
                      onChange={formik.handleChange}
                      className={`p-inputtext-sm w-full ${
                        formik.touched.email && formik.errors.email
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <FormField label="Adresse" col={4}>
                    <InputText
                      id="adresse"
                      name="adresse"
                      value={formik.values.adresse}
                      disabled={disabled}
                      onChange={formik.handleChange}
                      className="p-inputtext-sm w-full"
                    />
                  </FormField>
                </div>
              </CustomFieldset>
            </div>

            {/* Colonne droite : Matières & épreuves + Résumé */}
            <div className="candidature-col candidature-col--right">
              {/* <SectionTitle icon="pi-book" label="Matières & épreuves" /> */}

              {showMatieresOptionnelles && (
                <CustomFieldset
                  legend={
                    formik.values.serie?.code === "STIDD"
                      ? "Spécialité"
                      : "Matières optionnelles"
                  }
                >
                  <div className="grid">
                    <FormField
                      label={
                        formik.values.serie?.code === "STIDD"
                          ? "Spécialité"
                          : "LV1"
                      }
                      required
                      col={12}
                    >
                      <OptimizedDropdown
                        value={formik.values.matiere1}
                        onChange={(e) => handleMatiere1Change(e.value)}
                        options={getAvailableOptions(
                          formik.values.serie.code,
                          1,
                          formik.values.matiere2?.name,
                          formik.values.matiere3?.name,
                          formik.values.matiere4?.name,
                        )}
                        optionLabel="name"
                        placeholder="Choisir"
                        disabled={disabled}
                        filter
                        className="p-inputtext-sm w-full"
                      />
                    </FormField>
                    {formik.values.serie.code !== "STIDD" &&
                      formik.values.serie.code !== "S1A" &&
                      formik.values.serie.code !== "S2A" && (
                        <FormField label="LV2 ou Economie" required col={12}>
                          <OptimizedDropdown
                            value={formik.values.matiere2}
                            onChange={(e) => handleMatiere2Change(e.value)}
                            options={getAvailableOptions(
                              formik.values.serie.code,
                              2,
                              formik.values.matiere1?.name,
                              formik.values.matiere3?.name,
                              formik.values.matiere4?.name,
                            )}
                            optionLabel="name"
                            placeholder="Choisir"
                            disabled={disabled}
                            filter
                            className="p-inputtext-sm w-full"
                          />
                        </FormField>
                      )}
                    {formik.values.serie.code !== "LA" &&
                      formik.values.serie.code !== "STIDD" &&
                      formik.values.serie.code !== "L'1" &&
                      formik.values.serie.code !== "L1A" &&
                      formik.values.serie.code !== "S1A" &&
                      formik.values.serie.code !== "S2A" && (
                        <FormField
                          label={
                            formik.values.serie.code === "L1B"
                              ? "LC"
                              : "Sciences (PC ou SVT)"
                          }
                          required
                          col={12}
                        >
                          <OptimizedDropdown
                            value={formik.values.matiere3}
                            onChange={(e) => handleMatiere3Change(e.value)}
                            options={getAvailableOptions(
                              formik.values.serie.code,
                              3,
                              formik.values.matiere1?.name,
                              formik.values.matiere2?.name,
                              formik.values.matiere4?.name,
                            )}
                            optionLabel="name"
                            placeholder="Choisir"
                            disabled={disabled}
                            filter
                            className="p-inputtext-sm w-full"
                          />
                        </FormField>
                      )}
                  </div>
                </CustomFieldset>
              )}

              <CustomFieldset legend="Épreuves facultatives">
                <div className="grid">
                  <FormField label="Liste A" col={6}>
                    <OptimizedDropdown
                      value={formik.values.eprFacListA}
                      onChange={(e) =>
                        formik.setFieldValue("eprFacListA", e.value)
                      }
                      options={memoizedEfOptions}
                      placeholder="Dessin, Musique, Couture..."
                      disabled={disabled}
                      className="p-inputtext-sm w-full"
                    />
                  </FormField>
                  <FormField label="Liste B" col={6}>
                    <OptimizedDropdown
                      value={formik.values.matiere4}
                      onChange={(e) =>
                        formik.setFieldValue("matiere4", e.value)
                      }
                      options={getAvailableOptions2(
                        formik.values.serie?.code,
                        3,
                        formik.values.matiere1?.name,
                        formik.values.matiere2?.name,
                        formik.values.matiere3?.name,
                      )}
                      optionLabel="name"
                      placeholder="Choisir"
                      disabled={disabled}
                      filter
                      className="p-inputtext-sm w-full"
                    />
                  </FormField>
                </div>
              </CustomFieldset>

              <CustomFieldset legend="EPS et handicap">
                <div className="grid">
                  <FormField
                    label="EPS"
                    required
                    error={formik.touched.eps && formik.errors.eps}
                    col={6}
                  >
                    <OptimizedDropdown
                      id="eps"
                      name="eps"
                      value={formik.values.eps}
                      onChange={(e) => formik.setFieldValue("eps", e.value)}
                      options={memoizedEpsOptions}
                      placeholder="Aptitude"
                      disabled={disabled}
                      className={`p-inputtext-sm w-full ${
                        formik.touched.eps && formik.errors.eps
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                  <FormField label="Handicap" required col={6}>
                    <OptimizedDropdown
                      id="type_handicap"
                      name="type_handicap"
                      value={formik.values.type_handicap}
                      onChange={(e) => handleHandicapChange(e.value)}
                      options={memoizedHandicapOptions}
                      placeholder="Sélectionner"
                      disabled={disabled}
                      className={`p-inputtext-sm w-full ${
                        formik.touched.type_handicap &&
                        formik.errors.type_handicap
                          ? "p-invalid"
                          : ""
                      }`}
                    />
                  </FormField>
                </div>
              </CustomFieldset>

              {/* Résumé */}
              <CustomFieldset legend="Résumé du dossier">
                <div className="candidature-summary">
                  <div className="summary-row">
                    <span>Candidat</span>
                    <strong>
                      {formik.values.firstname} {formik.values.lastname}
                    </strong>
                  </div>
                  {/* <div className="summary-row">
                    <span>Sexe</span>
                    <strong>
                      {formik.values.gender === "M" ? "Masculin" : "Féminin"}
                    </strong>
                  </div> */}
                  <div className="summary-row">
                    <span>Série</span>
                    <strong>{formik.values.serie?.code || "—"}</strong>
                  </div>
                  <div className="summary-row">
                    <span>N° dossier</span>
                    <strong>{formik.values.dosNumber || "—"}</strong>
                  </div>
                </div>
              </CustomFieldset>

              {/* Statut (uniquement en mode view) */}
              {isView && (
                <div
                  className={`candidature-status candidature-status--${
                    formik.values.decision === 1
                      ? "ok"
                      : formik.values.decision === 2
                      ? "ko"
                      : "pending"
                  }`}
                >
                  <i
                    className={`pi ${
                      formik.values.decision === 1
                        ? "pi-check-circle"
                        : formik.values.decision === 2
                        ? "pi-times-circle"
                        : "pi-clock"
                    }`}
                  />
                  {formik.values.decision === 1
                    ? "Dossier validé par l'Office du Bac"
                    : formik.values.decision === 2
                    ? "Dossier rejeté"
                    : "En attente de traitement"}
                </div>
              )}

              {rejets && rejets.length > 0 && (
                <div className="mt-3">
                  <h5 className="text-red-500">Motifs de rejet</h5>
                  <DataTable
                    value={rejets}
                    responsiveLayout="scroll"
                    className="p-datatable-sm"
                  >
                    <Column field="name" header="Motif" />
                    <Column field="observation" header="Observations" />
                  </DataTable>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Footer avec le bouton d'action centré en bas */}
        {!isView && (
          <div className="candidature-footer">
            <Button
              severity="success"
              icon={isSaving ? "pi pi-spin pi-spinner" : "pi pi-save"}
              label={isSaving ? "Enregistrement..." : "ENREGISTRER LE DOSSIER"}
              type="submit"
              disabled={isSaving}
              className="candidature-submit"
            />
          </div>
        )}
      </form>
    </Dialog>
  );
};
