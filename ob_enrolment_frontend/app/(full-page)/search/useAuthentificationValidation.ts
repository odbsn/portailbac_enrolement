// hooks/useAuthentificationValidation.ts
import { useState, useCallback } from 'react';

export interface AuthFormData {
  codeEtablissement: string;
  numeroTable: string;
  dateNaissance: {
    jour: string;
    mois: string;
    annee: string;
  };
}

export interface AuthFormErrors {
  codeEtablissement?: string;
  numeroTable?: string;
  dateNaissance?: {
    jour?: string;
    mois?: string;
    annee?: string;
  };
}

export interface AuthValidationReturn {
  errors: AuthFormErrors;
  touched: Record<string, boolean>;
  setErrors: React.Dispatch<React.SetStateAction<AuthFormErrors>>;
  setTouched: React.Dispatch<React.SetStateAction<Record<string, boolean>>>;
  validateField: (name: string, value: any, formData: AuthFormData) => string | undefined;
  validateForm: (formData: AuthFormData) => boolean;
  handleBlur: (name: string, formData: AuthFormData) => void;
  handleChange: (name: string, value: any, formData: AuthFormData) => void;
  clearErrors: () => void;
  getMaxDays: (mois: string, annee: string) => number;
}

export const useAuthentificationValidation = (): AuthValidationReturn => {
  const [errors, setErrors] = useState<AuthFormErrors>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});

  const getMaxDays = useCallback((mois: string, annee: string): number => {
    const moisNum = parseInt(mois, 10);
    const anneeNum = parseInt(annee, 10);
    
    if (isNaN(moisNum) || isNaN(anneeNum)) return 31;
    
    if ([4, 6, 9, 11].includes(moisNum)) return 30;
    
    if (moisNum === 2) {
      const isLeapYear = (anneeNum % 4 === 0 && anneeNum % 100 !== 0) || anneeNum % 400 === 0;
      return isLeapYear ? 29 : 28;
    }
    
    return 31;
  }, []);

  const validateCodeEtablissement = (value: string): string | undefined => {
    if (!value || value.trim().length === 0) {
      return 'Le code établissement est obligatoire';
    }
    return undefined;
  };

  const validateNumeroTable = (value: string): string | undefined => {
    if (!value || value.trim().length === 0) {
      return 'Le numéro de table est obligatoire';
    }
    return undefined;
  };

  // Validation séparée pour le jour
  const validateJour = (jour: string, mois: string, annee: string): string | undefined => {
    if (!jour || jour.trim().length === 0) {
      return 'Le jour est obligatoire';
    }
    
    const jourNum = parseInt(jour, 10);
    if (isNaN(jourNum)) {
      return 'Veuillez saisir un nombre valide';
    }
    
    if (jourNum < 1) {
      return 'Le jour doit être supérieur à 0';
    }
    
    // Si mois et année sont remplis, vérifier la cohérence
    if (mois && mois.trim().length > 0 && annee && annee.trim().length > 0) {
      const moisNum = parseInt(mois, 10);
      const anneeNum = parseInt(annee, 10);
      if (!isNaN(moisNum) && !isNaN(anneeNum)) {
        const maxDays = getMaxDays(mois, annee);
        if (jourNum > maxDays) {
          if (moisNum === 2) {
            const isLeapYear = (anneeNum % 4 === 0 && anneeNum % 100 !== 0) || anneeNum % 400 === 0;
            if (isLeapYear) {
              return `Février ne compte que 29 jours cette année`;
            }
            return `Février ne compte que 28 jours cette année`;
          }
          return `Le mois ${moisNum} ne compte que ${maxDays} jours`;
        }
      }
    }
    
    return undefined;
  };

  // Validation séparée pour le mois
  const validateMois = (mois: string): string | undefined => {
    if (!mois || mois.trim().length === 0) {
      return 'Le mois est obligatoire';
    }
    
    const moisNum = parseInt(mois, 10);
    if (isNaN(moisNum)) {
      return 'Veuillez saisir un nombre valide';
    }
    
    if (moisNum < 1 || moisNum > 12) {
      return 'Le mois doit être entre 1 et 12';
    }
    
    return undefined;
  };

  // Validation séparée pour l'année
  const validateAnnee = (annee: string): string | undefined => {
    if (!annee || annee.trim().length === 0) {
      return 'L\'année est obligatoire';
    }
    
    const anneeNum = parseInt(annee, 10);
    if (isNaN(anneeNum)) {
      return 'Veuillez saisir un nombre valide';
    }
    
    const currentYear = new Date().getFullYear();
    if (anneeNum < 1900 || anneeNum > currentYear) {
      return `L'année doit être entre 1900 et ${currentYear}`;
    }
    
    // Vérification que la date n'est pas dans le futur
    // Note: cette vérification nécessite jour et mois, donc elle sera faite dans validateForm ou séparément
    return undefined;
  };

  const validateField = useCallback((
    name: string, 
    value: any, 
    formData: AuthFormData
  ): string | undefined => {
    switch (name) {
      case 'codeEtablissement':
        return validateCodeEtablissement(value);
      
      case 'numeroTable':
        return validateNumeroTable(value);
      
      case 'jour':
        return validateJour(value, formData.dateNaissance.mois, formData.dateNaissance.annee);
      
      case 'mois':
        return validateMois(value);
      
      case 'annee':
        return validateAnnee(value);
      
      default:
        return undefined;
    }
  }, []);

  const handleChange = useCallback((
    name: string, 
    value: any, 
    formData: AuthFormData
  ): void => {
    let error: string | undefined;
    
    if (name === 'jour') {
      error = validateJour(value, formData.dateNaissance.mois, formData.dateNaissance.annee);
      setErrors(prev => ({
        ...prev,
        dateNaissance: {
          ...prev.dateNaissance,
          jour: error
        }
      }));
    } else if (name === 'mois') {
      error = validateMois(value);
      setErrors(prev => ({
        ...prev,
        dateNaissance: {
          ...prev.dateNaissance,
          mois: error
        }
      }));
    } else if (name === 'annee') {
      error = validateAnnee(value);
      setErrors(prev => ({
        ...prev,
        dateNaissance: {
          ...prev.dateNaissance,
          annee: error
        }
      }));
    } else {
      error = validateField(name, value, formData);
      setErrors(prev => ({
        ...prev,
        [name]: error
      }));
    }
    
    setTouched(prev => ({
      ...prev,
      [name]: true
    }));
  }, [validateField]);

  const handleBlur = useCallback((
    name: string, 
    formData: AuthFormData
  ): void => {
    setTouched(prev => ({ ...prev, [name]: true }));
    
    let value: any;
    if (name === 'jour' || name === 'mois' || name === 'annee') {
      value = (formData.dateNaissance as any)[name];
    } else {
      value = (formData as any)[name];
    }
    
    let error: string | undefined;
    
    if (name === 'jour') {
      error = validateJour(value, formData.dateNaissance.mois, formData.dateNaissance.annee);
      setErrors(prev => ({
        ...prev,
        dateNaissance: {
          ...prev.dateNaissance,
          jour: error
        }
      }));
    } else if (name === 'mois') {
      error = validateMois(value);
      setErrors(prev => ({
        ...prev,
        dateNaissance: {
          ...prev.dateNaissance,
          mois: error
        }
      }));
    } else if (name === 'annee') {
      error = validateAnnee(value);
      setErrors(prev => ({
        ...prev,
        dateNaissance: {
          ...prev.dateNaissance,
          annee: error
        }
      }));
    } else {
      error = validateField(name, value, formData);
      setErrors(prev => ({
        ...prev,
        [name]: error
      }));
    }
  }, [validateField]);

  const validateForm = useCallback((formData: AuthFormData): boolean => {
    const newErrors: AuthFormErrors = {};
    const newTouched: Record<string, boolean> = {};

    // Valider code établissement
    newTouched.codeEtablissement = true;
    const codeError = validateCodeEtablissement(formData.codeEtablissement);
    if (codeError) newErrors.codeEtablissement = codeError;

    // Valider numéro de table
    newTouched.numeroTable = true;
    const tableError = validateNumeroTable(formData.numeroTable);
    if (tableError) newErrors.numeroTable = tableError;

    // Valider date de naissance (chaque champ séparément)
    newTouched.jour = true;
    newTouched.mois = true;
    newTouched.annee = true;
    
    const jourError = validateJour(
      formData.dateNaissance.jour,
      formData.dateNaissance.mois,
      formData.dateNaissance.annee
    );
    const moisError = validateMois(formData.dateNaissance.mois);
    const anneeError = validateAnnee(formData.dateNaissance.annee);
    
    if (jourError || moisError || anneeError) {
      newErrors.dateNaissance = {};
      if (jourError) newErrors.dateNaissance.jour = jourError;
      if (moisError) newErrors.dateNaissance.mois = moisError;
      if (anneeError) newErrors.dateNaissance.annee = anneeError;
    }

    setErrors(newErrors);
    setTouched(newTouched);

    return Object.keys(newErrors).length === 0;
  }, []);

  const clearErrors = useCallback(() => {
    setErrors({});
    setTouched({});
  }, []);

  return {
    errors,
    touched,
    setErrors,
    setTouched,
    validateField,
    validateForm,
    handleBlur,
    handleChange,
    clearErrors,
    getMaxDays
  };
};