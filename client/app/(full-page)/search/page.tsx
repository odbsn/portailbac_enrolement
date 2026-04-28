"use client";

import React, { useState, useRef, useEffect, useMemo } from "react";
import { useRouter } from "next/navigation";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Toast } from "primereact/toast";
import { useCandidatStore } from "@/app/(main)/convocations/candidatStore";
import { motion, AnimatePresence } from "framer-motion";
import { useSafeNavigation } from "../hooks/useSafeNavigation";
import { useAuthentificationValidation } from "./useAuthentificationValidation";
import { useCloudflareTurnstile } from "./useCloudflareTurnstile";
import { CloudflareTurnstile } from "./CloudflareTurnstile";
import ValidationTooltip from "./ValidationTooltip";

export default function AuthentificationCandidat() {
  const router = useRouter();
  const { safeNavigate } = useSafeNavigation();
  const toastRef = useRef<Toast>(null);
  const [mounted, setMounted] = useState(false);
  const [particles, setParticles] = useState<
    Array<{ id: number; x: number; y: number; delay: number; size: number }>
  >([]);

  // Utilisation du store Zustand pour les candidats
  const {
    currentCandidat,
    isLoading,
    error: storeError,
    fetchCandidat,
    generatePdf,
    clearError: clearStoreError,
    cacheStatus,
  } = useCandidatStore();

  // Hooks de validation et sécurité
  const {
    errors: authErrors,
    touched: authTouched,
    validateForm: validateAuthForm,
    handleBlur: handleAuthBlur,
    handleChange: handleAuthChange,
    clearErrors: clearAuthErrors,
    getMaxDays: getMaxDaysFromHook,
  } = useAuthentificationValidation();

  const {
    turnstileToken,
    isTurnstileVerified,
    turnstileError,
    isTurnstileLoading,
    widgetKey,
    handleTurnstileVerify,
    handleTurnstileError,
    handleTurnstileExpire,
    handleTurnstileLoad,
    resetTurnstile,
  } = useCloudflareTurnstile();

  const [formData, setFormData] = useState({
    codeEtablissement: "",
    numeroTable: "",
    dateNaissance: {
      jour: "",
      mois: "",
      annee: "",
    },
  });

  const [errors, setErrors] = useState<{
    codeEtablissement?: string;
    numeroTable?: string;
    dateNaissance?: string;
  }>({});

  // Vérifier si tous les champs sont remplis
  const isFormFilled = useMemo(() => {
    const { codeEtablissement, numeroTable, dateNaissance } = formData;
    return (
      codeEtablissement.trim() !== "" &&
      numeroTable.trim() !== "" &&
      dateNaissance.jour.trim() !== "" &&
      dateNaissance.mois.trim() !== "" &&
      dateNaissance.annee.trim() !== ""
    );
  }, [formData]);

  // Vérifier si tous les champs sont valides (sans erreurs)
  const isFormValid = useMemo(() => {
    if (!isFormFilled) return false;

    const hasCodeError = !!(
      authErrors.codeEtablissement || errors.codeEtablissement
    );
    const hasTableError = !!(authErrors.numeroTable || errors.numeroTable);

    const hasJourError = !!authErrors.dateNaissance?.jour;
    const hasMoisError = !!authErrors.dateNaissance?.mois;
    const hasAnneeError = !!authErrors.dateNaissance?.annee;

    return (
      !hasCodeError &&
      !hasTableError &&
      !hasJourError &&
      !hasMoisError &&
      !hasAnneeError
    );
  }, [isFormFilled, authErrors, errors]);

  // Vérifier si le formulaire est prêt à être soumis
  const isSubmitDisabled = useMemo(() => {
    return isLoading || !isFormValid || !isTurnstileVerified;
  }, [isLoading, isFormValid, isTurnstileVerified]);

  useEffect(() => {
    setMounted(true);
    const newParticles = Array.from({ length: 30 }, (_, i) => ({
      id: i,
      x: Math.random() * 100,
      y: Math.random() * 100,
      delay: Math.random() * 5,
      size: 20 + Math.random() * 40,
    }));
    setParticles(newParticles);

    const originalError = console.error;
    console.error = (...args) => {
      if (
        typeof args[0] === "string" &&
        args[0].includes("useInsertionEffect must not schedule updates")
      ) {
        return;
      }
      originalError.apply(console, args);
    };

    return () => {
      console.error = originalError;
    };
  }, []);

  // Afficher les erreurs du store
  useEffect(() => {
    if (storeError) {
      toastRef.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: storeError,
        life: 5000,
      });
      clearStoreError();
    }
  }, [storeError, clearStoreError]);

  const getMaxDays = () => {
    return getMaxDaysFromHook(
      formData.dateNaissance.mois,
      formData.dateNaissance.annee,
    );
  };

  const sanitizeCodeEtablissement = (value: string): string => {
    return value.replace(/[^A-Za-z]/g, "").toUpperCase();
  };
  const sanitizeNumeroTable = (value: string): string => {
    return value.replace(/[^0-9]/g, "");
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    let sanitizedValue = value;

    if (name === "codeEtablissement") {
      sanitizedValue = sanitizeCodeEtablissement(value);
    } else if (name === "numeroTable") {
      sanitizedValue = sanitizeNumeroTable(value);
    }

    setFormData((prev) => ({
      ...prev,
      [name]: sanitizedValue,
    }));

    handleAuthChange(name, sanitizedValue, {
      codeEtablissement:
        name === "codeEtablissement"
          ? sanitizedValue
          : formData.codeEtablissement,
      numeroTable:
        name === "numeroTable" ? sanitizedValue : formData.numeroTable,
      dateNaissance: formData.dateNaissance,
    });

    if (errors[name as keyof typeof errors]) {
      setErrors((prev) => ({
        ...prev,
        [name]: undefined,
      }));
    }
  };

  const handleDateInputWithAutoFocus = (
    field: string,
    value: string,
    currentLength: number,
  ) => {
    // Mettre à jour la valeur
    handleDateChange(field, value);

    // Si on a atteint 2 caractères et qu'on est sur jour ou mois
    if ((field === "jour" || field === "mois") && currentLength === 2) {
      // Déterminer le prochain champ
      let nextField: string | null = null;

      if (field === "jour") {
        nextField = "mois";
      } else if (field === "mois") {
        nextField = "annee";
      }

      // Si un prochain champ existe, lui donner le focus
      if (nextField) {
        // Petit délai pour permettre à React de terminer la mise à jour du DOM
        setTimeout(() => {
          // Chercher l'input correspondant par son attribut name ou par sélecteur
          let nextInput: HTMLElement | null = null;

          if (nextField === "mois") {
            // Chercher l'input du mois (parmi les inputs avec la classe custom-date-input)
            const dateInputs = document.querySelectorAll(".date-column input");
            if (dateInputs.length >= 2) {
              nextInput = dateInputs[1] as HTMLElement; // Index 1 pour mois (0: jour, 1: mois, 2: année)
            }
          } else if (nextField === "annee") {
            const dateInputs = document.querySelectorAll(".date-column input");
            if (dateInputs.length >= 3) {
              nextInput = dateInputs[2] as HTMLElement; // Index 2 pour année
            }
          }

          if (nextInput) {
            nextInput.focus();
          }
        }, 0);
      }
    }
  };

  const handleDateChange = (field: string, value: string) => {
    let cleanValue = value.replace(/[^0-9]/g, "");

    if (field === "jour" || field === "mois") {
      if (cleanValue.length > 2) cleanValue = cleanValue.slice(0, 2);
    } else if (field === "annee") {
      if (cleanValue.length > 4) cleanValue = cleanValue.slice(0, 4);
    }

    const newDateNaissance = {
      ...formData.dateNaissance,
      [field]: cleanValue,
    };

    setFormData({
      ...formData,
      dateNaissance: newDateNaissance,
    });

    handleAuthChange(field, cleanValue, {
      codeEtablissement: formData.codeEtablissement,
      numeroTable: formData.numeroTable,
      dateNaissance: newDateNaissance,
    });
  };

  const handleDateInputChange = (
    field: string,
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const newValue = e.target.value;
    const currentLength = newValue.length;
    const previousLength =
      formData.dateNaissance[field as keyof typeof formData.dateNaissance]
        .length;

    // Appeler handleDateChange normalement
    handleDateChange(field, newValue);

    // Si l'utilisateur ajoute un caractère et atteint exactement 2 caractères pour jour/mois
    if (
      (field === "jour" || field === "mois") &&
      currentLength === 2 &&
      currentLength > previousLength
    ) {
      // Déterminer le prochain champ
      let nextIndex = field === "jour" ? 1 : 2; // 1 pour mois, 2 pour année

      setTimeout(() => {
        const dateInputs = document.querySelectorAll(".date-column input");
        if (dateInputs[nextIndex]) {
          (dateInputs[nextIndex] as HTMLElement).focus();
        }
      }, 0);
    }
    // Optionnel : permettre le retour en arrière si l'utilisateur efface tout
    else if (field === "mois" && currentLength === 0 && previousLength === 1) {
      setTimeout(() => {
        const dateInputs = document.querySelectorAll(".date-column input");
        if (dateInputs[0]) {
          (dateInputs[0] as HTMLElement).focus();
        }
      }, 0);
    }
  };

  const handleDateBlur = (field: string) => {
    handleAuthBlur(field, {
      codeEtablissement: formData.codeEtablissement,
      numeroTable: formData.numeroTable,
      dateNaissance: formData.dateNaissance,
    });
  };

  const handleGoBack = () => {
    safeNavigate("/");
  };

  const validateForm = () => {
    const formDataForValidation = {
      codeEtablissement: formData.codeEtablissement,
      numeroTable: formData.numeroTable,
      dateNaissance: formData.dateNaissance,
    };

    return validateAuthForm(formDataForValidation);
  };

  const formatDate = (): string => {
    const { jour, mois, annee } = formData.dateNaissance;
    if (!jour || !mois || !annee) return "";
    const jourStr = jour.padStart(2, "0");
    const moisStr = mois.padStart(2, "0");
    return `${jourStr}/${moisStr}/${annee}`;
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      toastRef.current?.show({
        severity: "warn",
        summary: "Formulaire invalide",
        detail: "Veuillez vérifier tous les champs",
        life: 3000,
      });
      return;
    }

    if (!isTurnstileVerified || !turnstileToken) {
      toastRef.current?.show({
        severity: "warn",
        summary: "Vérification requise",
        detail:
          turnstileError || "Veuillez compléter la vérification de sécurité",
        life: 3000,
      });
      return;
    }

    try {
      const candidatData = await fetchCandidat({
        codeEtab: formData.codeEtablissement,
        numeroTable: formData.numeroTable,
        dateNaissance: formatDate(),
        turnstileToken: turnstileToken,
      });

      if (!candidatData) {
        toastRef.current?.show({
          severity: "error",
          summary: "Erreur",
          detail: "Aucun candidat trouvé avec ces informations",
          life: 3000,
        });
        resetTurnstile();
        return;
      }

      if (cacheStatus === "HIT") {
        toastRef.current?.show({
          severity: "info",
          summary: "Information",
          detail: "Données chargées depuis le cache",
          life: 2000,
        });
      }

      const pdfBlob = await generatePdf(formData.numeroTable);

      if (!pdfBlob) {
        toastRef.current?.show({
          severity: "warn",
          summary: "Attention",
          detail: "Candidat trouvé mais génération PDF impossible",
          life: 3000,
        });
      }

      const candidatStorageData = {
        codeEtab: formData.codeEtablissement,
        centreCode: candidatData.centreCode,
        codeEtablissement: formData.codeEtablissement,
        numeroTable: formData.numeroTable,
        dateNaissance: formatDate(),
        prenoms: candidatData.prenoms,
        nom: candidatData.nom,
        lieuNaissance: candidatData.lieuNaissance,
        nationalite: candidatData.nationalite,
        jury: candidatData.jury,
        serie: candidatData.serie,
        sexe: candidatData.sexe,
        typeCandidat: candidatData.typeCandidat,
        eps: candidatData.eps,
        etablissementName: candidatData.etablissementName,
        centreEcritName: candidatData.centreEcritName,
        centreEcritParticulier: candidatData.centreEcritParticulier,
        centreActEPSName: candidatData.centreActEPSName,
        mo1: candidatData.mo1,
        mo2: candidatData.mo2,
        mo3: candidatData.mo3,
        ef1: candidatData.ef1,
        ef2: candidatData.ef2,
        centreMatFac1: candidatData.centreMatFac1,
        libMatFac1: candidatData.libMatFac1,
        centreMatFac2: candidatData.centreMatFac2,
        libMatFac2: candidatData.libMatFac2,
        timestamp: Date.now(),
      };

      localStorage.setItem(
        "candidat_info",
        JSON.stringify(candidatStorageData),
      );

      toastRef.current?.show({
        severity: "success",
        summary: "Succès",
        detail: "Authentification réussie, redirection en cours...",
        life: 2000,
      });

      setTimeout(() => {
        safeNavigate("./candidats");
      }, 1500);
    } catch (error) {
      console.error("Erreur lors de l'authentification:", error);
      toastRef.current?.show({
        severity: "error",
        summary: "Erreur",
        detail: "Une erreur est survenue lors de l'authentification",
        life: 3000,
      });
      resetTurnstile();
    }
  };

  if (!mounted) {
    return null;
  }

  // Récupérer les erreurs individuelles de date
  const jourError = authErrors.dateNaissance?.jour || "";
  const moisError = authErrors.dateNaissance?.mois || "";
  const anneeError = authErrors.dateNaissance?.annee || "";

  return (
    <>
      <Toast ref={toastRef} />
      <div className="min-h-screen w-full bg-white relative overflow-y-auto overflow-x-hidden">
        {/* Particules animées */}
        <div className="particles-container">
          {particles.map((particle) => (
            <motion.div
              key={particle.id}
              className="particle"
              initial={{ opacity: 0, scale: 0 }}
              animate={{
                opacity: [0, 0.6, 0],
                scale: [0, 1.5, 0],
                x: [0, (Math.random() - 0.5) * 200],
                y: [0, (Math.random() - 0.5) * 200],
              }}
              transition={{
                duration: 4 + Math.random() * 3,
                delay: particle.delay,
                repeat: Infinity,
                repeatType: "loop",
              }}
              style={{
                left: `${particle.x}%`,
                top: `${particle.y}%`,
                width: `${particle.size}px`,
                height: `${particle.size}px`,
                background: `radial-gradient(circle at 30% 30%, rgba(33,150,243,0.8), rgba(21,101,192,0.6))`,
                boxShadow: `0 0 ${particle.size / 2}px rgba(33,150,243,0.5)`,
              }}
            />
          ))}
        </div>

        {/* Section bleue avec animation de vagues - conservée identique */}
        <div
          className="w-full relative"
          style={{
            background:
              "linear-gradient(135deg, #1565C0 0%, #0D47A1 50%, #0D47A1 100%)",
            minHeight: "55vh",
            position: "relative",
            zIndex: 1,
            paddingBottom: "2rem",
            overflow: "hidden",
          }}
        >
          <div className="waves">
            <svg
              className="waves-svg"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 1440 320"
            >
              <motion.path
                fill="rgba(255,255,255,0.1)"
                fillOpacity="1"
                d="M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,165.3C1248,149,1344,107,1392,85.3L1440,64L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"
                animate={{
                  d: [
                    "M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,122.7C672,117,768,139,864,154.7C960,171,1056,181,1152,165.3C1248,149,1344,107,1392,85.3L1440,64L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z",
                    "M0,160L48,138.7C96,117,192,75,288,74.7C384,75,480,117,576,138.7C672,160,768,160,864,144C960,128,1056,96,1152,101.3C1248,107,1344,149,1392,170.7L1440,192L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z",
                  ],
                }}
                transition={{
                  duration: 8,
                  repeat: Infinity,
                  repeatType: "reverse",
                  ease: "easeInOut",
                }}
              />
              <motion.path
                fill="rgba(255,255,255,0.15)"
                fillOpacity="1"
                d="M0,192L48,176C96,160,192,128,288,122.7C384,117,480,139,576,165.3C672,192,768,224,864,224C960,224,1056,192,1152,176C1248,160,1344,160,1392,160L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"
                animate={{
                  d: [
                    "M0,192L48,176C96,160,192,128,288,122.7C384,117,480,139,576,165.3C672,192,768,224,864,224C960,224,1056,192,1152,176C1248,160,1344,160,1392,160L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z",
                    "M0,224L48,202.7C96,181,192,139,288,144C384,149,480,203,576,213.3C672,224,768,192,864,170.7C960,149,1056,139,1152,149.3C1248,160,1344,192,1392,208L1440,224L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z",
                  ],
                }}
                transition={{
                  duration: 6,
                  repeat: Infinity,
                  repeatType: "reverse",
                  ease: "easeInOut",
                }}
              />
            </svg>
          </div>

          {/* Bouton retour */}
          <motion.div
            className="absolute"
            style={{ top: "20px", left: "20px", zIndex: 20 }}
            initial={{ opacity: 0, x: -50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5 }}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            <Button
              icon="pi pi-arrow-left"
              label="Retour"
              className="p-button-text p-button-rounded"
              onClick={handleGoBack}
              style={{
                color: "white",
                backgroundColor: "rgba(0,0,0,0.3)",
                border: "1px solid rgba(255,255,255,0.3)",
                borderRadius: "50px",
                padding: "8px 16px",
                backdropFilter: "blur(10px)",
              }}
            />
          </motion.div>

          {/* Overlay dégradé */}
          <motion.div
            className="absolute inset-0"
            style={{
              background:
                "linear-gradient(135deg, rgba(21,101,192,0.9) 0%, rgba(13,71,161,0.95) 100%)",
            }}
            animate={{
              opacity: [0.9, 0.95, 0.9],
            }}
            transition={{
              duration: 4,
              repeat: Infinity,
              repeatType: "reverse",
            }}
          />

          {/* Header */}
          <motion.div
            className="relative z-10 flex flex-column align-items-center pt-5"
            initial={{ opacity: 0, y: -50 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.2 }}
          >
            <motion.div
              className="flex justify-content-center mb-4"
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{
                type: "spring",
                stiffness: 260,
                damping: 20,
                delay: 0.3,
              }}
              whileHover={{ scale: 1.05, rotate: 360 }}
            >
              <img
                src="/layout/images/logo-UCAD.png"
                alt="logo-ucad"
                className="logo-img"
                style={{
                  width: "80px",
                  height: "80px",
                  backgroundColor: "white",
                  padding: "8px",
                  borderRadius: "50%",
                  boxShadow: "0 4px 20px rgba(0,0,0,0.3)",
                }}
              />
            </motion.div>

            <motion.div
              className="text-container"
              style={{ marginTop: "1rem" }}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.8, delay: 0.5 }}
            >
              <motion.h1
                className="text-white font-bold text-center page-title"
                animate={{
                  textShadow: [
                    "2px 2px 4px rgba(0,0,0,0.3)",
                    "4px 4px 8px rgba(0,0,0,0.5)",
                    "2px 2px 4px rgba(0,0,0,0.3)",
                  ],
                }}
                transition={{
                  duration: 3,
                  repeat: Infinity,
                  repeatType: "reverse",
                }}
              >
                Office du Baccalauréat
              </motion.h1>
              <motion.p
                className="text-white text-center page-subtitle mt-2"
                animate={{ opacity: [0.7, 1, 0.7] }}
                transition={{ duration: 2, repeat: Infinity }}
              >
                Accéder à votre espace candidat
              </motion.p>
            </motion.div>
          </motion.div>
        </div>

        {/* Formulaire */}
        <motion.div
          className="form-wrapper"
          initial={{ opacity: 0, y: 50 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.4 }}
        >
          <div className="flex justify-content-center align-items-start px-4 pb-5">
            <motion.div
              className="candidat-card w-full"
              style={{
                borderBottom: "8px solid #2196f3",
                borderRadius: "12px",
                pointerEvents: "auto",
              }}
              whileHover={{
                boxShadow: "0 25px 40px -12px rgba(33,150,243,0.3)",
              }}
              transition={{ duration: 0.3 }}
            >
              <motion.div
                className="card-header"
                initial={{ x: -100, opacity: 0 }}
                animate={{ x: 0, opacity: 1 }}
                transition={{ delay: 0.5, duration: 0.5 }}
              >
                <div className="flex align-items-center gap-2 mb-2">
                  <motion.i
                    className="pi pi-user"
                    style={{ color: "#2196f3", fontSize: "1.25rem" }}
                    animate={{ rotate: [0, 360] }}
                    transition={{ duration: 1, delay: 0.8 }}
                  />
                  <span
                    className="text-xl font-semibold"
                    style={{ color: "#1565C0" }}
                  >
                    Espace Candidat
                  </span>
                </div>
                {cacheStatus && (
                  <motion.div
                    className={`text-xs mt-2 ${
                      cacheStatus === "HIT" ? "text-green-600" : "text-blue-600"
                    }`}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ delay: 0.6, duration: 0.5 }}
                  >
                    {cacheStatus === "HIT"
                      ? "📦 Données en cache"
                      : "🔄 Données fraîches"}
                  </motion.div>
                )}
              </motion.div>

              <div className="card-body">
                {/* Code Établissement */}
                <motion.div
                  className="field"
                  initial={{ x: -50, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  transition={{ delay: 0.6, duration: 0.5 }}
                >
                  <label
                    htmlFor="codeEtablissement"
                    className="font-semibold block"
                  >
                    Code Établissement <span className="text-red-500">*</span>
                  </label>
                  <InputText
                    id="codeEtablissement"
                    name="codeEtablissement"
                    value={formData.codeEtablissement}
                    onChange={handleInputChange}
                    onBlur={() =>
                      handleAuthBlur("codeEtablissement", {
                        codeEtablissement: formData.codeEtablissement,
                        numeroTable: formData.numeroTable,
                        dateNaissance: formData.dateNaissance,
                      })
                    }
                    className={`w-full ${
                      errors.codeEtablissement ? "p-invalid" : ""
                    }`}
                    placeholder="Ex: LSLL"
                    disabled={isLoading}
                  />
                  <ValidationTooltip
                    message={
                      errors.codeEtablissement ||
                      authErrors.codeEtablissement ||
                      ""
                    }
                    isVisible={
                      !!(
                        errors.codeEtablissement ||
                        (authTouched.codeEtablissement &&
                          authErrors.codeEtablissement)
                      )
                    }
                  />
                </motion.div>

                {/* Numéro de table */}
                <motion.div
                  className="field"
                  initial={{ x: -50, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  transition={{ delay: 0.7, duration: 0.5 }}
                >
                  <label htmlFor="numeroTable" className="font-semibold block">
                    Numéro de table <span className="text-red-500">*</span>
                  </label>
                  <InputText
                    id="numeroTable"
                    name="numeroTable"
                    value={formData.numeroTable}
                    onChange={handleInputChange}
                    onBlur={() =>
                      handleAuthBlur("numeroTable", {
                        codeEtablissement: formData.codeEtablissement,
                        numeroTable: formData.numeroTable,
                        dateNaissance: formData.dateNaissance,
                      })
                    }
                    className={`w-full ${
                      errors.numeroTable ? "p-invalid" : ""
                    }`}
                    placeholder="Ex: 106610"
                    disabled={isLoading}
                  />
                  <ValidationTooltip
                    message={errors.numeroTable || authErrors.numeroTable || ""}
                    isVisible={
                      !!(
                        errors.numeroTable ||
                        (authTouched.numeroTable && authErrors.numeroTable)
                      )
                    }
                  />
                </motion.div>

                {/* Date de naissance avec erreurs séparées */}
                <motion.div
                  className="field"
                  initial={{ x: -50, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  transition={{ delay: 0.8, duration: 0.5 }}
                >
                  <label className="font-semibold block">
                    Date de naissance <span className="text-red-500">*</span>
                  </label>

                  <div className="date-picker-container">
                    <div className="date-column">
                      <label className="date-label">Jour</label>
                      <input
                        type="text"
                        className={`custom-date-input ${
                          jourError ? "p-invalid" : ""
                        }`}
                        value={formData.dateNaissance.jour}
                        onChange={(e) => handleDateInputChange("jour", e)}
                        onBlur={() => handleDateBlur("jour")}
                        placeholder="JJ"
                        maxLength={2}
                        disabled={isLoading}
                      />
                      <ValidationTooltip
                        message={jourError}
                        isVisible={!!jourError}
                      />
                    </div>

                    <div className="date-column">
                      <label className="date-label">Mois</label>
                      <input
                        type="text"
                        className={`custom-date-input ${
                          moisError ? "p-invalid" : ""
                        }`}
                        value={formData.dateNaissance.mois}
                        onChange={(e) => handleDateInputChange("mois", e)}
                        onBlur={() => handleDateBlur("mois")}
                        placeholder="MM"
                        maxLength={2}
                        disabled={isLoading}
                      />
                      <ValidationTooltip
                        message={moisError}
                        isVisible={!!moisError}
                      />
                    </div>

                    <div className="date-column">
                      <label className="date-label">Année</label>
                      <input
                        type="text"
                        className={`custom-date-input ${
                          anneeError ? "p-invalid" : ""
                        }`}
                        value={formData.dateNaissance.annee}
                        onChange={(e) =>
                          handleDateChange("annee", e.target.value)
                        }
                        onBlur={() => handleDateBlur("annee")}
                        placeholder="AAAA"
                        maxLength={4}
                        disabled={isLoading}
                      />
                      <ValidationTooltip
                        message={anneeError}
                        isVisible={!!anneeError}
                      />
                    </div>
                  </div>
                </motion.div>

                {/* Cloudflare Turnstile */}
                <motion.div
                  className="field mt-4"
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: 0.9, duration: 0.5 }}
                >
                  <CloudflareTurnstile
                    onVerify={handleTurnstileVerify}
                    onError={handleTurnstileError}
                    onExpire={handleTurnstileExpire}
                    onLoad={handleTurnstileLoad}
                    theme="light"
                    size="normal"
                    widgetKey={widgetKey}
                  />
                  {turnstileError && (
                    <div className="text-red-500 text-sm mt-2 text-center">
                      {turnstileError}
                    </div>
                  )}
                </motion.div>
              </div>

              <motion.div
                className="card-footer"
                initial={{ y: 50, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{ delay: 1, duration: 0.5 }}
              >
                <motion.div
                  whileHover={{ scale: isSubmitDisabled ? 1 : 1.02 }}
                  whileTap={{ scale: isSubmitDisabled ? 1 : 0.98 }}
                >
                  <Button
                    label={isLoading ? "Vérification en cours..." : "Soumettre"}
                    icon="pi pi-search"
                    onClick={handleSubmit}
                    loading={isLoading}
                    disabled={isSubmitDisabled}
                    className="submit-btn w-full"
                    style={{
                      background: isSubmitDisabled
                        ? "linear-gradient(135deg, #ccc 0%, #999 100%)"
                        : "linear-gradient(135deg, #2196f3 0%, #1565C0 100%)",
                      fontSize: "1rem",
                      fontWeight: "500",
                      border: "none",
                      cursor: isSubmitDisabled ? "not-allowed" : "pointer",
                      opacity: isSubmitDisabled ? 0.6 : 1,
                    }}
                  />
                </motion.div>
              </motion.div>
            </motion.div>
          </div>
        </motion.div>
      </div>

      <style jsx global>{`
        /* Tous vos styles existants restent identiques */
        @keyframes float {
          0%,
          100% {
            transform: translateY(0px);
          }
          50% {
            transform: translateY(-20px);
          }
        }
        @keyframes pulse-blue {
          0%,
          100% {
            box-shadow: 0 0 0 0 rgba(33, 150, 243, 0.4);
          }
          50% {
            box-shadow: 0 0 0 20px rgba(33, 150, 243, 0);
          }
        }
        .particles-container {
          position: fixed;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          pointer-events: none;
          z-index: 0;
        }
        .particle {
          position: absolute;
          border-radius: 50%;
          pointer-events: none;
          backdrop-filter: blur(2px);
        }
        .waves {
          position: absolute;
          bottom: 0;
          left: 0;
          width: 100%;
          overflow: hidden;
          line-height: 0;
          z-index: 2;
        }
        .waves-svg {
          position: relative;
          display: block;
          width: calc(100% + 1.3px);
          height: 80px;
        }
        :root {
          --spacing-xs: 0.5rem;
          --spacing-sm: 0.75rem;
          --spacing-md: 1rem;
          --spacing-lg: 1.5rem;
          --spacing-xl: 2rem;
          --border-radius: 12px;
          --input-padding: 0.75rem;
        }
        .page-title {
          font-size: 3rem !important;
          text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
          margin: 0;
          background: linear-gradient(135deg, #fff, #e3f2fd);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }
        .page-subtitle {
          font-size: 2rem;
          text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
        }
        .text-container {
          text-align: center;
          padding: 0 1rem;
        }
        .candidat-card {
          max-width: 400px;
          width: 100%;
          background: white;
          border-radius: 25px !important;
          box-shadow: 0 20px 35px -12px rgba(0, 0, 0, 0.25);
          overflow: hidden;
          margin-top: -140px;
          transition: all 0.3s ease;
        }
        .form-wrapper {
          position: relative;
          z-index: 10;
          padding: 0 1rem 1rem 1rem;
        }
        .card-header {
          padding: var(--spacing-md) var(--spacing-lg) var(--spacing-xs)
            var(--spacing-lg);
          background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
          border-bottom: 1px solid #e2e8f0;
        }
        .card-body {
          padding: var(--spacing-md);
        }
        .card-footer {
          padding: 0 var(--spacing-md) var(--spacing-md) var(--spacing-md);
          background: #fafafa;
        }
        .field {
          margin-bottom: var(--spacing-md);
        }
        .field:last-child {
          margin-bottom: 0;
        }
        .field label {
          margin-bottom: var(--spacing-xs);
          display: block;
        }
        .field input,
        .custom-date-input {
          padding: var(--input-padding);
        }
        .p-error {
          margin-top: var(--spacing-xs);
          display: block;
        }
        .date-picker-container {
          display: flex;
          gap: var(--spacing-md);
          justify-content: space-between;
        }
        .date-column {
          flex: 1;
          text-align: center;
        }
        .date-label {
          display: block;
          font-size: 0.7rem;
          color: #6c757d;
          margin-bottom: var(--spacing-xs);
          font-weight: 500;
          text-align: left;
        }
        .custom-date-input {
          width: 100%;
          border: 1px solid #ced4da;
          border-radius: 6px;
          font-size: 0.9rem;
          text-align: center;
          background-color: white;
          transition: all 0.3s ease;
        }
        .custom-date-input:hover:not(:disabled) {
          border-color: #2196f3;
          box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
        }
        .custom-date-input:focus:not(:disabled) {
          outline: none;
          border-color: #2196f3;
          box-shadow: 0 0 0 3px rgba(33, 150, 243, 0.3);
        }
        .custom-date-input:disabled {
          background-color: #f5f5f5;
          cursor: not-allowed;
        }
        .custom-date-input.p-invalid {
          border-color: #f44336;
        }
        .submit-btn {
          border-radius: 8px;
          padding: var(--input-padding) !important;
          transition: all 0.3s ease;
        }
        .submit-btn:hover:not(:disabled) {
          background: linear-gradient(
            135deg,
            #1e88e5 0%,
            #0d47a1 100%
          ) !important;
          transform: translateY(-2px);
        }
        .submit-btn:active:not(:disabled) {
          transform: translateY(0);
        }
        ::-webkit-scrollbar {
          width: 4px;
          height: 4px;
        }
        ::-webkit-scrollbar-track {
          background: #f1f1f1;
          border-radius: 10px;
        }
        ::-webkit-scrollbar-thumb {
          background: linear-gradient(135deg, #2196f3 0%, #1565c0 100%);
          border-radius: 10px;
        }
        @media (max-width: 768px) {
          .page-title {
            font-size: 2rem !important;
          }
          .page-subtitle {
            font-size: 1.25rem !important;
          }
          .candidat-card {
            margin-top: -130px;
          }
        }
        @media (max-width: 640px) {
          .page-title {
            font-size: 1.1rem;
          }
          .page-subtitle {
            font-size: 0.75rem;
          }
          .card-header {
            padding: var(--spacing-md) var(--spacing-md) var(--spacing-xs)
              var(--spacing-md);
          }
          .card-body {
            padding: var(--spacing-md);
          }
          .candidat-card {
            max-width: 100%;
            margin-top: -120px;
          }
          .card-footer {
            padding: 0 var(--spacing-md) var(--spacing-md) var(--spacing-md);
          }
          .date-column {
            width: auto;
            min-width: 0;
          }
          .field {
            margin-bottom: var(--spacing-md);
          }
        }
        @media (max-width: 480px) {
          .page-title {
            font-size: 1rem;
          }
          .page-subtitle {
            font-size: 0.7rem;
          }
          .candidat-card {
            margin-top: -100px;
          }
          .date-picker-container {
            gap: var(--spacing-xs);
          }
        }
      `}</style>
    </>
  );
}
