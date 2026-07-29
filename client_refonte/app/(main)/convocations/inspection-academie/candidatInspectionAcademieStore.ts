// store/candidatInspectionAcademieStore.ts
import { create } from "zustand";
import axiosInstance from "@/app/api/axiosInstance"; // réutilise les mêmes types que le store établissement
import { CandidatFinis, FilterParams, PageResponse } from "../convocationStore";

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

// ==================== STATE ====================

interface CandidatInspectionAcademieState {
  candidats: CandidatFinis[];
  currentCandidat: CandidatFinis | null;

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
  isExportingExcel: boolean;

  // Actions
  fetchCandidats: (filters?: FilterParams) => Promise<void>;
  exportCandidats: () => Promise<Blob | null>;

  // Filters actions
  setFilters: (filters: Partial<FilterParams>) => void;
  setPage: (page: number) => void;
  setPageSize: (size: number) => void;
  setKeyword: (keyword: string) => void;
  resetFilters: () => void;
  clearError: () => void;
  
}

// ==================== INITIAL STATE ====================

const initialState = {
  candidats: [],
  currentCandidat: null,
  
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
    page: 0,
    size: 20,
    sort: "nom,asc",
  },
  isLoading: false,
  error: null,
  isExportingExcel: false,
};

// ==================== STORE ====================

export const useCandidatInspectionAcademieStore =
  create<CandidatInspectionAcademieState>()((set, get) => ({
    ...initialState,

    fetchCandidats: async (filters?: FilterParams) => {
      console.log("🚀 fetchCandidats (Inspection Académie) - Chargement paginé...");

      set({ isLoading: true, error: null });

      try {
        const currentFilters = { ...get().filters, ...filters };
        const queryString = buildParams(currentFilters);

        const response = await axiosInstance.get<PageResponse<CandidatFinis>>(
          `candidats/inspection-academie?${queryString}`,
        );

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
        console.error("❌ Erreur fetchCandidats (Inspection Académie):", error);
        set({
          error: handleError(error),
          isLoading: false,
          candidats: [],
        });
      }
    },

    exportCandidats: async () => {
      console.log("📊 exportCandidats (Inspection Académie) - Export Excel...");
      set({ isExportingExcel: true, error: null });

      try {
        const response = await axiosInstance.get(
          `candidats/inspection-academie/export`,
          {
            responseType: "blob",
          },
        );

        set({ isExportingExcel: false });
        return response.data;
      } catch (error: any) {
        console.error("❌ Erreur export (Inspection Académie):", error);
        set({ error: handleError(error), isExportingExcel: false });
        return null;
      }
    },

    setFilters: (filters: Partial<FilterParams>) => {
      const newFilters = { ...get().filters, ...filters, page: 0 };
      set({ filters: newFilters });
      get().fetchCandidats(newFilters);
    },

    setPage: (page: number) => {
      const newFilters = { ...get().filters, page };
      set({ filters: newFilters });
      get().fetchCandidats(newFilters);
    },

    setPageSize: (size: number) => {
      const newFilters = { ...get().filters, size, page: 0 };
      set({ filters: newFilters });
      get().fetchCandidats(newFilters);
    },

    setKeyword: (keyword: string) => {
      const newFilters = { ...get().filters, keyword, page: 0 };
      set({ filters: newFilters });
      get().fetchCandidats(newFilters);
    },

    resetFilters: () => {
      set({ filters: initialState.filters });
      get().fetchCandidats(initialState.filters);
    },

    clearError: () => {
      set({ error: null });
    },
  }));