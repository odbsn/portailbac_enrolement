
'use client';
import { ChartData, ChartOptions } from 'chart.js';
import { Chart } from 'primereact/chart';
import React, { useCallback, useContext, useEffect, useRef, useState } from 'react';
import type { ChartDataState, ChartOptionsState } from '@/types';
import { LayoutContext } from '@/layout/context/layoutcontext';
import { CandidatureService } from '@/demo/service/CandidatureService';

import { Dropdown } from 'primereact/dropdown';
import { Calendar } from "primereact/calendar";
import { dt } from '@fullcalendar/core/internal-common';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { InputText } from 'primereact/inputtext';
import { text } from 'stream/consumers';
import { AlignCenter } from 'lucide-react';
import { Accordion, AccordionTab } from 'primereact/accordion';
import MapByMansour from './map';
import MapComponent from './map';
import senegalGeoJSON from '../tableau-de-bord/data/senegal.json';
import { ParametrageService } from '@/demo/service/ParametrageService';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { saveAs } from 'file-saver';
import { UserContext } from '@/app/userContext';

const Dashboard = () => {
    const [options, setOptions] = useState<ChartOptionsState>({});
    const [summarizeData, setSummarizeData] = useState([]);
    const [summarizeData_, setSummarizeData_] = useState(null);
    const [summarizeDataforIA, setSummarizeDataforIA] = useState([]);
    const [vignettes, setVignettes] = useState(null);
    const [chartData, setChartData] = useState<any>({});
    const [barData, setBarData] = useState(null);
    const [barOptions, setBarOptions] = useState({});
    const [pieData, setPieData] = useState(null);
    const [pieOptions, setPieOptions] = useState({});
    const [pieData1, setPieData1] = useState(null);
    const [pieOptions1, setPieOptions1] = useState({});
    const [doughnutData1, setDoughnutData1] = useState(null);
    const [DoughnutOptions1, setDoughnutOptions1] = useState({});
    const [barData2, setBarData2] = useState(null);
    const [barOptions2, setBarOptions2] = useState({});
    const [barData3, setBarData3] = useState(null);
    const [barOptions3, setBarOptions3] = useState({});
    const { layoutConfig } = useContext(LayoutContext);
    const [faeb, setFaebs] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [reloadTrigger, setReloadTrigger] = useState(false);
    const [compteEF, setCompteEFs] = useState(null);
    const [selectedSession, setSelectedSession] = useState(null);
    const [startDate, setStartDate] = useState(null);
    const [endDate, setEndDate] = useState(null);
    const [globalFilter, setGlobalFilter] = useState(null);
    const dt = useRef(null);
    const [stats, setStats] = useState<any[]>([]);
    const [statGlobales, setStatGlobales] = useState(null);
    const [statGlobalesIA, setStatGlobalesIA] = useState(null);
    const [statAcademies, setStatAcademies] = useState<any[]>([]);

    const [inspectionAcademie, setInspectionAcademie] = useState(null);

    const [barDataAcademie, setBarDataAcademie] = useState(null);
    const [barOptionsAcademie, setBarOptionsAcademie] = useState({});
    
    const [barDataTypeEtab, setBarDataTypeEtab] = useState(null);
    const [barOptionsTypeEtab, setBarOptionsTypeEtab] = useState({});

    const [barDataHandicap, setBarDataHandicap] = useState(null);
    const [barOptionsHandicap, setBarOptionsHandicap] = useState({});

    const [barDataSerie, setBarDataSerie] = useState(null);
    const [barOptionsSerie, setBarOptionsSerie] = useState({});

    const [barDataTypeSerie, setBarDataTypeSerie] = useState(null);
    const [barOptionsTypeSerie, setBarOptionsTypeSerie] = useState({});

    const [barDataTypeSerie_, setBarDataTypeSerie_] = useState(null);
    const [barOptionsTypeSerie_, setBarOptionsTypeSerie_] = useState({});

    const [ias, setIas] = useState([]);

    const { user } = useContext(UserContext);

    const [prog, setOneProg] = useState<{ edition?: number } | null>(null);
    
    const [annees, setAnnees] = useState(null);
    const [edition, setEdition] = useState(null);
    
    useEffect(() => {
        CandidatureService.getProgs().then((response) => {
            //console.log("📦 Séries chargées :", data);
            setAnnees(response);
        });
    }, []);

    useEffect(() => {
        if (!edition) return;
        ParametrageService.getStatByDepartment(edition.edition).then(res => setStats(res));
    }, [edition]);

    useEffect(() => {
        CandidatureService.getLastProg().then((response) => {
            console.log("📦 Ok chargées :", response);
        setOneProg(response);
        });
    }, []); 


    useEffect(() => {
        if (user?.profil?.name === "SCOLARITE" || user?.profil?.name === "ADMIN")
        {
            ParametrageService.getIAs().then((response) => {
                setIas(response);
            });
        }  
    }, [user]);
    

    // 🔹 Fonction mémorisée pour loadDatas
    const loadDatas = useCallback(async () => 
        {
            setLoading(true);
            setError(null);
            try 
            {
                const response = await CandidatureService.compteFAEBS_(user?.acteur?.etablissement?.id, prog?.edition);
                setFaebs(response);
                console.log(faeb);
            } 
            catch (err) 
            {
                console.error("Erreur chargement données :", err);
                setError("Erreur lors du chargement");
            } 
            finally 
            {
                setLoading(false);
            }
    }, [user, prog]); 

    const loadDatas2 = useCallback(async () => {
            setLoading(true);
            setError(null);
            try 
            {
                const response = await CandidatureService.compteEF_(prog?.edition, user?.acteur?.etablissement?.id);
                console.log("OK", response);
                setCompteEFs(response);
            } 
            catch (err) 
            {
                setError("Erreur lors du chargement");
            } 
            finally 
            {
                setLoading(false);
            }
    }, [user?.acteur?.etablissement?.id, prog?.edition]); 

    useEffect(() => {
       if (user?.acteur?.etablissement?.id && Number(prog?.edition)) {
            loadDatas();
            loadDatas2();
        }
    }, [reloadTrigger, loadDatas, loadDatas2]);


    useEffect(() => {
    if (!user?.acteur?.etablissement?.id  || !prog?.edition) return;

    if (user?.profil.name === "AGENT_DE_SAISIE" && prog?.edition)
        {
            const documentStyle = getComputedStyle(document.documentElement);
            const textColor = documentStyle.getPropertyValue('--text-color') || '#495057';
            const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary') || '#6c757d';
            const surfaceBorder = documentStyle.getPropertyValue('--surface-border') || '#dee2e6';

            const backgroundColors = [
                documentStyle.getPropertyValue('--primary-500') || '#42A5F5',
                documentStyle.getPropertyValue('--pink-500') || '#FFB6C1'
            ];

            const backgroundColors2 = [
                documentStyle.getPropertyValue('--red-500') || '#F44336',   // Rouge
                documentStyle.getPropertyValue('--green-500') || '#4CAF50' // Vert
            ];

            const generateColorPalette = (count) => {
                return Array.from({ length: count }, (_, i) =>
                    `hsl(${(i * 360) / count}, 100%, 45%)`
                );
            };

            const colorPalette = [
            '#4CAF50', '#FFC107', '#F44336', '#1E90FF'
            ];

            const fetchBarChartData = async () => {
                try {
                    const barStats = await CandidatureService.getNombreCandidatsSerieByEtab(user?.acteur?.etablissement?.id, prog?.edition);

                    const labels = barStats.map((item) => item.label);
                    const data = barStats.map((item) => item.data);
                    const colorPalette = generateColorPalette(data.length);

                    setBarData({
                        labels,
                        datasets: [
                            {
                                backgroundColor: colorPalette.slice(0, data.length),
                                borderColor: colorPalette.slice(0, data.length),
                                borderWidth: 1,
                                data
                            }
                        ]
                    });

                    setBarOptions({
                        plugins: {
                            legend: {
                                display: false
                            }
                        },
                        scales: {
                            x: {
                                ticks: { color: '#000000', display: true, font: {size: 10} },
                                grid: { color: '#000000', drawTicks: false }
                            },
                            y: {
                                ticks: { color: '#000000', display: true  },
                                grid: { color: '#000000', drawTicks: false }
                            }
                        }
                    });
                } catch (error) {
                    console.error('Erreur BarChart stats série établissement :', error);
                }
            };

            const fetchPieChartData = async () => {
                try {
                    const pieStats = await CandidatureService.getNombreCandidatsSexeByEtab(user?.acteur?.etablissement?.id, prog?.edition); // ← autre endpoint

                    const labels = pieStats.map((item) => item.label);
                    const data = pieStats.map((item) => item.data);

                    setPieData({
                        labels,
                        datasets: [
                            {
                                data,
                                backgroundColor: backgroundColors.slice(0, data.length),
                                hoverBackgroundColor: backgroundColors.slice(0, data.length)
                            }
                        ]
                    });

                    setPieOptions({
                        plugins: {
                            legend: {
                                labels: { color: textColor }
                            }
                        }
                    });
                } catch (error) {
                    console.error('Erreur PieChart stats globales série :', error);
                }
            };

            const fetchPieChartData2 = async () => {
                try {
                    const pieStats = await CandidatureService.getNombreCandidatsEPSByEtab(user?.acteur?.etablissement?.id, prog?.edition); // ← autre endpoint

                    const labels = pieStats.map((item) => item.label);
                    const data = pieStats.map((item) => item.data);

                    setPieData1({
                        labels,
                        datasets: [
                            {
                                data,
                                backgroundColor: backgroundColors2.slice(0, data.length),
                                hoverBackgroundColor: backgroundColors2.slice(0, data.length)
                            }
                        ]
                    });

                    setPieOptions1({
                        plugins: {
                            legend: {
                                labels: { color: textColor }
                            }
                        }
                    });
                } catch (error) {
                    console.error('Erreur PieChart stats globales série :', error);
                }
            };

            const fetchDoughnutChartData2 = async () => {
                try {
                    const pieStats = await CandidatureService.getNombreCandidatsHandicapByEtab(user?.acteur?.etablissement?.id, prog?.edition); // ← autre endpoint

                    const labels = pieStats.map((item) => item.label);
                    const data = pieStats.map((item) => item.data);

                    setDoughnutData1({
                        labels,
                        datasets: [
                            {
                                data,
                                backgroundColor: backgroundColors2.slice(0, data.length),
                                hoverBackgroundColor: backgroundColors2.slice(0, data.length)
                            }
                        ]
                    });

                    setDoughnutOptions1({
                        plugins: {
                            legend: {
                                labels: { color: textColor }
                            }
                        }
                    });
                } catch (error) {
                    console.error('Erreur PieChart stats globales série :', error);
                }
            };

            const fetchBarChartData2 = async () => {
                try {
                    const barStats = await CandidatureService.getNombreCandidatsEFLAByEtab(user?.acteur?.etablissement?.id, prog?.edition);

                    const labels = barStats.map((item) => item.label);
                    const data = barStats.map((item) => item.data);

                    setBarData2({
                        labels,
                        datasets: [
                            {
                                backgroundColor: colorPalette.slice(0, data.length),
                                borderColor: colorPalette.slice(0, data.length),
                                borderWidth: 1,
                                data
                            }
                        ]
                    });

                    setBarOptions2({
                        plugins: {
                            legend: {
                                display: false
                            }
                        },
                        scales: {
                            x: {
                                ticks: { color: '#000000', display: true, font: {size: 10} },
                                grid: { color: '#000000', drawTicks: false }
                            },
                            y: {
                                ticks: { color: '#000000', display: true  },
                                grid: { color: '#000000', drawTicks: false }
                            }
                        }
                    });
                } catch (error) {
                    console.error('Erreur BarChart stats série établissement :', error);
                }
            };

            const fetchBarChartData3 = async () => {
                try {
                    const barStats = await CandidatureService.getNombreCandidatsOptionsByEtab(user?.acteur?.etablissement?.id, prog?.edition);

                    const labels = barStats.map((item) => item.label);
                    const data = barStats.map((item) => item.data);
                    const colorPalette = generateColorPalette(data.length);

                    setBarData3({
                        labels,
                        datasets: [
                            {
                                backgroundColor: colorPalette.slice(0, data.length),
                                borderColor: colorPalette.slice(0, data.length),
                                borderWidth: 1,
                                data
                            }
                        ]
                    });

                    setBarOptions3({
                        
                        plugins: {
                            legend: {
                                display: false
                            }
                        },
                        scales: {
                            x: {
                                ticks: { color: '#000000', display: true, font: {size: 10}  },
                                grid: { color: '#000000', drawTicks: false }
                            },
                            y: {
                                ticks: { color: '#000000', display: true },
                                grid: { color: '#000000', drawTicks: false}
                            }
                        }
                    });
                } catch (error) {
                    console.error('Erreur BarChart stats série établissement :', error);
                }
            };

            fetchBarChartData();
            fetchPieChartData();
            fetchPieChartData2();
            fetchDoughnutChartData2();
            fetchBarChartData2();
            fetchBarChartData3();
    }

    }, [user, prog]);

    useEffect(() => {
    if (!prog?.edition) return;


    const fetchBarChartDataTypeEtab = async () => {
        try {
            const barStats = await ParametrageService.getStatTypeEtab(prog?.edition);

            if (!barStats || barStats.length === 0) return;

            const labels = barStats.map(item => item.academia);
            const maleData = barStats.map(item => item.male || 0);
            const femaleData = barStats.map(item => item.female || 0);

            const colorMale = '#42A5F5';
            const colorFemale = '#EC407A';

            setBarDataTypeEtab({
                labels,
                datasets: [
                    {
                        label: 'Garçons',
                        backgroundColor: colorMale,
                        data: maleData
                    },
                    {
                        label: 'Filles',
                        backgroundColor: colorFemale,
                        data: femaleData
                    }
                ]
            });

            setBarOptionsTypeEtab({
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                responsive: true,
                scales: {
                    x: {
                        stacked: true,
                        ticks: { color: '#000000', font: { size: 10 } },
                        grid: { color: '#e0e0e0' }
                    },
                    y: {
                        stacked: true,
                        ticks: { color: '#000000' },
                        grid: { color: '#e0e0e0' }
                    }
                }
            });
        } 
        catch (error) 
        {
            console.error('Erreur BarChart stats académie :', error);
        }
    };

    const fetchBarChartDataAcademie = async () => {
        try {
            const barStats = await ParametrageService.getStatAcademies_(prog?.edition);

            if (!barStats || barStats.length === 0) return;

            const labels = barStats.map(item => item.academia);
            const maleData = barStats.map(item => item.male || 0);
            const femaleData = barStats.map(item => item.female || 0);

            const colorMale = '#42A5F5';
            const colorFemale = '#EC407A';

            setBarDataAcademie({
                labels,
                datasets: [
                    {
                        label: 'Garçons',
                        backgroundColor: colorMale,
                        data: maleData
                    },
                    {
                        label: 'Filles',
                        backgroundColor: colorFemale,
                        data: femaleData
                    }
                ]
            });

            setBarOptionsAcademie({
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                responsive: true,
                scales: {
                    x: {
                        stacked: true,
                        ticks: { color: '#000000', font: { size: 10 } },
                        grid: { color: '#e0e0e0' }
                    },
                    y: {
                        stacked: true,
                        ticks: { color: '#000000' },
                        grid: { color: '#e0e0e0' }
                    }
                }
            });
        } 
        catch (error) 
        {
            console.error('Erreur BarChart stats académie :', error);
        }
    };

    const fetchBarChartDataHandicap = async () => {
        try {
            const barStats = await ParametrageService.getStatDisciplines_(prog?.edition, "PREMIERE");
            
            if (!barStats || barStats.length === 0) return;

            const specialiteCodeMap = {
                "MATHEMATIQUES": "MATH",
                "FRANCAIS": "FRA",
                "SCIENCE DE LA VIE ET DE LA TERRE": "SVT",
                "VERSION GRECQUE": "GREC",
                "HISTOIRE": "HISTO",
                "ANGLAIS": "ANG",
                "PHYSIQUE": "PHYS",
                "GEOGRAPHIE": "GEO",
                "VERSION LATINE": "LATIN",
                "ALLEMAND": "ALL",
                "ARABE": "ARAB",
                "ESPAGNOL": "ESP",
                "ITALIEN": "ITA",
                "PORTUGAIS": "POR",
                "RUSSE": "RUS",
                "CITOYENNETE ET DROITS DE L'HOMME": "CDH",
                "ELECTRONIQUE & ELECTROTECHNIQUE": "ELECT",
                "SCIENCES ECONOMIQUES": "ECO",
                "ETUDES ISLAMIQUES": "ETU. ISL", // mis à jour
                "TECHNIQUES COMPTABLES": "COMPTA", // mis à jour
                "CONSTRUCTION MECANIQUE": "MECA", // mis à jour
                "ANALYSE DE FABRICATION, ETUDE D'OUTILLAGES": "AFEO",
                "PHILOSOPHIE": "PHILO",
                "CHIMIE": "CHIM"
            };

            const labels = barStats.map(item => specialiteCodeMap[item.discipline] || item.discipline);
            const maleData = barStats.map(item => item.male || 0);
            const femaleData = barStats.map(item => item.female || 0);

            const colorMale = '#42A5F5';
            const colorFemale = '#EC407A';

            setBarDataHandicap({
                labels,
                datasets: [
                    {
                        label: 'Garçons',
                        backgroundColor: colorMale,
                        data: maleData
                    },
                    {
                        label: 'Filles',
                        backgroundColor: colorFemale,
                        data: femaleData
                    }
                ]
            });

            setBarOptionsHandicap({
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                responsive: true,
                scales: {
                    x: {
                        stacked: true,
                        ticks: { color: '#000000', font: { size: 10 } },
                        grid: { color: '#e0e0e0' }
                    },
                    y: {
                        stacked: true,
                        ticks: { color: '#000000' },
                        grid: { color: '#e0e0e0' }
                    }
                }
            });
        } 
        catch (error) 
        {
            console.error('Erreur BarChart stats académie :', error);
        }
    };

    const fetchBarChartDataSerie = async () => {
        try {
            const barStats = await ParametrageService.getStatDisciplines_(prog?.edition, "TERMINALE");

            if (!barStats || barStats.length === 0) return;

            const specialiteCodeMap = {
                "MATHEMATIQUES": "MATH",
                "FRANCAIS": "FRA",
                "SCIENCE DE LA VIE ET DE LA TERRE": "SVT",
                "VERSION GRECQUE": "GREC",
                "HISTOIRE": "HISTO",
                "ANGLAIS": "ANG",
                "PHYSIQUE": "PHYS",
                "GEOGRAPHIE": "GEO",
                "VERSION LATINE": "LATIN",
                "ALLEMAND": "ALL",
                "ARABE": "ARAB",
                "ESPAGNOL": "ESP",
                "ITALIEN": "ITA",
                "PORTUGAIS": "POR",
                "RUSSE": "RUS",
                "CITOYENNETE ET DROITS DE L'HOMME": "CDH",
                "ELECTRONIQUE & ELECTROTECHNIQUE": "ELECT",
                "SCIENCES ECONOMIQUES": "ECO",
                "ETUDES ISLAMIQUES": "ETU. ISL",
                "TECHNIQUES COMPTABLES": "COMPTA",
                "CONSTRUCTION MECANIQUE": "MECA",
                "ANALYSE DE FABRICATION, ETUDE D'OUTILLAGES": "AFEO",
                "PHILOSOPHIE": "PHILO",
                "CHIMIE": "CHIM"
            };

            const labels = barStats.map(item => specialiteCodeMap[item.discipline] || item.discipline);
            const maleData = barStats.map(item => item.male || 0);
            const femaleData = barStats.map(item => item.female || 0);

            const colorMale = '#42A5F5';
            const colorFemale = '#EC407A';

            setBarDataSerie({
                labels,
                datasets: [
                    {
                        label: 'Garçons',
                        backgroundColor: colorMale,
                        data: maleData
                    },
                    {
                        label: 'Filles',
                        backgroundColor: colorFemale,
                        data: femaleData
                    }
                ]
            });

            setBarOptionsSerie({
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                responsive: true,
                scales: {
                    x: {
                        stacked: true,
                        ticks: { color: '#000000', font: { size: 10 } },
                        grid: { color: '#e0e0e0' }
                    },
                    y: {
                        stacked: true,
                        ticks: { color: '#000000' },
                        grid: { color: '#e0e0e0' }
                    }
                }
            });
        } 
        catch (error) 
        {
            console.error('Erreur BarChart stats académie :', error);
        }
    };


    const fetchBarChartDataTypeSerie = async () => {
        try {
            const barStats = await ParametrageService.getStatForLitteraire(prog?.edition);

            if (!barStats || barStats.length === 0) return;

            const labels = barStats.map(item => item.serie);
            const maleData = barStats.map(item => item.male || 0);
            const femaleData = barStats.map(item => item.female || 0);

            const colorMale = '#42A5F5';
            const colorFemale = '#EC407A';

            setBarDataTypeSerie({
                labels,
                datasets: [
                    {
                        label: 'Garçons',
                        backgroundColor: colorMale,
                        data: maleData
                    },
                    {
                        label: 'Filles',
                        backgroundColor: colorFemale,
                        data: femaleData
                    }
                ]
            });

            setBarOptionsTypeSerie({
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                responsive: true,
                scales: {
                    x: {
                        stacked: true,
                        ticks: { color: '#000000', font: { size: 10 } },
                        grid: { color: '#e0e0e0' }
                    },
                    y: {
                        stacked: true,
                        ticks: { color: '#000000' },
                        grid: { color: '#e0e0e0' }
                    }
                }
            });
        } 
        catch (error) 
        {
            console.error('Erreur BarChart stats académie :', error);
        }
    };


    const fetchBarChartDataTypeSerie_ = async () => {
        try {
            const barStats = await ParametrageService.getStatForScience(prog?.edition);

            if (!barStats || barStats.length === 0) return;

            const labels = barStats.map(item => item.serie);
            const maleData = barStats.map(item => item.male || 0);
            const femaleData = barStats.map(item => item.female || 0);

            const colorMale = '#42A5F5';
            const colorFemale = '#EC407A';

            setBarDataTypeSerie_({
                labels,
                datasets: [
                    {
                        label: 'Garçons',
                        backgroundColor: colorMale,
                        data: maleData
                    },
                    {
                        label: 'Filles',
                        backgroundColor: colorFemale,
                        data: femaleData
                    }
                ]
            });

            setBarOptionsTypeSerie_({
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                responsive: true,
                scales: {
                    x: {
                        stacked: true,
                        ticks: { color: '#000000', font: { size: 10 } },
                        grid: { color: '#e0e0e0' }
                    },
                    y: {
                        stacked: true,
                        ticks: { color: '#000000' },
                        grid: { color: '#e0e0e0' }
                    }
                }
            });
        } 
        catch (error) 
        {
            console.error('Erreur BarChart stats académie :', error);
        }
    };

        if ((user?.profil.name === "ADMIN") || (user?.profil.name == "SCOLARITE") || (user?.profil.name == "DEMSG"))
        {
            fetchBarChartDataAcademie(),
            fetchBarChartDataHandicap(),
            fetchBarChartDataSerie(),
            fetchBarChartDataTypeSerie(),
            fetchBarChartDataTypeSerie_(),
            fetchBarChartDataTypeEtab()
        }
    }, [prog, user]);

    useEffect(() => 
    {
        if (startDate && endDate)
        {
            fetchDailyStats();
        }
    }, [startDate, endDate]);

    
    useEffect(() => 
    {
        if (prog?.edition && user?.profil?.name === "INSPECTEUR_ACADEMIE")
        {
            fetchSummarize();
            fetchSummarizeforIA();
        }
    }, [prog, user]);

    useEffect(() => 
    {
        if (prog?.edition && (user?.profil?.name === "SCOLARITE" || user?.profil?.name === "ADMIN") && inspectionAcademie)
        {
            fetchSummarize();
        }
    }, [prog, user, inspectionAcademie]);

    useEffect(() => 
    {
        if (prog?.edition && (user?.profil?.name === "SCOLARITE" || user?.profil?.name === "ADMIN" || user?.profil?.name === "FINANCE_COMPTA"))
        {
            fetchSummarize_();
            fetchVignettes_()    
        }
    }, [prog, user]);


    useEffect(() => {
        if (prog?.edition && user?.acteur?.inspectionAcademie)
        {
            ParametrageService.getStatGlobalesByIA(prog?.edition, user?.acteur?.inspectionAcademie?.code).then((response) => {
            setStatGlobalesIA(response);
            });
        }
    }, [prog]);

    useEffect(() => {
        if (prog?.edition && (user?.profil.name == "ADMIN" || user?.profil.name == "SCOLARITE"))
        {
            ParametrageService.getStatGlobalesCGS(prog?.edition).then((response) => {
            setStatGlobales(response);
            });
        }
    }, [prog, user]); 



    interface OperatorDecisionCount {
        operator: string;
        dateOperation: string;
        accepted: number;
        rejected: number;
    }

    const fetchDailyStats = async () => {
        try {
            const res = await CandidatureService.checkReception(startDate,endDate,prog?.edition);
            setChartData(res);
            console.log(chartData);
        } catch (err) {
            console.error("Erreur fetch operator-daily :", err);
        }
    };

    
    const fetchSummarize = async () => {
        try {
            setLoading(true); 
            const res = await CandidatureService.getSummarizeOperations(prog?.edition, inspectionAcademie?.code);
            setSummarizeData(res);
            setLoading(false); 
        } catch (err) {
            console.error("Erreur fetch operator-daily :", err);
        }
    };

    const fetchSummarizeforIA = async () => {
        try 
        {
            setLoading(true); 
            const res = await ParametrageService.getStatEtabByIA(prog?.edition, user?.acteur?.inspectionAcademie?.code);
            setSummarizeDataforIA(res);
            console.log(res);
            setLoading(false); 
            
        } catch (err) {
            console.error("Erreur fetch operator-daily :", err);
        }
    };



    const fetchSummarize_ = async () => {
        try {
            const res = await CandidatureService.getSummarizeOperations_CGS(prog?.edition);
            setSummarizeData_(res);
            console.log(summarizeData_);
        } catch (err) {
            console.error("Erreur fetch operator-daily :", err);
        }
    };

    const fetchVignettes_ = async () => {
        try {
            const res = await CandidatureService.getVignettes_(prog?.edition);
            setVignettes(res);
            console.log(vignettes);
        } catch (err) {
            console.error("Erreur fetch operator-daily :", err);
        }
    };
    

    return (
        <>


    {(user?.profil?.name === "SCOLARITE" || user?.profil?.name === "ADMIN") && (
        <div className="grid p-fluid mt-1">
           
            <div className='col-12'> 
                                  <Accordion activeIndex={0}> 
                                        <AccordionTab header="Statistiques globales du Concours Général Sénégalais">
                                            <div className="grid mt-4">
                                                        {/* Total Candidats */}
                                                        <div className="col-12 md:col-2">
                                                            <Card className="text-center shadow-3 border-round-xl" style={{ background: '#dbdbdb4b' }}>
                                                            <img
                                                                src="/layout/images/people.png"
                                                                alt="Candidats"
                                                                style={{ width: '40px', height: '40px', objectFit: 'contain' }}
                                                            />
                                                            <h3 className="mt-3 mb-1 text-black-700">Candidats</h3>
                                                            <h3 className="text-black-500 mt-2">{statGlobales?.candidats ?? 0}</h3>
                                                            </Card>
                                                        </div>
                
                                                        {/* Garçons */}
                                                        <div className="col-12 md:col-2">
                                                            <Card className="text-center shadow-3 border-round-xl" style={{ background: '#006aff21' }}>
                                                            <img
                                                                src="/layout/images/man.png"
                                                                alt="Garçons"
                                                                style={{ width: '40px', height: '40px', objectFit: 'contain' }}
                                                            />
                                                            <h3 className="mt-3 mb-1 text-blue-700">Garçons</h3>
                                                            <h3 className="text-blue-600 mt-2">{statGlobales?.male ?? 0}</h3>
                                                            </Card>
                                                        </div>
                
                                                        {/* Filles */}
                                                        <div className="col-12 md:col-2">
                                                            <Card className="text-center shadow-3 border-round-xl" style={{ background: '#fdf2f8' }}>
                                                            <img
                                                                src="/layout/images/woman.png"
                                                                alt="Filles"
                                                                style={{ width: '40px', height: '40px', objectFit: 'contain' }}
                                                            />
                                                            <h3 className="mt-3 mb-1 text-pink-700">Filles</h3>
                                                            <h3 className="text-pink-600 mt-2">{statGlobales?.female ?? 0}</h3>
                                                            </Card>
                                                        </div>

                                                        <div className="col-12 md:col-2">
                                                            <div className="card flex flex-column align-items-center" style={{ backgroundColor: 'yellow', minHeight: '100px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                                                                <span style={{ color: "black", textAlign:"center" }}><b>Dossier (s) en attente <br/>{prog?.edition}</b></span>
                                                                <h3 style={{ color: "black", textAlign:"center" }}>{summarizeData_?.decision0 || 0}</h3>
                                                                
                                                            </div>
                                                        </div>      

                                                        <div className="col-12 md:col-2">
                                                            <div className="card flex flex-column align-items-center" style={{ backgroundColor: 'palegreen', minHeight: '100px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                                                                <span style={{ color: "black", textAlign:"center" }}><b>Dossier (s) validés <br/>{prog?.edition}</b></span>
                                                                <h3 style={{ color: "black", textAlign:"center" }}>{summarizeData_?.decision1 || 0}</h3>
                                                            </div>
                                                        </div>
                                                        <div className="col-12 md:col-2">
                                                            <div className="card flex flex-column align-items-center" style={{ backgroundColor: 'red', minHeight: '100px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                                                                <span style={{ color: "white", textAlign:"center" }}><b>Dossier (s) rejetés <br/>{prog?.edition}</b></span>
                                                                <h3 style={{ color: "white", textAlign:"center" }}>{summarizeData_?.decision2 || 0}</h3>
                                                            </div>
                                                        </div>
                    
                                            </div>
                                        </AccordionTab> 
                
                                      </Accordion> 
            </div> 

            <div className="col-12">
                <h5>Répartition des candidats par type d&apos;établissement - Concours Général Sénégalais {prog?.edition}</h5>
                <div className="card flex flex-column align-items-center" style={{ minHeight: '400px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    {barDataTypeEtab && <Chart
                                            type="bar"
                                            data={barDataTypeEtab}
                                            options={barOptionsTypeEtab}
                                            style={{ width: '100%', height: '100%' }}
                                        />}
                </div>
            </div>

            <div className="col-12">
                <h5>Répartition des candidats par Académie - Concours Général Sénégalais {prog?.edition}</h5>
                <div className="card flex flex-column align-items-center" style={{ minHeight: '400px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    {barDataAcademie && <Chart
                                            type="bar"
                                            data={barDataAcademie}
                                            options={barOptionsAcademie}
                                            style={{ width: '100%', height: '100%' }}
                                        />}
                </div>
            </div>

            <div className="col-12">
                <h5>Répartition des candidats en classe de PREMIERE - Concours Général Sénégalais {prog?.edition}</h5>
                <div className="card flex flex-column align-items-center" style={{ minHeight: '400px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    {barDataHandicap && <Chart
                                            type="bar"
                                            data={barDataHandicap}
                                            options={barOptionsHandicap}
                                            style={{ width: '100%', height: '100%' }}
                                        />}
                </div>
            </div>

            <div className="col-12">
                <h5>Répartition des candidats en classe de TERMINALE - Concours Général Sénégalais {prog?.edition}</h5>
                <div className="card flex flex-column align-items-center" style={{ minHeight: '400px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    {barDataSerie && <Chart type="bar" data={barDataSerie} options={barOptionsSerie} style={{ width: '100%', height: '100%' }} />}
                </div>
            </div>
            
           

        </div> 

    )}


    </>


    );
};

export default Dashboard;