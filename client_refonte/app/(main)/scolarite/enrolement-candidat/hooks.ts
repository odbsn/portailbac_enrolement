// app/scolarite/enrolement-candidat/hooks.ts
import { useState, useEffect, useCallback, useRef } from "react";
import { useFormik } from "formik";
import * as Yup from "yup";
import { CandidatureService } from "./services";
import {
  getValidationSchema,
  formatDateToInput,
  calculateAgeFromDate,
} from "./utils";
import { DialogState } from "./types";

// ===== useCandidatData =====
export const useCandidatData = (etabId: string, edition: number) => {
  const [candidats, setCandidats] = useState<any[]>([]);
  const [groupedCdts, setGroupedCdts] = useState<any[]>([]);
  const [faeb, setFaeb] = useState<any>(null);
  const [nbrTCdts, setNbrTCdts] = useState(0);
  const [loading, setLoading] = useState(false);

  const loadData = useCallback(async () => {
    if (!etabId || !edition) return;
    setLoading(true);
    try {
      const [data, faebData, allCdt] = await Promise.all([
        CandidatureService.getCdtsBySerie(etabId, edition),
        CandidatureService.compteFAEBS_(etabId, edition),
        CandidatureService.getCandidatsByEtablissement(etabId, edition),
      ]);
      if (data && typeof data === "object") {
        setGroupedCdts(
          Object.entries(data).map(([serieName, cdts]) => ({
            serieName,
            cdts,
          })),
        );
      }
      setFaeb(faebData);
      setCandidats(allCdt);
      setNbrTCdts(allCdt?.length || 0);
    } catch (err) {
      console.error("Erreur chargement données:", err);
    } finally {
      setLoading(false);
    }
  }, [etabId, edition]);

  useEffect(() => {
    loadData();
  }, [loadData]);
  return {
    candidats,
    groupedCdts,
    faeb,
    nbrTCdts,
    loading,
    reloadData: loadData,
  };
};

// ===== useDialogs =====
export const useDialogs = () => {
  const [state, setState] = useState<DialogState>({
    form: { visible: false, mode: "create" },
    print: false,
    confirm: { visible: false, title: "", message: "", onConfirm: () => {} },
    result: { visible: false, data: null, loading: false },
  });

  return {
    ...state,
    openForm: (mode: "create" | "edit" | "view" = "create") =>
      setState((s) => ({ ...s, form: { visible: true, mode } })),
    closeForm: () =>
      setState((s) => ({ ...s, form: { visible: false, mode: "create" } })),
    openPrint: () => setState((s) => ({ ...s, print: true })),
    closePrint: () => setState((s) => ({ ...s, print: false })),
    openConfirm: (title: string, message: string, onConfirm: () => void) =>
      setState((s) => ({
        ...s,
        confirm: { visible: true, title, message, onConfirm },
      })),
    closeConfirm: () =>
      setState((s) => ({ ...s, confirm: { ...s.confirm, visible: false } })),
    openResult: (data: any, loading = false) =>
      setState((s) => ({ ...s, result: { visible: true, data, loading } })),
    closeResult: () =>
      setState((s) => ({
        ...s,
        result: { visible: false, data: null, loading: false },
      })),
  };
};

// ===== useCandidatureForm =====
export const useCandidatureForm = (
  user: any,
  prog: any,
  candidats: any[],
  cecs: any[],
  matieres: any[],
  onSuccess: () => void,
  onShowResult: (data: any, loading: boolean) => void,
) => {
  const [age, setAge] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [baseMorte, setBaseMorte] = useState<any>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [rejets, setRejets] = useState<any[]>([]);
  const isUpdate = useRef(false);
  const idCdt = useRef<string | null>(null);

  // Fonction pour calculer l'âge
  const calculateAge = useCallback(
    (dateStr: string) => {
      const ageValue = calculateAgeFromDate(dateStr, prog?.edition);
      setAge(ageValue);
      return ageValue;
    },
    [prog?.edition],
  );

  const formik = useFormik({
    initialValues: {
      dosNumber: "",
      session: 0,
      firstname: "",
      lastname: "",
      date_birth: "",
      place_birth: "",
      adresse: "",
      gender: "",
      phone1: "",
      phone2: "",
      email: "",
      year_registry_num: "",
      registry_num: "",
      bac_do_count: "1",
      year_bfem: "",
      origine_bfem: "",
      subject: "",
      handicap: false,
      type_handicap: "Néant",
      eps: "Apte",
      decision: 1,
      matiere1: null,
      matiere2: null,
      matiere3: null,
      matiere4: null,
      etablissement: null,
      centreEtatCivil: null,
      centreExamen: null,
      serie: null,
      nationality: null,
      countryBirth: null,
      eprFacListA: "",
      eprFacListB: null,
      codeCentre: "",
      hasBac: "no",
      tableNum: "",
      yearBac: "",
    },
    validationSchema: Yup.object().shape(
      getValidationSchema(user, candidats, isUpdate.current),
    ),
    onSubmit: async (values, { setSubmitting }) => {
      setIsSaving(true);
      try {
        // Construire le DTO compatible avec le service existant
        const dto = {
          dosNumber: values.dosNumber,
          session: Number(prog?.edition),
          firstname: values.firstname,
          lastname: values.lastname,
          date_birth: values.date_birth,
          place_birth: values.place_birth,
          adresse: values.adresse || "",
          gender: values.gender,
          phone1: values.phone1.replace(/\s/g, ""),
          phone2: values.phone2 || "",
          email: values.email,
          year_registry_num: Number(values.year_registry_num),
          registry_num: values.registry_num,
          bac_do_count: Number(values.bac_do_count),
          year_bfem: Number(values.year_bfem) || 0,
          origine_bfem: values.origine_bfem || "Aucun",
          subject: values.subject || "",
          handicap: values.handicap || false,
          type_handicap: values.type_handicap,
          eps: values.eps,
          alreadyBac: false,
          decision: values.decision || 1,
          etablissement: user?.acteur?.etablissement,
          centreEtatCivil: values.centreEtatCivil,
          centreExamen: values.centreExamen,
          typeCandidat: user?.acteur?.etablissement?.typeCandidat,
          serie: values.serie,
          matiere1: values.matiere1,
          matiere2: values.matiere2,
          matiere3: values.matiere3,
          nationality: values.nationality,
          countryBirth: values.countryBirth,
          eprFacListA: values.eprFacListA || "Aucun",
          eprFacListB: values.matiere4,
          concoursGeneral: null,
          codeEnrolementEC: baseMorte?.codeEnrolement,
        };

        if (isUpdate.current && idCdt.current) {
          await CandidatureService.updateCandidat(idCdt.current, dto);
          onSuccess();
        } else {
          const response = await CandidatureService.createCandidat(dto);
          if (response) {
            onShowResult(response, false);
          } else {
            onSuccess();
          }
        }
      } catch (error: any) {
        const errorMessage =
          error.response?.data?.errorMessage ||
          "Erreur lors de l'enregistrement";
        onShowResult({ error: errorMessage }, false);
      } finally {
        setIsSaving(false);
        setSubmitting(false);
      }
    },
  });

  const formatPhoneForMask = (phone?: string) => {
    if (!phone) return "";
    const digits = phone.replace(/\D/g, "");
    if (digits.length !== 9) return digits; // fallback si format inattendu
    return digits.replace(/(\d{2})(\d{3})(\d{2})(\d{2})/, "$1 $2 $3 $4");
  };
  const findMatiere = (
    matieres: any[],
    name: string,
    order: number,
    serieCode?: string,
  ) => {
    if (!name) return null;
    return (
      matieres.find(
        (m: any) =>
          m.name === name &&
          m.order === order &&
          (!serieCode || m.serie?.code === serieCode),
      ) || null
    );
  };

  const loadCandidatForEdit = useCallback(
    (candidat: any) => {
      isUpdate.current = true;
      idCdt.current = candidat.id;

      const serieCode = candidat.serie?.code;

      const formatted = {
        ...candidat,
        date_birth: formatDateToInput(candidat.date_birth),
        matiere1: findMatiere(matieres, candidat.matiere1?.name, 1, serieCode),
        matiere2: findMatiere(matieres, candidat.matiere2?.name, 2, serieCode),
        matiere3: findMatiere(matieres, candidat.matiere3?.name, 3, serieCode),
        matiere4: findMatiere(
          matieres,
          candidat.eprFacListB?.name,
          3,
          serieCode,
        ),
        phone1: formatPhoneForMask(candidat.phone1),
        phone2: formatPhoneForMask(candidat.phone2),
        codeCentre: candidat.centreEtatCivil?.code || "",
        centreExamen: candidat.centreExamen || null,
        nationality: candidat.nationality || null,
        countryBirth: candidat.countryBirth || null,
        serie: candidat.serie || null,
        eps: candidat.eps || "Apte",
        type_handicap: candidat.type_handicap || "Néant",
        handicap: candidat.handicap || false,
        hasBac: candidat.hasBac || "no",
        tableNum: candidat.tableNum || "",
        yearBac: candidat.yearBac || "",
        eprFacListA: candidat.eprFacListA || "",
      };

      formik.setValues(formatted);

      if (formatted.date_birth) {
        calculateAge(formatted.date_birth);
      }

      setRejets(candidat.rejets || []);
      setBaseMorte(null);
    },
    [matieres, calculateAge, formik.setValues],
  );

  const resetForm = useCallback(() => {
    isUpdate.current = false;
    idCdt.current = null;
    formik.resetForm();
    setAge(null);
    setBaseMorte(null);
    setRejets([]);
  }, [formik]);

  const handleSearchBac = useCallback(
    async (tableNum: string, yearBac: string) => {
      if (!tableNum || !yearBac) return;
      setLoading(true);
      try {
        const response = await CandidatureService.checkRedoublantOrFraude(
          Number(tableNum),
          Number(yearBac),
        );
        if (response) {
          const civil = await CandidatureService.checkByEtatCivil(
            response.codeCentreEtatCivil,
            response.yearRegistryNum,
            response.registryNum,
          );
          if (civil) {
            setBaseMorte(civil);
            onShowResult(civil, false);
          }
        }
      } catch (err) {
        console.error("Erreur recherche BAC:", err);
      } finally {
        setLoading(false);
      }
    },
    [onShowResult],
  );

  const handleSearchEtatCivil = useCallback(
    async (
      codeCentre: string,
      yearRegistryNum: string,
      registryNum: string,
    ) => {
      if (!codeCentre || !yearRegistryNum || !registryNum) return;
      setLoading(true);
      try {
        const response = await CandidatureService.checkByEtatCivil(
          codeCentre,
          Number(yearRegistryNum),
          registryNum,
        );
        if (response) {
          setBaseMorte(response);
          onShowResult(response, false);
        }
      } catch (err) {
        console.error("Erreur recherche état civil:", err);
      } finally {
        setLoading(false);
      }
    },
    [onShowResult],
  );

  return {
    formik,
    age,
    loading,
    isSaving,
    baseMorte,
    rejets,
    isUpdate: isUpdate.current,
    loadCandidatForEdit,
    resetForm,
    calculateAge,
    handleSearchBac,
    handleSearchEtatCivil,
    setBaseMorte,
  };
};

// ===== useLocalStorage =====
export const useLocalStorage = <T>(key: string, initialValue: T) => {
  const [value, setValue] = useState<T>(() => {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch {
      return initialValue;
    }
  });

  const setStored = useCallback(
    (val: T | ((v: T) => T)) => {
      try {
        const newVal = val instanceof Function ? val(value) : val;
        setValue(newVal);
        localStorage.setItem(key, JSON.stringify(newVal));
      } catch (error) {
        console.error(`Erreur sauvegarde localStorage pour ${key}:`, error);
      }
    },
    [key, value],
  );

  const remove = useCallback(() => {
    try {
      localStorage.removeItem(key);
      setValue(initialValue);
    } catch (error) {
      console.error(`Erreur suppression localStorage pour ${key}:`, error);
    }
  }, [key, initialValue]);

  return [value, setStored, remove] as const;
};

export const useFormikLocalStorage = (
  formik: any,
  fieldName: string,
  deps: string[] = [],
) => {
  // Restauration de la valeur depuis localStorage (au montage uniquement)
  useEffect(() => {
    try {
      const stored = localStorage.getItem(fieldName);
      if (stored && stored !== "undefined" && stored !== "null") {
        const parsed = JSON.parse(stored);
        formik.setFieldValue(fieldName, parsed);
      }
    } catch (error) {
      console.error(`Erreur parsing localStorage pour ${fieldName}:`, error);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fieldName]);

  // Sauvegarde de la valeur + nettoyage des dépendances,
  // déclenché UNIQUEMENT lors d'un changement utilisateur explicite
  const handleChange = useCallback(
    (value: any) => {
      formik.setFieldValue(fieldName, value);
      try {
        if (value !== undefined && value !== null && value !== "") {
          localStorage.setItem(fieldName, JSON.stringify(value));
        } else {
          localStorage.removeItem(fieldName);
        }
      } catch (error) {
        console.error(
          `Erreur sauvegarde localStorage pour ${fieldName}:`,
          error,
        );
      }

      if (deps.length > 0) {
        deps.forEach((dep) => {
          try {
            localStorage.removeItem(dep);
            formik.setFieldValue(dep, null);
          } catch (error) {
            console.error(`Erreur nettoyage localStorage pour ${dep}:`, error);
          }
        });
      }
    },
    [fieldName, deps, formik.setFieldValue],
  );

  return handleChange;
};
