'use client';

import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Rating } from 'primereact/rating';
import { Toast } from 'primereact/toast';
import { Toolbar } from 'primereact/toolbar';
import React, { useContext, useEffect, useMemo, useRef, useState } from 'react';
import type { Demo } from '@/types';
import { ProductService } from '@/demo/service/ProductService';
import { Dropdown } from 'primereact/dropdown';
import { Checkbox } from 'primereact/checkbox';
import { Calendar } from 'primereact/calendar';
import { Carousel } from 'primereact/carousel';
import { ActeurDTO, ParametrageService, ProfilDTO, ProgrammationDTO, SujetDTO, UserDTO } from '@/demo/service/ParametrageService';
import * as Yup from 'yup';
import { saveAs } from 'file-saver';

import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import { InputTextarea } from 'primereact/inputtextarea';
import { useFormik } from 'formik';
import { UserContext } from '@/app/userContext';
import { InputMask } from 'primereact/inputmask';
import ProtectedRoute from '@/layout/ProtectedRoute';
import { FileUpload } from 'primereact/fileupload';
import { FiEdit } from 'react-icons/fi';
import { MdLockReset } from 'react-icons/md';
import { TabView, TabPanel } from 'primereact/tabview';
import { classNames } from 'primereact/utils';
import { ProgressSpinner } from 'primereact/progressspinner';
import { data } from 'react-router-dom';
import { CandidatureService } from '@/demo/service/CandidatureService';

type Repartition = {
    jury: number;
    centreEcrit: string;
    session: number;
    effectif: number;
    matieres?: Record<string, number>;
};

interface RegleMatiere {
    id?: string;
    code: string;
    type: string;
    champ?: string;
    valeur?: string;
    series?: string[];
    groupe?: string;
    date1?: string;
    heure1?: string;
    date2?: string;
    heure2?: string;
}

const CalendarDemo = () => {
    const [is_update, setIsUpdate] = useState(false); // <== valeur persistante entre les appels
    var id_acces = useRef(null); // <== même chose pour l'ID du candidat
    const [email, setEmail] = useState('');
    const [id_user, setIdUser] = useState('');
    const [getResultDialog, setGetResultDialog] = useState(false);
    const [resultImport, setResultImport] = useState(false);
    const [is_go_by_smtp, setIsGoBySmtp] = useState(false); // <== valeur persistante entre les appels
    const [dataRep, setDataRep] = useState([]);
    const { user } = useContext(UserContext);
    const [data, setData] = useState<Repartition[]>([]);
    const [prog, setOneProg] = useState<{ edition?: number; bfem_IfEPI?: number; bfem_IfI?: number } | null>(null);
       

    let emptyProduct: Demo.Product = {
        id: null,
        name: '',
        image: null,
        description: '',
        category: null,
        price: 0,
        quantity: 0,
        rating: 0,
        inventoryStatus: 'INSTOCK'
    };

    const [products, setProducts] = useState(null);
    const [productDialog, setProductDialog] = useState(false);
    const [productDialog2, setProductDialog2] = useState(false);
    const [productDialog3, setProductDialog3] = useState(false);
    const [codifDialog, setCodifDialog] = useState(false);
    const [deleteProductDialog, setDeleteProductDialog] = useState(false);
    const [supprimerDialog, setSupprimerDialog] = useState(false);
    const [desactiveAccessDialog, setDesactiveAccessDialog] = useState(false);
    const [deleteProductsDialog, setDeleteProductsDialog] = useState(false);
    const [product, setProduct] = useState(emptyProduct);
    const [selectedProducts, setSelectedProducts] = useState(null);
    const [submitted, setSubmitted] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);
    const [globalFilter, setGlobalFilter] = useState(null);
    const toast = useRef(null);
    const dt = useRef(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');
    const [file, setFile] = useState(null);
    const [fileId, setFileId] = useState(null);
    const [fileUrl, setFileUrl] = useState(null);

    const [dialogVisible, setDialogVisible] = useState(false);

    const [dialogVisible_, setDialogVisible_] = useState(false);

    const [ia, setIa] = useState(null);

    const [centres, setCentres] = useState([]);

    const [centre, setCentre] = useState(null);

    const [niveaux, setNiveaux] = useState([]);

    const [niveau, setNiveau] = useState(null);

    const [disciplines, setDisciplines] = useState([]);

    const [discipline, setDiscipline] = useState(null);

    const [session, setSession] = useState(2024);
    const [resultat, setResultat] = useState([]);
    const [resultat_, setResultat_] = useState([]);
    const [resultat__, setResultat__] = useState([]);

    const [users, setUsers] = useState([]);

    const [etabs, setEtabs] = useState([]);

    const [infosUsers, setInfosUsers] = useState(null);

    const [errorMessage, setErrorMessage] = useState('');

    const [exporting, setExporting] = useState(false);
    const [exportStep, setExportStep] = useState('');
    const [seconds, setSeconds] = useState(0);
    const timerRef = useRef<NodeJS.Timeout | null>(null);

    const [regles, setRegles] = useState<RegleMatiere[]>([]);
    const [regle, setRegle] = useState('');
    const [groupe, setGroupe] = useState('');

    const [level, setLevel] = useState(null);

    const [specia, setSpecia] = useState(null);

    const [specialites, setSpecialiteData] = useState([]);

    const [ias, setIas] = useState(null);

    const profilsOptions = [
        { label: 'ADMIN', value: 'ADMIN' },
        { label: 'AGENT DE SAISIE', value: 'AGENT_DE_SAISIE' },
        // { label: 'CHEF D\'ETABLISSEMENT', value: 'CHEF_ETABLISSEMENT' },
        // { label: 'PLANIFICATION', value: 'PLANIFICATION' },
        //{ label: 'PEDAGOGIE', value: 'PEDAGOGIE' },
        { label: 'SCOLARITE', value: 'SCOLARITE' },
        { label: 'VIGNETTES ET COUPONS', value: 'VIGNETTES_COUPONS' },
        { label: 'AUTORISATION RECEPTION', value: 'AUTORISATION_RECEPTION' },
        { label: 'RECEPTIONNISTE', value: 'RECEPTIONNISTE' }
        //{ label: 'STATISTIQUES', value: 'STATISTIQUES' }
    ];

    useEffect(() => {
        ProductService.getProducts().then((data) => setProducts(data));
    }, []);

    useEffect(() => {
                ParametrageService.getIAs().then((response) => {
                    //console.log("📦 Séries chargées :", data);
                    setIas(response);
                });
    }, []);

    useEffect(() => {
            CandidatureService.getLastProg().then((response) => {
                //console.log("📦 Séries chargées :", data);
                setOneProg(response);
            });
    }, []);

    useEffect(() => {
        loadData();
        loadData2()
    }, []);

    useEffect(() => {
        ParametrageService.getEtablissements().then((response) => {
            setEtabs(response);
        });
    }, []);

    useEffect(() => {}, [is_update]);

    useEffect(() => {
        ParametrageService.getInfoUsers().then((response) => {
            setInfosUsers(response);
        });
    }, []);

    useEffect(() => {
        if (ia && prog)
        {
            CandidatureService.getCentreByAcademiaAndSession(ia?.name, prog?.edition).then((response) => {
                setCentres(response);
                console.log(response)
            });
        }
    }, [ia, prog]);

    useEffect(() => {
        if (centre && prog)
        {
            CandidatureService.getNiveauByCentreAndSession(centre, prog?.edition).then((response) => {
                setNiveaux(response);
                console.log(response)
            });
        }
    }, [centre, prog]);

    useEffect(() => {
        if (centre && level && prog)
        {
            CandidatureService.getDisciplineByCentreAndNivAndSession(centre, level, prog?.edition).then((response) => {
                setDisciplines(response);
                console.log(response)
            });
        }
    }, [centre, level, prog]);

    useEffect(() => {
        if (exporting) {
            setSeconds(0);
            timerRef.current = setInterval(() => setSeconds(s => s + 1), 1000);
        } else {
            if (timerRef.current) clearInterval(timerRef.current);
        }

        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
        };
    }, [exporting]);


    useEffect(() => {
            CandidatureService.getAllSpecialite().then((response) => {
                setSpecialiteData(response);
            });
    }, []);

    const hideDialog = () => {
        setSubmitted(false);
        setProductDialog(false);
    };


    const hideDialog2 = () => {
        setSubmitted(false);
        setProductDialog2(false);
        setIsUpdate(false);
        setIsAdmin(false)
    };

    const hideDialog3 = () => {
        setProductDialog3(false);
    };

    const hideDialog4 = () => {
        setCodifDialog(false);
    };

    const hideDeleteProductDialog = () => {
        setDeleteProductDialog(false);
    };

    const hideBatchCreatedDialog = () => {
        setGetResultDialog(false);
        window.location.replace('/pedagogie/gestion-donnees');
    };


    const hideDeleteProductDialog_ = () => {
        setSupprimerDialog(false);
    };

    const hideDeleteProductDialog__ = () => {
        setDesactiveAccessDialog(false);
    };

    const hideDeleteProductsDialog = () => {
        setDeleteProductsDialog(false);
    };

    const saveProduct = () => {
        setSubmitted(true);

        if (product.name.trim()) {
            let _products = [...products];
            let _product = { ...product };
            if (product.id) {
                const index = findIndexById(product.id);

                _products[index] = _product;
                toast.current.show({
                    severity: 'success',
                    summary: 'Successful',
                    detail: 'Product Updated',
                    life: 3000
                });
            } else {
                _product.id = createId();
                _product.code = createId();
                _product.image = 'product-placeholder.svg';
                _products.push(_product);
                toast.current.show({
                    severity: 'success',
                    summary: 'Successful',
                    detail: 'Product Created',
                    life: 3000
                });
            }

            setProducts(_products);
            setProductDialog(false);
            setProduct(emptyProduct);
        }
    };

    const formatProfil = (profil) => {
        if (!profil) return null;
        // Si c’est déjà un objet complet avec un id, on le retourne tel quel
        if (typeof profil === 'object' && profil.id && profil.name) return profil;
        // Sinon on cherche par nom
        return profil.find((p) => p.name === profil) || null;
    };

    const formatEtab = (etab) => {
        console.log(etab);
        if (!etab) return null;
        // Si c’est déjà un objet complet avec un id, on le retourne tel quel
        if (typeof etab === 'object' && etab.id && etab.name) return etab;
        // Sinon on cherche par nom
        return etab.find((e) => e.name === etab) || null;
    };

    


    const generateSimplePassword = () => {
        const letters = 'abcdefghijklmnopqrstuvwxyz';
        const randomLetters = letters.charAt(Math.floor(Math.random() * letters.length)) + letters.charAt(Math.floor(Math.random() * letters.length));

        const randomDigits = String(Math.floor(1000 + Math.random() * 9000)); // 4 chiffres

        return randomLetters + randomDigits;
    };


    const handleClick = async () => {
        setLoading(true);
        try {
            // 🔹 Étape 1 : doRepCEP
            console.log(prog?.edition);
            const data = await ParametrageService.doRepCdtCGS(prog?.edition);
            setResultat(data);

            // 🔹 Redirection si toutes les étapes ont produit un résultat
            if (data.length && data.length) {
                window.location.replace('/scolarite/suivi-concours-general/repartition-des-candidats');
            }
        } finally {
            setLoading(false);
        }
    };


    const exportAllCandidats = async () => {
        try {
            console.log("Début export...");
            setExporting(true);
            setExportStep('📡 Récupération des données...');

            // 1. Appel API : récupère les données avec le groupe choisi
            const allCandidats = await ParametrageService.getListes_(centre, specia, prog?.edition, level);

            if (!allCandidats || allCandidats.length === 0) {
                setExportStep('✅ Aucune donnée à exporter');
                setTimeout(() => setExporting(false), 1000);
                return;
            }

            setExportStep('🔄 Préparation des données...');
            setExportStep('💾 Génération du fichier PDF...');
            setExportStep('✅ Export terminé avec succès !');
            setTimeout(() => setExporting(false), 1500);

        } 
        catch (error) 
        {
            setExportStep('❌ Erreur lors de l’export');
            setTimeout(() => setExporting(false), 2000);
        }
    };
    

    const exportAllCandidats_ = async () => {
        try {
            console.log("Début export...");
            setExporting(true);
            setExportStep('📡 Récupération des données...');

            // 1. Appel API : récupère les données avec le groupe choisi
            const allCandidats = await ParametrageService.getAllCdtCGS(prog?.edition);

            if (!allCandidats || allCandidats.length === 0) 
            {
                setExportStep('✅ Aucune donnée à exporter');
                setTimeout(() => setExporting(false), 1000);
                return;
            }

            setExportStep('🔄 Préparation des données...');
            import('xlsx').then((xlsx) => {
                                const worksheet = xlsx.utils.json_to_sheet(
                                    allCandidats.map(row => ({
                                        Prénom_s: row.firstname,
                                        Nom: row.lastname,
                                        Date_de_naissance: row.date_birth
                                        ? new Date(row.date_birth).toLocaleString('fr-FR', {
                                            year: 'numeric',
                                            month: '2-digit',
                                            day: '2-digit'
                                        }): '',
                                        Lieu_de_naissance: row.place_birth,
                                        Sexe: row.sexe,
                                        Classe: row.level,
                                        Discipline : row.discipline,
                                        Centre_de_Composition : row.centreComposition,
                                        Etablissement_Origine: row.etablissementOrigine,
                                        Académie : row.academia,
                                        Série : row.serie,
                                        Session : row.session                                        
                                    }))
                                );
                    
                                const workbook = { Sheets: { data: worksheet }, SheetNames: ['data'] };
                                const excelBuffer = xlsx.write(workbook, { bookType: 'xlsx', type: 'array' });
                    
                                const blob = new Blob([excelBuffer], { type: 'application/octet-stream' });
                                saveAs(blob, `Liste complete des candidats au CGS ${prog?.edition}.xlsx`);
                            });
            setExportStep('💾 Génération du fichier PDF...');
            setExportStep('✅ Export terminé avec succès !');
            setTimeout(() => setExporting(false), 1500);

        } 
        catch (error) 
        {
            setExportStep('❌ Erreur lors de l’export');
            setTimeout(() => setExporting(false), 2000);
        }
    };




    const dialogFooter_ = (
            <>
                <Button label="Annuler" icon="pi pi-times" outlined onClick={() => setDialogVisible_(false)} />
                <Button label="Valider" icon="pi pi-check" onClick={exportAllCandidats} />
            </>
    );


    const deleteProduct = async (values, { setSubmitting, resetForm }) => {
        setDeleteProductDialog(false);
        console.log('PUT');
        console.log(email);

        try {
            const response = await ParametrageService.updatePassword(email);
            console.log('✅ Candidat mis à jour:', response.data);
            setMessage('Candidat créé avec succès');
            toast.current.show({ severity: 'success', summary: 'Office du Bac', detail: 'Mot de passe réinitialisé avec succés', life: 4000 });
            resetForm();
        } catch (error) {
            console.error('❌ Erreur lors de la création du candidat:', error);
            setMessage('Erreur lors de la réinitialisation');
            toast.current.show({ severity: 'error', summary: 'Office du Bac', detail: 'Erreur lors de la réinitialisation', life: 4000 });
        } finally {
            setSubmitting(false);
        }
    };

    const deleteUser = async (values, { setSubmitting, resetForm }) => {
        console.log('DELETE');
        try 
        {
            const response = await ParametrageService.deleteUser(id_user);
            console.log('✅ Candidat mis à jour:', response);
            setMessage('Candidat supprimé avec succès');
            toast.current.show({ severity: 'success', summary: 'Office du Bac', detail: 'Compte supprimé avec succés', life: 4000 });
            resetForm();
        } 
        catch (error) 
        {
            console.error('❌ Erreur lors de la suppression du compte:', error);
            setMessage('Erreur lors de la suppression');
            toast.current.show({ severity: 'error', summary: 'Office du Bac', detail: 'Erreur lors de la suppression', life: 4000 });
        } 
        finally 
        {
            setSubmitting(false);
        }
        await loadData();
        setSupprimerDialog(false);
    };


    const desactiveUser = async (values, { setSubmitting, resetForm }) => {
        console.log('DELETE');
        try 
        {
            const response = await ParametrageService.desactiveUser(id_user, true);
            console.log('✅ Candidat mis à jour:', response);
            setMessage('Candidat supprimé avec succès');

            if (response)
            {
                toast.current.show({ severity: 'success', summary: 'Office du Bac', detail: 'Compte réactivé avec succés', life: 4000 });
            }
            else
            {
                toast.current.show({ severity: 'warn', summary: 'Office du Bac', detail: 'Compte désactivé avec succés', life: 4000 });
            }
            
            resetForm();
        } 
        catch (error) 
        {
            console.error('❌ Erreur lors de la suppression du compte:', error);
            setMessage('Erreur lors de la suppression');
            toast.current.show({ severity: 'error', summary: 'Office du Bac', detail: 'Erreur lors de la suppression', life: 4000 });
        } 
        finally 
        {
            setSubmitting(false);
        }
        await loadData();
        setDesactiveAccessDialog(false);
    };

    const loadData = async () => {
        setLoading(true);
        setError(null);
        try 
        {
            const data = await ParametrageService.getRep();
            setDataRep(data);
        } 
        catch (err) 
        {
            console.error('❌ Erreur chargement données :', err);
            setError('Erreur lors du chargement');
            setDataRep([]);
        } 
        finally 
        {
            setLoading(false);
        }
    };

    const loadData2 = async () => {
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

    const findIndexById = (id) => {
        let index = -1;
        for (let i = 0; i < products.length; i++) {
            if (products[i].id === id) {
                index = i;
                break;
            }
        }

        return index;
    };

    const createId = () => {
        let id = '';
        let chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        for (let i = 0; i < 5; i++) {
            id += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        return id;
    };

    const exportCSV = () => {
        dt.current.exportCSV();
    };

    const confirmDeleteSelected = () => {
        setDeleteProductsDialog(true);
    };

    const deleteSelectedProducts = () => {
        let _products = products.filter((val) => !selectedProducts.includes(val));
        setProducts(_products);
        setDeleteProductsDialog(false);
        setSelectedProducts(null);
        toast.current.show({
            severity: 'success',
            summary: 'Successful',
            detail: 'Products Deleted',
            life: 3000
        });
    };

    const onCategoryChange = (e) => {
        let _product = { ...product };
        _product['category'] = e.value;
        setProduct(_product);
    };

    const onInputChange = (e, name) => {
        const val = (e.target && e.target.value) || '';
        let _product = { ...product };
        _product[`${name}`] = val;

        setProduct(_product);
    };

    const onInputNumberChange = (e, name) => {
        const val = e.value || 0;
        let _product = { ...product };
        _product[`${name}`] = val;

        setProduct(_product);
    };

    const leftToolbarTemplate = () => {
    return (
        <div className="flex flex-column">

            <div>
                <h3>Gestion de la répartition des candidats au CGS édition {prog?.edition}</h3>
            </div>

            <div className="flex align-items-center gap-1 flex-wrap">
                <Button
                    severity="info"
                    onClick={handleClick}
                    icon="pi pi-download"
                    label="Lancer toutes les répartitions"
                    className="p-button-primary"
                />

                <Button
                    type="button"
                    icon="pi pi-print"
                    severity="help"
                    label="Exporter les listes d'émargement"
                    onClick={() => setDialogVisible_(true)}
                    className="p-button-primary"
                />

                <Button
                    type="button"
                    icon="pi pi-file"
                    severity="success"
                    label="Exporter la liste complète des candidats"
                    onClick={exportAllCandidats_}
                    className="p-button-primary"
                />

                {/* <Button
                    type="button"
                    icon="pi pi-file-excel"
                    severity="success"
                    label="Exporter le chiffrage"
                    onClick={() => setDialogVisible(true)}
                    className="p-button-primary"
                /> */}
            </div>

        </div>
    );
};


    const rightToolbarTemplate = () => {
        return <React.Fragment>{/* <Button severity="help" label="Exporter la liste" icon="pi pi-upload" onClick={exportCSV} /> */}</React.Fragment>;
    };

    const academieTemplate = (rowData) => {
        return (
            <>
                {rowData.academia}
            </>
        );
    };


    const disciplineTemplate = (rowData) => {
        return (
            <>
                {rowData.discipline}
            </>
        );
    };

    const specialiteTemplate = (rowData) => {
        return (
            <>
                {rowData.specialite}
            </>
        );
    };

    const firstnameTemplate = (rowData) => {
        return (
            <>
                {rowData.firstname}
            </>
        );
    };

    const lastnameTemplate = (rowData) => {
        return (
            <>
                {rowData.lastname}
            </>
        );
    };

    const sexeTemplate = (rowData) => {
        return (
            <>
                {rowData.sexe}
            </>
        );
    };

    const dateBirthTemplate = (rowData) => {
        return (
            <>
                {rowData.date_birth}
            </>
        );
    };

    const placeBirthTemplate = (rowData) => {
        return (
            <>
                {rowData.place_birth}
            </>
        );
    };

    const serieTemplate = (rowData) => {
        return (
            <>
                {rowData.serie}
            </>
        );
    };

    const etablissementOrigineTemplate = (rowData) => {
        return (
            <>
                {rowData.etablissementOrigine}
                <br />
                ({rowData.level})
            </>
        );
    };

    const centreCompositionTemplate = (rowData) => {
        return (
            <>
                {rowData.centreComposition}
                <br />
                ({rowData.academia})
            </>
        );
    };



    const getMatiereColumns = (rows: Repartition[]) => {
        const set = new Set<string>();

        if (!Array.isArray(rows)) return [];

        rows.forEach(item => {
            if (item.matieres) {
                Object.keys(item.matieres).forEach(k => set.add(k));
            }
        });

        return Array.from(set).sort();
    };

   const matiereBody = (rowData: any, code: string) => {
        const matiere = rowData.matieres?.[code];
        const premier = matiere?.premierGroupe ?? 0;
        const second = Math.round(matiere?.secondGroupe) ?? 0;

        return (
            <div style={{ display: 'flex', flexDirection: 'column', lineHeight: '1.3', justifyContent: 'left' }}>
                <span style={{ fontWeight: 600, color: '#333' }}>1er Grp : {premier}</span>
                <span style={{ fontWeight: 600, color: '#555' }}>2nd Grp : {second}</span>
            </div>
        );
    };

    

    const cecTemplate = (rowData) => {
        return (
            <>
                {rowData.centreEcrit}
            </>
        );
    };

    const sessionTemplate = (rowData) => {
        return (
            <>
                {rowData.session}
            </>
        );
    };


    

    
    const header = (
        <div className="flex flex-column md:flex-row md:justify-content-between md:align-items-center">
            <h5 className="m-0">Répartition par centre de composition</h5>
            <span className="block mt-2 md:mt-0 p-input-icon-left">
                <i className="pi pi-search" />
                <InputText type="search" onChange={(e) => setGlobalFilter((e.target as HTMLInputElement).value)} placeholder="Recherche..." />
            </span>
        </div>
    );

    const productDialogFooter = (
        <>
            <Button label="Valider" icon="pi pi-check" text onClick={saveProduct} />
            <Button label="Fermer" icon="pi pi-times" text onClick={hideDialog} />
        </>
    );
       

    const handleFileChange = (e) => {
        const fileOK = e.files?.[0];
        setFile(fileOK);
        setErrorMessage('');
    };

    const handleUpload = async () => {
        setResultImport(null);
        setLoading(false);
        if (!file) 
        {
            setErrorMessage("⚠️ Veuillez d'abord charger un fichier Excel valide.");
            return;
        } 
        else {
            setCodifDialog(false);
            setLoading(true);
            setGetResultDialog(true);
            console.log(file);
            const message = await ParametrageService.uploadFile(file);
            setLoading(false);
            setResultImport(message);
            toast.current.show({ severity: 'success', summary: 'Office du Bac', detail: 'Fichier chargé avec succès', life: 4000 });
            
        }
    };


    



    //console.log(is_update);

    return (
        <ProtectedRoute allowedRoles={['ADMIN', 'SCOLARITE']}>
            <div className="grid crud-demo">
                <div className="col-12">
                    <div className="card">
                        <style>
                                {`
                    .accepted-row {
                        background-color: #e6ffed !important; /* Vert très clair */
                    }

                    .rejected-row {
                        background-color: #ffe6e6 !important; /* Rouge très clair */
                    }
                    `}
                            </style>
                        <Toast ref={toast} />
                        <Toolbar className="mb-4" left={leftToolbarTemplate} right={rightToolbarTemplate}></Toolbar>

                            {(loading || dataRep.length > 0) && (
                            <DataTable
                                ref={dt}
                                loading={loading}
                                loadingIcon="pi pi-spin pi-spinner"
                                stripedRows
                                showGridlines
                                scrollable
                                value={dataRep}
                                paginator
                                rows={10}
                                rowsPerPageOptions={[5, 10]}
                                className="p-datatable-sm"
                                currentPageReportTemplate="Affichage de {first} à {last} des {totalRecords} enregistrement (s)"
                                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                                globalFilter={globalFilter}
                                emptyMessage="Aucune donnée n'a été trouvée"
                                header={header}
                            >
                                
                                <Column field="centreComposition" header="Centre de Composition" body={centreCompositionTemplate} style={{ minWidth: '25rem' }} />
                                <Column field="etablissementOrigine" header="Etab. Origine & (Niveau)" body={etablissementOrigineTemplate} style={{ minWidth: '25rem' }} />
                                <Column field="discipline" header="Discipline" body={disciplineTemplate} style={{ minWidth: '20rem' }} />
                                <Column field="firstname" header="Prénom (s)" body={firstnameTemplate} style={{ minWidth: '15rem' }} />
                                <Column field="lastname" header="Nom" body={lastnameTemplate} style={{ minWidth: '10rem' }} />
                                <Column field="sexe" header="Sexe" body={sexeTemplate} />

                                <Column field="date_birth" header="Date de Naiss." body={dateBirthTemplate} style={{ minWidth: '8rem' }}  />
                                <Column field="place_birth" header="Lieu de Naiss." body={placeBirthTemplate} style={{ minWidth: '8rem' }}   />
                                <Column field="serie" header="Série" body={serieTemplate} />
                            </DataTable>
                        )}


                        <Dialog visible={codifDialog} style={{ width: '1000px' }} header="Création automatique d'accés pour les établissements" modal className="p-fluid" onHide={hideDialog4}>
                            <div style={{ color: 'red' }}>
                                <span><b>Mention utile 1 : </b>Veuillez charger exclusivement un fichier Excel.</span>
                                <br />
                                <span><b>Mention utile 2 : </b>Le fichier devra contenir obligatoirement ces colonnes dans l&apos;ordre suivant : </span>
                                <br />
                                <span>- Colonne 1 : Adresse email existante et utilisable de l&apos;établissement ;</span>
                                <br />
                                <span>- Colonne 2 : Code du nom de l&apos;établissement ;</span>
                                <br />
                                <span>- Colonne 3 : Numéro de téléphone de l&apos;établissement.</span>
                                <br />
                                <span><b>Mention utile 3 : </b>Deux établissements ne peuvent en aucun cas partager le même code ni la même adresse email.</span>
                            </div>
                            <div className="col-md-6">
                                <FileUpload mode="basic" accept=".xls, .xlsx" customUpload name="xls" chooseLabel="Charger le fichier excel" onSelect={handleFileChange} className="mr-2 mt-5" />
                                {errorMessage && <div style={{ color: 'red', marginTop: '10px' }}>{errorMessage}</div>}

                                <Button label="Enregistrer les données du fichier pour la création des accés" icon="pi pi-upload" className="p-button-success mt-2" onClick={handleUpload} />
                            </div>
                        </Dialog>

  

                        <Dialog visible={exporting} onHide={() => {}} modal closable={false} header="Préparation du fichier">
                            <div className="flex flex-column align-items-center justify-content-center">
                                <ProgressSpinner style={{ width: '50px', height: '50px' }} />
                                <p style={{ marginTop: '1rem', fontWeight: 'bold' }}>{exportStep}</p>
                                <p style={{ marginTop: '1rem', fontWeight: 'bold' }}>
                                    Veuillez patienter... ⏱ {seconds} seconde{seconds > 1 ? 's' : ''}
                                </p>
                            </div>
                        </Dialog>

                     

                        <Dialog 
                            visible={getResultDialog} 
                            style={{ width: '600px' }} 
                            header="Création de comptes pour les Agents de Saisie" 
                            modal 
                            onHide={hideBatchCreatedDialog}
                        >
                            <div className="flex flex-column align-items-center">
                                {loading && (
                                    <div className="flex flex-column justify-content-center align-items-center" style={{ height: '100px' }}>
                                        <ProgressSpinner style={{ width: '50px', height: '50px' }} strokeWidth="6" />
                                        <span>Les comptes sont en cours de création...</span>
                                    </div>
                                )}

                                {!loading && resultImport && (
                                    <div className="flex flex-column align-items-center">
                                        <span>
                                            <b style={{ color: "green" }}>
                                                {resultImport}
                                            </b>
                                        </span>
                                        <Button 
                                            label="OK" 
                                            icon="pi pi-times" 
                                            text 
                                            onClick={hideBatchCreatedDialog} 
                                            className="mt-3"
                                        />
                                        
                                    </div>
                                )}
                            </div>
                        </Dialog>


                         <Dialog
                                        header="Export des listes"
                                        visible={dialogVisible_}
                                        style={{ width: '520px' }}
                                        footer={dialogFooter_}
                                        onHide={() => setDialogVisible_(false)}
                                    >
                                        <div className="p-fluid">
                                            <div className="field grid">
                                                <label className="col-4 mb-0">Académies</label>
                                                <div className="col-5">
                                                    <Dropdown
                                                                                                showClear
                                                                                                id="ia"
                                                                                                name="ia"
                                                                                                options={ias}
                                                                                                optionLabel="name"
                                                                                                placeholder="Choisir une académie"
                                                                                                value={ia}
                                                                                                onChange={(e) => setIa(e.value)}
                                                                                                filter
                                                                                                className="p-inputtext-sm w-full"
                                                                                                style={{ width: '100%' }}
                                                                                                virtualScrollerOptions={{ itemSize: 40 }} // ou 30 selon le style
                                                                                            />

                                                    
                                                </div>
                                            </div>
                                            <div className="field grid">
                                                <label className="col-4 mb-0">Centres de composition</label>
                                                <div className="col-5">
                                                    <Dropdown
                                                        filter
                                                        value={centre}
                                                        
                                                        options={centres}
                                                        onChange={(e) =>
                                                            setCentre(e.value)
                                                        }
                                                        placeholder="Sélectionner"
                                                    />

                                                    
                                                </div>
                                            </div>
                                            <div className="field grid">
                                                <label className="col-4 mb-0">Niveaux</label>
                                                <div className="col-5">
                                                    <Dropdown
                                                        filter
                                                        value={level}
                                                        
                                                        options={niveaux}
                                                        onChange={(e) =>
                                                            setLevel(e.value)
                                                        }
                                                        placeholder="Sélectionner"
                                                    />

                                                    
                                                </div>
                                            </div>

                                            <div className="field grid">
                                                <label className="col-4 mb-0">Spécialités</label>
                                                <div className="col-5">
                                                    <Dropdown
                                                        showClear
                                                        value={specia}
                                                        options={disciplines}
                                                        onChange={(e) => setSpecia(e.value)}
                                                        placeholder="Liste des matières"
                                                    />
                                                </div>
                                            </div>

                                          
                                        </div>
                        </Dialog>

                    </div>
                </div>
            </div>
        </ProtectedRoute>
    );
};

export default CalendarDemo;