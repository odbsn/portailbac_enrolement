"use client";

import React, { useState, useRef, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Button } from "primereact/button";
import { Toast } from "primereact/toast";
import ClientOnly from "./components/ClientOnly";

const HomePage: React.FC = () => {
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [isNavigating, setIsNavigating] = useState(false);
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setMounted(true);
    setIsClient(true);

    // Solution 5: Ignorer l'erreur spécifique dans l'environnement mobile/simulateur
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

  const handleNavigation = (path: string) => {
    if (isNavigating || !isClient) return;

    setIsNavigating(true);

    // Utiliser window.location pour les simulateurs et appareils mobiles
    // Cela évite les conflits avec le router de Next.js dans les environnements simulés
    if (
      window.innerWidth <= 768 ||
      /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent)
    ) {
      window.location.href = path;
    } else {
      router.push(path);
    }
  };

  const handleEtablissementClick = () => {
    handleNavigation("/se-connecter");
  };

  const handleCandidatClick = () => {
    handleNavigation("/search");
  };

  // Ne rien rendre tant que le client n'est pas prêt
  if (!mounted || !isClient) {
    return null;
  }

  return (
    <ClientOnly>
      <div
        className="flex flex-column relative justify-content-center align-items-center min-h-screen w-full bg-cover bg-center bg-no-repeat"
        style={{ backgroundImage: "url('/log1.jpg')" }}
      >
        <div className="absolute inset-0 bg-black bg-opacity-50"></div>

        {/* Header */}
        <div
          className="absolute w-full text-center header-title"
          style={{
            top: "30px",
            left: "50%",
            transform: "translateX(-50%)",
            zIndex: 10,
          }}
        >
          <div className="flex justify-content-center gap-3 mb-3 desktop-logo">
            <img
              src={`/layout/images/mesri.jpeg`}
              alt="logo-mesri"
              style={{
                width: "150px",
                padding: "4px",
                borderRadius: "10%",
                boxShadow: "0 2px 6px rgba(0,0,0,0.15)",
              }}
            />
            {/* <img
              src="/layout/images/logo-UCAD.png"
              alt="logo-ucad"
              className="desktop-logo-img"
              style={{
                width: "100px",
                height: "100px",
                backgroundColor: "white",
                padding: "8px",
                borderRadius: "50%",
                boxShadow: "0 4px 12px rgba(0,0,0,0.2)",
              }}
            /> */}
          </div>
          <h1 className="font-bold text-white desktop-title">
            Office du Baccalauréat
          </h1>
          <p className="text-white text-center desktop-subtitle mt-2">
            Une plateforme, des milliers d'acteurs, une mission
          </p>
        </div>

        {/* Container pour les deux boutons */}
        <div
          className="flex gap-6 align-items-stretch buttons-container desktop-container"
          style={{
            maxWidth: "900px",
            width: "100%",
            padding: "2rem",
            zIndex: 5,
          }}
        >
          {/* Espace Établissement */}
          <div
            className="flex-1 border-round-xl shadow-4 button-card desktop-card"
            style={{
              background: "linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)",
              borderRadius: "20px",
              cursor: "pointer",
              touchAction: "manipulation",
            }}
            onClick={handleEtablissementClick}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
              if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                handleNavigation("/connexion");
              }
            }}
          >
            <div className="flex flex-column align-items-center text-center p-5 desktop-content">
              <div className="button-icon-wrapper desktop-icon mb-0">
                <i
                  className="pi pi-building"
                  style={{
                    fontSize: "3rem",
                    color: "white",
                  }}
                />
              </div>
              <h2 className="text-white text-xl font-bold mb-2 desktop-card-title">
                Espace Etablissement
              </h2>
              <p className="text-white text-center opacity-80 text-sm desktop-card-desc">
                Gestion des inscriptions, convocations et suivi des candidats
              </p>
              <Button
                label="Accéder"
                icon="pi pi-arrow-right"
                className="mt-3 desktop-btn"
                style={{
                  backgroundColor: "rgba(255,255,255,0.2)",
                  border: "1px solid rgba(255,255,255,0.3)",
                  borderRadius: "12px",
                  padding: "8px 20px",
                }}
              />
            </div>
          </div>

          {/* Espace Candidat */}
          <div
            className="flex-1 border-round-xl shadow-4 button-card desktop-card"
            style={{
              background: "linear-gradient(135deg, #1e3c72 0%, #2a5298 100%)",
              borderRadius: "20px",
              cursor: "pointer",
              touchAction: "manipulation",
            }}
            onClick={handleCandidatClick}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => {
              if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                handleNavigation("/search");
              }
            }}
          >
            <div className="flex flex-column align-items-center text-center p-5 desktop-content">
              <div className="button-icon-wrapper desktop-icon mb-0">
                <i
                  className="pi pi-user"
                  style={{
                    fontSize: "3rem",
                    color: "white",
                  }}
                />
              </div>
              <h2 className="text-white text-xl font-bold mb-2 desktop-card-title">
                Espace Candidat
              </h2>
              <p className="text-white text-center opacity-80 text-sm desktop-card-desc">
                Vérifier vos informations et résultats du Bac, télécharger votre
                convocation, ...
              </p>
              <Button
                label="Accéder"
                icon="pi pi-arrow-right"
                className="mt-3 desktop-btn"
                style={{
                  backgroundColor: "rgba(255,255,255,0.2)",
                  border: "1px solid rgba(255,255,255,0.3)",
                  borderRadius: "12px",
                  padding: "8px 20px",
                }}
              />
            </div>
          </div>
        </div>

        {/* Footer */}
        <div
          className="absolute w-full text-center desktop-footer"
          style={{
            bottom: "2rem",
            left: "50%",
            transform: "translateX(-50%)",
            zIndex: 10,
          }}
        >
          <p className="text-white text-sm">
            <span className="footer-copyright">
              &#169; Office du Baccalauréat. Tous droits réservés
            </span>
          </p>
        </div>
      </div>

      <style jsx global>{`
        .desktop-title {
          font-size: 2.5rem;
          padding: 0 1rem;
          line-height: 1.3;
          text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
        }

        .desktop-subtitle {
          font-size: 2rem;
          text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
        }

        .desktop-container {
          display: flex;
          flex-direction: row;
          gap: 1.5rem;
        }

        .desktop-card {
          transition: transform 0.3s ease, box-shadow 0.3s ease;
        }

        .desktop-card:hover {
          transform: translateY(-8px);
          box-shadow: 0 25px 35px -12px rgba(0, 0, 0, 0.25) !important;
        }

        .desktop-icon {
          width: 80px;
          height: 80px;
          background: rgba(255, 255, 255, 0.15);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .desktop-card-title {
          font-size: 1.5rem;
        }

        .desktop-card-desc {
          font-size: 0.875rem;
        }

        .desktop-btn {
          font-size: 0.875rem;
        }

        @media (max-width: 768px) {
          .header-title {
            top: 15px !important;
            position: relative !important;
            margin-bottom: 1rem;
          }

          .desktop-logo-img {
            width: 60px !important;
            height: 60px !important;
            padding: 6px !important;
          }

          .desktop-title {
            font-size: 1.3rem !important;
          }

          .desktop-subtitle {
            font-size: 0.8rem !important;
          }

          .desktop-container {
            flex-direction: column !important;
            gap: 1rem !important;
            padding: 1rem !important;
            max-width: 90% !important;
            margin-top: 1rem;
          }

          .desktop-card {
            width: 100% !important;
            border-radius: 12px !important;
          }

          .desktop-content {
            padding: 1.5rem !important;
          }

          .desktop-icon {
            width: 60px !important;
            height: 60px !important;
            margin-bottom: 0.75rem !important;
          }

          .desktop-icon i {
            font-size: 2rem !important;
          }

          .desktop-card-title {
            font-size: 1.2rem !important;
            margin-bottom: 0.5rem !important;
          }

          .desktop-card-desc {
            font-size: 0.8rem !important;
            padding: 0 1rem !important;
          }

          .desktop-btn {
            font-size: 0.8rem !important;
            padding: 6px 16px !important;
            margin-top: 0.75rem !important;
          }

          .desktop-footer {
            position: relative !important;
            margin-top: 2rem;
            bottom: auto !important;
          }

          .desktop-footer p {
            font-size: 0.7rem !important;
          }
        }

        @media (max-width: 640px) {
          .desktop-container {
            gap: 0.8rem !important;
            max-width: 95% !important;
          }

          .desktop-content {
            padding: 1.2rem !important;
          }

          .desktop-icon {
            width: 50px !important;
            height: 50px !important;
          }

          .desktop-icon i {
            font-size: 1.6rem !important;
          }

          .desktop-card-title {
            font-size: 1rem !important;
          }

          .desktop-card-desc {
            font-size: 0.7rem !important;
          }

          .desktop-btn {
            font-size: 0.7rem !important;
            padding: 5px 12px !important;
          }
        }

        @media (max-width: 480px) {
          .desktop-logo-img {
            width: 50px !important;
            height: 50px !important;
          }

          .desktop-title {
            font-size: 1.1rem !important;
          }

          .desktop-subtitle {
            font-size: 0.7rem !important;
          }

          .desktop-container {
            gap: 0.8rem !important;
          }

          .desktop-content {
            padding: 1rem !important;
          }

          .desktop-icon {
            width: 45px !important;
            height: 45px !important;
          }

          .desktop-icon i {
            font-size: 1.4rem !important;
          }

          .desktop-card-title {
            font-size: 0.9rem !important;
          }

          .desktop-card-desc {
            font-size: 0.65rem !important;
          }

          .desktop-btn {
            font-size: 0.65rem !important;
            padding: 4px 10px !important;
          }
        }

        @media (max-width: 768px) {
          .desktop-card:active {
            transform: scale(0.98);
            transition: transform 0.1s ease;
          }

          .desktop-card:hover {
            transform: none;
          }
        }

        @media (min-width: 1024px) and (max-width: 1366px) {
          .header-title {
            top: 15px !important;
            position: absolute !important;
          }

          .desktop-logo-img {
            width: 70px !important;
            height: 70px !important;
            padding: 6px !important;
          }

          .desktop-title {
            font-size: 2rem !important;
          }

          .desktop-subtitle {
            font-size: 1.9rem !important;
            margin-top: 0.5rem !important;
          }

          .desktop-container {
            margin-top: 120px !important;
            gap: 1rem !important;
            padding: 1rem !important;
          }

          .desktop-card {
            min-height: auto !important;
          }

          .desktop-content {
            padding: 1.2rem !important;
          }

          .desktop-icon {
            width: 60px !important;
            height: 60px !important;
            margin-bottom: 0.5rem !important;
          }

          .desktop-icon i {
            font-size: 2rem !important;
          }

          .desktop-card-title {
            font-size: 1.1rem !important;
            margin-bottom: 0.3rem !important;
          }

          .desktop-card-desc {
            font-size: 0.75rem !important;
          }

          .desktop-footer {
            bottom: 1rem !important;
          }

          .desktop-footer p {
            font-size: 0.7rem !important;
          }
        }

        @media (min-width: 1280px) and (max-width: 1366px) and (min-height: 600px) and (max-height: 800px) {
          .desktop-container {
            margin-top: 100px !important;
          }

          .header-title {
            top: 10px !important;
          }

          .desktop-title {
            font-size: 2rem !important;
          }

          .desktop-subtitle {
            font-size: 1.9rem !important;
          }
        }
      `}</style>
    </ClientOnly>
  );
};

export default HomePage;