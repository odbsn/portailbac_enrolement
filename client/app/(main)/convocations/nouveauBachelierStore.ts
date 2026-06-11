import axiosInstance from '@/app/api/axiosInstance';
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

// ─── Types ────────────────────────────────────────────────────────────────────

export interface Jury {
  id: string;
  code?: string;
  nom?: string;
  prenom?: string;
}
export interface RechercheBachelierResponse {
  id: string;
  telephone: string;
  prenoms: string;
  nom: string;
  numeroTable: string;
  resultat: string;
  mention: string | null;  // null si pas Admis
  jury: {
    id: string;
    numero?: string;
    name?: string;
  } | null;
  dateCreation: string;
}

export interface NouveauBachelierResponse {
  id: string;
  telephone: string;
  prenoms: string;
  nom: string;
  numeroTable: string;
  resultat: string;
  mention: string;
  jury: Jury;
  utiCree: number;
  dateCreation: string;
  utiModifie: number;
  dateModification: string;
}

export interface NouveauBachelierRequest {
  telephone: string;
  prenoms: string;
  nom: string;
  numeroTable: string;
  resultat: string;
  mention: string;
  juryId: string;
}

export interface ImportResult {
  fichier: string;
  total: number;
  nouveaux: number;
  modifies: number;
  inchanges: number;
  juryIntrouvable: number;
  warnings: string[];
}

// ─── Store State ─────────────────────────────────────────────────────────────

interface NouveauBachelierState {
  // Data
  bacheliers: NouveauBachelierResponse[];
  selectedBachelier: NouveauBachelierResponse | null;
  
  
  // UI State
  isLoading: boolean;
  isSubmitting: boolean;
  error: string | null;
  
  // Pagination & Filters
  searchTerm: string;
  currentPage: number;
  pageSize: number;
  totalElements: number;
  
  // Actions
  fetchAll: () => Promise<void>;
  fetchOne: (id: string) => Promise<void>;
  create: (request: NouveauBachelierRequest) => Promise<NouveauBachelierResponse>;
  update: (id: string, request: NouveauBachelierRequest) => Promise<NouveauBachelierResponse>;
  searchByNumeroTable: (numeroTable: string) => Promise<NouveauBachelierResponse | null>;
  delete: (id: string) => Promise<void>;
  importExcel: (file: File) => Promise<ImportResult>;
  importCsv: (file: File) => Promise<string[]>;
  
  // UI Actions
  setSearchTerm: (term: string) => void;
  setCurrentPage: (page: number) => void;
  setPageSize: (size: number) => void;
  clearError: () => void;
  reset: () => void;
}

// ─── Store Implementation ────────────────────────────────────────────────────

export const useNouveauBachelierStore = create<NouveauBachelierState>()(
  devtools(
    (set, get) => ({
      // Initial state
      bacheliers: [],
      selectedBachelier: null,
      isLoading: false,
      isSubmitting: false,
      error: null,
      searchTerm: '',
      currentPage: 0,
      pageSize: 10,
      totalElements: 0,

      // ─── CRUD Actions ──────────────────────────────────────────────────────
      
      fetchAll: async () => {
        set({ isLoading: true, error: null }, false, 'bachelier/fetchAll/pending');
        try {
          const response = await axiosInstance.get<NouveauBachelierResponse[]>(
            '/nouveauBacheliers/all'
          );
          set({ 
            bacheliers: response.data, 
            isLoading: false,
            totalElements: response.data.length 
          }, false, 'bachelier/fetchAll/fulfilled');
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors du chargement';
          set({ error: message, isLoading: false }, false, 'bachelier/fetchAll/rejected');
          throw error;
        }
      },
      fetchOne: async (id: string) => {
        set({ isLoading: true, error: null }, false, 'bachelier/fetchOne/pending');
        try {
          const response = await axiosInstance.get<NouveauBachelierResponse>(
            `/nouveauBacheliers/${id}`
          );
          set({ 
            selectedBachelier: response.data, 
            isLoading: false 
          }, false, 'bachelier/fetchOne/fulfilled');
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors du chargement';
          set({ error: message, isLoading: false }, false, 'bachelier/fetchOne/rejected');
          throw error;
        }
      },

      create: async (request: NouveauBachelierRequest) => {
        set({ isSubmitting: true, error: null }, false, 'bachelier/create/pending');
        try {
          const response = await axiosInstance.post<NouveauBachelierResponse>(
            '/nouveauBacheliers/',
            request
          );
          const newBachelier = response.data;
          set(state => ({ 
            bacheliers: [newBachelier, ...state.bacheliers], 
            isSubmitting: false,
            totalElements: state.totalElements + 1
          }), false, 'bachelier/create/fulfilled');
          return newBachelier;
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors de la création';
          set({ error: message, isSubmitting: false }, false, 'bachelier/create/rejected');
          throw error;
        }
      },

      update: async (id: string, request: NouveauBachelierRequest) => {
        set({ isSubmitting: true, error: null }, false, 'bachelier/update/pending');
        try {
          const response = await axiosInstance.put<NouveauBachelierResponse>(
            `/nouveauBacheliers/${id}`,
            request
          );
          const updatedBachelier = response.data;
          set(state => ({ 
            bacheliers: state.bacheliers.map(b => b.id === id ? updatedBachelier : b),
            selectedBachelier: updatedBachelier,
            isSubmitting: false
          }), false, 'bachelier/update/fulfilled');
          return updatedBachelier;
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors de la mise à jour';
          set({ error: message, isSubmitting: false }, false, 'bachelier/update/rejected');
          throw error;
        }
      },

      delete: async (id: string) => {
        set({ isSubmitting: true, error: null }, false, 'bachelier/delete/pending');
        try {
          await axiosInstance.delete(`/nouveauBacheliers/${id}`);
          set(state => ({ 
            bacheliers: state.bacheliers.filter(b => b.id !== id),
            selectedBachelier: null,
            isSubmitting: false,
            totalElements: state.totalElements - 1
          }), false, 'bachelier/delete/fulfilled');
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors de la suppression';
          set({ error: message, isSubmitting: false }, false, 'bachelier/delete/rejected');
          throw error;
        }
      },

      importExcel: async (file: File) => {
        set({ isSubmitting: true, error: null }, false, 'bachelier/importExcel/pending');
        const formData = new FormData();
        formData.append('file', file);
        
        try {
          const response = await axiosInstance.post<ImportResult>(
            '/nouveauBacheliers/import/excel',
            formData,
            {
              headers: { 'Content-Type': 'multipart/form-data' }
            }
          );
          set({ isSubmitting: false }, false, 'bachelier/importExcel/fulfilled');
          // Rafraîchir la liste après import
          await get().fetchAll();
          return response.data;
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors de l\'import';
          set({ error: message, isSubmitting: false }, false, 'bachelier/importExcel/rejected');
          throw error;
        }
      },

      importCsv: async (file: File) => {
        set({ isSubmitting: true, error: null }, false, 'bachelier/importCsv/pending');
        const formData = new FormData();
        formData.append('file', file);
        
        try {
          const response = await axiosInstance.post<string[]>(
            '/nouveauBacheliers/csv',
            formData,
            {
              headers: { 'Content-Type': 'multipart/form-data' }
            }
          );
          set({ isSubmitting: false }, false, 'bachelier/importCsv/fulfilled');
          await get().fetchAll();
          return response.data;
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors de l\'import CSV';
          set({ error: message, isSubmitting: false }, false, 'bachelier/importCsv/rejected');
          throw error;
        }
      },
      // Ajouter dans le corps du store, après les autres action
        searchByNumeroTable: async (numeroTable: string) => {
        set({ isLoading: true, error: null }, false, 'bachelier/searchByNumeroTable/pending');
        try {
            const response = await axiosInstance.get<NouveauBachelierResponse>(
            `/nouveauBacheliers/resultat/${numeroTable}`
            );
            set({ isLoading: false }, false, 'bachelier/searchByNumeroTable/fulfilled');
            return response.data;
        } catch (error: any) {
            if (error?.response?.status === 404) {
            set({ isLoading: false }, false, 'bachelier/searchByNumeroTable/notfound');
            return null;
            }
            const message = error?.response?.data?.message || 'Erreur lors de la recherche';
            set({ error: message, isLoading: false }, false, 'bachelier/searchByNumeroTable/rejected');
            throw error;
        }
        },

      // ─── UI Actions ────────────────────────────────────────────────────────
      
      setSearchTerm: (term: string) => {
        set({ searchTerm: term, currentPage: 0 }, false, 'bachelier/setSearchTerm');
      },

      setCurrentPage: (page: number) => {
        set({ currentPage: page }, false, 'bachelier/setCurrentPage');
      },

      setPageSize: (size: number) => {
        set({ pageSize: size, currentPage: 0 }, false, 'bachelier/setPageSize');
      },

      clearError: () => {
        set({ error: null }, false, 'bachelier/clearError');
      },

      reset: () => {
        set({ 
          bacheliers: [],
          selectedBachelier: null,
          isLoading: false,
          isSubmitting: false,
          error: null,
          searchTerm: '',
          currentPage: 0,
          totalElements: 0
        }, false, 'bachelier/reset');
      }
    }),
    { name: 'NouveauBachelierStore' }
  )
);