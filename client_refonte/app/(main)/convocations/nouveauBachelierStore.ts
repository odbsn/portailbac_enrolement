import axiosInstance from '@/app/api/axiosInstance';
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

// ─── Types ────────────────────────────────────────────────────────────────────

export interface Academie {
  id?: string;
  name?: string;
  code?: string;
}

export interface Ville {
  id?: string;
  name?: string;
  inspectionAcademie?: Academie;
}

export interface Centre {
  id?: string;
  code?: string;
  name?: string;
  ville?: Ville;
}

export interface Jury {
  id: string;
  numero?: string;
  name?: string;
  technique?: boolean;
  centre?: Centre;
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

// ✅ Format Spring Data Page<T>
export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;       // page courante (0-indexée)
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

// ─── Store State ─────────────────────────────────────────────────────────────

interface NouveauBachelierState {
  // Data
  bacheliers: NouveauBachelierResponse[];
  selectedBachelier: NouveauBachelierResponse | null;
  jurysNonCharges: Jury[];

  // UI State
  isLoading: boolean;
  isSubmitting: boolean;
  error: string | null;

  // Pagination & Filters (serveur)
  searchTerm: string;
  currentPage: number;      // 0-indexée, comme Spring Data
  pageSize: number;
  totalElements: number;
  totalPages: number;

  // Actions
  fetchPage: (page?: number, size?: number, search?: string) => Promise<void>;
  fetchOne: (id: string) => Promise<void>;
  create: (request: NouveauBachelierRequest) => Promise<NouveauBachelierResponse>;
  update: (id: string, request: NouveauBachelierRequest) => Promise<NouveauBachelierResponse>;
  searchByNumeroTable: (numeroTable: string) => Promise<NouveauBachelierResponse | null>;
  delete: (id: string) => Promise<void>;
  importExcel: (file: File) => Promise<ImportResult>;
  importExcelMultiple: (files: File[]) => Promise<ImportResult[]>;
  importCsv: (file: File) => Promise<string[]>;
  fetchJurysNonCharges: (technique?: boolean) => Promise<void>;

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
      jurysNonCharges: [],
      isLoading: false,
      isSubmitting: false,
      error: null,
      searchTerm: '',
      currentPage: 0,
      pageSize: 10,
      totalElements: 0,
      totalPages: 0,

      // ─── Pagination serveur ──────────────────────────────────────────────
      // Remplace fetchAll() : ne charge plus jamais toute la collection (180k+ lignes)
      fetchPage: async (page, size, search) => {
        const state = get();
        const targetPage = page ?? state.currentPage;
        const targetSize = size ?? state.pageSize;
        const targetSearch = search !== undefined ? search : state.searchTerm;

        set({ isLoading: true, error: null }, false, 'bachelier/fetchPage/pending');
        try {
          const response = await axiosInstance.get<SpringPage<NouveauBachelierResponse>>(
            '/nouveauBacheliers',
            {
              params: {
                page: targetPage,
                size: targetSize,
                search: targetSearch || undefined,
              },
            }
          );
          set({
            bacheliers: response.data.content,
            totalElements: response.data.totalElements,
            totalPages: response.data.totalPages,
            currentPage: response.data.number,
            pageSize: response.data.size,
            isLoading: false,
          }, false, 'bachelier/fetchPage/fulfilled');
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors du chargement';
          set({ error: message, isLoading: false }, false, 'bachelier/fetchPage/rejected');
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
          set({ isSubmitting: false }, false, 'bachelier/create/fulfilled');
          // Recharger la page courante pour rester cohérent avec le serveur
          await get().fetchPage();
          return response.data;
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
          set({ isSubmitting: false }, false, 'bachelier/delete/fulfilled');
          // Recharger la page courante (la suppression peut décaler les éléments)
          await get().fetchPage();
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
          await get().fetchPage(0); // retour à la première page après import
          return response.data;
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors de l\'import';
          set({ error: message, isSubmitting: false }, false, 'bachelier/importExcel/rejected');
          throw error;
        }
      },

      // ✅ Import multi-fichiers : parsing parallèle + batch fusionné côté serveur,
      //    retourne un résultat détaillé par fichier
      importExcelMultiple: async (files: File[]) => {
        set({ isSubmitting: true, error: null }, false, 'bachelier/importExcelMultiple/pending');
        const formData = new FormData();
        files.forEach(file => formData.append('files', file));

        try {
          const response = await axiosInstance.post<ImportResult[]>(
            '/nouveauBacheliers/import/excel/multiple',
            formData,
            {
              headers: { 'Content-Type': 'multipart/form-data' }
            }
          );
          set({ isSubmitting: false }, false, 'bachelier/importExcelMultiple/fulfilled');
          await get().fetchPage(0); // retour à la première page après import
          return response.data;
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors de l\'import multiple';
          set({ error: message, isSubmitting: false }, false, 'bachelier/importExcelMultiple/rejected');
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
          await get().fetchPage(0);
          return response.data;
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors de l\'import CSV';
          set({ error: message, isSubmitting: false }, false, 'bachelier/importCsv/rejected');
          throw error;
        }
      },

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

      // ✅ Jurys n'ayant encore aucun bachelier chargé (filtrable par technique)
      fetchJurysNonCharges: async (technique?: boolean) => {
        set({ isLoading: true, error: null }, false, 'bachelier/fetchJurysNonCharges/pending');
        try {
          const response = await axiosInstance.get<Jury[]>('/nouveauBacheliers/jurys-non-charges', {
            params: technique !== undefined ? { technique } : undefined,
          });
          set({ jurysNonCharges: response.data, isLoading: false }, false, 'bachelier/fetchJurysNonCharges/fulfilled');
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors du chargement des jurys non chargés';
          set({ error: message, isLoading: false }, false, 'bachelier/fetchJurysNonCharges/rejected');
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
          jurysNonCharges: [],
          isLoading: false,
          isSubmitting: false,
          error: null,
          searchTerm: '',
          currentPage: 0,
          pageSize: 10,
          totalElements: 0,
          totalPages: 0,
        }, false, 'bachelier/reset');
      }
    }),
    { name: 'NouveauBachelierStore' }
  )
);