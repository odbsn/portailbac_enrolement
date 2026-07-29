'use client';

import React, { useEffect, useRef, useState } from 'react';
import { Button } from 'primereact/button';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { ProgressSpinner } from 'primereact/progressspinner';
import { classNames } from 'primereact/utils';
import { ParametrageService } from '@/demo/service/ParametrageService';
import { MultiSelect } from 'primereact/multiselect';
import { Etablissement, InspectionAcademie } from '@/app/userContext';
import { CandidatureService } from '@/demo/service/CandidatureService';
import { InputNumber } from 'primereact/inputnumber';
import ProtectedRoute from '@/layout/ProtectedRoute';
import '../style.css';

interface RegleCentre
{
    id?: string;
    session: number;
    provenanceVille?: string[];
    provenanceAcademie?: InspectionAcademie;
    discipline?: string[];
    classes?: string[];
    centreDeComposition?:Etablissement
}

const PlanningRegleCentre = () => {
    const toast = useRef<any>(null);

    const emptyRegle: RegleCentre = {
        session: 0,
        provenanceVille: [],
        provenanceAcademie: undefined,
        discipline: [],
        classes: [],
        centreDeComposition: undefined
    };

    const [regles, setRegles] = useState<RegleCentre[]>([]);
    const [regle, setRegle] = useState<RegleCentre>(emptyRegle);
    const [getIas, setIAs] = useState([]);
    const [matieres, setMatieres] = useState([]);
    const [dialogVisible, setDialogVisible] = useState(false);
    const [loading, setLoading] = useState(true);
    const [submitted, setSubmitted] = useState(false);
    const [specialites, setSpecialiteData] = useState([]);
    const [error, setError] = useState(null);
    const [villes, setVilles] = useState([]);
    const [etabs, setEtabs] = useState([]);

    const [prog, setOneProg] = useState<{ edition?: number; bfem_IfEPI?: number; bfem_IfI?: number } | null>(null);
        

    const classeOptions = [
        { label: 'TERMINALE', value: 'TERMINALE' },
        { label: 'PREMIERE', value: 'PREMIERE' }
    ];

    const groupes = [
        { label: 'PREMIER GROUPE', value: '1ERGRP' },
        { label: 'PREMIER GROUPE & SECOND GROUPE', value: '1ER2NDGRP' }
    ];

    const champs = [
        { label: 'Matière Optionnelle 1', value: 'matiere1' },
        { label: 'Matière Optionnelle 2', value: 'matiere2' },
        { label: 'Matière Optionnelle 3', value: 'matiere3' },
        { label: 'Epreuve Facultative Liste A', value: 'eprFacListA' },
        { label: 'Epreuve Facultative Liste B', value: 'eprFacListB' }
    ];

    // 🔹 LOAD
    const loadData = async () => {
        try {
            setLoading(true);
            const data = await ParametrageService.getAllRegles();
            setRegles(data || []);
        } catch (e) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erreur',
                detail: 'Chargement Régles impossible'
            });
        } finally {
            setLoading(false);
        }
    };

    const loadIA = async () => {
        try {
            setLoading(true);
            const data = await ParametrageService.getIAs();
            setIAs(data || []);
        } catch (e) {
            toast.current?.show({
                severity: 'error',
                summary: 'Erreur',
                detail: 'Chargement Série impossible'
            });
        } finally {
            setLoading(false);
        }
    };


    useEffect(() => {
        loadData();
        loadIA();
        loadEtabs();
    }, []);

    useEffect(() => {
            CandidatureService.getLastProg().then((response) => {
                console.log("Prog :", response);
                    setOneProg(response);
            });
    }, []);


    useEffect(() => {
        CandidatureService.getAllSpecialite()
            .then((response) => {
                if (!Array.isArray(response)) return setSpecialiteData([]);

                // récupérer des noms uniques, filtrer les valeurs null/undefined
                const uniqueSpecialites = Array.from(
                    new Set(response
                        .map(item => item.specialite)
                        .filter(Boolean) // supprime undefined/null
                    )
                );

                // transformer en objets { label, value } pour MultiSelect
                const options = uniqueSpecialites.map(s => ({ label: s, value: s }));

                setSpecialiteData(options); // met à jour le state
            })
            .catch(err => {
                console.error("Erreur récupération spécialités :", err);
                setSpecialiteData([]); // fallback vide
            });
    }, []);



    useEffect(() => {
        if (prog?.edition) 
        {
            loadData_();
        }
    }, [prog]);
    
    const loadData_ = async () => {
            setLoading(true);
            setError(null);
            try {
                ParametrageService.getVillesCGS(prog?.edition)
                    .then((response) => {
                        setVilles(response);
                    })
                    .catch((error) => {
                        console.error('❌ Erreur lors du chargement des données :', error);
                    });
            } 
            catch (err) 
            {
                console.error('❌ Erreur chargement données :', err);
                setError('Erreur lors du chargement');
            } 
            finally 
            {
                setLoading(false);
            }
        };
        

    const villesOptions = villes.map(ville => ({ name: ville }));

    const loadEtabs = async () => {
                    try 
                    {
                        ParametrageService.getEtablissements()
                        .then((response) => {
                            setEtabs(response);
                        })
                        .catch((error) => {
                            console.error("❌ Erreur lors du chargement des séries :", error);
                        });
                    } 
                    catch (err) 
                    {
                        console.error("❌ Erreur chargement candidats :", err);
                        setError("Erreur lors du chargement");
                    } 
                };


    // 🔹 NEW
    const openNew = () => {
        setRegle(emptyRegle);
        setSubmitted(false);
        setDialogVisible(true);
    };

    // 🔹 EDIT
    const editRegle = (row: RegleCentre) => {
        console.log(row);
        setRegle({ ...row });
        setDialogVisible(true);
    };

    // 🔹 DELETE
    const deleteRegle = async (row: RegleCentre) => {
        try {
            await ParametrageService.deleteRegle(row.id);
            toast.current.show({
                severity: 'success',
                summary: 'Succès',
                detail: 'Supprimé'
            });
            loadData();
        } catch {
            toast.current.show({
                severity: 'error',
                summary: 'Erreur',
                detail: 'Suppression échouée'
            });
        }
    };

    // 🔹 SAVE (create/update)
    const saveRegle = async () => {
        setSubmitted(true);

        // Validation obligatoire
        if (
            !regle.session ||
            !regle.discipline?.length ||
            !regle.classes?.length ||
            !regle.centreDeComposition
        ) {
            toast.current?.show({
                severity: 'error',
                summary: 'PortailBAC',
                detail: 'Veuillez remplir tous les champs obligatoires'
            });
            return;
        }

        try {
            if (regle.id) {
                await ParametrageService.updateRegle(regle.id, regle);
                toast.current.show({ severity: 'success', summary: 'Mis à jour' });
            } else {
                await ParametrageService.createRegle(regle);
                toast.current.show({ severity: 'success', summary: 'Créé' });
            }
            setDialogVisible(false);
            loadData();
            setSubmitted(false);
        } catch (e) {
            toast.current.show({
                severity: 'error',
                summary: 'Erreur',
                detail: 'Enregistrement échoué'
            });
        }
    };

    // 🔹 ACTIONS COLUMN
    const actionBody = (row: RegleCentre) => (
        <div className="flex gap-2">
            <Button icon="pi pi-pencil" rounded onClick={() => editRegle(row)} />
            <Button icon="pi pi-trash" severity="danger" rounded onClick={() => deleteRegle(row)} />
        </div>
    );

    const dialogFooter = (
        <>
            <Button label="Annuler" icon="pi pi-times" outlined onClick={() => setDialogVisible(false)} />
            <Button label="Enregistrer" icon="pi pi-check" onClick={saveRegle} />
        </>
    );

    if (loading) return <ProgressSpinner />;

    return (
        <ProtectedRoute allowedRoles={['ADMIN', 'SCOLARITE']}>
            <div className="card">
                <Toast ref={toast} />

                <div className="flex justify-content-between mb-3">
                    <h3>Programmation des centres</h3>
                    <Button label="Nouvelle règle" icon="pi pi-plus" onClick={openNew} />
                </div>

                <DataTable value={regles} paginator rows={10} responsiveLayout="scroll">
                    <Column field="centreDeComposition.name" header="Centre de compo." />
                    <Column field="session" header="Edition du BAC" />
                    <Column 
                        field="discipline" 
                        header="Spécialité(s)" 
                        body={(row) => row.discipline?.join(", ")} 
                    />
                    <Column field="classes" 
                        header="Classe (s) concernée (s)"
                        body={(row) => row.classes?.join(", ")}  
                    />
                    <Column
                        field="provenanceVille" 
                        header="Ville de provenance" 
                        body={(row) => row.provenanceVille?.join(", ")} 
                    />
                    <Column field="provenanceAcademie.name" header="Académie de provenance" />
                    <Column body={actionBody} header="Actions" />
                </DataTable>

                <Dialog
                    header="Règle de programmation"
                    visible={dialogVisible}
                    style={{ width: '1250px' }}
                    footer={dialogFooter}
                    onHide={() => setDialogVisible(false)}
                >
                    <div className="p-fluid">

                        {/* Code */}
                        

                        <div className="field grid">
                                    <label className="col-1 mb-0">Edition du BAC</label>
                                    <div className="col-2">
                                        <InputNumber
                                            value={regle.session}
                                            onValueChange={(e) =>
                                                setRegle(prev => ({ ...prev, session: e.value ?? 0 }))
                                            }
                                            mode="decimal"
                                            min={0}
                                            maxFractionDigits={0}
                                            useGrouping={false}
                                            style={{ fontWeight: 'bold' }}
                                            className={classNames({ 'p-invalid': submitted && !regle.session })}
                                        />
                                        {submitted && !regle.session && <small className="p-error">Champ requis</small>}
                                    </div>
                        </div>

                        <div className="field grid">
                            <label className="col-4 mb-0">Villes de provenance</label>
                            <div className="col-12">
                                <MultiSelect
                                        value={regle.provenanceVille}
                                        options={villesOptions}
                                        optionLabel="name"
                                        optionValue="name"
                                        onChange={(e) =>
                                            setRegle(prev => ({ ...prev, provenanceVille: e.value || [] }))
                                        }
                                        placeholder="Choisir les villes"
                                        filter
                                        display="chip"
                                        className="w-full"
                                        style={{ fontWeight: 'bold' }}
                                        filterPlaceholder="Rechercher une ville..." 
                                    />
                            </div>
                        </div>

                        <div className="field grid">
                            <label className="col-3 mb-0">Inspection d'académie de provenance</label>
                            <div className="col-4">
                                <Dropdown
                                    value={regle.provenanceAcademie}
                                    options={getIas}
                                    optionLabel="name"
                                    style={{ fontWeight: 'bold' }}
                                    onChange={(e) =>
                                        setRegle(prev => ({ ...prev, provenanceAcademie: e.value }))
                                    }
                                    placeholder="Sélectionner"
                                />
                            </div>
                        </div>

                        <div className="field grid">
                            <label className="col-4 mb-0">Spécialité</label>
                            <div className="col-12">
                                <MultiSelect
                                        value={regle.discipline}
                                        options={specialites}
                                        optionLabel="label"
                                        optionValue="value"
                                        style={{ fontWeight: 'bold' }}
                                        onChange={(e) =>
                                            setRegle(prev => ({ ...prev, discipline: e.value || [] }))
                                        }
                                        placeholder="Choisir les spécialités"
                                        filter
                                        display="chip"
                                        filterPlaceholder="Rechercher une spécialité..." 
                                        className={classNames({ 'p-invalid': submitted && !regle.discipline?.length })}
                                />
                                {submitted && !regle.discipline?.length && <small className="p-error">Champ requis</small>}
                            </div>
                        </div>

                        <div className="field grid">
                            <label className="col-3 mb-0">Classe (niveau)</label>
                            <div className="col-5">
                                <MultiSelect
                                        value={regle.classes}
                                        options={classeOptions}
                                        optionLabel="label"
                                        optionValue="value"
                                        style={{ fontWeight: 'bold' }}
                                        onChange={(e) =>
                                            setRegle(prev => ({ ...prev, classes: e.value || [] }))
                                        }
                                        placeholder="Choisir les niveaux"
                                        filter
                                        display="chip"
                                        filterPlaceholder="Rechercher un niveau..." 
                                        className={classNames({ 'p-invalid': submitted && !regle.classes?.length })}
                                />
                                {submitted && !regle.classes?.length && <small className="p-error">Champ requis</small>}
                            </div>
                        </div>

                        <div className="field grid">
                            <label className="col-3 mb-0">Centre de composition</label>
                            <div className="col-5">
                                <Dropdown
                                    value={regle.centreDeComposition}
                                    options={etabs}
                                    optionLabel="name"
                                    style={{ fontWeight: 'bold' }}
                                    filter
                                    virtualScrollerOptions={{ itemSize: 25 }}
                                    scrollHeight="200px"
                                    onChange={(e) =>
                                        setRegle(prev => ({ ...prev, centreDeComposition: e.value }))
                                    }
                                    placeholder="Sélectionner"
                                    className={classNames({ 'p-invalid': submitted && !regle.centreDeComposition })}
                                />
                                {submitted && !regle.centreDeComposition && <small className="p-error">Champ requis</small>}
                            </div>
                        </div>
                    </div>
                </Dialog>
            </div>
        </ProtectedRoute>
    );
};

export default PlanningRegleCentre;