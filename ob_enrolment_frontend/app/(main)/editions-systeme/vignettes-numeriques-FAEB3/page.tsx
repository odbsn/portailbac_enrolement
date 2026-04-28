'use client';

import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Rating } from 'primereact/rating';
import { Toast } from 'primereact/toast';
import { Toolbar } from 'primereact/toolbar';
import React, { useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import type { Demo } from '@/types';
import { ProductService } from '@/demo/service/ProductService';
import { Dropdown } from 'primereact/dropdown';
import { Checkbox } from 'primereact/checkbox';
import { Calendar } from 'primereact/calendar';
import { Carousel } from 'primereact/carousel';
import { CandidatDecisionDTO, CandidatDTO, CandidatureService, ValidationManuelleCallBack } from '@/demo/service/CandidatureService';
import * as Yup from 'yup';
import './style.css'; // ton style custom

import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import { UserContext } from '@/app/userContext';
import { useFormik } from 'formik';
import { type } from 'os';

import { InputMask } from 'primereact/inputmask';
import { Candidat } from '@/types/candidat';
import { useFormikLocalStorageDefault } from '../../scolarite/enrolement-candidat/useFormikLocalStorageDefault';
import { RadioButton } from 'primereact/radiobutton';
import { InputTextarea } from 'primereact/inputtextarea';
import ProtectedRoute from '@/layout/ProtectedRoute';
import './style.css'
import { ParametrageService } from '@/demo/service/ParametrageService';

const ValidationCandidat = () => {
    const { user } = useContext(UserContext);

    var is_update = useRef(false); // <== valeur persistante entre les appels
    var id_cdt = useRef(null); // <== même chose pour l'ID du candidat

    //console.log(user);

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


    interface AuditLog {
        _id: string;
        natureOperation: string;
        idCandidate: string;
        operationType: string;
        fieldValues: Record<string, any>;
        login: string;
        ipAddress: string;
        timestamp: string;
    }

    const [products, setProducts] = useState(null);
    const [productDialog, setProductDialog] = useState(false);
    const [modifCandDialog, setModifCandDialog] = useState(false);
    const [deleteProductDialog, setDeleteProductDialog] = useState(false);
    const [deleteProductsDialog, setDeleteProductsDialog] = useState(false);
    const [product, setProduct] = useState(emptyProduct);
    const [selectedProducts, setSelectedProducts] = useState(null);
    const [submitted, setSubmitted] = useState(false);
    const [globalFilter, setGlobalFilter] = useState(null);
    const toast = useRef(null);
    const dt = useRef(null);
    const [series, setSeries] = useState(null);
    const [cecs, setCentreECivils] = useState(null);
    const [pays, setPays] = useState(null);
    const [etabs, setEtabs] = useState(null);

    const [matieres, setMatiereOptions] = useState([]);

    const [candidats, setCandidatData] = useState([]);
    const [candidat, setCandidat] = useState(null);
    const [rawCandidats, setRawCandidats] = useState([]);
    const [reloadTrigger, setReloadTrigger] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');

    const [deleteDialog, setDeleteDialog] = useState(false);

    const [deleteDialog_, setDeleteDialog_] = useState(false);

    const [deleteDialog_2, setDeleteDialog_2] = useState(false);

    const [serieCode, setSerieCode] = useState(null);
    const [session, setSession] = useState(null);
    const [etablissement, setEtablissement] = useState(null);
    const [rejets, setRejets] = useState(null);

    const [prog, setOneProg] = useState<{ edition?: number } | null>(null);

    const [decisionStats, setDecisionStats] = useState({ decision0: 0, decision1: 0});

    const [filterText, setFilterText] = useState('');

    const [selectedValues, setSelectedValues] = useState<string[]>([]);

    const [logs, setLogs] = useState<AuditLog[]>([]);

    const [situ, setSitu] = useState(null);

    const [annees, setAnnees] = useState(null);
    const [edition, setEdition] = useState(null);

    const situClaire = [
        { label: 'Isolés', value: 'isolate' },
        { label: 'Non Isolés', value: 'not_isolate' },
    ];

    useEffect(() => {
        CandidatureService.getProgs().then((response) => {
            //console.log("📦 Séries chargées :", data);
            setAnnees(response);
        });
    }, []);

    const formatTrace = (fieldValues: Record<string, any>) => {
        const modified = fieldValues?.modified || fieldValues;

        return Object.entries(modified || {})
            .map(([key, value]) => {
                if (value && typeof value === 'object') {
                    // Vérifie si les valeurs sont des objets et les stringify
                    const ancienne = typeof value['ancienne-donnée'] === 'object' ? JSON.stringify(value['ancienne-donnée']) : value['ancienne-donnée'] ?? '';
                    const nouvelle = typeof value['nouvelle-donnée'] === 'object' ? JSON.stringify(value['nouvelle-donnée']) : value['nouvelle-donnée'] ?? '';

                    return `<b><span style="color:blue;">${key}</span></b><br/>
            <span style="color:red;">ancienne-donnée : ${ancienne}</span><br/>
            <span style="color:green;">nouvelle-donnée : ${nouvelle}</span>`;
                }
                return `${key}: ${String(value)}`;
            })

            .join('<br/>');
    };

    useEffect(() => {
        if (rejets && Array.isArray(rejets)) {
            setSelectedValues(rejets.map((r) => r.name));
        }
    }, [rejets]);

    useEffect(() => {
        ProductService.getProducts().then((data) => setProducts(data));
    }, []);

    useEffect(() => {
        CandidatureService.getSeries().then((response) => {
            //console.log("📦 Séries chargées :", data);
            setSeries(response);
        });
    }, []);

    useEffect(() => {
        CandidatureService.getRejets().then((response) => {
            console.log('📦 Rejets chargés :', response);
            setRejets(response);
        });
    }, []);

    // Si tu as déjà rejets, pas besoin de redéclarer ici
    const filteredRejets = rejets?.filter((r) => r.name.toLowerCase().includes(filterText.toLowerCase()));

    useEffect(() => {
        CandidatureService.getCentreEtatCivils().then((response) => {
            //console.log("📦 Séries chargées :", data);
            setCentreECivils(response);
        });
    }, []);

    useEffect(() => {
        CandidatureService.getPays().then((response) => {
            //console.log("📦 Séries chargées :", data);
            setPays(response);
        });
    }, []);

    useEffect(() => {
        CandidatureService.getMatiereOptions().then((response) => {
            //console.log("📦 Séries chargées :", data);
            setMatiereOptions(response);
        });
    }, []);

    useEffect(() => {
        CandidatureService.getEtablissements().then((response) => {
            //console.log("📦 Séries chargées :", data);
            setEtabs(response);
        });
    }, []);

    // useEffect(() => {
    //     CandidatureService.getProg().then((response) => {
    //         //console.log("📦 Séries chargées :", data);
    //         setProgs(response);
    //     });
    // }, []);

    useEffect(() => {
        CandidatureService.getLastProg().then((response) => {
            //console.log("📦 Séries chargées :", data);
            setOneProg(response);
        });
    }, []);

    const loadCandidats = useCallback(async () => {
            setLoading(true);
            setError(null);
            try
            {
                    if (edition)
                    {
                        console.log(edition)
                        const response = await ParametrageService.getAllDroitsInscription(edition?.edition);
                        setRawCandidats(response || []);
                        console.log(rawCandidats)
                    }

                    if (edition && etablissement)
                    {
                        console.log(edition)
                        const response = await CandidatureService.getDIByEtab(etablissement?.code, edition?.edition);
                        setRawCandidats(response || []);
                        console.log(rawCandidats)
                    }
            } 
            catch (err) 
            {
                console.error(err);
                setError("Erreur lors du chargement");
            } 
            finally 
            {
                setLoading(false);
            }
        }, [edition, etablissement]);
    
        useEffect(() => {
            loadCandidats(); 
        }, [loadCandidats]);
    
        const filteredCandidats = useMemo(() => {
        if (!rawCandidats || rawCandidats.length === 0) return [];
            const code = typeof serieCode === 'string' ? serieCode : serieCode?.code;
            return code ? rawCandidats.filter(c => c.serie?.code === code) : rawCandidats;
        }, [rawCandidats, serieCode]);
    
        useEffect(() => {
            setCandidatData(filteredCandidats);
    
            const decisionCounts = {
                decision0: filteredCandidats.filter(c => c.paid === false).length,
                decision1: filteredCandidats.filter(c => c.paid === true).length,
            };
            setDecisionStats(decisionCounts);
        }, [filteredCandidats]);

    const origineOptions = [
        { label: 'National', value: 'National' },
        { label: 'Etranger', value: 'Etranger' }
    ];

    const sexeOptions = [
        { label: 'M', value: 'M' },
        { label: 'F', value: 'F' }
    ];

    const handicapOptions = [
        { label: 'Aucun', value: 'Néant' },
        { label: 'Aveugle', value: 'Aveugle' },
        { label: 'Mal Entendant', value: 'Mal Entendant' },
        { label: 'Mal Voyant', value: 'Mal Voyant' },
        { label: 'Sourd-Muet', value: 'Sourd-Muet' },
        { label: 'Moteur', value: 'Moteur' }
    ];

    const epsOptions = [
        { label: 'Apte', value: 'Apte' },
        { label: 'Inapte', value: 'Inapte' }
    ];

    const efOptions = [
        { label: 'Dessin', value: 'Dessin' },
        { label: 'Couture', value: 'Couture' },
        { label: 'Musique', value: 'Musique' }
    ];

    const decisions = [
        { label: 'Dossier Accepté', value: 1 },
        { label: 'Dossier Rejeté', value: 2 }
    ];

    const getColor = (value) => {
        if (value === 1) return 'SpringGreen';
        if (value === 2) return 'Salmon';
        return 'white';
    };

    const countryOptionTemplate = (country) => {
        return (
            <div className="country-item">
                <img alt={country.code} src={`https://flagcdn.com/16x12/${country.code.toLowerCase()}.png`} className="mr-2" />
                <span>{country.name}</span>
            </div>
        );
    };

    const getAvailableOptions = (code, order, exclude1, exclude2, exclude3) =>
        matieres.filter((m) => {
            return m.order == order && m.serie.code === code && m.name !== exclude1 && m.name !== exclude2 && m.name !== exclude3;
        });

    const getAvailableOptions2 = (code: string | undefined, order: number, exclude1?: string, exclude2?: string, exclude3?: string) => {
        if (!code) {
            return [];
        }

        console.log('Etape 1');

        const allForSerie = matieres.filter((m) => m.serie.code === code);

        // Séries pour lesquelles on doit "vider" les exclusions
        const seriesSansExclusion = ['F6', 'L-AR', 'S1', 'S2', 'S3', 'S4', 'S5', 'T1', 'T2', 'STIDD', 'STEG', 'S1AR', 'S2AR'];

        const isSerieSansExclusion = seriesSansExclusion.includes(code);

        console.log('Etape 2');

        console.log(isSerieSansExclusion);

        if (isSerieSansExclusion) {
            const allForSerieX = matieres.filter((m) => m.serie.code !== code);
            console.log('✅ Contenu de allForSerie pour une série sans exclusion :', allForSerie);
            const uniqueNames = new Set<string>();
            return allForSerieX.filter((m) => {
                const isExcluded = m.matiere === 'SN';
                const isExcluded2 = m.name === 'ECONOMIE';
                if (!isExcluded && !isExcluded2 && !uniqueNames.has(m.name)) {
                    uniqueNames.add(m.name);
                    return true;
                }
                return false;
            });
        }

        console.log('Etape 3');

        // Si aucune matière encore choisie (toutes exclusions vides)
        const noExclusionSelected = !exclude1 && !exclude2 && !exclude3;

        console.log('TEK KHEL', exclude1, exclude2, exclude3);
        console.log(noExclusionSelected);

        if (noExclusionSelected) {
            // Aucun filtre d'exclusion → toutes les matières sans doublon
            const uniqueNames = new Set<string>();
            return allForSerie.filter((m) => {
                const isExcluded = m.matiere === 'SN';
                const isExcluded2 = m.name === 'ECONOMIE';
                if (!isExcluded && !isExcluded2 && !uniqueNames.has(m.name)) {
                    uniqueNames.add(m.name);
                    return true;
                }
                return false;
            });
        } else {
            // Cas normal : filtre par ordre + exclusions, puis suppression des doublons sur le name
            const uniqueNames = new Set<string>();
            return allForSerie.filter((m) => {
                const isExcluded = m.matiere === 'SN' || m.name === 'ECONOMIE' || m.name === exclude1 || m.name === exclude2 || m.name === exclude3;

                if (!isExcluded && !uniqueNames.has(m.name)) {
                    uniqueNames.add(m.name);
                    return true;
                }
                return false;
            });
        }
    };

    const formatCurrency = (value) => {
        return value.toLocaleString('en-US', {
            style: 'currency',
            currency: 'USD'
        });
    };

    const openNew = () => {
        setModifCandDialog(true); // ou ton Dialog / Carousel
        formik.resetForm(); // remet à zéro les erreurs et touches
        formik.setValues(Object); // valeurs initiales
        setSubmitted(false);
        //setIsEditMode(false); // facultatif : flag pour différencier "ajouter" / "modifier"
    };

    const hideDialog = () => {
        if (productDialog) {
            setSubmitted(false);
            setProductDialog(false);
        }
        if (modifCandDialog) {
            setSubmitted(false);
            setModifCandDialog(false);
        }
    };

    const hideDeleteProductDialog = () => {
        setDeleteProductDialog(false);
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

    const formatDateToInput = (isoDateStr) => {
        const date = new Date(isoDateStr);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    };

    const formatMatiere = (matiere) => {
        if (!matiere) return null;
        // Si c’est déjà un objet complet avec un id, on le retourne tel quel
        if (typeof matiere === 'object' && matiere.id && matiere.name) return matiere;
        // Sinon on cherche par nom
        return matieres.find((m) => m.name === matiere) || null;
    };

    const editProduct = async (candidat) => {
        setModifCandDialog(true);
        // setCandidat({ ...candidat });

        const candidatFormatted = {
            ...candidat,
            date_birth: candidat.date_birth ? formatDateToInput(candidat.date_birth) : '',
            matiere1: formatMatiere(candidat.matiere1),
            matiere2: formatMatiere(candidat.matiere2),
            matiere3: formatMatiere(candidat.matiere3),
            matiere4: formatMatiere(candidat.eprFacListB),
            codeCentre: candidat.centreEtatCivil?.code || ''
        };
        (id_cdt.current = candidat.id), console.log(id_cdt);
        formik.setValues(candidatFormatted);
        is_update.current = true;
        console.log(is_update);

        const response = await CandidatureService.getAuditReceptionDossier(id_cdt.current);
        setLogs(response); // ici tu affectes la vraie valeur

        // récupérer les rejets
        const rejets = candidat.rejets
            ? candidat.rejets.map((r) => ({
                  id: r.id,
                  name: r.name,
                  observation: r.observation
              }))
            : [];

        // initialiser les checkboxes avec les noms
        setSelectedValues(rejets.map((r) => r.name));
    };

    const editProduct2 = (candidat) => {
        setDeleteDialog(true);
        setCandidat({ ...candidat });
        id_cdt.current = candidat.id;
    };

    const editProduct3 = (candidat) => {
        setDeleteDialog_(true);
        setCandidat({ ...candidat });
        id_cdt.current = candidat.id;
    };

    const editProduct4 = (candidat) => {
        setDeleteDialog_2(true);
        setCandidat({ ...candidat });
        id_cdt.current = candidat.id;
    };

    const hideDeleteProductDialog_ = () => {
        setDeleteDialog(false);
    };

    const hideDeleteProductDialog_2 = () => {
        setDeleteDialog_(false);
    };

    const hideDeleteProductDialog_2_ = () => {
        setDeleteDialog_2(false);
    };

    const deleteData = async () => {
            console.log(id_cdt.current);
    
            if (id_cdt.current) {
                try {
                await CandidatureService.isolatedDossier(id_cdt.current, user?.login);
                toast.current.show({
                    severity: 'success',
                    summary: 'Office du Bac',
                    detail: 'Le dossier de candidature a été isolé avec succès',
                    life: 5000
                });
                await loadCandidats();
                } 
                catch (error) 
                {
                toast.current.show({
                    severity: 'error',
                    summary: 'Erreur',
                    detail: 'Impossible de supprimer le dossier',
                    life: 5000
                });
                console.error("Erreur suppression:", error);
                }
    
            }
    
            setDeleteDialog(false);
    };

    const deleteData_ = async () => {
            console.log(id_cdt.current);
    
            if (id_cdt.current) {
                try {
                await CandidatureService.deleteIsolateCandidat(id_cdt.current, user?.login);
                toast.current.show({
                    severity: 'success',
                    summary: 'Office du Bac',
                    detail: 'Le dossier de candidature a été supprimé avec succès',
                    life: 5000
                });
                await loadCandidats();
                } 
                catch (error) 
                {
                toast.current.show({
                    severity: 'error',
                    summary: 'Erreur',
                    detail: 'Impossible de supprimer le dossier',
                    life: 5000
                });
                console.error("Erreur suppression:", error);
                }
    
            }
    
            setDeleteDialog_(false);
    };

    const deleteData_2 = async () => {
            console.log(id_cdt.current);
    
            if (id_cdt.current) {
                try {
                await CandidatureService.reintegratedDossier(id_cdt.current, user?.login);
                toast.current.show({
                    severity: 'success',
                    summary: 'Office du Bac',
                    detail: 'Le dossier de candidature a été réintégré avec succès',
                    life: 5000
                });
                await loadCandidats();
                } 
                catch (error) 
                {
                toast.current.show({
                    severity: 'error',
                    summary: 'Erreur',
                    detail: 'Impossible de supprimer le dossier',
                    life: 5000
                });
                console.error("Erreur suppression:", error);
                }
    
            }
    
            setDeleteDialog_2(false);
    };

    const deleteDialogFooter = (
            <>
                <Button label="Oui" icon="pi pi-check" text onClick={deleteData} />
                <Button label="Non" icon="pi pi-times" text onClick={hideDeleteProductDialog_} />
            </>
    );

    const deleteDialogFooter_ = (
            <>
                <Button label="Oui" icon="pi pi-check" text onClick={deleteData_} />
                <Button label="Non" icon="pi pi-times" text onClick={hideDeleteProductDialog_2} />
            </>
    );

    const deleteDialogFooter_2 = (
            <>
                <Button label="Oui" icon="pi pi-check" text onClick={deleteData_2} />
                <Button label="Non" icon="pi pi-times" text onClick={hideDeleteProductDialog_2_} />
            </>
    );

    const confirmDeleteProduct = (product) => {
        setProduct(product);
        setDeleteProductDialog(true);
    };

    const deleteProduct = () => {
        let _products = products.filter((val) => val.id !== product.id);
        setProducts(_products);
        setDeleteProductDialog(false);
        setProduct(emptyProduct);
        toast.current.show({
            severity: 'success',
            summary: 'Successful',
            detail: 'Product Deleted',
            life: 3000
        });
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
            <React.Fragment>
                <div className="formgrid grid">
                    <div className="field col-12">
                        <fieldset className="p-2 border-round-md surface-border text-sm" style={{ padding: '16px' }}>
                            <legend className="text-primary font-bold text-sm">Effectuer les filtres (Tous les champs doivent être sélectionnés)</legend>

                            <div
                                className="filter-container"
                                style={{
                                    display: 'flex',
                                    gap: '10px', // espace entre les deux champs
                                    alignItems: 'flex-start',
                                    flexWrap: 'wrap'
                                }}
                            >
                                <div style={{ width: '150px' }}>
                                    <label htmlFor="serieCode">
                                        <h6 className="m-0">Edition du BAC</h6>
                                    </label>
                                    <Dropdown 
                                                    showClear 
                                                    id="edition" 
                                                    name="edition" 
                                                    options={annees} 
                                                    value={edition} 
                                                    onChange={(e) => setEdition(e.value)} 
                                                    optionLabel="edition" 
                                                    placeholder="Choisir une édition du bac" 
                                                    filter 
                                                    className="p-inputtext-sm w-full"
                                                    style={{ width: '100%', color: 'black', fontWeight: 'bold' }} /> 
                                </div>

                                <div style={{ width: '350px' }}>
                                    <label htmlFor="etablissement">
                                        <h6 className="m-0">Choisir un établissement</h6>
                                    </label>
                                    <Dropdown
                                        showClear
                                        id="etablissement"
                                        name="etablissement"
                                        options={etabs}
                                        optionLabel="code"
                                        placeholder="Choisir un établissement"
                                        value={etablissement}
                                        onChange={(e) => setEtablissement(e.value)}
                                        filter
                                        className="p-inputtext-sm w-full"
                                        style={{ width: '100%' }}
                                        virtualScrollerOptions={{ itemSize: 40 }} // ou 30 selon le style
                                    />
                                </div>

                                

                                
                            </div>
                        </fieldset>
                    </div>
                </div>
            </React.Fragment>
        );
    };

    const rightToolbarTemplate = () => {
        return (
            <React.Fragment>
                {/* <Button severity="info" label="Importer la liste" icon="pi pi-arrow-down-right" className="mr-2 inline-block"/>
                <Button severity="help" label="Exporter la liste" icon="pi pi-upload" onClick={exportCSV} /> */}
                {/* <DownloadPDFButton etablissementId={etablissement?.id} etablissementName={etablissement?.name} session={prog?.edition} /> */}
            </React.Fragment>
        );
    };

    interface DownloadPDFButtonProps {
        etablissementId: String;
        etablissementName: String;
        session: number;
    }

    // const DownloadPDFButton: React.FC<DownloadPDFButtonProps> = ({ etablissementId, session, etablissementName }) => {
    //     const [loading, setLoading] = useState(false);

    //     // ✅ Si aucun établissement n'est sélectionné, ne rien afficher
    //     if (!etablissementId) return null;

    //     const handleDownload = async () => {
    //         setLoading(true);
    //         try {
    //             await CandidatureService.getListByEtab(etablissementId, session, etablissementName, user?.login);
    //         } catch (error) {
    //             console.error('Erreur lors du téléchargement du fichier PDF.', error);
    //         } finally {
    //             setLoading(false);
    //         }
    //     };

    //     return <Button label={loading ? 'Téléchargement...' : "Télécharger la liste compléte de l'établissement"} icon="pi pi-download" onClick={handleDownload} disabled={loading} className="p-button-primary" />;
    // };

    const oNBodyTemplate = (rowData) => {
        return (
            <>
                <span className="p-column-title">orderNumber</span>
                {rowData.orderNumber}
            </>
        );
    };

    const pNBodyTemplate = (rowData) => {
        return (
            <>
                <span className="p-column-title">phoneNumber</span>
                {rowData.phoneNumber}
            </>
        );
    };

    const mVBodyTemplate = (rowData) => {
        return (
            <>
                <span className="p-column-title">montantVerser</span>
                {rowData.montantVerser}
            </>
        );
    };

    const vBodyTemplate = (rowData) => {
        return (
            <>
                <span className="p-column-title">nbCdtsInscrits</span>
                {rowData.nbCdtsInscrits}
            </>
        );
    };

    const etabBodyTemplate = (rowData) => {
        return (
            <>
                <span className="p-column-title">establishment</span>
                {rowData.establishment}
            </>
        );
    };

    const dateBodyTemplate = (rowData) => {
        return (
            <>
                <span className="p-column-title">dateTransaction</span>
                {rowData.dateTransaction}
            </>
        );
    };

    const paidBodyTemplate = (rowData) => {
        return (
            <>
                <span className="p-column-title">paid</span>
            <span
                className={`font-semibold ${
                rowData.paid ? "text-green-600" : "text-red-600"
                }`}
            >
                <b>{rowData.paid ? "✅ VALIDÉ" : "❌ INVALIDÉ"}</b>
            </span>

            </>
        );
    };

    
    const header = (
        <div className="flex flex-column md:flex-row md:justify-content-between md:align-items-center">
            <div className="flex flex-wrap gap-3">
                {candidats && candidats.length > 0 && (
                    <div className="flex flex-wrap gap-3 mb-3">
                        <div className="bg-cyan-100 text-black-800 px-2 py-2 rounded-xl">
                            <strong>Etat des vignettes numériques 1000 F (FAEB3) BAC {edition?.edition}</strong>
                        </div>
                        <div className="bg-green-100 text-black-800 px-2 py-2 rounded-xl">
                            ✅ Validés :{' '}
                            <strong>
                                {decisionStats.decision1} sur {decisionStats.decision0 + decisionStats.decision1}
                            </strong>
                        </div>
                        <div className="bg-red-100 text-black-800 px-2 py-2 rounded-xl">
                            ❌ Invalidés :{' '}
                            <strong>
                                {decisionStats.decision0} sur {decisionStats.decision0 + decisionStats.decision1}
                            </strong>
                        </div>
                        {user?.profil?.name !== "FINANCE_COMPTA" && (
                            <Button icon="pi pi-plus" rounded severity="help" className="mr-1" onClick={() => openNew()} />
                        )}
                    </div>
                )}
            </div>
            <span className="block mt-2 md:mt-0 p-input-icon-left">
                <i className="pi pi-search" />
                <InputText type="search" onInput={(e) => setGlobalFilter((e.target as HTMLInputElement).value)} placeholder="Recherche..." />
            </span>
        </div>
    );

    const productDialogFooter = (
        <>
            <Button label="Valider" icon="pi pi-check" text onClick={saveProduct} />
            <Button label="Fermer" icon="pi pi-times" text onClick={hideDialog} />
        </>
    );
    const deleteProductDialogFooter = (
        <>
            <Button label="No" icon="pi pi-times" text onClick={hideDeleteProductDialog} />
            <Button label="Yes" icon="pi pi-check" text onClick={deleteProduct} />
        </>
    );
    const deleteProductsDialogFooter = (
        <>
            <Button label="No" icon="pi pi-times" text onClick={hideDeleteProductsDialog} />
            <Button label="Yes" icon="pi pi-check" text onClick={deleteSelectedProducts} />
        </>
    );

    function dateTextToTimestamp(dateStr: string): string {
        const date = new Date(dateStr);

        if (isNaN(date.getTime())) {
            throw new Error("Date invalide : " + dateStr);
        }

        return date.getTime().toString();
    }


    const formik = useFormik({
        initialValues: {
            payment_mode: '',
            paid_sum: '',
            payment_token: '',
            command_number: '',
            payment_validation_date: ''
        },

        validationSchema: Yup.object({
            payment_mode: Yup.string().required('Champ obligatoire'),
            paid_sum: Yup.string().required('Champ obligatoire'),
            payment_token: Yup.string().nullable().required('Champ obligatoire'),
            command_number: Yup.string().required('Champ obligatoire'),
            payment_validation_date: Yup.string().required('Champ obligatoire'),
           
        }),

        onSubmit: async (values, { setSubmitting, resetForm }) => {
            const callbackDTO: ValidationManuelleCallBack = {
                payment_mode: values.payment_mode,
                paid_sum: values.paid_sum,
                payment_token: values.payment_token,
                command_number: values.command_number,
                payment_validation_date: dateTextToTimestamp(values.payment_validation_date),
                payment_status: '200'
            };

            try {
                console.log('PATCH');
                const response = await ParametrageService.updateCallBack(callbackDTO);
                setMessage('Candidat créé avec succès');
                toast.current.show({ severity: 'success', summary: 'Office du Bac', detail: 'Callback validé avec succès', life: 4000 });
                //resetForm();
                await loadCandidats();
            } catch (error) {
                            toast.current.show({ 
                                severity: 'error', 
                                summary: 'Office du Bac', 
                                detail: 'Erreur de validation, veuillez revoir le numéro de transaction', 
                                life: 4000 
                            });
            } finally {
                setSubmitting(false);
            }
            setSubmitted(false);
            setProductDialog(false);
            setModifCandDialog(false);
        }
    });

    const carouselItems2 = [
        <div key="step1">
            <h5 className="text-primary">Données de la transaction du BackOffice</h5>

            <div className="formgrid grid">
                <div className="field col-12">
                    
                        <div className="formgrid grid">
                            <div className="field col-2">
                                <label htmlFor="price">
                                    <b><span className="text-red-600">*</span> Mode de paiement</b>
                                </label>
                                <InputText style={{
                                    fontWeight: 'bold',
                                    color: "black"
                                }} value={formik.values.payment_mode || ''} onChange={formik.handleChange}
                            onBlur={formik.handleBlur} id="payment_mode"
                            autoComplete="off" 
                            name="payment_mode" className="p-inputtext-sm" />
                            {formik.touched.payment_mode && typeof formik.errors.payment_mode === 'string' && <small className="p-error">{formik.errors.payment_mode}</small>}
                            </div>
                            <div className="field col-2">
                                <label htmlFor="price">
                                    <b><span className="text-red-600">*</span> Somme versée</b>
                                </label>
                                <InputText style={{
                                    fontWeight: 'bold',
                                    color: "black"
                                }} value={formik.values.paid_sum || ''} onChange={formik.handleChange}
                            onBlur={formik.handleBlur} id="paid_sum"
                            autoComplete="off" 
                            name="paid_sum" className="p-inputtext-sm" />
                            {formik.touched.paid_sum && typeof formik.errors.paid_sum === 'string' && <small className="p-error">{formik.errors.paid_sum}</small>}
                            </div>
                            <div className="field col-2">
                                <label htmlFor="price">
                                    <b><span className="text-red-600">*</span> Token</b>
                                </label>
                                <InputText style={{
                                    fontWeight: 'bold',
                                    color: "black"
                                }} value={formik.values.payment_token || ''} onChange={formik.handleChange}
                            onBlur={formik.handleBlur} id="payment_token"
                            autoComplete="off" 
                            name="payment_token" className="p-inputtext-sm" />
                            {formik.touched.payment_token && typeof formik.errors.payment_token === 'string' && <small className="p-error">{formik.errors.payment_token}</small>}
                            </div>
                            <div className="field col-3">
                                <label htmlFor="price">
                                    <b><span className="text-red-600">*</span> Date</b>
                                </label>
                                <InputText style={{
                                    fontWeight: 'bold',
                                    color: "black"
                                }} value={formik.values.payment_validation_date || ''} onChange={formik.handleChange}
                            onBlur={formik.handleBlur} id="payment_validation_date"
                            autoComplete="off" 
                            name="payment_validation_date" className="p-inputtext-sm" />
                            {formik.touched.payment_validation_date && typeof formik.errors.payment_validation_date === 'string' && <small className="p-error">{formik.errors.payment_validation_date}</small>}
                            </div>
                            <div className="field col-3">
                                <label htmlFor="price">
                                    <b><span className="text-red-600">*</span> Numéro transaction</b>
                                </label>
                                <InputText style={{
                                    fontWeight: 'bold',
                                    color: "black"
                                }} value={formik.values.command_number || ''} id="command_number"
                            autoComplete="off" 
                            name="command_number" onChange={formik.handleChange}
                            onBlur={formik.handleBlur} className="p-inputtext-sm" />
                            {formik.touched.command_number && typeof formik.errors.command_number === 'string' && <small className="p-error">{formik.errors.command_number}</small>}
                            </div>
                            
                        </div>

                        <div className="formgrid grid">
                            <div className="field col-9">
                                
                            </div>
                            <div className="field col-3">
                                <Button label={'Enregistrer les informations'} severity="success" className="mt-4" type="submit" />
                            </div>
                        </div>

                </div>
            </div>

            
        </div>,
    ];
    const carouselResponsiveOptions = [
        {
            breakpoint: '1024px',
            numVisible: 3,
            numScroll: 3
        },
        {
            breakpoint: '768px',
            numVisible: 2,
            numScroll: 2
        },
        {
            breakpoint: '560px',
            numVisible: 1,
            numScroll: 1
        }
    ];

   

    return (
        <ProtectedRoute allowedRoles={['ADMIN', 'SCOLARITE', 'FINANCE_COMPTA']}>
            <div className="grid crud-demo">
                <div className="col-12">
                    <div className="card">
                        <Toast ref={toast} />
                        <Toolbar className="mb-4" left={leftToolbarTemplate} right={rightToolbarTemplate}></Toolbar>

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
                            <DataTable
                                ref={dt}
                                value={candidats}
                                paginator
                                rows={10}
                                rowsPerPageOptions={[5, 10, 25]}
                                className="datatable-responsive"
                                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                                currentPageReportTemplate="Affichage de {first} à {last} des {totalRecords} enregistrement (s)"
                                globalFilter={globalFilter}
                                emptyMessage="Aucun dossier n'a été retrouvée"
                                header={header}
                                rowClassName={(rowData) => {
                                    if (rowData.decision === 1) return 'accepted-row';
                                    if (rowData.decision === 2) return 'rejected-row';
                                    return '';
                                }}
                            >
                                <Column field="orderNumber" header="N° transaction" sortable body={oNBodyTemplate} headerStyle={{ minWidth: '10rem' }} />
                                <Column field="phoneNumber" header="Téléphone" body={pNBodyTemplate} headerStyle={{ minWidth: '5rem' }} />
                                <Column field="montantVerser" header="Somme" sortable body={mVBodyTemplate} headerStyle={{ minWidth: '5rem' }} />
                                <Column
                                    field="dateTransaction"
                                    header="Date"
                                    sortable
                                    headerStyle={{ minWidth: '5rem' }}
                                    body={(rowData) => {
                                        const date = new Date(rowData.dateTransaction);
                                        const day = String(date.getDate()).padStart(2, '0');
                                        const month = String(date.getMonth() + 1).padStart(2, '0');
                                        const year = date.getFullYear();
                                        return `${day}/${month}/${year}`;
                                    }}
                                />
                                <Column field="nbCdtsInscrits" header="Vignettes" body={vBodyTemplate} sortable headerStyle={{ minWidth: '5rem' }} />
                                <Column field="establishment" header="Etab." body={etabBodyTemplate} sortable headerStyle={{ minWidth: '5rem' }} />
                                <Column field="paid" header="Etat" body={paidBodyTemplate} sortable headerStyle={{ minWidth: '5rem' }} />
                            </DataTable>

                            <Dialog visible={modifCandDialog} style={{ width: '1250px' }} header="Validation manuelle du callback de paiement" modal className="p-fluid" onHide={hideDialog}>
                                {/* {product.image && <img src={`/demo/images/product/${product.image}`} alt={product.image} width="150" className="mt-0 mx-auto mb-5 block shadow-2" />} */}

                                <form
                                    onSubmit={(e) => {
                                        e.preventDefault();
                                        console.log('Formik errors:', formik.errors);
                                        formik.handleSubmit(e);
                                    }}
                                >
                                    <Carousel
                                        value={carouselItems2}
                                        itemTemplate={(item) => <div>{item}</div>}
                                        numVisible={1}
                                        numScroll={1}
                                        showNavigators={false}
                                        showIndicators={false}
                                        responsiveOptions={carouselResponsiveOptions}
                                        className="custom-carousel"
                                    />
                                </form>
                            </Dialog>

                            <Dialog visible={deleteDialog} style={{ width: '500px' }} header="Avertissement pour isolement de dossier" modal footer={deleteDialogFooter} onHide={hideDeleteProductDialog_}>
                                                        <div className="flex align-items-center justify-content-center">
                                                            <i className="pi pi-exclamation-triangle mr-3" style={{ fontSize: '2rem' }} />
                                                            <span>Attention vous vous appretez à isoler le dossier du candidat <b>{candidat?.dosNumber}</b></span><br />
                                                            <span style={{ color: 'red' }}>NB : Une fois le dossier isolé, il ne sera plus visible dans son établissement.</span>
                                                        </div>
                            </Dialog>

                            <Dialog visible={deleteDialog_} style={{ width: '500px' }} header="Avertissement pour suppression de dossier" modal footer={deleteDialogFooter_} onHide={hideDeleteProductDialog_2}>
                                                        <div className="flex align-items-center justify-content-center">
                                                            <i className="pi pi-exclamation-triangle mr-3" style={{ fontSize: '2rem' }} />
                                                            <span>Attention vous vous appretez à supprimer définitement le dossier du candidat <b>{candidat?.dosNumber}</b></span><br />
                                                            
                                                        </div>
                            </Dialog>

                            <Dialog visible={deleteDialog_2} style={{ width: '500px' }} header="Avertissement pour la réintégration de dossier" modal footer={deleteDialogFooter_2} onHide={hideDeleteProductDialog_2_}>
                                                        <div className="flex align-items-center justify-content-center">
                                                            <i className="pi pi-exclamation-triangle mr-3" style={{ fontSize: '2rem' }} />
                                                            <span>Attention vous vous appretez à réintégrer le dossier <b>{candidat?.dosNumber}</b> du candidat dans son établissement</span><br />
                                                            
                                                        </div>
                            </Dialog>

                            <Dialog visible={deleteProductDialog} style={{ width: '450px' }} header="Confirm" modal footer={deleteProductDialogFooter} onHide={hideDeleteProductDialog}>
                                <div className="flex align-items-center justify-content-center">
                                    <i className="pi pi-exclamation-triangle mr-3" style={{ fontSize: '2rem' }} />
                                    {product && (
                                        <span>
                                            Are you sure you want to delete <b>{product.name}</b>?
                                        </span>
                                    )}
                                </div>
                            </Dialog>

                            <Dialog visible={deleteProductsDialog} style={{ width: '450px' }} header="Confirm" modal footer={deleteProductsDialogFooter} onHide={hideDeleteProductsDialog}>
                                <div className="flex align-items-center justify-content-center">
                                    <i className="pi pi-exclamation-triangle mr-3" style={{ fontSize: '2rem' }} />
                                    {product && <span>Are you sure you want to delete the selected products?</span>}
                                </div>
                            </Dialog>
                        </div>
                    </div>
                </div>
            </div>
        </ProtectedRoute>
    );
};

export default ValidationCandidat;
