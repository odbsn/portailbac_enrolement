'use client';

import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Rating } from 'primereact/rating';
import { Toast } from 'primereact/toast';
import { Toolbar } from 'primereact/toolbar';
import React, { useContext, useEffect, useRef, useState } from 'react';
import type { Demo } from '@/types';
import { ProductService } from '@/demo/service/ProductService';
import { Dropdown } from 'primereact/dropdown';
import { Checkbox } from 'primereact/checkbox';
import { Calendar } from 'primereact/calendar';
import { Carousel } from 'primereact/carousel';
import { BaseMorteDTO_, ParametrageService, ProgrammationDTO, SujetDTO } from '@/demo/service/ParametrageService';
import { AutorisationReception, CandidatDecisionDTO, CandidatDTO, CandidatureService, VignetteAddDTO } from '@/demo/service/CandidatureService';
import * as Yup from 'yup';

import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import { InputTextarea } from 'primereact/inputtextarea';
import { useFormik } from 'formik';
import { UserContext } from '@/app/userContext';
import ProtectedRoute from '@/layout/ProtectedRoute';
import { idText } from 'typescript';
import { FiEdit } from 'react-icons/fi';
import { InputNumber } from 'primereact/inputnumber';
import { saveAs } from 'file-saver';
import { ProgressSpinner } from 'primereact/progressspinner';

const CalendarDemo = () => {
    var is_update = useRef(false); // <== valeur persistante entre les appels
    var id_cdt = useRef(null); // <== même chose pour l'ID du candidat

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
    const [deleteProductDialog, setDeleteProductDialog] = useState(false);
    const [deleteProductsDialog, setDeleteProductsDialog] = useState(false);
    const [product, setProduct] = useState(emptyProduct);
    const [selectedProducts, setSelectedProducts] = useState(null);
    const [submitted, setSubmitted] = useState(false);
    const [globalFilter, setGlobalFilter] = useState(null);
    const [annees, setAnnees] = useState(null);
    const [edition, setEdition] = useState(null);
    const toast = useRef(null);
    const dt = useRef(null);
    const [deleteDialog, setDeleteDialog] = useState(false);
    const [code, setCode] = useState("");
    const [candidat, setCandidat] = useState(null);
    const [ia, setIa] = useState(null);
    const [exporting, setExporting] = useState(false);
    const [exportStep, setExportStep] = useState('');
    const [seconds, setSeconds] = useState(0);
    const timerRef = useRef<NodeJS.Timeout | null>(null);
    const [listeSerie, setListeSerie] = useState([]);
    const [groupedCdts, setGroupedCdts] = useState([]);
    const [selectedSerie, setSelectedSerie] = useState(null);
    const [selectedSexe, setSelectedSexe] = useState(null);
    const [selectedCdt, setSelectedCdt] = useState(null);

    const [series, setSeries] = useState(null);

    const [specialites, setSpecialites] = useState(null);

    const [specialite, setSpecialite] = useState('');
    const [wording, setWording] = useState('');
    const [num_sujet, setNumsujet] = useState(0);
    const [etab_id, setEtab] = useState('');
    const [spec_id, setSpec] = useState('');

    const [message, setMessage] = useState('');

    const [sujets, setSujetData] = useState([]);

    const [printDialog, setPrintDialog] = useState(false);

    const [archives, setArchives] = useState([]);
    const [totalRecords, setTotalRecords] = useState(0);
    const [lazyParams, setLazyParams] = useState({
        first: 0,
        rows: 10,
        page: 0,
        iaCode: "",
        session : 0
    });

    const mapCode = {
        Dessin: "DE",
        Couture: "CO",
        Musique : "MU"
    };

    const handCode = 
    {
        "Aveugle": "AVGL",
        "Mal Entendant" : "MLTD",
        "Mal Voyant" : "MVYT",
        "Sourd-Muet" : "SRMT",
        "Moteur" : "IFRM",
        "Autre" : "AUT",
    };

    const matieresNameToCode = {
        "ALLEMAND": "AL",
        "ANGLAIS": "AN",
        "ARABE MODERNE": "ARAM",
        "ESPAGNOL": "ES",
        "PORTUGAIS": "PO",
        "ECONOMIE": "ECO",
        "RUSSE": "RU",
        "ITALIEN": "IT",
        "ARABE CLASSIQUE": "ARAC",
        "LATIN": "LA",
        "SCIENCES PHYSIQUES": "SCPH",
        "SCIENCES DE LA VIE ET DE LA TERRE": "SVT",
        "FRANCAIS": "FR",
        "GENIE ELECTRIQUE": "GELEC",
        "GENIE MECANIQUE": "GMECA"
    };


    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [ias, setIas] = useState(null);

    useEffect(() => {
        ProductService.getProducts().then((data) => setProducts(data));
    }, []);

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
            ParametrageService.getIAs().then((response) => {
                //console.log("📦 Séries chargées :", data);
                setIas(response);
            });
    }, []);
    
    useEffect(() => {
            CandidatureService.getProgs().then((response) => {
                console.log("📦 Séries chargées :", response);
                setAnnees(response);
            });
    }, []);

    useEffect(() => 
    {
        loadData();
    }, [ia, edition, lazyParams.page, lazyParams.rows]);


    const loadData = () => {
        setLoading(true);

        if (!ia || !edition) 
        {
            setLoading(false);
            return;
        }

        try 
        {
            CandidatureService.getAllCandidats(
            lazyParams.page,
            lazyParams.rows,
            edition.edition,
            ia.code

        )
            .then((result) => {
                console.log(result);
                setArchives(result.data);
                setTotalRecords(result.total);
            })
            .catch((error) => {
                console.error("❌ Erreur chargement archives :", error);
            });
        }
        catch (err)
        {
        console.error("❌ Erreur chargement FAEB :", err);
        setError("Erreur lors du chargement");
        } 
        finally 
        {
            setLoading(false);
        }
    };

    const fetchData = async () => {
            try 
            {
                const data = await CandidatureService.getSerieInAcademia(ia?.code, Number(edition?.edition));
                setListeSerie(data);
                console.log(data);
            } 
            catch (error) {
                console.error('Erreur lors du chargement des séries :', error);
                setGroupedCdts([]);
            }
        };

    useEffect(() => 
    {
        if (ia && edition)
        {
            fetchData();
        }
    }, [ia, edition]);



    const hideDeleteProductDialog = () => {
        setDeleteProductDialog(false);
    };

    
    const hideDeleteProductDialog__ = () => {
        setPrintDialog(false);
    };


    const formatDateToInput = (isoDateStr) => {
        const date = new Date(isoDateStr);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    };

    const editProduct = (product) => {
        const progFormatted = {
            ...product
        };
        console.log(progFormatted);
        setProductDialog(true);
        (id_cdt.current = product.id),
            // console.log(id_cdt);
            formik.setValues(progFormatted);
        is_update.current = true;
    };

    const editProduct2 = (candidat) => {
        setDeleteDialog(true);
        setCandidat({ ...candidat });
        id_cdt.current = candidat.id;
    };

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

   
    const sexeOptions = [
        { label: 'M', value: 'M' },
        { label: 'F', value: 'F' }
    ];

    const situOptions = [
        { label: 'Candidats Validé (s) ou Rejetés', value: 'VR' },
        { label: 'Candidats Isolés', value: 'I' }
    ];

    const leftToolbarTemplate = () => {
            return (
                <React.Fragment>
                    <div className="formgrid grid">
                        <div className="field col-12">
                            <fieldset className="custom-fieldset text-sm" style={{ padding: '5px' }}>
                                <legend className="text-primary font-bold text-sm">Effectuer les filtres (Tous les champs doivent être sélectionnés)</legend>
    
                                <div
                                    className="filter-container"
                                    style={{
                                        display: 'flex',
                                        gap: '10px', // espace entre les deux champs
                                        alignItems: 'flex-start',
                                        flexWrap: 'wrap',
                                        width: '100%'
                                    }}
                                >
                                    <div style={{ width: '400px' }}>
                                        <label htmlFor="etablissement">
                                            <h6 className="m-0">Liste des académies du Sénégal</h6>
                                        </label>
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
    
                                    <div style={{ width: '200px' }}>
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
                                            disabled={!ias || ias.length === 0}
                                            optionLabel="edition"
                                            placeholder="Choisir une édition du bac"
                                            filter
                                            className="p-inputtext-sm w-full"
                                            style={{ width: '100%' }}
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
        return <React.Fragment>{/* <Button severity="help" label="Exporter la liste" icon="pi pi-upload" onClick={exportCSV} /> */}</React.Fragment>;
    };


    
    const openPrint = () => {
        setPrintDialog(true);
    };

    const deleteData = async () => {
            if (id_cdt.current) {
                try {
                await ParametrageService.deleteArchive(id_cdt.current);
                toast.current.show({
                    severity: 'success',
                    summary: 'Office du Bac',
                    detail: 'Le dossier de candidature a été supprimé avec succès',
                    life: 5000
                });
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

    const hideDeleteProductDialog_ = () => {
        setDeleteDialog(false);
    };
    

    const actionBodyTemplate = (rowData) => {
        return (
            <>
                {/* <Button tooltip="Modifier la programmation" tooltipOptions={{ position: 'bottom' }} rounded className="p-2" severity="warning" onClick={() => editProduct(rowData)}>
                    <FiEdit style={{ width: '20px', height: '20px' }} />
                </Button> */}
                <Button
                    icon="pi pi-pencil"
                    rounded
                    label="Editer"
                    severity="warning"
                    className="mr-2"
                    onClick={() => editProduct(rowData)}
                />

                <Button
                    icon="pi pi-trash"
                    rounded
                    label="Supprimer"
                    className="mr-2"
                    severity="danger"
                    onClick={() => editProduct2(rowData)}
                />
                {/* <Button icon="pi pi-trash" rounded severity="warning" onClick={() => confirmDeleteProduct(rowData)} /> */}
            </>
        );
    };

    const exportAllCandidats = async () => {
        if (!ia || !edition) {
            console.warn("Académie ou édition non sélectionnée");
            return;
        }

        try {
            setExporting(true);
            setExportStep('📡 Récupération des données depuis le serveur...');
            
            const [result1, result2] = await Promise.all([
                CandidatureService.getAllCandidatsBySerieAndSexe(
                    edition?.edition,
                    ia?.code,
                    selectedSerie?.code
                ),
                CandidatureService.getAllIsolatedCandidatsBySerieAndSexe(
                    edition?.edition,
                    ia?.code,
                    selectedSerie?.code
                )
            ]);

            const modifiedResult2Data = (result2?.data || []).map(c => ({
                ...c,
                decision: 3
            }));


            const result = {
                data: [
                    ...(result1?.data || []),
                    ...modifiedResult2Data
                ]
            };

            const allCandidats = result?.data ?? [];

            if (!allCandidats || allCandidats.length === 0) 
            {
                setExportStep('✅ Aucune donnée à exporter');
                setTimeout(() => setExporting(false), 1000);
                return;
            }

            setExportStep('🔄 Préparation des données...');

            // 2. Import dynamique de XLSX (déjà bien, mais on peut extraire utils)
            const { utils, write } = await import('xlsx');

            // 3. Transformation performante (éviter toLocaleString en boucle)
            const worksheetData = [];
            
            // On utilise une boucle for classique (plus rapide que map pour de gros volumes)
            for (let i = 0; i < allCandidats.length; i++) {
                const row = allCandidats[i];
                
                worksheetData.push({
                    "Numéro de dossier": row.dosNumber,
                    "Prénom(s)": row.firstname,
                    "Nom": row.lastname,
                    "Date de Naissance": row.date_birth
                    ? new Date(row.date_birth).toLocaleString('fr-FR', {
                        year: 'numeric',
                        month: '2-digit',
                        day: '2-digit'
                    })
                    : '',
                    "Lieu de Naissance": row.place_birth || '',
                    "Sexe": row.gender,
                    "Centre Etat Civil": row.centreEtatCivil?.code || '',
                    "Année Registre": row.year_registry_num,
                    "N° Acte": row.registry_num,
                    "Code ISO Pays de Nationalité": row.nationality?.code || '',
                    "Nationalité": row.nationality?.name || '',
                    "Code ISO Pays de naissance": row.countryBirth?.code || '',
                    "Pays de naissance": row.countryBirth?.name || '',
                    "Code handicap": handCode[row.type_handicap] ?? "",
                    "Handicap": row.type_handicap != "Néant" ? row.type_handicap : '',
                    "EPS": row.eps || '',
                    "Téléphone": row.phone1 || '',
                    "Email": row.email || '',
                    "Nb Fois": row.bac_do_count,
                    "Année BFEM": row.year_bfem && row.year_bfem !== 0 ? row.year_bfem : '',
                    "Code Etab.": row.etablissement?.code || '',
                    "Libellé Etab.": row.etablissement?.name || '',
                    "Code Centre d'Examen Candidat": row.centreExamen?.code || '',
                    "Centre Examen": row.centreExamen?.name || '',
                    "Académie": row.etablissement?.inspectionAcademie?.name || '',
                    "Code Centre d'Examen Par Défaut": row.etablissement?.centreExamen?.code || '',
                    "Centre Examen Par Défaut": row.etablissement?.centreExamen?.name || '',
                    "Edition du Bac": row.session,
                    "Série": row.serie?.code || '',
                    "Code Opt 1": row.subject?.specialite?.code || row.matiere1?.code || '',
                    "Matière Optionnelle 1": row.matiere1?.name || row.subject?.specialite?.name || '',
                    "Code Opt 2": row.matiere2?.code || '',
                    "Matière Optionnelle 2": row.matiere2?.name || '',
                    "Code Opt 3": row.matiere3?.code || '',
                    "Matière Optionnelle 3": row.matiere3?.name || '',
                    "Code Mat. Facult. 1": mapCode[row.eprFacListA] ?? "",
                    "Epreuve Facultative 1": row.eprFacListA != "Aucun" ? row.eprFacListA : '',
                    "Code Mat. Facult. 2": matieresNameToCode[row.eprFacListB?.name] ?? "", // A remplacer par row.eprFacListB?.code || '', une fois les code ajouté
                    "Epreuve Facultative 2": row.eprFacListB?.name || '',
                    "Type de Candidat": row.typeCandidat?.name || '',
                    "N° groupe": row.subject?.numSujet || '',
                    "Libellé du sujet de projet": row.subject?.wording || '',
                    "Situation du dossier" : row?.decision || ''
                });
            }

            setExportStep('💾 Génération du fichier Excel...');

            // 4. Création du fichier
            const worksheet = utils.json_to_sheet(worksheetData);
            const workbook = utils.book_new();
            utils.book_append_sheet(workbook, worksheet, 'Candidats');

            // 5. Génération du buffer (type 'array' est correct pour Blob)
            const excelBuffer = write(workbook, { bookType: 'xlsx', type: 'array', compression: true });

            const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
            saveAs(blob, `Export_des_Candidats_de_la_serie_${selectedSerie?.code}_${ia?.name}_BAC_${edition?.edition}.xlsx`);

            setExportStep('✅ Export terminé avec succés !');
            setTimeout(() => setExporting(false), 1500);

        } 
        catch (error) 
        {
            console.error("❌ Erreur export :", error);
            setExportStep('❌ Erreur lors de l’export');
            setTimeout(() => setExporting(false), 2000);
        }

    };




    const header = (
        <div className="flex flex-column md:flex-row md:justify-content-between md:align-items-center">
            <h5 className="m-0">Listing des candidats pour le BAC édition {edition?.edition}</h5>
            {(edition && (
                                                                                    <div className="flex gap-3">
                                                                                        <Button
                                                                                            type="button"
                                                                                            icon="pi pi-file-excel"
                                                                                            rounded
                                                                                            severity="success"
                                                                                            onClick={openPrint}
                                                                                            label="Exporter la liste des candidats"
                                                                                        />
                                                                                    </div>
                                                                                ))}
        </div>
    );



   
    const deleteProductDialogFooter = (
        <>
            <Button label="No" icon="pi pi-times" text onClick={hideDeleteProductDialog} />
            <Button label="Yes" icon="pi pi-check" text onClick={deleteProduct} />
        </>
    );
    const deleteDialogFooter = (
            <>
                <Button label="Oui" icon="pi pi-check" text onClick={deleteData} />
                <Button label="Non" icon="pi pi-times" text onClick={hideDeleteProductDialog_} />
            </>
    );

    const formik = useFormik({
        initialValues: {
            tableNum : 0,
            exYearBac : 0,
            firstname : '',
            lastname : '',
            date_birth: '',
            place_birth : '',
            gender : '',
            countryBirth : '',
            etablissement : '',
            bac_do_count : 0,
            codeCentreEtatCivil : '',
            yearRegistryNum : 0,
            registryNum : '',
            exclusionDuree : 0
        },
        validationSchema: Yup.object({
            exclusionDuree : Yup.number().required("Champ requis"),
            codeCentreEtatCivil : Yup.string().required("Champ requis"),
            yearRegistryNum : Yup.number().required("Champ requis"),
            registryNum : Yup.number().required("Champ requis"),
        }),

        onSubmit: async (values, { setSubmitting, resetForm }) => {

            const baseMorteDTO : BaseMorteDTO_ = {
                tableNum: values.tableNum,
                exYearBac: values.exYearBac,
                place_birth: values.place_birth,
                exclusionDuree: values.exclusionDuree,
                codeCentreEtatCivil: values.codeCentreEtatCivil,
                yearRegistryNum: values.yearRegistryNum,
                registryNum: values.registryNum,
                firstname: values.firstname,
                lastname: values.lastname,
                date_birth: values.date_birth,
                gender: values.gender,
                countryBirth: values.countryBirth,
                etablissement: values.etablissement,
                bac_do_count: values.bac_do_count
            };

            console.log(baseMorteDTO);
            console.log(id_cdt);

            try {
                if (is_update.current === false) {
                    console.log('POST');
                    const response = await ParametrageService.createArchive(baseMorteDTO);
                    console.log('✅ Prog créé:', response.data);
                    toast.current.show({ severity: 'success', summary: 'Office du Bac', detail: 'Programmation créé avec succès', life: 4000 });
                }
                if (is_update.current === true) {
                    console.log('PUT');
                    const response = await ParametrageService.updateDureeMention(id_cdt.current, baseMorteDTO);
                    console.log('✅ Progrg mis à jour:', response.data);
                    toast.current.show({ severity: 'success', summary: 'Office du Bac', detail: 'Durée d\'exclusion mise à jour avec succès', life: 4000 });
                    is_update.current = false;
                }
                await loadData();
                resetForm();
            } 
            catch (error) 
            {
                const errorMessage = error.response?.data?.errorMessage;
                setMessage(errorMessage);
                    toast.current.show({ 
                        severity: 'error', 
                        summary: 'Office du Bac', 
                        detail: errorMessage, 
                        life: 4000 
                    });
            } 
            finally 
            {
                setSubmitting(false);
            }
            setSubmitted(false);
            setProductDialog(false);
        }
    });

    return (
        <ProtectedRoute allowedRoles={['ADMIN','SCOLARITE','PLANIFICATION']}>
            <div className="grid crud-demo">
                <div className="col-12">
                    <div className="card">
                        <Toast ref={toast} />
                        <Toolbar className="mb-4" left={leftToolbarTemplate} right={rightToolbarTemplate}></Toolbar>

                        <DataTable
                            ref={dt}
                            loading={loading}
                            loadingIcon="pi pi-spin pi-spinner"
                            paginator
                            size='small'
                            showGridlines
                            value={archives}
                            lazy
                            scrollable
                            style={{ width: '100%', whiteSpace: 'nowrap' }}
                            totalRecords={totalRecords}
                            first={lazyParams.first}
                            rows={lazyParams.rows}
                            rowsPerPageOptions={[5, 10, 25]}
                            onPage={(e) => {
                                setLazyParams({
                                    ...lazyParams,
                                    first: e.first,
                                    rows: e.rows,
                                    page: e.page
                                });
                            }}
                            className="p-datatable-sm"
                            paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                            currentPageReportTemplate="Affichage de {first} à {last} des {totalRecords} enregistrement (s)"
                            emptyMessage="Aucune donnée n'a été trouvée"
                            header={header}
                            responsiveLayout="scroll"
                        >
                                                            <Column field="dosNumber" header="N° dossier" frozen alignFrozen="left"/>
                                                            <Column field="firstname" header="Prénom (s)" frozen alignFrozen="left"/>
                                                            <Column field="lastname" header="Nom" frozen alignFrozen="left"/>
                                                            <Column
                                                                field="date_birth"
                                                                header="Date Naiss."
                                                                sortable
                                                                body={(rowData) => {
                                                                    return formatDateToInput(rowData.date_birth);
                                                                }}
                                                            />
                            
                                                            <Column field="place_birth" header="Lieu Naiss." />
                                                            <Column
                                                                field="gender"
                                                                header="Sexe"
                                                            
                                                                style={{ width: '1500px' }}
                                                                sortable
                                                            />
                                                            <Column field="centreEtatCivil.code" header="Centre Etat Civil"/>
                                                            <Column field="year_registry_num" header="Année registre"/>
                                                            <Column field="registry_num" header="N° Acte"/>
                                                            <Column field="nationality.name" header="Nationalité" />
                                                            <Column field="countryBirth.name" header="Pays de naissance"/>
                                                            <Column field="place_birth" header="Lieu Naiss."/>
                                                            <Column field="type_handicap" header="Handicap"/>
                                                            
                                                            <Column field="eps" header="EPS"/>
                                                            <Column field="phone1" header="Téléphone"/>
                                                            <Column field="email" header="Email" sortable />
                                                            <Column field="bac_do_count" header="Nb. Fois"/>
                                                            <Column field="year_bfem" header="Année BFEM" sortable />
                                                            <Column field="etablissement.code" header="Etablissement"/>
                                                            <Column field="etablissement.centreExamen.name" header="Centre Examen Etab."/>
                                                            <Column field="etablissement.inspectionAcademie.name" header="Académie"/>
                                                            <Column field="centreExamen.name" header="Centre Examen Candidat"/>

                                                            <Column field="session" header="Edition du Bac"/>
                                                            <Column 
                                                                field="serie.code" 
                                                                header="Série" 
                                                                sortable 
                                                                
                                                                />
                                                            <Column field="matiere1.name" header="Matière Optionnelle 1"/>
                                                            <Column field="matiere2.name" header="Matière Optionnelle 2"/>
                                                            <Column field="matiere3.name" header="Matière Optionnelle 3"/>
                                                            <Column field="eprFacListA" header="Epreuve Facultative 1"/>
                                                            <Column field="eprFacListB.name" header="Epreuve Facultative 2" sortable />
                                                            <Column field="typeCandidat.name" header="Type de Candidat" sortable />
                                                            <Column field="subject.numSujet" header="N° de groupe"/>
                                                            <Column field="subject.wording" header="Libellé du sujet de projet"/>
                                                            <Column field="subject.specialite.name" header="Spécialité"/>
                                                            <Column field="subject.specialite.code" header="Code de la spécialité"/>
                        </DataTable>

                        

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

                        <Dialog visible={exporting} onHide={() => {}} modal closable={false} header="Préparation du fichier">
                            <div className="flex flex-column align-items-center justify-content-center">
                                <ProgressSpinner style={{ width: '50px', height: '50px' }} />
                                <p style={{ marginTop: '1rem', fontWeight: 'bold' }}>{exportStep}</p>
                                <p style={{ marginTop: '1rem', fontWeight: 'bold' }}>
                                    Veuillez patienter... ⏱ {seconds} seconde{seconds > 1 ? 's' : ''}
                                </p>
                            </div>
                        </Dialog>

                        <Dialog visible={deleteDialog} style={{ width: '500px' }} header="Avertissement pour suppression" modal footer={deleteDialogFooter} onHide={hideDeleteProductDialog_}>
                                                    <div className="flex align-items-center justify-content-center">
                                                        <i className="pi pi-exclamation-triangle mr-3" style={{ fontSize: '2rem' }} />
                                                        <span>Attention vous vous appretez à supprimer le dossier du candidat {candidat?.dosNumber}</span>
                                                    </div>
                        </Dialog>

                                                <Dialog
                                                    visible={printDialog}
                                                    style={{ width: '450px' }}
                                                    header="Export des listes"
                                                    modal
                                                    onHide={hideDeleteProductDialog__}
                                                >
                                                    <form className="p-fluid">
                                                        <div className="grid align-items-end">
                                                            
                                                            {/* Dropdown */}
                                                            <div className="col-12 md:col-6">
                                                                <label htmlFor="serie">
                                                                    <span className="text-red-500">*</span> Série (optionnelle)
                                                                </label>
                                                                <Dropdown
                                                                    id="serie"
                                                                    value={selectedSerie}
                                                                    optionLabel="code"
                                                                    options={listeSerie}
                                                                    onChange={(e) => setSelectedSerie(e.value)}
                                                                    placeholder="Sélectionner la série"
                                                                    showClear
                                                                    className="p-inputtext-sm w-full"
                                                                    style={{ fontWeight: 'bold', color: 'black' }}
                                                                />
                                                            </div>

                                                            {/* Bouton */}
                                                          
                                                                <div className="col-12 md:col-6 flex justify-content-end">
                                                                    <Button
                                                                        type="button"
                                                                        icon="pi pi-file-excel"
                                                                        severity="success"
                                                                        label="Exporter la liste"
                                                                        onClick={exportAllCandidats}
                                                                    />
                                                                </div>
                                                          

                                                        </div>
                                                    </form>
                                                </Dialog>

                    </div>
                </div>
            </div>
        </ProtectedRoute>
    );
};

export default CalendarDemo;
