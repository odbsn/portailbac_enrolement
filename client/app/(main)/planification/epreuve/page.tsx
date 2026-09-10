'use client';

import {
    ColumnDef,
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    SortingState,
    useReactTable
} from '@tanstack/react-table';
import { Badge } from 'primereact/badge';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { ConfirmDialog, confirmDialog } from 'primereact/confirmdialog';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { FileUpload, FileUploadHandlerEvent } from 'primereact/fileupload';
import { InputNumber } from 'primereact/inputnumber';
import { InputSwitch } from 'primereact/inputswitch';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { ProgressBar } from 'primereact/progressbar';
import { TabPanel, TabView } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import ProtectedRoute from '@/layout/ProtectedRoute';
import { EpreuveRequest, EpreuveResponse, Jour, JourInitialisationRequest, useJourEpreuveStore } from './joursEpreuvesStore';
// ─── Helpers ─────────────────────────────────────────────────────────────────

const TYPE_LABELS: Record<string, string> = {
    BAC_GENERAL: 'Général 1er',
    BAC_TECHNIQUE: 'Technique 1er',
    BAC_GENERAL_2TOUR: 'Général 2ème',
    BAC_TECHNIQUE_2TOUR: 'Technique 2ème',
    EPS: 'EPS',
    FACULTATIVE: 'Facultative',
    PRJT: 'Projet',
};

const TYPE_SEVERITY: Record<string, 'success' | 'info' | 'warning' | 'danger'> = {
    BAC_GENERAL: 'info',
    BAC_TECHNIQUE: 'warning',
    BAC_GENERAL_2TOUR: 'success',
    BAC_TECHNIQUE_2TOUR: 'danger',
    EPS: 'info',
    FACULTATIVE: 'warning',
    PRJT: 'success',
};

// ─── Table générique TanStack ─────────────────────────────────────────────────

function TanTable<T>({
    data,
    columns,
    globalFilter,
    paginated = false,
    pageSize = 20,
}: {
    data: T[];
    columns: ColumnDef<T>[];
    globalFilter?: string;
    /** Pagination CLIENT (TanStack) — pour une liste déjà entièrement chargée
     *  (ex: jours). Ne pas activer sur une liste déjà paginée côté serveur
     *  (ex: épreuves), qui ne reçoit qu'une seule page de données à la fois. */
    paginated?: boolean;
    pageSize?: number;
}) {
    const [sorting, setSorting] = useState<SortingState>([]);

    const table = useReactTable({
        data,
        columns,
        state: { sorting, globalFilter },
        onSortingChange: setSorting,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        ...(paginated ? { getPaginationRowModel: getPaginationRowModel() } : {}),
        initialState: paginated ? { pagination: { pageSize } } : undefined,
    });

    return (
        <div>
        <div className="overflow-x-auto border-round-lg border-1 surface-border">
            <table className="w-full" style={{ borderCollapse: 'collapse' }}>
                <thead className="surface-100">
                    {table.getHeaderGroups().map((hg) => (
                        <tr key={hg.id}>
                            {hg.headers.map((header) => (
                                <th
                                    key={header.id}
                                    onClick={header.column.getToggleSortingHandler()}
                                    className="text-left p-3 font-semibold text-sm"
                                    style={{ cursor: header.column.getCanSort() ? 'pointer' : 'default', whiteSpace: 'nowrap' }}
                                >
                                    <div className="flex align-items-center gap-1">
                                        {flexRender(header.column.columnDef.header, header.getContext())}
                                        {header.column.getIsSorted() === 'asc' && <i className="pi pi-sort-up text-xs" />}
                                        {header.column.getIsSorted() === 'desc' && <i className="pi pi-sort-down text-xs" />}
                                        {!header.column.getIsSorted() && header.column.getCanSort() && (
                                            <i className="pi pi-sort text-xs text-300" />
                                        )}
                                    </div>
                                </th>
                            ))}
                        </tr>
                    ))}
                </thead>
                <tbody>
                    {table.getRowModel().rows.length === 0 ? (
                        <tr>
                            <td colSpan={columns.length} className="text-center p-5 text-500">
                                <i className="pi pi-inbox text-3xl mb-2 block" />
                                Aucune donnée
                            </td>
                        </tr>
                    ) : (
                        table.getRowModel().rows.map((row, idx) => (
                            <tr
                                key={row.id}
                                className={idx % 2 === 0 ? 'surface-0' : 'surface-50'}
                                style={{ borderTop: '1px solid var(--surface-border)' }}
                            >
                                {row.getVisibleCells().map((cell) => (
                                    <td key={cell.id} className="p-3 text-sm">
                                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                    </td>
                                ))}
                            </tr>
                        ))
                    )}
                </tbody>
            </table>
        </div>

        {paginated && table.getPageCount() > 1 && (
            <div className="flex justify-content-between align-items-center mt-2 flex-wrap gap-2">
                <span className="text-sm text-500">
                    Page <strong>{table.getState().pagination.pageIndex + 1}</strong> / <strong>{table.getPageCount()}</strong>
                    &nbsp;—&nbsp;{data.length} ligne(s) au total
                </span>
                <div className="flex gap-1 align-items-center">
                    <Button icon="pi pi-angle-double-left" size="small" text disabled={!table.getCanPreviousPage()} onClick={() => table.setPageIndex(0)} tooltip="Première page" />
                    <Button icon="pi pi-angle-left" size="small" text disabled={!table.getCanPreviousPage()} onClick={() => table.previousPage()} tooltip="Page précédente" />
                    <span className="text-sm px-2">{table.getState().pagination.pageIndex + 1}</span>
                    <Button icon="pi pi-angle-right" size="small" text disabled={!table.getCanNextPage()} onClick={() => table.nextPage()} tooltip="Page suivante" />
                    <Button icon="pi pi-angle-double-right" size="small" text disabled={!table.getCanNextPage()} onClick={() => table.setPageIndex(table.getPageCount() - 1)} tooltip="Dernière page" />
                </div>
            </div>
        )}
        </div>
    );
}

// ─── Onglet Jours ─────────────────────────────────────────────────────────────

function TabJours() {
    const {
        jours, joursLoading, joursError,
        statut, initLoading, updateLoading,
        fetchJours, fetchStatut,
        initializeJours, updateJoursNames,
        clearErrors,
    } = useJourEpreuveStore();

    const toast = useRef<Toast>(null);
    const [showDialog, setShowDialog] = useState(false);
    const [globalFilter, setGlobalFilter] = useState('');
    const [form, setForm] = useState<Partial<JourInitialisationRequest>>({
        dateBacGeneralStart: null,
        dateBacTechniqueStart: null,
        dateBacGeneralDTour: null,
        dateBacTechniqueDTour: null,
        dateEPS: null,
        dateLAFAC: null,
        dateLBFAC: null,
        dateJPRJT: null,
    });

    useEffect(() => {
        fetchJours();
        fetchStatut();
    }, []);

    const setDate = (field: keyof JourInitialisationRequest, value: Date | null) =>
        setForm((prev) => ({ ...prev, [field]: value ? value.toISOString().split('T')[0] : null }));

    // ✅ Précharge les dates déjà enregistrées (via le jour de référence de
    // chaque groupe) pour ne pas devoir tout ressaisir à chaque ouverture.
    const findDateByCode = (code: string): string | null =>
        jours.find((j) => j.code === code)?.date ?? null;

    const openDatesDialog = () => {
        setForm({
            dateBacGeneralStart: findDateByCode('J1'),
            dateBacTechniqueStart: findDateByCode('J01'),
            dateBacGeneralDTour: findDateByCode('Y1'),
            dateBacTechniqueDTour: findDateByCode('Y01'),
            dateEPS: findDateByCode('JEPS'),
            dateLAFAC: findDateByCode('JLAFAC'),
            dateLBFAC: findDateByCode('JLBFAC'),
            dateJPRJT: findDateByCode('JPRJT'),
        });
        setShowDialog(true);
    };

    const handleInit = async () => {
        try {
            await initializeJours();
            toast.current?.show({ severity: 'success', summary: 'Succès', detail: 'Jours initialisés', life: 3000 });
        } catch {
            toast.current?.show({ severity: 'error', summary: 'Erreur', detail: joursError ?? 'Erreur', life: 3000 });
        }
    };

    const handleUpdate = async () => {
        try {
            await updateJoursNames(form as JourInitialisationRequest);
            toast.current?.show({ severity: 'success', summary: 'Succès', detail: 'Dates enregistrées', life: 3000 });
            setShowDialog(false);
        } catch {
            toast.current?.show({ severity: 'error', summary: 'Erreur', detail: joursError ?? 'Erreur', life: 3000 });
        }
    };

    const columns = useMemo<ColumnDef<Jour>[]>(() => [
        {
            accessorKey: 'code',
            header: 'Code',
            cell: ({ getValue }) => <span className="font-bold text-primary">{getValue<string>()}</span>,
        },
        {
            accessorKey: 'name',
            header: 'Nom / Date',
            cell: ({ getValue }) =>
                getValue<string>()
                    ? <span>{getValue<string>()}</span>
                    : <span className="text-400 italic">Non défini</span>,
        },
        {
            accessorKey: 'type',
            header: 'Type',
            cell: ({ getValue }) => {
                const t = getValue<string>();
                return <Tag severity={TYPE_SEVERITY[t] ?? 'info'} value={TYPE_LABELS[t] ?? t} />;
            },
        },
        {
            accessorKey: 'ordre',
            header: 'Ordre',
            cell: ({ getValue }) => <span className="text-500 text-xs">{getValue<number>()}</span>,
        },
    ], []);

    const groupedCount = useMemo(() => {
        const counts: Record<string, number> = {};
        jours.forEach((j) => { counts[j.type] = (counts[j.type] ?? 0) + 1; });
        return counts;
    }, [jours]);

    return (
        <div className="p-3">
            <Toast ref={toast} />

            {/* Cartes de statut */}
            <div className="grid mb-4">
                <div className="col-12 md:col-3">
                    <Card className="text-center surface-50 border-1 surface-border">
                        <div className="text-3xl font-bold text-primary">{statut?.totalJours ?? jours.length}</div>
                        <div className="text-sm text-500 mt-1">Jours total</div>
                    </Card>
                </div>
                <div className="col-12 md:col-3">
                    <Card className="text-center surface-50 border-1 surface-border">
                        <div className={`text-3xl font-bold ${(statut?.nonInitialises ?? 0) > 0 ? 'text-orange-500' : 'text-green-500'}`}>
                            {statut?.nonInitialises ?? 0}
                        </div>
                        <div className="text-sm text-500 mt-1">Sans date</div>
                    </Card>
                </div>
                <div className="col-12 md:col-3">
                    <Card className="text-center surface-50 border-1 surface-border">
                        <div className="text-2xl font-bold text-blue-500">
                            {(groupedCount['BAC_GENERAL'] ?? 0) + (groupedCount['BAC_GENERAL_2TOUR'] ?? 0)}
                        </div>
                        <div className="text-sm text-500 mt-1">Jours Général</div>
                        <div className="flex justify-content-center gap-1 mt-1">
                            <Tag value={`1er: ${groupedCount['BAC_GENERAL'] ?? 0}`} severity="info" style={{ fontSize: '10px' }} />
                            <Tag value={`2ème: ${groupedCount['BAC_GENERAL_2TOUR'] ?? 0}`} severity="success" style={{ fontSize: '10px' }} />
                        </div>
                    </Card>
                </div>
                <div className="col-12 md:col-3">
                    <Card className="text-center surface-50 border-1 surface-border">
                        <div className="text-2xl font-bold text-orange-500">
                            {(groupedCount['BAC_TECHNIQUE'] ?? 0) + (groupedCount['BAC_TECHNIQUE_2TOUR'] ?? 0)}
                        </div>
                        <div className="text-sm text-500 mt-1">Jours Technique</div>
                        <div className="flex justify-content-center gap-1 mt-1">
                            <Tag value={`1er: ${groupedCount['BAC_TECHNIQUE'] ?? 0}`} severity="warning" style={{ fontSize: '10px' }} />
                            <Tag value={`2ème: ${groupedCount['BAC_TECHNIQUE_2TOUR'] ?? 0}`} severity="danger" style={{ fontSize: '10px' }} />
                        </div>
                    </Card>
                </div>
            </div>

            {joursError && (
                <div className="flex align-items-center justify-content-between mb-3 p-3 border-round border-1 border-red-300 bg-red-50 text-red-700">
                    <span className="text-sm"><i className="pi pi-times-circle mr-2" />{joursError}</span>
                    <Button icon="pi pi-times" text size="small" severity="danger" onClick={clearErrors} />
                </div>
            )}

            {/* Barre d'actions */}
            <div className="flex justify-content-between align-items-center mb-3 gap-2 flex-wrap">
                <span className="p-input-icon-left">
                    <i className="pi pi-search" />
                    <InputText
                        placeholder="Filtrer (code, type...)"
                        value={globalFilter}
                        onChange={(e) => setGlobalFilter(e.target.value)}
                        className="p-inputtext-sm w-18rem"
                    />
                </span>
                <div className="flex gap-2">
                    <Button
                        label="Initialiser les jours"
                        icon="pi pi-database"
                        severity="secondary"
                        size="small"
                        loading={initLoading}
                        onClick={handleInit}
                        tooltip="Crée les jours vides : J1-J10, J01-J10, Y1-Y10, Y01-Y10, JEPS, JLAFAC, JLBFAC, JPRJT"
                        tooltipOptions={{ position: 'top' }}
                    />
                    <Button label="Définir les dates" icon="pi pi-calendar-plus" size="small" onClick={openDatesDialog} />
                    <Button icon="pi pi-refresh" size="small" text loading={joursLoading} onClick={() => { fetchJours(); fetchStatut(); }} tooltip="Actualiser" />
                </div>
            </div>

            {joursLoading
                ? <ProgressBar mode="indeterminate" style={{ height: '4px' }} />
                : <TanTable data={jours} columns={columns} globalFilter={globalFilter} paginated pageSize={20} />
            }

            {/* Dialog dates */}
            <Dialog
                header={<span><i className="pi pi-calendar-plus mr-2" />Définir les dates du calendrier</span>}
                visible={showDialog}
                onHide={() => setShowDialog(false)}
                style={{ width: '700px' }}
                footer={
                    <div className="flex justify-content-end gap-2">
                        <Button label="Annuler" severity="secondary" size="small" onClick={() => setShowDialog(false)} />
                        <Button label="Enregistrer" icon="pi pi-save" size="small" loading={updateLoading} onClick={handleUpdate} />
                    </div>
                }
            >
                <div className="grid">
                    <div className="col-12">
                        <div className="flex align-items-center gap-2 mb-2">
                            <Tag severity="info" value="1er GROUPE" />
                            <span className="text-sm text-500">Jours J1... / J01...</span>
                        </div>
                    </div>
                    <div className="col-6">
                        <label className="block text-sm font-medium mb-1">Bac Général — début (J1)</label>
                        <Calendar value={form.dateBacGeneralStart ? new Date(form.dateBacGeneralStart) : null} onChange={(e) => setDate('dateBacGeneralStart', e.value as Date | null)} dateFormat="dd/mm/yy" showIcon className="w-full" />
                    </div>
                    <div className="col-6">
                        <label className="block text-sm font-medium mb-1">Bac Technique — début (J01)</label>
                        <Calendar value={form.dateBacTechniqueStart ? new Date(form.dateBacTechniqueStart) : null} onChange={(e) => setDate('dateBacTechniqueStart', e.value as Date | null)} dateFormat="dd/mm/yy" showIcon className="w-full" />
                    </div>
                    <div className="col-12 mt-3">
                        <div className="flex align-items-center gap-2 mb-2">
                            <Tag severity="success" value="2ème GROUPE" />
                            <span className="text-sm text-500">Jours Y1... / Y01...</span>
                        </div>
                    </div>
                    <div className="col-6">
                        <label className="block text-sm font-medium mb-1">Bac Général 2ème — début (Y1)</label>
                        <Calendar value={form.dateBacGeneralDTour ? new Date(form.dateBacGeneralDTour) : null} onChange={(e) => setDate('dateBacGeneralDTour', e.value as Date | null)} dateFormat="dd/mm/yy" showIcon className="w-full" />
                    </div>
                    <div className="col-6">
                        <label className="block text-sm font-medium mb-1">Bac Technique 2ème — début (Y01)</label>
                        <Calendar value={form.dateBacTechniqueDTour ? new Date(form.dateBacTechniqueDTour) : null} onChange={(e) => setDate('dateBacTechniqueDTour', e.value as Date | null)} dateFormat="dd/mm/yy" showIcon className="w-full" />
                    </div>
                    <div className="col-12 mt-3">
                        <div className="flex align-items-center gap-2 mb-2">
                            <Tag severity="warning" value="ÉPREUVES SPÉCIALES" />
                        </div>
                    </div>
                    {([
                        { field: 'dateEPS', label: 'EPS (JEPS)' },
                        { field: 'dateLAFAC', label: 'LA Facultative (JLAFAC)' },
                        { field: 'dateLBFAC', label: 'LB Facultative (JLBFAC)' },
                        { field: 'dateJPRJT', label: 'Projet (JPRJT)' },
                    ] as { field: keyof JourInitialisationRequest; label: string }[]).map(({ field, label }) => (
                        <div key={field} className="col-6">
                            <label className="block text-sm font-medium mb-1">{label}</label>
                            <Calendar
                                value={form[field] ? new Date(form[field] as string) : null}
                                onChange={(e) => setDate(field, e.value as Date | null)}
                                dateFormat="dd/mm/yy" showIcon className="w-full"
                            />
                        </div>
                    ))}
                </div>
            </Dialog>
        </div>
    );
}

// ─── Types statiques ────────────────────────────────────────────────────────

const EPREUVE_TYPE_OPTIONS = [
    { label: 'Écrit', value: 'Ecrit' },
    { label: 'Oral/Pratique', value: 'Oral/Pratique' },
];

const OUI_NON_OPTIONS = [
    { label: 'Oui', value: true },
    { label: 'Non', value: false },
];

const emptyEpreuveForm: EpreuveRequest = {
    matiere: '',
    serie: '',
    coefficient: null,
    autorisation: true,
    estDominant: false,
    nombrePoints: null,
    jourDebut: null,
    heureDebut: null,
    duree: null,
    type: 'Ecrit',
};

// ─── Dialog Création / Édition d'une épreuve ───────────────────────────────────

function EpreuveFormDialog({
    visible,
    onHide,
    editing,
    onSaved,
}: {
    visible: boolean;
    onHide: () => void;
    editing: EpreuveResponse | null;
    onSaved: (message: string) => void;
}) {
    const { matieres, series, jours, heures, saveLoading, createEpreuve, updateEpreuve } = useJourEpreuveStore();
    const [form, setForm] = useState<EpreuveRequest>(emptyEpreuveForm);
    const [error, setError] = useState<string | null>(null);

    // ✅ Libellés enrichis pour éviter les erreurs de sélection : la date pour
    // un jour (déjà formatée en français côté backend dans "name"), l'heure
    // pour une heure — au lieu d'afficher seulement le code (ex: "J1", "H3").
    const jourOptions = useMemo(() => jours.map((j) => ({
        ...j,
        label: j.name ? `${j.code} — ${j.name}` : j.code,
    })), [jours]);
    const heureOptions = useMemo(() => heures.map((h) => ({
        ...h,
        label: `${h.code} — ${h.heure}`,
    })), [heures]);

    useEffect(() => {
        if (!visible) return;
        if (editing) {
            setForm({
                matiere: editing.matiere?.code ?? '',
                serie: editing.serie?.code ?? '',
                coefficient: editing.coefficient,
                autorisation: editing.autorisation,
                estDominant: editing.estDominant,
                nombrePoints: editing.nombrePoints,
                jourDebut: editing.jourDebut?.code ?? null,
                heureDebut: editing.heureDebut?.code ?? null,
                duree: editing.duree,
                type: editing.type ?? 'Ecrit',
            });
        } else {
            setForm(emptyEpreuveForm);
        }
        setError(null);
    }, [visible, editing]);

    const handleSave = async () => {
        if (!form.matiere || !form.serie || !form.type) {
            setError('Matière, série et type sont obligatoires.');
            return;
        }
        setError(null);
        try {
            if (editing) {
                await updateEpreuve(editing.id, form);
                onSaved('Épreuve modifiée');
            } else {
                await createEpreuve(form);
                onSaved('Épreuve créée');
            }
            onHide();
        } catch (err: any) {
            setError(err?.response?.data?.message ?? "Erreur lors de l'enregistrement");
        }
    };

    return (
        <Dialog
            header={<span><i className={`pi ${editing ? 'pi-pencil' : 'pi-plus'} mr-2`} />{editing ? "Modifier l'épreuve" : 'Nouvelle épreuve'}</span>}
            visible={visible}
            onHide={onHide}
            style={{ width: '600px' }}
            footer={
                <div className="flex justify-content-end gap-2">
                    <Button label="Annuler" severity="secondary" size="small" onClick={onHide} />
                    <Button label="Enregistrer" icon="pi pi-save" size="small" loading={saveLoading} onClick={handleSave} />
                </div>
            }
        >
            {error && <Message severity="error" className="mb-3 w-full" text={error} />}
            <div className="grid">
                <div className="col-6">
                    <label className="block text-sm font-medium mb-1">Matière *</label>
                    <Dropdown
                        value={form.matiere}
                        options={matieres}
                        optionLabel="name"
                        optionValue="code"
                        filter
                        virtualScrollerOptions={{ itemSize: 38 }}
                        placeholder="Sélectionner"
                        className="w-full"
                        onChange={(e) => setForm((f) => ({ ...f, matiere: e.value }))}
                    />
                </div>
                <div className="col-6">
                    <label className="block text-sm font-medium mb-1">Série *</label>
                    <Dropdown
                        value={form.serie}
                        options={series}
                        optionLabel="code"
                        optionValue="code"
                        filter
                        virtualScrollerOptions={{ itemSize: 38 }}
                        placeholder="Sélectionner"
                        className="w-full"
                        onChange={(e) => setForm((f) => ({ ...f, serie: e.value }))}
                    />
                </div>
                <div className="col-6">
                    <label className="block text-sm font-medium mb-1">Type *</label>
                    {/* ✅ editable : les types viennent aussi de l'import Excel (texte
                        libre, ex: "Ecrit", "ECRIT", "Oral/Pratique"...) et ne
                        correspondent pas toujours à l'une des 2 options ci-dessous —
                        sans "editable" la valeur existante s'affiche vide au lieu de
                        montrer le texte réel. */}
                    <Dropdown
                        value={form.type}
                        options={EPREUVE_TYPE_OPTIONS}
                        editable
                        className="w-full"
                        onChange={(e) => setForm((f) => ({ ...f, type: e.value }))}
                    />
                </div>
                <div className="col-6">
                    <label className="block text-sm font-medium mb-1">Coefficient</label>
                    <InputNumber
                        value={form.coefficient}
                        onValueChange={(e) => setForm((f) => ({ ...f, coefficient: e.value ?? null }))}
                        className="w-full"
                        min={0}
                    />
                </div>
                <div className="col-6">
                    <label className="block text-sm font-medium mb-1">Nombre de points</label>
                    <InputNumber
                        value={form.nombrePoints}
                        onValueChange={(e) => setForm((f) => ({ ...f, nombrePoints: e.value ?? null }))}
                        className="w-full"
                        min={0}
                    />
                </div>
                <div className="col-6">
                    <label className="block text-sm font-medium mb-1">Durée</label>
                    <InputText
                        value={form.duree ?? ''}
                        placeholder="ex: 04:00:00"
                        className="w-full"
                        onChange={(e) => setForm((f) => ({ ...f, duree: e.target.value || null }))}
                    />
                </div>
                <div className="col-6">
                    <label className="block text-sm font-medium mb-1">Jour</label>
                    <Dropdown
                        value={form.jourDebut}
                        options={jourOptions}
                        optionLabel="label"
                        optionValue="code"
                        filter
                        showClear
                        virtualScrollerOptions={{ itemSize: 38 }}
                        placeholder="Aucun"
                        className="w-full"
                        onChange={(e) => setForm((f) => ({ ...f, jourDebut: e.value ?? null }))}
                    />
                </div>
                <div className="col-6">
                    <label className="block text-sm font-medium mb-1">Heure</label>
                    <Dropdown
                        value={form.heureDebut}
                        options={heureOptions}
                        optionLabel="label"
                        optionValue="code"
                        filter
                        showClear
                        virtualScrollerOptions={{ itemSize: 38 }}
                        placeholder="Aucune"
                        className="w-full"
                        onChange={(e) => setForm((f) => ({ ...f, heureDebut: e.value ?? null }))}
                    />
                </div>
                <div className="col-6 flex align-items-center gap-2 mt-3">
                    <InputSwitch
                        checked={!!form.autorisation}
                        onChange={(e) => setForm((f) => ({ ...f, autorisation: e.value }))}
                    />
                    <label className="text-sm font-medium">Autorisation</label>
                </div>
                <div className="col-6 flex align-items-center gap-2 mt-3">
                    <InputSwitch
                        checked={!!form.estDominant}
                        onChange={(e) => setForm((f) => ({ ...f, estDominant: e.value }))}
                    />
                    <label className="text-sm font-medium">Matière dominante</label>
                </div>
            </div>
        </Dialog>
    );
}

// ─── Onglet Épreuves ──────────────────────────────────────────────────────────

function TabEpreuves() {
    const {
        epreuves, epreuvesLoading, epreuvesError,
        epreuveFilters,
        importLoading, importResult,
        matieres, series,
        fetchEpreuves, importEpreuves, fetchReferences,
        deleteEpreuve, clearImportResult, clearErrors,
    } = useJourEpreuveStore();

    const toast = useRef<Toast>(null);
    const [keyword, setKeyword] = useState('');
    const [showImport, setShowImport] = useState(false);
    const [showForm, setShowForm] = useState(false);
    const [editing, setEditing] = useState<EpreuveResponse | null>(null);

    useEffect(() => { fetchEpreuves(); fetchReferences(); }, []);

    // ── Recherche serveur (debounce) ────────────────────────────────────────
    useEffect(() => {
        const t = setTimeout(() => {
            fetchEpreuves({ keyword: keyword || undefined, page: 0 });
        }, 400);
        return () => clearTimeout(t);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [keyword]);

    const applyFilter = (patch: Partial<typeof epreuveFilters>) => {
        fetchEpreuves({ ...patch, page: 0 });
    };

    const openCreate = () => { setEditing(null); setShowForm(true); };
    const openEdit = (epreuve: EpreuveResponse) => { setEditing(epreuve); setShowForm(true); };
    const handleSaved = (message: string) => {
        toast.current?.show({ severity: 'success', summary: 'Succès', detail: message, life: 3000 });
    };

    const handleUpload = useCallback(async (event: FileUploadHandlerEvent) => {
        const file = event.files[0];
        if (!file) return;
        try {
            const result = await importEpreuves(file);
            toast.current?.show({
                severity: 'success', summary: 'Import terminé',
                detail: `${result.imported} importée(s), ${result.ignored} ignorée(s)`,
                life: 5000,
            });
            setShowImport(false);
        } catch {
            toast.current?.show({ severity: 'error', summary: 'Erreur', detail: "Échec de l'import", life: 3000 });
        }
    }, [importEpreuves]);

    // ✅ Suppression avec confirmation
    const handleDelete = useCallback((id: string, matiere: string, serie: string) => {
        confirmDialog({
            message: `Supprimer l'épreuve "${matiere} — ${serie}" ?`,
            header: 'Confirmation de suppression',
            icon: 'pi pi-exclamation-triangle',
            acceptClassName: 'p-button-danger',
            acceptLabel: 'Supprimer',
            rejectLabel: 'Annuler',
            accept: async () => {
                try {
                    await deleteEpreuve(id);
                    toast.current?.show({ severity: 'success', summary: 'Supprimé', detail: 'Épreuve supprimée', life: 3000 });
                } catch {
                    toast.current?.show({ severity: 'error', summary: 'Erreur', detail: 'Erreur lors de la suppression', life: 3000 });
                }
            },
        });
    }, [deleteEpreuve]);

    const columns = useMemo<ColumnDef<EpreuveResponse>[]>(() => [
        {
            accessorFn: (r) => r.matiere?.name,
            id: 'matiere',
            header: 'Matière',
            cell: ({ getValue, row }) => (
                <div>
                    <div className="font-medium text-sm">{getValue<string>()}</div>
                    <small className="text-400">{row.original.matiere?.code}</small>
                </div>
            ),
        },
        {
            accessorFn: (r) => r.serie?.code,
            id: 'serie',
            header: 'Série',
            cell: ({ getValue, row }) => (
                <div>
                    <span className="font-bold text-sm">{getValue<string>()}</span>
                    <div><small className="text-400">{row.original.serie?.name}</small></div>
                </div>
            ),
        },
        {
            accessorKey: 'type',
            header: 'Type',
            cell: ({ getValue }) => {
                const t = getValue<string>();
                const s = t === 'Ecrit' ? 'info' : t === 'Oral' ? 'success' : 'warning';
                return <Tag severity={s} value={t} />;
            },
        },
        {
            accessorKey: 'coefficient',
            header: 'Coef.',
            cell: ({ getValue }) => {
                const v = getValue<number | null>();
                return v != null ? <Badge value={v} severity="info" /> : <span className="text-400">—</span>;
            },
        },
        {
            accessorKey: 'nombrePoints',
            header: 'Points',
            cell: ({ getValue }) => {
                const v = getValue<number | null>();
                return v != null ? <span className="font-medium">{v}</span> : <span className="text-400">—</span>;
            },
        },
        {
            accessorKey: 'estDominant',
            header: 'Dominant',
            cell: ({ getValue }) =>
                getValue<boolean>()
                    ? <i className="pi pi-check-circle text-green-500" />
                    : <i className="pi pi-minus text-300" />,
        },
        {
            accessorKey: 'autorisation',
            header: 'Autor.',
            cell: ({ getValue }) =>
                getValue<boolean>()
                    ? <Tag severity="success" value="OUI" />
                    : <Tag severity="danger" value="NON" />,
        },
        {
            accessorFn: (r) => r.jourDebut?.code,
            id: 'jour',
            header: 'Jour',
            cell: ({ getValue, row }) => {
                const code = getValue<string>();
                const name = row.original.jourDebut?.name;
                if (!code) return <span className="text-400 italic">—</span>;
                return (
                    <div>
                        <span className="font-bold text-primary text-sm">{code}</span>
                        {name && <div><small className="text-400">{name}</small></div>}
                    </div>
                );
            },
        },
        {
            // ⚠️ Heure n'a pas de champ "name" côté portailbac_enrolement — on
            // affiche directement la valeur brute de l'heure (ex: "07:30:00").
            accessorFn: (r) => r.heureDebut?.heure,
            id: 'heure',
            header: 'Heure',
            cell: ({ getValue, row }) => {
                const heure = getValue<string>();
                const code = row.original.heureDebut?.code;
                if (!heure) return <span className="text-400 italic">—</span>;
                return (
                    <div>
                        <span className="font-medium text-sm">{heure}</span>
                        {code && <div><small className="text-400">{code}</small></div>}
                    </div>
                );
            },
        },
        {
            accessorKey: 'duree',
            header: 'Durée',
            cell: ({ getValue }) => {
                const v = getValue<string>();
                return v ? <span>{v}</span> : <span className="text-400 italic">—</span>;
            },
        },
        {
            id: 'actions',
            header: '',
            cell: ({ row }) => (
                <div className="flex gap-1">
                    <Button
                        icon="pi pi-pencil"
                        severity="info"
                        size="small"
                        text
                        rounded
                        tooltip="Modifier"
                        tooltipOptions={{ position: 'top' }}
                        onClick={() => openEdit(row.original)}
                    />
                    <Button
                        icon="pi pi-trash"
                        severity="danger"
                        size="small"
                        text
                        rounded
                        tooltip="Supprimer"
                        tooltipOptions={{ position: 'top' }}
                        onClick={() => handleDelete(
                            row.original.id,
                            row.original.matiere?.name ?? '',
                            row.original.serie?.code ?? ''
                        )}
                    />
                </div>
            ),
        },
    ], [handleDelete]);

    const data = epreuves?.content ?? [];

    // ✅ Spring Boot 3 — pagination dans epreuves.page (sous-objet)
    const currentPage = epreuves?.page?.number ?? epreuveFilters.page;
    const totalPages = epreuves?.page?.totalPages ?? 0;
    const totalElements = epreuves?.page?.totalElements ?? 0;

    const goToPage = (page: number) => fetchEpreuves({ page });

    return (
        <div className="p-3">
            <Toast ref={toast} />
            <ConfirmDialog />

            {epreuvesError && (
                <div className="flex align-items-center justify-content-between mb-3 p-3 border-round border-1 border-red-300 bg-red-50 text-red-700">
                    <span className="text-sm"><i className="pi pi-times-circle mr-2" />{epreuvesError}</span>
                    <Button icon="pi pi-times" text size="small" severity="danger" onClick={clearErrors} />
                </div>
            )}

            {/* Résultat import */}
            {importResult && (
                <div className="mb-3 p-3 border-round border-1 surface-border surface-50">
                    <div className="flex justify-content-between align-items-center mb-2">
                        <span className="font-bold text-sm">Résultat de l&#39;import</span>
                        <Button icon="pi pi-times" text size="small" onClick={clearImportResult} />
                    </div>
                    <div className="flex gap-4 text-sm">
                        <span className="text-green-600 font-medium">
                            <i className="pi pi-check mr-1" />{importResult.imported} importée(s)
                        </span>
                        <span className="text-orange-500 font-medium">
                            <i className="pi pi-exclamation-triangle mr-1" />{importResult.ignored} ignorée(s)
                        </span>
                        {Array.isArray(importResult.errors) && importResult.errors.length > 0 && (
                            <span className="text-red-500 font-medium">
                                <i className="pi pi-times mr-1" />{importResult.errors.length} erreur(s)
                            </span>
                        )}
                    </div>

                    {/* ✅ Détail des motifs — erreurs ET lignes ignorées fusionnées,
                        triées par numéro de ligne, liste complète avec scroll. */}
                    {(() => {
                        const erreursAvecOrigine = (importResult.errors ?? []).map((e) => ({ ...e, origine: 'erreur' as const }));
                        const ignoreesAvecOrigine = (importResult.ignoredRows ?? []).map((e) => ({ ...e, origine: 'ignorée' as const }));
                        const motifs = [...erreursAvecOrigine, ...ignoreesAvecOrigine].sort(
                            (a, b) => (a.row ?? 0) - (b.row ?? 0)
                        );

                        if (motifs.length === 0) return null;

                        return (
                            <div
                                className="mt-2 border-top-1 surface-border pt-2"
                                style={{ maxHeight: '260px', overflowY: 'auto' }}
                            >
                                <ul className="text-xs list-none p-0 m-0">
                                    {motifs.map((m, i) => {
                                        const motif = m.reason ?? m.message ?? 'Motif non précisé';
                                        const isErreur = m.origine === 'erreur';
                                        return (
                                            <li
                                                key={i}
                                                className="flex align-items-start gap-2 py-1"
                                                style={{ borderBottom: i < motifs.length - 1 ? '1px solid var(--surface-200)' : undefined }}
                                            >
                                                <Tag
                                                    severity={isErreur ? 'danger' : 'warning'}
                                                    value={m.row != null ? `Ligne ${m.row}` : '—'}
                                                    style={{ minWidth: '70px', textAlign: 'center', flexShrink: 0 }}
                                                />
                                                <span className={isErreur ? 'text-red-600' : 'text-orange-600'}>
                                                    {motif}
                                                </span>
                                            </li>
                                        );
                                    })}
                                </ul>
                            </div>
                        );
                    })()}
                </div>
            )}

            {/* Barre d'actions */}
            <div className="flex justify-content-between align-items-center mb-3 gap-2 flex-wrap">
                <div className="flex gap-2 align-items-center">
                    <span className="p-input-icon-left">
                        <i className="pi pi-search" />
                        <InputText
                            placeholder="Rechercher (matière, série, type...)"
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                            className="p-inputtext-sm w-18rem"
                        />
                    </span>
                    {totalElements > 0 && (
                        <span className="text-sm text-500">
                            <strong>{totalElements}</strong> épreuve(s)
                        </span>
                    )}
                </div>
                <div className="flex gap-2">
                    <Button
                        label="Nouvelle épreuve"
                        icon="pi pi-plus"
                        severity="success"
                        size="small"
                        onClick={openCreate}
                    />
                    <Button
                        label="Importer Excel"
                        icon="pi pi-file-excel"
                        severity="info"
                        size="small"
                        loading={importLoading}
                        onClick={() => setShowImport(true)}
                    />
                    <Button
                        icon="pi pi-refresh" size="small" text
                        loading={epreuvesLoading}
                        onClick={() => fetchEpreuves({ page: 0 })}
                        tooltip="Actualiser"
                    />
                </div>
            </div>

            {/* Filtres serveur */}
            <div className="flex gap-2 mb-3 flex-wrap">
                <Dropdown
                    value={epreuveFilters.matiereId ?? null}
                    options={matieres}
                    optionLabel="name"
                    optionValue="id"
                    filter
                    showClear
                    virtualScrollerOptions={{ itemSize: 38 }}
                    placeholder="Matière"
                    className="w-14rem p-inputtext-sm"
                    onChange={(e) => applyFilter({ matiereId: e.value ?? undefined })}
                />
                <Dropdown
                    value={epreuveFilters.serieId ?? null}
                    options={series}
                    optionLabel="code"
                    optionValue="id"
                    filter
                    showClear
                    virtualScrollerOptions={{ itemSize: 38 }}
                    placeholder="Série"
                    className="w-10rem p-inputtext-sm"
                    onChange={(e) => applyFilter({ serieId: e.value ?? undefined })}
                />
                <Dropdown
                    value={epreuveFilters.type ?? null}
                    options={EPREUVE_TYPE_OPTIONS}
                    showClear
                    placeholder="Type"
                    className="w-10rem p-inputtext-sm"
                    onChange={(e) => applyFilter({ type: e.value ?? undefined })}
                />
                <Dropdown
                    value={epreuveFilters.autorisation ?? null}
                    options={OUI_NON_OPTIONS}
                    showClear
                    placeholder="Autorisation"
                    className="w-10rem p-inputtext-sm"
                    onChange={(e) => applyFilter({ autorisation: e.value ?? undefined })}
                />
                <Dropdown
                    value={epreuveFilters.estDominant ?? null}
                    options={OUI_NON_OPTIONS}
                    showClear
                    placeholder="Dominant"
                    className="w-10rem p-inputtext-sm"
                    onChange={(e) => applyFilter({ estDominant: e.value ?? undefined })}
                />
            </div>

            {epreuvesLoading
                ? <ProgressBar mode="indeterminate" style={{ height: '4px' }} />
                : <TanTable data={data} columns={columns} />
            }

            {/* ✅ Pagination serveur correcte — affichée dès qu'il y a du contenu */}
            {totalElements > 0 && (
                <div className="flex justify-content-between align-items-center mt-3">
                    <span className="text-sm text-500">
                        Page <strong>{currentPage + 1}</strong> / <strong>{totalPages}</strong>
                        &nbsp;—&nbsp;{totalElements} épreuve(s) au total
                    </span>
                    <div className="flex gap-1 align-items-center">
                        <Button
                            icon="pi pi-angle-double-left"
                            size="small" text
                            disabled={currentPage === 0}
                            onClick={() => goToPage(0)}
                            tooltip="Première page"
                        />
                        <Button
                            icon="pi pi-angle-left"
                            size="small" text
                            disabled={currentPage === 0}
                            onClick={() => goToPage(currentPage - 1)}
                            tooltip="Page précédente"
                        />

                        {/* Numéros de pages */}
                        {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                            const start = Math.max(0, Math.min(currentPage - 2, totalPages - 5));
                            const page = start + i;
                            return (
                                <Button
                                    key={page}
                                    label={String(page + 1)}
                                    size="small"
                                    text={page !== currentPage}
                                    severity={page === currentPage ? 'info' : 'secondary'}
                                    onClick={() => goToPage(page)}
                                    style={{ minWidth: '36px' }}
                                />
                            );
                        })}

                        <Button
                            icon="pi pi-angle-right"
                            size="small" text
                            disabled={currentPage >= totalPages - 1}
                            onClick={() => goToPage(currentPage + 1)}
                            tooltip="Page suivante"
                        />
                        <Button
                            icon="pi pi-angle-double-right"
                            size="small" text
                            disabled={currentPage >= totalPages - 1}
                            onClick={() => goToPage(totalPages - 1)}
                            tooltip="Dernière page"
                        />
                    </div>
                </div>
            )}

            {/* Dialog import */}
            <Dialog
                header={<span><i className="pi pi-file-excel mr-2 text-green-500" />Importer des épreuves depuis Excel</span>}
                visible={showImport}
                onHide={() => setShowImport(false)}
                style={{ width: '500px' }}
            >
                <Message
                    severity="info"
                    className="mb-3 w-full"
                    text="Le fichier doit contenir : Code Matière, Code Série, Type, Coef, Dominant, Autorisation, Code Jour, Code Heure, Durée."
                />
                <FileUpload
                    mode="advanced"
                    name="file"
                    accept=".xlsx,.xls"
                    maxFileSize={10_000_000}
                    customUpload
                    uploadHandler={handleUpload}
                    chooseLabel="Choisir un fichier"
                    uploadLabel="Importer"
                    cancelLabel="Annuler"
                    emptyTemplate={
                        <div className="text-center text-500 p-4">
                            <i className="pi pi-file-excel text-5xl text-green-400 mb-3 block" />
                            <div>Glissez votre fichier Excel ici</div>
                            <small>.xlsx ou .xls, max 10 Mo</small>
                        </div>
                    }
                />
            </Dialog>

            {/* Dialog création / édition */}
            <EpreuveFormDialog
                visible={showForm}
                onHide={() => setShowForm(false)}
                editing={editing}
                onSaved={handleSaved}
            />
        </div>
    );
}

// ─── Page principale ──────────────────────────────────────────────────────────

export default function JoursEpreuvesPage() {
    return (
        <ProtectedRoute allowedRoles={["ADMIN"]}>
            <div className="p-4">
                <div className="mb-4">
                    <h1 className="text-2xl font-bold m-0 flex align-items-center gap-2">
                        <i className="pi pi-calendar text-primary" />
                        Calendrier des épreuves
                    </h1>
                    <p className="text-500 mt-1 mb-0 text-sm">
                        Gestion des jours (1er et 2ème groupe) et import des épreuves du baccalauréat
                    </p>
                </div>
                <Card className="shadow-1">
                    <TabView>
                        <TabPanel header={<span className="flex align-items-center gap-2"><i className="pi pi-calendar" />Jours</span>}>
                            <TabJours />
                        </TabPanel>
                        <TabPanel header={<span className="flex align-items-center gap-2"><i className="pi pi-list" />Épreuves</span>}>
                            <TabEpreuves />
                        </TabPanel>
                    </TabView>
                </Card>
            </div>
        </ProtectedRoute>
    );
}
