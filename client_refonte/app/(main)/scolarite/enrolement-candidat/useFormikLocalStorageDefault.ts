// app/scolarite/enrolement-candidat/useFormikLocalStorageDefault.ts

import { useEffect, useCallback } from "react";

/**
 * Hook personnalisé pour la gestion du localStorage avec Formik
 * Version par défaut avec gestion d'erreur améliorée
 */
export const useFormikLocalStorageDefault = (
  formik: any,
  fieldName: string,
  deps: string[] = [],
  storageKey?: string,
) => {
  const key = storageKey || fieldName;

  // Restauration de la valeur depuis localStorage
  useEffect(() => {
    try {
      const stored = localStorage.getItem(key);
      if (stored && stored !== "undefined" && stored !== "null") {
        const parsed = JSON.parse(stored);
        // Vérifier que la valeur est valide avant de la restaurer
        if (parsed !== null && parsed !== undefined) {
          formik.setFieldValue(fieldName, parsed);
        }
      }
    } catch (error) {
      console.error(`Erreur parsing localStorage pour ${key}:`, error);
      // En cas d'erreur, supprimer la clé corrompue
      try {
        localStorage.removeItem(key);
      } catch (e) {
        // Ignorer
      }
    }
  }, [key, fieldName, formik.setFieldValue]);

  // Sauvegarde de la valeur
  const handleChange = useCallback(
    (value: any) => {
      formik.setFieldValue(fieldName, value);
      try {
        if (value !== undefined && value !== null && value !== "") {
          localStorage.setItem(key, JSON.stringify(value));
        } else {
          localStorage.removeItem(key);
        }
      } catch (error) {
        console.error(`Erreur sauvegarde localStorage pour ${key}:`, error);
      }
    },
    [fieldName, key, formik.setFieldValue],
  );

  // Nettoyage des dépendances
  useEffect(
    () => {
      if (deps.length > 0) {
        // Nettoyer les dépendances quand elles changent
        deps.forEach((dep) => {
          try {
            localStorage.removeItem(dep);
            // Réinitialiser les champs dépendants dans Formik
            if (formik.values[dep] !== undefined) {
              formik.setFieldValue(dep, null);
            }
          } catch (error) {
            console.error(`Erreur nettoyage localStorage pour ${dep}:`, error);
          }
        });
      }
    },
    deps.map((d) => formik.values[d]),
  );

  // Récupérer la valeur actuelle
  const getStoredValue = useCallback(() => {
    try {
      const stored = localStorage.getItem(key);
      if (stored && stored !== "undefined" && stored !== "null") {
        return JSON.parse(stored);
      }
      return null;
    } catch {
      return null;
    }
  }, [key]);

  // Supprimer la valeur
  const removeStoredValue = useCallback(() => {
    try {
      localStorage.removeItem(key);
      formik.setFieldValue(fieldName, null);
    } catch (error) {
      console.error(`Erreur suppression localStorage pour ${key}:`, error);
    }
  }, [key, fieldName, formik.setFieldValue]);

  return {
    handleChange,
    getStoredValue,
    removeStoredValue,
    // Alias pour la compatibilité
    onChange: handleChange,
  };
};

export default useFormikLocalStorageDefault;
