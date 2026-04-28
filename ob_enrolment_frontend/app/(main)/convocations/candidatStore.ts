// store/candidatStore.ts
import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import axiosInstance1 from "@/app/api/axiosInstance1";

// ==================== TYPES ====================

export interface CandidatDTO {
  prenoms?: string;
  nom?: string;
  dateNaissance?: string;
  lieuNaissance?: string;
  nationalite?: string;
  numeroTable?: string;
  jury?: string;
  serie?: string;
  sexe?: string;
  typeCandidat?: string;
  eps?: string;
  etablissementName?: string;
  centreEcritName?: string;
  centreCode?: string;
  centreEcritParticulier?: string;
  centreActEPSName?: string;
  mo1?: string;
  mo2?: string;
  mo3?: string;
  ef1?: string;
  ef2?: string;
  centreMatFac1?: string;
  libMatFac1?: string;
  centreMatFac2?: string;
  libMatFac2?: string;
}

export interface CandidatRequest {
  codeEtab: string;
  numeroTable: string;
  dateNaissance: string;
  turnstileToken?: string;
}

export interface CacheInvalidationRequest {
  codeEtab: string;
  numeroTable: string;
  dateNaissance: string;
}

export interface DownloadConvocationRequest {
  centreCode: string;
  numeroTable: string;
}

// ==================== STATE ====================

interface CandidatState {
  // Data
  currentCandidat: CandidatDTO | null;
  pdfBlob: Blob | null;

  // Loading states
  isLoading: boolean;
  isGeneratingPdf: boolean;
  error: string | null;

  // Cache info
  cacheStatus: "HIT" | "MISS" | null;

  // Actions
  fetchCandidat: (params: CandidatRequest) => Promise<CandidatDTO | null>;
  downloadConvocation: (params: DownloadConvocationRequest) => Promise<boolean>;
  generatePdf: (numeroTable: string) => Promise<Blob | null>;
  clearCurrentCandidat: () => void;
  clearPdf: () => void;
  clearError: () => void;
  invalidateCache: (params: CacheInvalidationRequest) => Promise<boolean>;
}

// ==================== HELPERS ====================

const buildCandidatKey = (params: CandidatRequest): string => {
  return `${params.codeEtab}:${params.numeroTable}:${params.dateNaissance}`;
};

const handleError = (error: any): string => {
  if (error?.response?.status === 404) {
    return "Aucune information trouvée pour ce candidat";
  }
  if (error?.code === "ECONNABORTED") {
    return "La requête a expiré. Veuillez réessayer.";
  }
  return error?.response?.data?.message || error?.message || "Erreur serveur";
};

// ==================== INITIAL STATE ====================

const initialState = {
  currentCandidat: null,
  pdfBlob: null,
  isLoading: false,
  isGeneratingPdf: false,
  error: null,
  cacheStatus: null,
};

// ==================== STORE ====================

export const useCandidatStore = create<CandidatState>()((set, get) => ({
  ...initialState,

  // ==================== FETCH CANDIDAT ====================

  fetchCandidat: async (params: CandidatRequest) => {
    const { codeEtab, numeroTable, dateNaissance, turnstileToken } = params;

    console.log("📄 fetchCandidat - Récupération candidat:", {
      codeEtab,
      numeroTable,
      dateNaissance: dateNaissance.substring(0, 10),
    });

    set({ isLoading: true, error: null, cacheStatus: null });

    try {
      const response = await axiosInstance1.get<CandidatDTO>(
        "/convocations/candidat",
        {
          params: {
            codeEtab,
            numeroTable,
            dateNaissance,
          },
          timeout: 10000,
          headers: {
            "Cache-Control": "no-cache",
          },
        },
      );

      const cacheStatus =
        (response.headers["x-cache-status"] as "HIT" | "MISS") || null;

      console.log(`✅ Candidat récupéré - Cache: ${cacheStatus || "unknown"}`);

      set({
        currentCandidat: response.data,
        isLoading: false,
        error: null,
        cacheStatus,
      });

      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur fetchCandidat:", error);

      const errorMessage = handleError(error);

      set({
        error: errorMessage,
        isLoading: false,
        currentCandidat: null,
        cacheStatus: null,
      });

      return null;
    }
  },

  // ==================== DOWNLOAD CONVOCATION  ====================

  downloadConvocation: async (params: DownloadConvocationRequest) => {
    const { centreCode, numeroTable } = params;

    console.log("📥 downloadConvocation - Téléchargement:", {
      centreCode,
      numeroTable,
    });

    set({ isGeneratingPdf: true, error: null });

    try {
      const response = await axiosInstance1.get(
        "/convocations/download-attachment",
        {
          params: {
            centreCode: centreCode,
            numeroTable: numeroTable,
          },
          responseType: "blob",
          timeout: 15000,
        },
      );

      console.log("✅ Convocation téléchargée avec succès");

      // Créer un lien pour télécharger le fichier
      const contentDisposition = response.headers["content-disposition"];
      let filename = `convocation_${numeroTable}.pdf`;

      if (contentDisposition) {
        const match = contentDisposition.match(
          /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/,
        );
        if (match && match[1]) {
          filename = match[1].replace(/['"]/g, "");
        }
      }

      // Créer un objet URL pour le blob
      const url = window.URL.createObjectURL(response.data);

      // Créer un élément <a> pour déclencher le téléchargement
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", filename);
      document.body.appendChild(link);
      link.click();

      // Nettoyer
      link.remove();
      window.URL.revokeObjectURL(url);

      set({
        pdfBlob: response.data,
        isGeneratingPdf: false,
        error: null,
      });

      return true;
    } catch (error: any) {
      console.error("❌ Erreur downloadConvocation:", error);

      let errorMessage = "Erreur lors du téléchargement de la convocation";
      if (error?.response?.status === 404) {
        errorMessage = "Aucune convocation trouvée pour ce numéro de table";
      } else if (error?.response?.status === 500) {
        errorMessage = "Erreur serveur lors du téléchargement";
      }

      set({
        error: errorMessage,
        isGeneratingPdf: false,
        pdfBlob: null,
      });

      return false;
    }
  },

  // ==================== GENERATE PDF ====================

  generatePdf: async (numeroTable: string) => {
    console.log("📄 generatePdf - Génération PDF pour:", numeroTable);

    set({ isGeneratingPdf: true, error: null });

    try {
      const response = await axiosInstance1.get(
        `/convocations/${numeroTable}`,
        {
          responseType: "blob",
          timeout: 15000,
        },
      );

      console.log("✅ PDF généré avec succès");

      set({
        pdfBlob: response.data,
        isGeneratingPdf: false,
        error: null,
      });

      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur generatePdf:", error);

      let errorMessage = "Erreur lors de la génération du PDF";
      if (error?.response?.status === 404) {
        errorMessage = "Aucune convocation trouvée pour ce numéro de table";
      } else if (error?.response?.status === 500) {
        errorMessage = "Erreur serveur lors de la génération du PDF";
      }

      set({
        error: errorMessage,
        isGeneratingPdf: false,
        pdfBlob: null,
      });

      return null;
    }
  },

  // ==================== INVALIDATE CACHE ====================

  invalidateCache: async (params: CacheInvalidationRequest) => {
    const { codeEtab, numeroTable, dateNaissance } = params;

    console.log("🗑️ invalidateCache - Invalidation du cache:", {
      codeEtab,
      numeroTable,
    });

    set({ isLoading: true, error: null });

    try {
      await axiosInstance1.delete("/v1/convocations/cache", {
        params: {
          codeEtab,
          numeroTable,
          dateNaissance,
        },
      });

      console.log("✅ Cache invalidé avec succès");

      const current = get().currentCandidat;
      if (current?.numeroTable === numeroTable) {
        set({ currentCandidat: null, cacheStatus: null });
      }

      set({ isLoading: false });
      return true;
    } catch (error: any) {
      console.error("❌ Erreur invalidateCache:", error);
      set({
        error: handleError(error),
        isLoading: false,
      });
      return false;
    }
  },

  // ==================== UTILITY ACTIONS ====================

  clearCurrentCandidat: () => {
    console.log("🧹 clearCurrentCandidat");
    set({
      currentCandidat: null,
      cacheStatus: null,
      error: null,
    });
  },

  clearPdf: () => {
    console.log("🧹 clearPdf");
    set({ pdfBlob: null });
  },

  clearError: () => {
    set({ error: null });
  },
}));

// ==================== HOOK PERSONNALISÉ AVEC PERSISTENCE ====================

export const useCandidatStoreWithPersistence = create<CandidatState>()(
  persist(
    (set, get) => ({
      ...initialState,

      fetchCandidat: async (params: CandidatRequest) => {
        set({ isLoading: true, error: null, cacheStatus: null });

        try {
          const response = await axiosInstance1.get<CandidatDTO>(
            "/convocations/candidat",
            {
              params: {
                codeEtab: params.codeEtab,
                numeroTable: params.numeroTable,
                dateNaissance: params.dateNaissance,
              },
              timeout: 10000,
            },
          );

          const cacheStatus =
            (response.headers["x-cache-status"] as "HIT" | "MISS") || null;

          set({
            currentCandidat: response.data,
            isLoading: false,
            error: null,
            cacheStatus,
          });

          return response.data;
        } catch (error: any) {
          set({
            error: handleError(error),
            isLoading: false,
            currentCandidat: null,
            cacheStatus: null,
          });
          return null;
        }
      },

      downloadConvocation: async (params: DownloadConvocationRequest) => {
        const { centreCode, numeroTable } = params;

        console.log("📥 downloadConvocation - Téléchargement:", {
          centreCode,
          numeroTable,
        });

        set({ isGeneratingPdf: true, error: null });

        try {
          const response = await axiosInstance1.get(
            "/convocations/download-attachment",
            {
              params: {
                centreCode: centreCode,
                numeroTable: numeroTable,
              },
              responseType: "blob",
              timeout: 15000,
            },
          );

          console.log("✅ Convocation téléchargée avec succès");

          const contentDisposition = response.headers["content-disposition"];
          let filename = `convocation_${numeroTable}.pdf`;

          if (contentDisposition) {
            const match = contentDisposition.match(
              /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/,
            );
            if (match && match[1]) {
              filename = match[1].replace(/['"]/g, "");
            }
          }

          const url = window.URL.createObjectURL(response.data);
          const link = document.createElement("a");
          link.href = url;
          link.setAttribute("download", filename);
          document.body.appendChild(link);
          link.click();
          link.remove();
          window.URL.revokeObjectURL(url);

          set({
            pdfBlob: response.data,
            isGeneratingPdf: false,
            error: null,
          });

          return true;
        } catch (error: any) {
          console.error("❌ Erreur downloadConvocation:", error);

          let errorMessage = "Erreur lors du téléchargement de la convocation";
          if (error?.response?.status === 404) {
            errorMessage = "Aucune convocation trouvée pour ce numéro de table";
          } else if (error?.response?.status === 500) {
            errorMessage = "Erreur serveur lors du téléchargement";
          }

          set({
            error: errorMessage,
            isGeneratingPdf: false,
            pdfBlob: null,
          });

          return false;
        }
      },

      generatePdf: async (numeroTable: string) => {
        set({ isGeneratingPdf: true, error: null });

        try {
          const response = await axiosInstance1.get(
            `/convocations/${numeroTable}`,
            { responseType: "blob", timeout: 15000 },
          );

          set({
            pdfBlob: response.data,
            isGeneratingPdf: false,
            error: null,
          });

          return response.data;
        } catch (error: any) {
          set({
            error: handleError(error),
            isGeneratingPdf: false,
            pdfBlob: null,
          });
          return null;
        }
      },

      invalidateCache: async (params: CacheInvalidationRequest) => {
        set({ isLoading: true, error: null });

        try {
          await axiosInstance1.delete("/v1/convocations/cache", {
            params: {
              codeEtab: params.codeEtab,
              numeroTable: params.numeroTable,
              dateNaissance: params.dateNaissance,
            },
          });

          const current = get().currentCandidat;
          if (current?.numeroTable === params.numeroTable) {
            set({ currentCandidat: null, cacheStatus: null });
          }

          set({ isLoading: false });
          return true;
        } catch (error: any) {
          set({ error: handleError(error), isLoading: false });
          return false;
        }
      },

      clearCurrentCandidat: () => {
        set({ currentCandidat: null, cacheStatus: null, error: null });
      },

      clearPdf: () => {
        set({ pdfBlob: null });
      },

      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: "candidat-storage",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        currentCandidat: state.currentCandidat,
        cacheStatus: state.cacheStatus,
      }),
    },
  ),
);
