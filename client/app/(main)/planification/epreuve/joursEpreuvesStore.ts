import { create } from 'zustand';
import axiosInstance from '@/app/api/axiosInstance';

// ─── Types ───────────────────────────────────────────────────────────────────

export interface Jour {
    id: string;
    code: string;
    name: string;
    date: string | null;
    ordre: number;
    type: 'BAC_GENERAL' | 'BAC_TECHNIQUE' | 'BAC_GENERAL_2TOUR' | 'BAC_TECHNIQUE_2TOUR' | 'EPS' | 'FACULTATIVE' | 'PRJT';
}

export interface Matiere {
    id: string;
    code: string;
    name: string;
}

export interface Serie {
    id: string;
    code: string;
    name: string;
}

export interface Heure {
    id: string;
    code: string;       // ex: "H1", "H2"
    heure: string;      // ex: "07:30:00"
}

export interface EpreuveResponse {
    id: string;
    matiere: Matiere;
    serie: Serie;
    coefficient: number | null;
    autorisation: boolean;
    estDominant: boolean;
    nombrePoints: number | null;
    jourDebut: Jour | null;
    heureDebut: Heure | null;
    duree: string | null;
    type: string;
}

export interface JourInitialisationRequest {
    dateBacGeneralStart: string | null;
    dateBacTechniqueStart: string | null;
    dateBacGeneralDTour: string | null;
    dateBacTechniqueDTour: string | null;
    dateEPS: string | null;
    dateLAFAC: string | null;
    dateLBFAC: string | null;
    dateJPRJT: string | null;
}

export interface EpreuveFilters {
    keyword?: string;
    matiereId?: string;
    serieId?: string;
    type?: string;
    autorisation?: boolean;
    estDominant?: boolean;
    page: number;
    size: number;
}

// Structure Spring Boot 3 — pagination dans un sous-objet "page"
export interface PageResponse<T> {
    content: T[];
    page: {
        size: number;
        number: number;   // page courante (0-based)
        totalElements: number;
        totalPages: number;
    };
}

// ✅ Une ligne d'erreur ou d'ignoré — reflète ImportError/IgnoredRecord côté
// backend. `dto` contient le détail complet de la ligne Excel d'origine
// (toutes ses colonnes), pour la traçabilité.
export interface ImportError {
    row?: number;
    reason?: string;
    message?: string;
    dto?: Record<string, any> | null;
}

// ✅ Reflète EXACTEMENT la sérialisation Jackson de
// EpreuveImportService.ImportResult côté backend. Les getters Java sont
// importedCount/errorRows/ignoredCount (pas imported/ignored), et "ignored"
// est la LISTE des lignes ignorées (IgnoredRecord), pas un compteur — d'où
// le besoin d'un mapping explicite vers la forme normalisée ImportResult
// utilisée par l'UI (voir normalizeImportResult ci-dessous).
export interface ImportResultRaw {
    importedCount: number;
    errorRows: number;
    ignoredCount: number;
    errors: ImportError[];
    duplicates: { key: string; type: string }[];
    ignored: ImportError[];
    missingMatieres?: Record<string, number>;
    missingSeries?: Record<string, number>;
    missingJours?: Record<string, number>;
    missingHeures?: Record<string, number>;
}

// Forme normalisée, stable, utilisée par les composants — ne change pas même
// si les noms de champs côté backend évoluent (seul normalizeImportResult
// aurait à être mis à jour).
export interface ImportResult {
    imported: number;
    ignored: number;
    errors: ImportError[];
    ignoredRows: ImportError[];
}

// ✅ Convertit la réponse brute du backend (noms Java) vers la forme stable
// attendue par l'UI. Centralise le mapping pour éviter les "NaN" silencieux
// si quelqu'un lit directement data.imported / data.ignored par erreur.
function normalizeImportResult(raw: Partial<ImportResultRaw> | null | undefined): ImportResult {
    return {
        imported: Number(raw?.importedCount ?? 0),
        ignored: Number(raw?.ignoredCount ?? 0),
        errors: Array.isArray(raw?.errors) ? raw!.errors : [],
        ignoredRows: Array.isArray(raw?.ignored) ? raw!.ignored : [],
    };
}

export interface StatutInitialisation {
    fullyInitialized: boolean;
    totalJours: number;
    nonInitialises: number;
}

// ✅ Payload CRUD — reflète EpreuveRequest côté backend : les références
// (matière, série, jour, heure) sont passées par CODE, pas par id.
export interface EpreuveRequest {
    matiere: string;       // code matière
    serie: string;         // code série
    coefficient: number | null;
    autorisation: boolean | null;
    estDominant: boolean | null;
    nombrePoints: number | null;
    jourDebut: string | null;   // code jour
    heureDebut: string | null;  // code heure
    duree: string | null;
    type: string;
}

// ─── Store ───────────────────────────────────────────────────────────────────

interface JourEpreuveState {
    // ── Jours ──
    jours: Jour[];
    joursLoading: boolean;
    joursError: string | null;
    statut: StatutInitialisation | null;
    statutLoading: boolean;
    initLoading: boolean;
    updateLoading: boolean;

    // ── Épreuves ──
    epreuves: PageResponse<EpreuveResponse> | null;
    epreuvesLoading: boolean;
    epreuvesError: string | null;
    epreuveFilters: EpreuveFilters;
    importLoading: boolean;
    importResult: ImportResult | null;
    saveLoading: boolean;

    // ── Référentiels (filtres + formulaire CRUD) ──
    matieres: Matiere[];
    series: Serie[];
    heures: Heure[];
    referencesLoading: boolean;

    // ── Actions Jours ──
    fetchJours: () => Promise<void>;
    fetchJoursByType: (type: string) => Promise<Jour[]>;
    fetchStatut: () => Promise<void>;
    initializeJours: () => Promise<void>;
    updateJoursNames: (request: JourInitialisationRequest) => Promise<void>;

    // ── Actions Épreuves ──
    fetchEpreuves: (filters?: Partial<EpreuveFilters>) => Promise<void>;
    setEpreuveFilters: (filters: Partial<EpreuveFilters>) => void;
    importEpreuves: (file: File) => Promise<ImportResult>;
    createEpreuve: (request: EpreuveRequest) => Promise<EpreuveResponse>;
    updateEpreuve: (id: string, request: EpreuveRequest) => Promise<EpreuveResponse>;
    deleteEpreuve: (id: string) => Promise<void>;
    clearImportResult: () => void;
    clearErrors: () => void;

    // ── Actions Référentiels ──
    fetchReferences: () => Promise<void>;
}

export const useJourEpreuveStore = create<JourEpreuveState>((set, get) => ({
    // ── État initial ──────────────────────────────────────────────────────────
    jours: [],
    joursLoading: false,
    joursError: null,
    statut: null,
    statutLoading: false,
    initLoading: false,
    updateLoading: false,

    epreuves: null,
    epreuvesLoading: false,
    epreuvesError: null,
    epreuveFilters: { page: 0, size: 20 },
    importLoading: false,
    importResult: null,
    saveLoading: false,

    matieres: [],
    series: [],
    heures: [],
    referencesLoading: false,

    // ── Actions Jours ─────────────────────────────────────────────────────────

    fetchJours: async () => {
        set({ joursLoading: true, joursError: null });
        try {
            const { data } = await axiosInstance.get<Jour[]>('/jours');
            set({ jours: data });
        } catch (err: any) {
            set({ joursError: err.response?.data?.message ?? 'Erreur lors du chargement des jours' });
        } finally {
            set({ joursLoading: false });
        }
    },

    fetchJoursByType: async (type: string) => {
        const { data } = await axiosInstance.get<Jour[]>(`/jours/type/${type}`);
        return data;
    },

    fetchStatut: async () => {
        set({ statutLoading: true });
        try {
            const { data } = await axiosInstance.get<StatutInitialisation>('/jours/statut');
            set({ statut: data });
        } catch {
            // silencieux
        } finally {
            set({ statutLoading: false });
        }
    },

    initializeJours: async () => {
        set({ initLoading: true, joursError: null });
        try {
            await axiosInstance.post('/jours/initialize');
            await get().fetchJours();
            await get().fetchStatut();
        } catch (err: any) {
            set({ joursError: err.response?.data?.message ?? "Erreur lors de l'initialisation" });
            throw err;
        } finally {
            set({ initLoading: false });
        }
    },

    updateJoursNames: async (request: JourInitialisationRequest) => {
        set({ updateLoading: true, joursError: null });
        try {
            await axiosInstance.put('/jours/initialiser', request);
            await get().fetchJours();
            await get().fetchStatut();
        } catch (err: any) {
            set({ joursError: err.response?.data?.message ?? 'Erreur lors de la mise à jour des dates' });
            throw err;
        } finally {
            set({ updateLoading: false });
        }
    },

    // ── Actions Épreuves ──────────────────────────────────────────────────────

    fetchEpreuves: async (filters?: Partial<EpreuveFilters>) => {
        const currentFilters = { ...get().epreuveFilters, ...filters };
        set({ epreuvesLoading: true, epreuvesError: null, epreuveFilters: currentFilters });
        try {
            const params: Record<string, any> = {
                page: currentFilters.page,
                size: currentFilters.size,
            };
            if (currentFilters.keyword) params.keyword = currentFilters.keyword;
            if (currentFilters.matiereId) params.matiereId = currentFilters.matiereId;
            if (currentFilters.serieId) params.serieId = currentFilters.serieId;
            if (currentFilters.type) params.type = currentFilters.type;
            if (currentFilters.autorisation !== undefined) params.autorisation = currentFilters.autorisation;
            if (currentFilters.estDominant !== undefined) params.estDominant = currentFilters.estDominant;

            const { data } = await axiosInstance.get<PageResponse<EpreuveResponse>>(
                '/epreuves/filters', { params }
            );
            set({ epreuves: data });
        } catch (err: any) {
            set({ epreuvesError: err.response?.data?.message ?? 'Erreur lors du chargement des épreuves' });
        } finally {
            set({ epreuvesLoading: false });
        }
    },

    setEpreuveFilters: (filters: Partial<EpreuveFilters>) => {
        set((state) => ({ epreuveFilters: { ...state.epreuveFilters, ...filters } }));
    },

    importEpreuves: async (file: File) => {
        set({ importLoading: true, importResult: null });
        try {
            const formData = new FormData();
            formData.append('file', file);
            const { data } = await axiosInstance.post<ImportResultRaw>(
                '/epreuves/import-excel', formData,
                { headers: { 'Content-Type': 'multipart/form-data' } }
            );
            const result = normalizeImportResult(data);
            set({ importResult: result });
            await get().fetchEpreuves();
            return result;
        } catch (err: any) {
            const result: ImportResult = {
                imported: 0,
                ignored: 0,
                errors: [{ message: err.response?.data?.message ?? "Erreur lors de l'import" }],
                ignoredRows: [],
            };
            set({ importResult: result });
            throw err;
        } finally {
            set({ importLoading: false });
        }
    },

    deleteEpreuve: async (id: string) => {
        await axiosInstance.delete(`/epreuves/${id}`);
        await get().fetchEpreuves();
    },

    createEpreuve: async (request: EpreuveRequest) => {
        set({ saveLoading: true });
        try {
            const { data } = await axiosInstance.post<EpreuveResponse>('/epreuves/', request);
            await get().fetchEpreuves();
            return data;
        } finally {
            set({ saveLoading: false });
        }
    },

    updateEpreuve: async (id: string, request: EpreuveRequest) => {
        set({ saveLoading: true });
        try {
            const { data } = await axiosInstance.put<EpreuveResponse>(`/epreuves/${id}`, request);
            await get().fetchEpreuves();
            return data;
        } finally {
            set({ saveLoading: false });
        }
    },

    clearImportResult: () => set({ importResult: null }),
    clearErrors: () => set({ joursError: null, epreuvesError: null }),

    // ── Actions Référentiels ──────────────────────────────────────────────────

    fetchReferences: async () => {
        set({ referencesLoading: true });
        try {
            const [matieresRes, seriesRes, heuresRes] = await Promise.all([
                axiosInstance.get<Matiere[]>('/parametrage/matieres'),
                axiosInstance.get<Serie[]>('/parametrage/series'),
                axiosInstance.get<Heure[]>('/heures'),
            ]);
            set({ matieres: matieresRes.data, series: seriesRes.data, heures: heuresRes.data });
        } catch {
            // silencieux — les dropdowns resteront vides, les filtres/formulaire le géreront
        } finally {
            set({ referencesLoading: false });
        }
    },
}));
