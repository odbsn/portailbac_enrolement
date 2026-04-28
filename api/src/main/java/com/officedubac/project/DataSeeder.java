package com.officedubac.project;

import com.officedubac.project.models.*;
import com.officedubac.project.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Configuration
public class DataSeeder {
    @Autowired
    private CandidatRepository candidatRepository;
    @Autowired
    private CentreEtatCivilRepository centreEtatCivilRepository;
    @Autowired
    private DepartementRepository departementRepository;
    @Autowired
    private EtablissementRepository etablissementRepository;
    @Autowired
    private NationalityRepository nationalityRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private InspectionAcademieRepository inspectionAcademieRepository;
    @Autowired
    private CentreExamenRepository centreExamenRepository;
    @Autowired
    private TypeFiliereRepository typeFiliereRepository;
    @Autowired
    private TypeMatiereRepository typeMatiereRepository;
    @Autowired
    private TypeSerieRepository typeSerieRepository;
    @Autowired
    private SerieRepository serieRepository;
    @Autowired
    private TypeEnseignementRepository typeEnseignementRepository;
    @Autowired
    private PorteeMatiereRepository porteeMatiereRepository;
    @Autowired
    private MatiereRepository matiereRepository;
    @Autowired
    private VilleRepository villeRepository;
    @Autowired
    private TypeEtablissementRepository typeEtablissementRepository;
    @Autowired
    private TypeCandidatRepository typeCandidatRepository;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private SpecialiteRepository specialiteRepository;
    @Autowired
    private ProfilRepository profilRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ActeursRepository acteursRepository;
    @Autowired
    private RejetRepository rejetRepository;
    @Autowired
    private SpecialiteCGSRepository specialiteCGSRepository;

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    public String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num); // entier
                } else {
                    return String.valueOf(num); // décimal
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue(); // plus sûr que cell.getCellFormula()
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
            case _NONE:
            case ERROR:
            default:
                return null;
        }
    }


    @Value("${file.path.excel0}")
    private String filePath0;

    @Value("${file.path.excel1}")
    private String filePath1;

    @Value("${file.path.excel2}")
    private String filePath2;

    @Value("${file.path.excel3}")
    private String filePath3;
    @Value("${file.path.excel4}")
    private String filePath4;

    @Value("${file.path.excel5}")
    private String filePath5;

    @Value("${file.path.excel6}")
    private String filePath6;

    @Value("${file.path.excel7}")
    private String filePath7;

    @Value("${file.path.excel8}")
    private String filePath8;

    @Value("${file.path.excel9}")
    private String filePath9;

    @Value("${file.path.excel10}")
    private String filePath10;

    @Value("${file.path.excel11}")
    private String filePath11;

    @Value("${file.path.excel12}")
    private String filePath12;

    @Value("${file.path.excel13}")
    private String filePath13;

    @Value("${file.path.excel14}")
    private String filePath14;

    @Value("${file.path.excel15}")
    private String filePath15;

    @Value("${file.path.excel16}")
    private String filePath16;

    @Value("${file.path.excel17}")
    private String filePath17;

    @Value("${file.path.excel18}")
    private String filePath18;

    @Value("${file.path.excel19}")
    private String filePath19;

    @Value("${file.path.excel20}")
    private String filePath20;

    @Bean
    @Transactional
    public CommandLineRunner seedData(Environment env)
    {
        return args -> {
            String ddlAuto = env.getProperty("spring.mongodb.drop-collection");
            if ("true".equalsIgnoreCase(ddlAuto))
            {

                try (InputStream file = new ClassPathResource(filePath8).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet typeCandSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorTCand = typeCandSheet.iterator();
                    while (iteratorTCand.hasNext())
                    {
                        Row row = iteratorTCand.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        TypeCandidat tCdt = new TypeCandidat();
                        tCdt.setName(getCellValue(row.getCell(0)));
                        typeCandidatRepository.save(tCdt);
                    }
                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath15).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet typeEtabSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorTEtab = typeEtabSheet.iterator();
                    while (iteratorTEtab.hasNext())
                    {
                        Row row = iteratorTEtab.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        TypeEtablissement tEtab = new TypeEtablissement();
                        tEtab.setName(getCellValue(row.getCell(0)));
                        tEtab.setCode(getCellValue(row.getCell(1)));
                        typeEtablissementRepository.save(tEtab);
                    }
                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath10).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet villeSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorV = villeSheet.iterator();
                    while (iteratorV.hasNext())
                    {
                        Row row = iteratorV.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        Ville vil = new Ville();
                        vil.setName(getCellValue(row.getCell(0)));
                        villeRepository.save(vil);
                    }
                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath14).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet porteeMatiereSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorPM = porteeMatiereSheet.iterator();
                    while (iteratorPM.hasNext())
                    {
                        Row row = iteratorPM.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        PorteeMatiere pm = new PorteeMatiere();
                        pm.setName(getCellValue(row.getCell(0)));
                        porteeMatiereRepository.save(pm);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath9).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet typeEnseignementSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorTE = typeEnseignementSheet.iterator();
                    while (iteratorTE.hasNext())
                    {
                        Row row = iteratorTE.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        TypeEnseignement te = new TypeEnseignement();
                        te.setName(getCellValue(row.getCell(0)));
                        typeEnseignementRepository.save(te);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath11).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet typeFiliereSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorTF = typeFiliereSheet.iterator();
                    while (iteratorTF.hasNext())
                    {
                        Row row = iteratorTF.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        TypeFiliere tf = new TypeFiliere();
                        tf.setName(getCellValue(row.getCell(0)));
                        typeFiliereRepository.save(tf);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath12).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet typeMatiereSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorTM = typeMatiereSheet.iterator();
                    while (iteratorTM.hasNext())
                    {
                        Row row = iteratorTM.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        TypeMatiere tm = new TypeMatiere();
                        tm.setName(getCellValue(row.getCell(0)));
                        typeMatiereRepository.save(tm);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath13).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet typeSerieSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorTS = typeSerieSheet.iterator();
                    while (iteratorTS.hasNext())
                    {
                        Row row = iteratorTS.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        TypeSerie ts = new TypeSerie();
                        ts.setName(getCellValue(row.getCell(0)));
                        typeSerieRepository.save(ts);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath6).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet regSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorRegion = regSheet.iterator();
                    while (iteratorRegion.hasNext())
                    {
                        Row row = iteratorRegion.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        Region rg = new Region();
                        rg.setName(getCellValue(row.getCell(0)));
                        regionRepository.save(rg);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath7).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet serieSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorSerie = serieSheet.iterator();
                    while (iteratorSerie.hasNext())
                    {
                        Row row = iteratorSerie.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        Serie serie = new Serie();
                        serie.setCode(getCellValue(row.getCell(0)));
                        serie.setName(getCellValue(row.getCell(1)));
                        String type_filiere = getCellValue(row.getCell(2));
                        System.out.println("@@@@@@@@@@@@@@@@@@ "+ type_filiere);
                        TypeFiliere tf = typeFiliereRepository.findByName(type_filiere);
                        String type_serie = getCellValue(row.getCell(3));
                        TypeSerie ts = typeSerieRepository.findByName(type_serie);

                        serie.setTypeFiliere(tf);
                        serie.setTypeSerie(ts);

                        serieRepository.save(serie);
                    }
                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath0).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet acaSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorAca = acaSheet.iterator();
                    while (iteratorAca.hasNext())
                    {
                        Row row = iteratorAca.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        InspectionAcademie aca = new InspectionAcademie();
                        aca.setName(getCellValue(row.getCell(0)));
                        aca.setCode(getCellValue(row.getCell(1)));
                        aca.setRegion(regionRepository.findByName(getCellValue(row.getCell(2))));
                        inspectionAcademieRepository.save(aca);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath3).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet departementSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorDep = departementSheet.iterator();
                    while (iteratorDep.hasNext())
                    {
                        Row row = iteratorDep.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        Departement dep = new Departement();
                        dep.setName(getCellValue(row.getCell(0)));
                        departementRepository.save(dep);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath5).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet nationalitySheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorNationality = nationalitySheet.iterator();
                    while (iteratorNationality.hasNext())
                    {
                        Row row = iteratorNationality.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        Nationality nat = new Nationality();
                        nat.setCode(getCellValue(row.getCell(0)));
                        nat.setName(getCellValue(row.getCell(1)));
                        nationalityRepository.save(nat);
                    }

                    workbook.close();
                }
                try (InputStream file = new ClassPathResource(filePath1).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet centreEtatCivilSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorCEC = centreEtatCivilSheet.iterator();
                    while (iteratorCEC.hasNext())
                    {
                        Row row = iteratorCEC.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        CentreEtatCivil cec = new CentreEtatCivil();
                        cec.setCode(getCellValue(row.getCell(0)));
                        cec.setName(getCellValue(row.getCell(1)));
                        cec.setDepartement(departementRepository.findByName(getCellValue(row.getCell(3))));
                        centreEtatCivilRepository.save(cec);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath2).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet centreExamenSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorCE = centreExamenSheet.iterator();
                    while (iteratorCE.hasNext())
                    {
                        Row row = iteratorCE.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        CentreExamen cec = new CentreExamen();
                        cec.setName(getCellValue(row.getCell(0)));
                        centreExamenRepository.save(cec);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath4).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet matiereSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorM = matiereSheet.iterator();
                    while (iteratorM.hasNext())
                    {
                        Row row = iteratorM.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)


                        Matiere mat = new Matiere();
                        mat.setCode(getCellValue(row.getCell(0)));
                        mat.setName(getCellValue(row.getCell(1)));

                        String cellValue0 = getCellValue(row.getCell(2));
                        if (cellValue0 == null || cellValue0.trim().isEmpty())
                        {
                            mat.setCoef_princ(0.0);
                        }
                        else
                        {
                            mat.setCoef_princ(Double.parseDouble(cellValue0));
                        }

                        //mat.setCoef_princ(Double.parseDouble(getCellValue(row.getCell(2))));
                        String cellValue1 = getCellValue(row.getCell(3));
                        if (cellValue1 == null || cellValue1.trim().isEmpty())
                        {
                            mat.setCoef_prat(0.0);
                        }
                        else
                        {
                            mat.setCoef_prat(Double.parseDouble(cellValue1));
                        }
                        //mat.setCoef_prat(Double.parseDouble(getCellValue(row.getCell(3))));
                        //mat.setMemo(Double.parseDouble(getCellValue(row.getCell(4))));

                        String cellValue2 = getCellValue(row.getCell(4));
                        if (cellValue2 == null || cellValue2.trim().isEmpty())
                        {
                            mat.setMemo(0.0);
                        }
                        else
                        {
                            mat.setMemo(Double.parseDouble(cellValue2));
                        }
                        mat.setPorteeMatiere(porteeMatiereRepository.findByName(getCellValue(row.getCell(5))));
                        mat.setTypeMatiere(typeMatiereRepository.findByName(getCellValue(row.getCell(7))));
                        System.out.println("@@Serie "+ getCellValue(row.getCell(8)));
                        mat.setSerie(serieRepository.findByCode(getCellValue(row.getCell(8))));
                        //mat.setTypeSerie(typeSerieRepository.findByName(getCellValue(row.getCell(10))));
                        matiereRepository.save(mat);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath16).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet listeEtabSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorLE = listeEtabSheet.iterator();
                    while (iteratorLE.hasNext())
                    {
                        Row row = iteratorLE.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        Etablissement etab = new Etablissement();
                        etab.setCode(getCellValue(row.getCell(0)));
                        etab.setSigle(getCellValue(row.getCell(1)));
                        etab.setName(getCellValue(row.getCell(2)));
                        String v = getCellValue(row.getCell(3));
                        System.out.println("@@@@@@@@@@@@@@@@@"+ v);
                        etab.setVille(villeRepository.findByName(v));
                        etab.setTypeEnseignement(typeEnseignementRepository.findByName(getCellValue(row.getCell(4))));
                        etab.setTypeCandidat(typeCandidatRepository.findByName(getCellValue(row.getCell(5))));

                        String cellValue0 = getCellValue(row.getCell(6));
                        int capacity = (cellValue0 != null && !cellValue0.trim().isEmpty())
                                ? Integer.parseInt(cellValue0)
                                : 0; // ou une autre valeur par défaut
                        etab.setCapacity(capacity);

                        String cellValue1 = getCellValue(row.getCell(7));
                        int nb_of_jury = (cellValue1 != null && !cellValue1.trim().isEmpty())
                                ? Integer.parseInt(cellValue1)
                                : 0; // ou une autre valeur par défaut
                        etab.setNb_of_jury(nb_of_jury);

                        String cellValue2 = getCellValue(row.getCell(7));
                        int capacity_eps = (cellValue2 != null && !cellValue2.trim().isEmpty())
                                ? Integer.parseInt(cellValue2)
                                : 0; // ou une autre valeur par défaut

                        etab.setCapacity_eps(capacity_eps);

                        etab.setCan_have_cdt(Boolean.parseBoolean(getCellValue(row.getCell(9))));
                        etab.setEtb_with_actor(Boolean.parseBoolean(getCellValue(row.getCell(10))));
                        etab.setEtb_was_ce(Boolean.parseBoolean(getCellValue(row.getCell(11))));
                        etab.setEtb_with_other_actor(Boolean.parseBoolean(getCellValue(row.getCell(12))));
                        etab.setTypeEtablissement(typeEtablissementRepository.findByCode(getCellValue(row.getCell(13))));
                        etab.setCentreExamen(centreExamenRepository.findByName(getCellValue(row.getCell(14))));
                        etab.setInspectionAcademie(inspectionAcademieRepository.findByName(getCellValue(row.getCell(15))));
                        etab.setDepartement(departementRepository.findByName(getCellValue(row.getCell(17))));
                        etab.setEtab_have_cdt(Boolean.parseBoolean(getCellValue(row.getCell(22))));
                        etab.setEtb_prov_actor(Boolean.parseBoolean(getCellValue(row.getCell(23))));
                        etab.setEtb_is_ce(Boolean.parseBoolean(getCellValue(row.getCell(24))));
                        etab.setCe_for_other(Boolean.parseBoolean(getCellValue(row.getCell(25))));
                        etablissementRepository.save(etab);
                    }
                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath17).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet optionsSerieSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorOS = optionsSerieSheet.iterator();
                    while (iteratorOS.hasNext())
                    {
                        Row row = iteratorOS.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        Option option = new Option();
                        option.setName(getCellValue(row.getCell(0)));
                        option.setMatiere(getCellValue(row.getCell(1)));
                        Serie serie = serieRepository.findByCode(getCellValue(row.getCell(2)));
                        option.setOrder(Integer.parseInt(getCellValue(row.getCell(3))));
                        option.setSerie(serie);

                        optionRepository.save(option);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath18).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet optionsSerieSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorOS = optionsSerieSheet.iterator();
                    while (iteratorOS.hasNext())
                    {
                        Row row = iteratorOS.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        Specialite specialite = new Specialite();
                        specialite.setName(getCellValue(row.getCell(0)));
                        specialite.setCode(getCellValue(row.getCell(1)));
                        Serie serie = serieRepository.findByCode(getCellValue(row.getCell(2)));
                        specialite.setSerie(serie);

                        specialiteRepository.save(specialite);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath19).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet rejetSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorR = rejetSheet.iterator();
                    while (iteratorR.hasNext())
                    {
                        Row row = iteratorR.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        Rejet r = new Rejet();
                        r.setName(getCellValue(row.getCell(0)));
                        r.setObservation(getCellValue(row.getCell(1)));

                        rejetRepository.save(r);
                    }

                    workbook.close();
                }

                try (InputStream file = new ClassPathResource(filePath20).getInputStream())
                {
                    //System.out.println("Chemin absolu : " + new File(filePath).getAbsolutePath());
                    Workbook workbook = new XSSFWorkbook(file);
                    // Lire les agences
                    Sheet spCGSSheet = workbook.getSheetAt(0);
                    Iterator<Row> iteratorR = spCGSSheet.iterator();
                    while (iteratorR.hasNext())
                    {
                        Row row = iteratorR.next();
                        if (row.getRowNum() == 0) continue; // Ignorer la première ligne (en-têtes)

                        SpecialiteCGS sp = new SpecialiteCGS();
                        sp.setSpecialite(getCellValue(row.getCell(0)));
                        sp.setClasse(getCellValue(row.getCell(1)));

                        specialiteCGSRepository.save(sp);
                    }

                    workbook.close();
                }


                catch (IOException e)
                {
                    e.printStackTrace();
                }

                Profil profil = Profil.builder()
                        .name(Role.AGENT_DE_SAISIE)
                        .addUser(true)
                        .updatePassword(true)
                        .grantUser(true)
                        .revokeUser(true)
                        //
                        .manageSettings(true)
                        .addDate(true)
                        .updateDate(true)
                        .deleteDate(true)
                        .shareDate(true)
                        .viewPlan(true)
                        //Manage Scolarité
                        .addCandidate(true)
                        .viewCandidate(true)
                        .updateCandidate(true)
                        .deleteCandidate(true)
                        .acceptCandidate(true)
                        .rejectCandidate(true)
                        //Manage Examinator
                        .addExaminator(true)
                        .updateExaminator(true)
                        .addSJ(true)
                        .updateSJ(true)
                        //Manage PJ
                        .addPJ(true)
                        .updatePJ(true)
                        //Manage Intrant
                        .planIntrant(true)
                        .build();

                profilRepository.save(profil);

                Profil profil2 = Profil.builder()
                        .name(Role.SCOLARITE)
                        .addUser(true)
                        .updatePassword(true)
                        .grantUser(true)
                        .revokeUser(true)
                        //
                        .manageSettings(true)
                        .addDate(true)
                        .updateDate(true)
                        .deleteDate(true)
                        .shareDate(true)
                        .viewPlan(true)
                        //Manage Scolarité
                        .addCandidate(true)
                        .viewCandidate(true)
                        .updateCandidate(true)
                        .deleteCandidate(true)
                        .acceptCandidate(true)
                        .rejectCandidate(true)
                        //Manage Examinator
                        .addExaminator(true)
                        .updateExaminator(true)
                        .addSJ(true)
                        .updateSJ(true)
                        //Manage PJ
                        .addPJ(true)
                        .updatePJ(true)
                        //Manage Intrant
                        .planIntrant(true)
                        .build();

                profilRepository.save(profil2);

                Profil profil3 = Profil.builder()
                        .name(Role.ADMIN)
                        .addUser(true)
                        .updatePassword(true)
                        .grantUser(true)
                        .revokeUser(true)
                        //
                        .manageSettings(true)
                        .addDate(true)
                        .updateDate(true)
                        .deleteDate(true)
                        .shareDate(true)
                        .viewPlan(true)
                        //Manage Scolarité
                        .addCandidate(true)
                        .viewCandidate(true)
                        .updateCandidate(true)
                        .deleteCandidate(true)
                        .acceptCandidate(true)
                        .rejectCandidate(true)
                        //Manage Examinator
                        .addExaminator(true)
                        .updateExaminator(true)
                        .addSJ(true)
                        .updateSJ(true)
                        //Manage PJ
                        .addPJ(true)
                        .updatePJ(true)
                        //Manage Intrant
                        .planIntrant(true)
                        .build();

                Profil prf3 = profilRepository.save(profil3);

                //Etablissement etab = etablissementRepository.findByName("LYCEE DE BAMBEY");

                Acteurs acteur3 = Acteurs.builder()
                        .firstname("")
                        .lastname("")
                        .phone("")
                        .email("")
                        .matricule("")
                        .civilite(Civilite.Mr)
                        .indice_sal("")
                        .grade(null)
                        .anc(0)
                        .decision(true)
                        .matiere(null)
                        .bonus(0)
                        .pond_bonus(0)
                        .nb_of_me(0)
                        .last_ce(null)
                        .structure(null)
                        .etablissement(null)
                        .inspectionAcademie(null)
                        .universite(null)
                        .classes("")
                        .ce(null)
                        .place_of_activity(null)
                        .aca_of_prov(null)
                        .aca_place_of_activity(null)
                        .numb_jury("")
                        .bank("")
                        .code_bank("")
                        .code_agc("")
                        .num_compte("")
                        .key_rib("")
                        .key_rib_correct(false)
                        .duplicate(false)
                        .eligible(false)
                        .build();

                Acteurs act3_ = acteursRepository.save(acteur3);

                User user3 = new User();
                user3.setFirstname("ADMIN CENTRAL");
                user3.setLastname("ADMIN CENTRAL");
                user3.setLogin("ADMIN CENTRAL");
                    user3.setPassword(new BCryptPasswordEncoder().encode(""));
                user3.setPhone("");
                user3.setEmail("");
                user3.setState_account(true);
                user3.setProfil(prf3);
                user3.setActeur(act3_);

                userRepository.save(user3);

                Profil profil4 = Profil.builder()
                        .name(Role.RECEPTIONNISTE)
                        .addUser(true)
                        .updatePassword(true)
                        .grantUser(true)
                        .revokeUser(true)
                        //
                        .manageSettings(true)
                        .addDate(true)
                        .updateDate(true)
                        .deleteDate(true)
                        .shareDate(true)
                        .viewPlan(true)
                        //Manage Scolarité
                        .addCandidate(true)
                        .viewCandidate(true)
                        .updateCandidate(true)
                        .deleteCandidate(true)
                        .acceptCandidate(true)
                        .rejectCandidate(true)
                        //Manage Examinator
                        .addExaminator(true)
                        .updateExaminator(true)
                        .addSJ(true)
                        .updateSJ(true)
                        //Manage PJ
                        .addPJ(true)
                        .updatePJ(true)
                        //Manage Intrant
                        .planIntrant(true)
                        .build();

                profilRepository.save(profil4);

                Profil profil5 = Profil.builder()
                        .name(Role.VIGNETTES_COUPONS)
                        .addUser(true)
                        .updatePassword(true)
                        .grantUser(true)
                        .revokeUser(true)
                        //
                        .manageSettings(true)
                        .addDate(true)
                        .updateDate(true)
                        .deleteDate(true)
                        .shareDate(true)
                        .viewPlan(true)
                        //Manage Scolarité
                        .addCandidate(true)
                        .viewCandidate(true)
                        .updateCandidate(true)
                        .deleteCandidate(true)
                        .acceptCandidate(true)
                        .rejectCandidate(true)
                        //Manage Examinator
                        .addExaminator(true)
                        .updateExaminator(true)
                        .addSJ(true)
                        .updateSJ(true)
                        //Manage PJ
                        .addPJ(true)
                        .updatePJ(true)
                        //Manage Intrant
                        .planIntrant(true)
                        .build();

                profilRepository.save(profil5);

                Profil profil6 = Profil.builder()
                        .name(Role.AUTORISATION_RECEPTION)
                        .addUser(true)
                        .updatePassword(true)
                        .grantUser(true)
                        .revokeUser(true)
                        //
                        .manageSettings(true)
                        .addDate(true)
                        .updateDate(true)
                        .deleteDate(true)
                        .shareDate(true)
                        .viewPlan(true)
                        //Manage Scolarité
                        .addCandidate(true)
                        .viewCandidate(true)
                        .updateCandidate(true)
                        .deleteCandidate(true)
                        .acceptCandidate(true)
                        .rejectCandidate(true)
                        //Manage Examinator
                        .addExaminator(true)
                        .updateExaminator(true)
                        .addSJ(true)
                        .updateSJ(true)
                        //Manage PJ
                        .addPJ(true)
                        .updatePJ(true)
                        //Manage Intrant
                        .planIntrant(true)
                        .build();

                profilRepository.save(profil6);



            }

        };
    }
}
