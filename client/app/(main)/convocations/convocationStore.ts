// store/candidatStore.ts
import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import axiosInstance from "@/app/api/axiosInstance";

// ==================== TYPES ====================

export interface Etablissement {
  id?: string;
  name?: string;
  code?: string;
  ville?: Ville;
}

export interface Ville {
  id?: string;
  nom?: string;
}

export interface EpreuveMatiere {
  id?: string;
  code?: string;
  name?: string;
  type?: string;
}
export interface JourEPS {
  id?: string;
  code?: string;
  name?: string;
  date?: string;
  ordre?: number;
  type?: string;
}
export interface Ville {
  id?: string;
  code?: string;
  name?: string;
  type?: string;
}
export interface EpreuveSerie {
  id?: string;
  code?: string;
  nom?: string;
}

export interface EpreuveJour {
  id?: string;
  code?: string;
  nom?: string;
  ordre?: number;
}

export interface EpreuveHeure {
  id?: string;
  code?: string;
  heure?: string;
  ordre?: number;
}

export interface Epreuve {
  id?: string;
  matiere?: EpreuveMatiere;
  serie?: EpreuveSerie;
  coefficient?: number;
  autorisation?: boolean;
  estDominant?: boolean;
  nombrePoints?: number;
  jourDebut?: EpreuveJour;
  heureDebut?: EpreuveHeure;
  duree?: string;
  type?: string;
  compositeKey?: string;
}

export interface CandidatFinis {
  id?: string;
  // IDENTITÉ
  prenoms?: string;
  nom?: string;
  dateNaissance?: string;
  lieuNaissance?: string;
  nationalite?: string;
  // EXAMEN
  numeroTable?: string;
  jury?: string;
  serie?: string;
  sexe?: string;
  age?: number;
  eps?: string;
  numeroDossier?: string;
  etablissement?: Etablissement;
  centreExamen?: Ville;
  centreActEPS?: Etablissement;
  // MATIERES OPTIONNELLES
  mo1?: string;
  mo2?: string;
  mo3?: string;
  ef1?: string;
  ef2?: string;
  nbMatFacult?: number;
  ia?: number;
  nti?: number;
  centreEcrit?: Etablissement;
  codeCES?: string;
  centreEcritParticulier?: string;
  statutResultat?: string;
  typeCandidat?: string;
  codeEtatCivil?: string;
  libEtatCivil?: string;
  anneeActe?: string;
  refActeNaissance?: string;
  dossierEnAttente?: string;
  resultat?: string;
  raisonRejet?: string;
  datePassageEPS?: string;
  npEC?: string;
  // ODAE
  idOrigine?: string;
  anneeODAE?: string;
  paysODAE?: string;
  identifiantODAE?: string;
  serieODAE?: string;
  // PROVENANCE
  codeEtsProvenance?: string;
  pasDeResultat?: string;
  classeEtsProvenance?: string;
  departementProvenance?: string;
  departementVilleExamen?: string;
  candidatDeplace?: string;
  academieProvenance?: string;
  academieEcrit?: string;
  telephone?: string;
  handicap?: string;
  typeFiliere?: string;
  sessionJury?: string;
  moyenneFinale?: number;
  mention?: string;
  absence?: string;
  exclusion?: string;
  titreProjet?: string;
  groupeEts?: string;
  codeCentreSoutenance?: string;
  libCentreSoutenance?: string;
  villeSoutenance?: string;
  centreMatFac1?: string;
  libMatFac1?: string;
  villeMatFac1?: string;
  centreMatFac2?: string;
  libMatFac2?: string;
  villeMatFac2?: string;
  // Épreuves
  epreuves?: Epreuve[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageSize: number;
  pageNumber: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface FilterParams {
  keyword?: string;
  serie?: string;
  jury?: string;
  typeCandidat?: string;
  statutResultat?: string;
  sexe?: string;
  nationalite?: string;
  numeroDossier?: string;
  codeEtablissement?: string;
  page?: number;
  size?: number;
  sort?: string;
}

// ==================== STATE ====================

interface CandidatState {
  // Data
  candidats: CandidatFinis[];
  regenerateConvocation: (numeroTable: string) => Promise<any>;
  isRegenerating: boolean;
  exportZipBySerie: () => Promise<Blob | null>;
  isExportingZip: boolean;
  jourEPS: JourEPS | null;
  fetchJourEPS: () => Promise<JourEPS | null>;
  currentCandidat: CandidatFinis | null;
  exportCandidats: (filters?: FilterParams) => Promise<Blob | null>;
  fetchAllCandidatsBySerie: (serie?: string) => Promise<void>;
  exportPdf: () => Promise<Blob | null>;
  isExportingPdf: boolean;

  // Pagination
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;

  // Filters
  filters: FilterParams;

  // Loading states
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchCandidats: (filters?: FilterParams) => Promise<void>;
  fetchCandidatById: (id: string) => Promise<CandidatFinis | null>;
  fetchCandidatByIdWithEpreuves: (id: string) => Promise<CandidatFinis | null>;
  createCandidat: (data: any) => Promise<CandidatFinis | null>;
  updateCandidat: (id: string, data: any) => Promise<CandidatFinis | null>;
  deleteCandidat: (id: string) => Promise<boolean>;

  // Filters actions
  setFilters: (filters: Partial<FilterParams>) => void;
  setPage: (page: number) => void;
  setPageSize: (size: number) => void;
  setKeyword: (keyword: string) => void;
  resetFilters: () => void;
  clearError: () => void;
}

// ==================== HELPERS ====================

const buildParams = (filters: FilterParams): string => {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.append(key, String(value));
    }
  });
  return params.toString();
};

const handleError = (error: any): string => {
  return error?.response?.data?.message || error?.message || "Erreur serveur";
};

// ==================== INITIAL STATE ====================

const initialState = {
  candidats: [],
  isExportingZip: false,
  currentCandidat: null,
  jourEPS: null,
  isExportingPdf: false,
  isRegenerating: false,
  totalElements: 0,
  totalPages: 0,
  currentPage: 0,
  pageSize: 20,
  filters: {
    keyword: "",
    serie: "",
    jury: "",
    typeCandidat: "",
    statutResultat: "",
    sexe: "",
    nationalite: "",
    numeroDossier: "",
    codeEtablissement: "",
    page: 0,
    size: 20,
    sort: "nom,asc",
  },
  isLoading: false,
  error: null,
};

// ==================== STORE ====================

export const useCandidatStore = create<CandidatState>()((set, get) => ({
  ...initialState,

  // ==================== FETCH CANDIDATS (PAGINÉ) ====================

  fetchCandidats: async (filters?: FilterParams) => {
    console.log("🚀 fetchCandidats - Chargement paginé...");
    console.log("📄 Page demandée:", filters?.page ?? get().filters.page);

    set({ isLoading: true, error: null });

    try {
      const currentFilters = { ...get().filters, ...filters };
      const queryString = buildParams(currentFilters);

      console.log("📝 Paramètres:", currentFilters);

      const response = await axiosInstance.get<PageResponse<CandidatFinis>>(
        `candidats/all?${queryString}`,
      );

      console.log("📦 Réponse complète:", response.data);
      console.log("📦 Content:", response.data.content);
      console.log("📦 Premier candidat:", response.data.content[0]);

      console.log(
        `✅ ${response.data.content.length} candidats chargés sur ${response.data.totalElements}`,
      );

      set({
        candidats: response.data.content,
        totalElements: response.data.totalElements,
        totalPages: response.data.totalPages,
        currentPage: response.data.pageNumber,
        pageSize: response.data.pageSize,
        filters: {
          ...currentFilters,
          page: response.data.pageNumber,
          size: response.data.pageSize,
        },
        isLoading: false,
        error: null,
      });
    } catch (error: any) {
      console.error("❌ Erreur fetchCandidats:", error);
      set({
        error: handleError(error),
        isLoading: false,
        candidats: [],
      });
    }
  },
  regenerateConvocation: async (numeroTable: string) => {
    console.log(`🔄 regenerateConvocation - Régénération pour: ${numeroTable}`);
    set({ isLoading: true, error: null });

    try {
      const response = await axiosInstance.post(
        `candidats/regenerate/${numeroTable}`,
        {},
      );

      console.log("✅ Convocation régénérée:", response.data);
      set({ isLoading: false });
      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur regenerateConvocation:", error);
      set({ error: handleError(error), isLoading: false });
      return null;
    }
  },
  exportZipBySerie: async () => {
    console.log("📦 exportZipBySerie - Génération du ZIP par série...");

    set({ isExportingZip: true, error: null });

    try {
      const response = await axiosInstance.get(
        `candidats/export-zip-by-serie`,
        {
          responseType: "blob",
        },
      );

      set({ isExportingZip: false });

      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur export ZIP par série:", error);

      set({
        error: handleError(error),
        isExportingZip: false,
      });

      return null;
    }
  },
  fetchJourEPS: async () => {
    console.log("📅 fetchJourEPS - Récupération du jour EPS");
    set({ isLoading: true, error: null });

    try {
      const response = await axiosInstance.get<JourEPS>("candidats/jour-eps");

      console.log("✅ Jour EPS récupéré:", response.data);

      set({
        jourEPS: response.data,
        isLoading: false,
      });

      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur fetchJourEPS:", error);
      set({
        error: handleError(error),
        isLoading: false,
      });
      return null;
    }
  },
  exportPdf: async () => {
    console.log("📄 exportPdf - génération PDF...");

    set({ isExportingPdf: true, error: null });

    try {
      const response = await axiosInstance.get(`candidats/export-pdf`, {
        responseType: "blob",
      });

      set({ isExportingPdf: false });

      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur export PDF:", error);

      set({
        error: handleError(error),
        isExportingPdf: false,
      });

      return null;
    }
  },
  fetchAllCandidatsBySerie: async (serie?: string) => {
    console.log("🚀 fetchAllCandidatsBySerie - Sans pagination");

    set({ isLoading: true, error: null });

    try {
      let url = "candidats/me/all";

      if (serie) {
        url += `?serie=${encodeURIComponent(serie)}`;
      }

      const response = await axiosInstance.get<CandidatFinis[]>(url);

      console.log(`✅ ${response.data.length} candidats chargés`);

      set({
        candidats: response.data,
        isLoading: false,
        error: null,

        // 👉 IMPORTANT : reset pagination (car ici liste complète)
        totalElements: response.data.length,
        totalPages: 1,
        currentPage: 0,
      });
    } catch (error: any) {
      console.error("❌ Erreur fetchAllCandidatsBySerie:", error);

      set({
        error: handleError(error),
        isLoading: false,
        candidats: [],
      });
    }
  },
  // Dans votre store (Zustand)

  exportCandidats: async () => {
    console.log("📊 exportCandidats - Export des candidats...");
    set({ isLoading: true, error: null });

    try {
      const response = await axiosInstance.get("candidats/me/export", {
        responseType: "blob",
      });

      set({ isLoading: false });
      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur export:", error);
      set({ error: handleError(error), isLoading: false });
      return null;
    }
  },

  // ==================== FETCH CANDIDAT BY ID ====================

  fetchCandidatById: async (id: string) => {
    console.log(`🚀 fetchCandidatById - ID: ${id}`);
    set({ isLoading: true, error: null });

    try {
      const response = await axiosInstance.get<CandidatFinis>(
        `candidats/${id}`,
      );
      set({ currentCandidat: response.data, isLoading: false });
      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur fetchCandidatById:", error);
      set({ error: handleError(error), isLoading: false });
      return null;
    }
  },

  // ==================== FETCH CANDIDAT WITH EPREUVES ====================

  fetchCandidatByIdWithEpreuves: async (id: string) => {
    console.log(`🚀 fetchCandidatByIdWithEpreuves - ID: ${id}`);
    set({ isLoading: true, error: null });

    try {
      const response = await axiosInstance.get<CandidatFinis>(
        `candidats/${id}/with-epreuves`,
      );
      set({ currentCandidat: response.data, isLoading: false });
      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur fetchCandidatByIdWithEpreuves:", error);
      set({ error: handleError(error), isLoading: false });
      return null;
    }
  },

  // ==================== CREATE CANDIDAT ====================

  createCandidat: async (data: any) => {
    console.log("🚀 createCandidat");
    set({ isLoading: true, error: null });

    try {
      const response = await axiosInstance.post<CandidatFinis>(
        "candidats/",
        data,
      );

      // Rafraîchir la liste si on est sur la première page
      if (get().currentPage === 0) {
        await get().fetchCandidats();
      }

      set({ isLoading: false });
      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur createCandidat:", error);
      set({ error: handleError(error), isLoading: false });
      return null;
    }
  },

  // ==================== UPDATE CANDIDAT ====================

  updateCandidat: async (id: string, data: any) => {
    console.log(`🚀 updateCandidat - ID: ${id}`);
    set({ isLoading: true, error: null });

    try {
      const response = await axiosInstance.put<CandidatFinis>(
        `candidats/${id}`,
        data,
      );

      // Mettre à jour dans la liste
      const updatedCandidats = get().candidats.map((c) =>
        c.id === id ? response.data : c,
      );

      set({
        candidats: updatedCandidats,
        currentCandidat: response.data,
        isLoading: false,
      });

      return response.data;
    } catch (error: any) {
      console.error("❌ Erreur updateCandidat:", error);
      set({ error: handleError(error), isLoading: false });
      return null;
    }
  },

  // ==================== DELETE CANDIDAT ====================

  deleteCandidat: async (id: string) => {
    console.log(`🚀 deleteCandidat - ID: ${id}`);
    set({ isLoading: true, error: null });

    try {
      await axiosInstance.delete(`candidats/${id}`);

      set({
        candidats: get().candidats.filter((c) => c.id !== id),
        totalElements: get().totalElements - 1,
        isLoading: false,
      });

      return true;
    } catch (error: any) {
      console.error("❌ Erreur deleteCandidat:", error);
      set({ error: handleError(error), isLoading: false });
      return false;
    }
  },

  // ==================== FILTERS ACTIONS ====================

  setFilters: (filters: Partial<FilterParams>) => {
    console.log("🎯 setFilters:", filters);
    const newFilters = { ...get().filters, ...filters, page: 0 };
    set({ filters: newFilters });
    get().fetchCandidats(newFilters);
  },

  setPage: (page: number) => {
    console.log("📄 setPage:", page);
    const newFilters = { ...get().filters, page };
    set({ filters: newFilters });
    get().fetchCandidats(newFilters);
  },

  setPageSize: (size: number) => {
    console.log("📏 setPageSize:", size);
    const newFilters = { ...get().filters, size, page: 0 };
    set({ filters: newFilters });
    get().fetchCandidats(newFilters);
  },

  setKeyword: (keyword: string) => {
    console.log("🔍 setKeyword:", keyword);
    const newFilters = { ...get().filters, keyword, page: 0 };
    set({ filters: newFilters });
    get().fetchCandidats(newFilters);
  },

  resetFilters: () => {
    console.log("🔄 resetFilters");
    set({ filters: initialState.filters });
    get().fetchCandidats(initialState.filters);
  },

  clearError: () => {
    set({ error: null });
  },
}));
