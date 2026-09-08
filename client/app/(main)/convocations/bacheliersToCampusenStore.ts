import axiosInstance from '@/app/api/axiosInstance';
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

// ─── Types ────────────────────────────────────────────────────────────────────

export interface BacheliersToCampusen {
  id: string;
  annee: number;
  serie: string;
  numeroTable: string;
  prenoms: string;
  nom: string;
  dateNaissance: string;
  anneeNaissance: string;
  lieuNaissance: string;
  telephone: string;
  sexe: string;
  etsProvenance: string;
  typeCandidature: string;
  academieProvenance: string;
  residence: string;
  centreEcrit: string;
  numeroJury: string;
  nombreFois: string;
  nationalite: string;
  matiereOptionnelle1: string;
  matiereOptionnelle2: string;
  matiereOptionnelle3: string;
  epreuveFacultativeListeA: string;
  epreuveFacultativeListeB: string;
  noteEpreuveFacultativeA: string;
  noteEpreuveFacultativeB: string;
  noteEps: string;
  present: string;
  mention: string;
  resultat: string;
  groupeResultat: string;
  dateDeliberation: string;
  paysNaissance: string;
  cec: string;
  numeroAec: string;
  anneeExtraitEc: string;
  typeAec: string;
  moyenneSeconde: string;
  moyennePremiere: string;
  moyenneS1Terminale: string;
  moyenneS2Terminale: string;
  totalPointsGroupe1: string;
  moyenneGroupe1: string;
  totalPointsG1G2: string;
  moyenneGenerale: string;
  moyenneMatieresFondamentales: string;
  moyenneRetenue: string;
  moyenneDefinitive: string;
  notes?: Record<string, string>;
}

export interface SerieImportStat {
  total: number;
  nouveaux: number;
  misAJour: number;
}

export interface ImportLog {
  id: string;
  annee: number;
  nomFichier: string;
  nombreFeuilles: number;
  nombreCandidats: number;
  nombreNouveaux: number;
  nombreMisAJour: number;
  candidatsParSerie: Record<string, SerieImportStat>;
  dateImport: string;
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
// Même logique que nouveauBachelierStore : liste plate paginée côté serveur,
// sans notion de session à sélectionner (les données sont sessionnières —
// une seule session en base à la fois, purgée après exploitation).

interface BacheliersToCampusenState {
  bacheliers: BacheliersToCampusen[];

  isLoading: boolean;
  isSubmitting: boolean;
  error: string | null;

  searchTerm: string;
  currentPage: number;      // 0-indexée, comme Spring Data
  pageSize: number;
  totalElements: number;
  totalPages: number;

  fetchPage: (page?: number, size?: number, search?: string) => Promise<void>;
  importer: (fichier: File, annee: number, remplacer?: boolean) => Promise<ImportLog>;

  setSearchTerm: (term: string) => void;
  setCurrentPage: (page: number) => void;
  setPageSize: (size: number) => void;
  clearError: () => void;
  reset: () => void;
}

// ─── Store Implementation ────────────────────────────────────────────────────

export const useBacheliersToCampusenStore = create<BacheliersToCampusenState>()(
  devtools(
    (set, get) => ({
      bacheliers: [],
      isLoading: false,
      isSubmitting: false,
      error: null,
      searchTerm: '',
      currentPage: 0,
      pageSize: 10,
      totalElements: 0,
      totalPages: 0,

      fetchPage: async (page, size, search) => {
        const state = get();
        const targetPage = page ?? state.currentPage;
        const targetSize = size ?? state.pageSize;
        const targetSearch = search !== undefined ? search : state.searchTerm;

        set({ isLoading: true, error: null }, false, 'bacheliersCampusen/fetchPage/pending');
        try {
          const response = await axiosInstance.get<SpringPage<BacheliersToCampusen>>(
            '/bacheliers-to-campusen',
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
          }, false, 'bacheliersCampusen/fetchPage/fulfilled');
        } catch (error: any) {
          const message = error?.response?.data?.message || 'Erreur lors du chargement';
          set({ error: message, isLoading: false }, false, 'bacheliersCampusen/fetchPage/rejected');
          throw error;
        }
      },

      importer: async (fichier: File, annee: number, remplacer = false) => {
        set({ isSubmitting: true, error: null }, false, 'bacheliersCampusen/importer/pending');
        const formData = new FormData();
        formData.append('fichier', fichier);
        formData.append('annee', String(annee));
        formData.append('remplacer', String(remplacer));

        try {
          const response = await axiosInstance.post<ImportLog>(
            '/imports-bacheliers/load',
            formData,
            {
              headers: { 'Content-Type': 'multipart/form-data' }
            }
          );
          set({ isSubmitting: false }, false, 'bacheliersCampusen/importer/fulfilled');
          // L'import a réussi : un échec du rafraîchissement de la liste ne doit pas
          // faire passer l'opération pour un échec côté appelant (modal).
          get().fetchPage(0).catch(() => {});
          return response.data;
        } catch (error: any) {
          const message = error?.response?.data?.message || "Erreur lors de l'import";
          set({ error: message, isSubmitting: false }, false, 'bacheliersCampusen/importer/rejected');
          throw error;
        }
      },

      setSearchTerm: (term: string) => {
        set({ searchTerm: term, currentPage: 0 }, false, 'bacheliersCampusen/setSearchTerm');
      },

      setCurrentPage: (page: number) => {
        set({ currentPage: page }, false, 'bacheliersCampusen/setCurrentPage');
      },

      setPageSize: (size: number) => {
        set({ pageSize: size, currentPage: 0 }, false, 'bacheliersCampusen/setPageSize');
      },

      clearError: () => {
        set({ error: null }, false, 'bacheliersCampusen/clearError');
      },

      reset: () => {
        set({
          bacheliers: [],
          isLoading: false,
          isSubmitting: false,
          error: null,
          searchTerm: '',
          currentPage: 0,
          pageSize: 10,
          totalElements: 0,
          totalPages: 0,
        }, false, 'bacheliersCampusen/reset');
      }
    }),
    { name: 'BacheliersToCampusenStore' }
  )
);
