"use client";

import React, { useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Toast } from "primereact/toast";
import { authService } from "@/demo/service/AuthService";
import { userService } from "@/demo/service/UserService";
import Link from "next/link";
import { classNames } from "primereact/utils";

const Login: React.FC = () => {
  const [credentials, setCredentials] = useState({ login: "", password: "" });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<{ login?: string; password?: string }>(
    {},
  );
  const router = useRouter();
  const toast = useRef<any>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCredentials({
      ...credentials,
      [e.target.name]: e.target.value,
    });

    // Nettoyage de l'erreur quand l'utilisateur saisit
    setErrors({ ...errors, [e.target.name]: "" });
  };

  const handleGoBack = () => {
    router.push("/");
  };

  const login = async (e: React.FormEvent) => {
    e.preventDefault();

    let newErrors: { login?: string; password?: string } = {};

    if (!credentials.login) newErrors.login = "Le login est obligatoire";
    if (!credentials.password)
      newErrors.password = "Le mot de passe est obligatoire";

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    try {
      const response = await authService.login(credentials);
      if (response.user.state_account) {
        toast.current.show({
          severity: "success",
          summary: "Office du Bac",
          detail: "Connexion réussie",
          life: 4000,
        });
        //console.log('Login successful', response.user);
        userService.setUser(response.user);
        if (response.user.first_connexion) {
          router.push("/changement-mot-de-passe");
          toast.current.show({
            severity: "success",
            summary: "Office du Bac",
            detail: "Veuillez changer votre mot de passe",
            life: 4000,
          });
        } else {
          if (response.token) {
            localStorage.setItem("token", response.token);
            if (
              response.user.profil.name === "FINANCE_COMPTA" ||
              response.user.profil.name === "SCOLARITE" ||
              response.user.profil.name === "ADMIN" ||
              response.user.profil.name === "DEMSG" ||
              response.user.profil.name === "INSPECTEUR_ACADEMIE"
            ) {
              // console.log('IF-SCO');
              router.push("/tableau-de-bord");
            }
            if (response.user.profil.name === "VIGNETTES_COUPONS") {
              // console.log('IF-VC');
              router.push("/scolarite/vignettes-coupons");
            }
            if (response.user.profil.name === "AUTORISATION_RECEPTION") {
              // console.log('IF-AR');
              router.push("/scolarite/autorisation-reception");
            }
            if (response.user.profil.name === "RECEPTIONNISTE") {
              // console.log('IF-SCO');
              router.push("/scolarite/reception-candidats");
            }

            if (
              response.user.profil.name === "AGENT_DE_SAISIE" ||
              response.user.profil.name === "CHEF_ETABLISSEMENT"
            ) {
              // console.log('IF-SCO');
              router.push("/scolarite/enrolement-candidat");
            }
          } else {
            router.push("/");
          }
        }
      } else {
        toast.current.show({
          severity: "warn",
          summary: "Office du Bac",
          detail: "Compte désactivé veuillez contacter OB",
          life: 4000,
        });
        //console.warn('Account locked', response.user);
      }
    } catch (error) {
      toast.current.show({
        severity: "error",
        summary: "Office du Bac",
        detail: "Erreur de connexion",
        life: 3000,
      });
      //console.error('Login failed', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Toast ref={toast} />
      <div
        className="flex flex-column relative justify-content-center align-items-center min-h-screen w-full bg-cover bg-center bg-no-repeat"
        style={{ backgroundImage: "url('/log1.jpg')" }}
      >
        {/* Overlay pour améliorer la lisibilité */}
        <div className="absolute inset-0 bg-black bg-opacity-50"></div>

        {/* Bouton retour */}
        <div
          className="absolute"
          style={{ top: "20px", left: "20px", zIndex: 20 }}
        >
          <Button
            icon="pi pi-arrow-left"
            label="Retour"
            className="p-button-text p-button-rounded"
            onClick={handleGoBack}
            style={{
              color: "white",
              backgroundColor: "rgba(0,0,0,0.5)",
              border: "1px solid rgba(255,255,255,0.3)",
              borderRadius: "50px",
              padding: "8px 16px",
            }}
          />
        </div>

        <div
          className="absolute w-full text-center"
          style={{ top: "10px", left: "50%", transform: "translateX(-50%)" }}
        >
          <h1 className="font-bold text-white">
            UNE PLATEFORME, DES MILLIERS D&apos;ACTEURS, UNE MISSION
          </h1>
        </div>

        {/* Card centrée */}
        <div
          className="p-5 border-round-xl shadow-3"
          style={{
            width: "400px",
            backgroundColor: "rgba(0, 0, 0, 0.4)",
            color: "white",
            zIndex: 5,
          }}
        >
          {/* Logo centré */}
          <div className="flex justify-content-center mb-3">
            <img
              src={`/layout/images/logo-UCAD.png`}
              alt="logo-ucad"
              style={{
                width: "100px",
                height: "100px",
                backgroundColor: "white",
                padding: "4px",
                borderRadius: "50%",
                boxShadow: "0 2px 6px rgba(0,0,0,0.15)",
              }}
            />
          </div>

          <div className="flex flex-column justify-content-center mb-3">
            <h2 className="text-white text-center mb-2">
              Connexion à PortailBAC
            </h2>
            <p className="text-sm text-white text-center mb-4">
              Veuillez fournir votre login et votre mot de passe
            </p>
          </div>

          <div className="py-2">
            <InputText
              autoComplete="off"
              name="login"
              placeholder="Login"
              value={credentials.login}
              onChange={handleChange}
              className={classNames("w-full", { "p-invalid": errors.login })}
            />
            {errors.login && <small className="p-error">{errors.login}</small>}
          </div>

          <div className="py-2">
            <div className="relative w-full">
              <InputText
                autoComplete="off"
                name="password"
                type={showPassword ? "text" : "password"}
                placeholder="Mot de passe"
                value={credentials.password}
                onChange={handleChange}
                className={classNames("w-full", {
                  "p-invalid": errors.password,
                })}
                style={{ paddingRight: "2.5rem" }}
              />
              <i
                className={classNames("pi", {
                  "pi-eye": !showPassword,
                  "pi-eye-slash": showPassword,
                })}
                style={{
                  position: "absolute",
                  right: "0.75rem",
                  top: "50%",
                  transform: "translateY(-50%)",
                  cursor: "pointer",
                  zIndex: 10,
                  color: "#6c757d",
                }}
                onClick={() => setShowPassword(!showPassword)}
              />
            </div>
            {errors.password && (
              <small className="p-error">{errors.password}</small>
            )}
          </div>

          <div className="text-left py-2">
            <Link
              href="/mot-de-passe-oublie"
              className="text-white font-bold text-sm hover:underline"
            >
              Mot de passe oublié ?
            </Link>
          </div>

          <div className="py-3">
            <Button
              label="Connexion"
              className="w-full text-xl font-bold"
              onClick={login}
              loading={loading}
            />
          </div>

          <div
            className="absolute w-full text-center"
            style={{
              bottom: "2rem",
              left: "50%",
              transform: "translateX(-50%)",
            }}
          >
            <p className="font-bold text-white">
              <span className="footer-copyright">
                &#169; Office du Baccalauréat. Tous droits réservés
              </span>
            </p>
            <div className="font-bold text-white flex justify-content-center gap-5">
              {/* <Link href="/#" className="text-white font-bold text-sm hover:underline">
                                <span className="footer-copyright">Conditions d'utilisation</span>
                            </Link>
                            <Link href="/#" className="text-white font-bold text-sm hover:underline">
                                <span className="footer-copyright">Confidentialité</span>
                            </Link>
                            <Link href="/#" className="text-white font-bold text-sm hover:underline">
                                <span className="footer-copyright">Support</span>
                            </Link> */}
            </div>
          </div>
        </div>

        <style jsx global>{`
          /* Styles mobiles */
          @media (max-width: 768px) {
            .absolute.w-full.text-center h1 {
              font-size: 0.9rem !important;
              padding: 0 1rem;
              top: 60px !important;
              position: relative;
            }

            div[style*="width: 400px"] {
              width: 90% !important;
              margin-top: 2rem;
            }

            /* Bouton retour mobile */
            button[icon="pi pi-arrow-left"] {
              padding: 6px 12px !important;
              font-size: 0.8rem !important;
            }

            .footer-copyright {
              font-size: 0.7rem;
            }
          }

          @media (max-width: 480px) {
            .absolute.w-full.text-center h1 {
              font-size: 0.7rem !important;
            }

            div[style*="width: 400px"] {
              width: 95% !important;
              padding: 1rem !important;
            }

            h2 {
              font-size: 1.2rem !important;
            }

            p.text-sm {
              font-size: 0.7rem !important;
            }
          }
        `}</style>
      </div>
    </>
  );
};

export default Login;
