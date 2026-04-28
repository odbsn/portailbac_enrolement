// store/serieStore.ts
import { create } from "zustand";
import axiosInstance from "@/app/api/axiosInstance";

interface SerieState {
  // Toutes les séries (admin)
  allSeries: string[];
  // Séries de l'établissement connecté
  mySeries: string[];
  
  // Loading states
  isLoadingAll: boolean;
  isLoadingMy: boolean;
  
  // Errors
  errorAll: string | null;
  errorMy: string | null;
  
  // Actions
  fetchAllSeries: () => Promise<void>;
  fetchMySeries: () => Promise<void>;
  reset: () => void;
}

export const useSerieStore = create<SerieState>((set, get) => ({
  // Initial state
  allSeries: [],
  mySeries: [],
  isLoadingAll: false,
  isLoadingMy: false,
  errorAll: null,
  errorMy: null,

  // Récupérer toutes les séries (admin)
  fetchAllSeries: async () => {
    console.log("🚀 fetchAllSeries - Chargement de toutes les séries...");
    set({ isLoadingAll: true, errorAll: null });
    
    try {
      const response = await axiosInstance.get<string[]>("/candidats/series");
      console.log("✅ Toutes les séries chargées:", response.data);
      set({ allSeries: response.data, isLoadingAll: false });
    } catch (error: any) {
      console.error("❌ Erreur chargement toutes les séries:", error);
      set({ 
        errorAll: error.response?.data?.message || error.message || "Erreur de chargement",
        isLoadingAll: false 
      });
    }
  },

  // Récupérer les séries de l'établissement connecté
  fetchMySeries: async () => {
    console.log("🚀 fetchMySeries - Chargement des séries de mon établissement...");
    set({ isLoadingMy: true, errorMy: null });
    
    try {
      const response = await axiosInstance.get<string[]>("/candidats/me/series");
      console.log("✅ Séries de mon établissement chargées:", response.data);
      set({ mySeries: response.data, isLoadingMy: false });
    } catch (error: any) {
      console.error("❌ Erreur chargement séries de l'établissement:", error);
      set({ 
        errorMy: error.response?.data?.message || error.message || "Erreur de chargement",
        isLoadingMy: false 
      });
    }
  },

  // Reset all states
  reset: () => {
    set({
      allSeries: [],
      mySeries: [],
      isLoadingAll: false,
      isLoadingMy: false,
      errorAll: null,
      errorMy: null,
    });
  },
}));