package com.officedubac.project.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "candidat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidat
{
    @Id
    private String id;
    private Long session;
    @Indexed(unique = true)
    private String numEnrolement;
    private String dosNumber;
    private String dosNumber_by_session_and_etablissement;
    private String firstname;
    private String lastname;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date_birth;
    private String place_birth;
    private Gender gender;
    private String phone1;
    private String phone2;
    private String email;
    private String adresse;
    private int year_registry_num;
    private String registry_num;
    private int bac_do_count;
    private OrigineBfem origine_bfem;
    private int year_bfem;
    private String subject;
    private boolean handicap;
    private String type_handicap;
    private String eps;
    private boolean cdt_is_cgs;
    //0 - En cours, 1 - Validé, 2 - Rejeté

    private int decision;

    private Option matiere1;
    private Option matiere2;
    private Option matiere3;

    // Epreuve Facultative Liste A
    private ListeA eprFacListA;

    // Epreuve Facultative Liste B
    private Option eprFacListB;

    private TypeCandidat typeCandidat;
    private Etablissement etablissement;
    private CentreEtatCivil centreEtatCivil;
    private Serie serie;
    private Nationality nationality;
    private Nationality countryBirth;
    private ConcoursGeneral concoursGeneral;

    private CentreExamen centreExamen;

    private List<Rejet> rejets;

    private boolean alreadyBac;

    private String operator;
    private LocalDateTime dateOperation;

    private String codeEnrolementEC;

    public Candidat(CandidateIsolated c) {
        this.id = null; // Mongo générera un nouvel ID
        this.session = c.getSession();
        this.numEnrolement = c.getNumEnrolement(); // unique=false dans CandidateIsolated
        this.dosNumber = c.getDosNumber();
        this.dosNumber_by_session_and_etablissement = c.getDosNumber_by_session_and_etablissement();
        this.firstname = c.getFirstname();
        this.lastname = c.getLastname();
        this.date_birth = c.getDate_birth();
        this.place_birth = c.getPlace_birth();
        this.gender = c.getGender();
        this.phone1 = c.getPhone1();
        this.phone2 = c.getPhone2();
        this.email = c.getEmail();
        this.adresse = c.getAdresse();
        this.year_registry_num = c.getYear_registry_num();
        this.registry_num = c.getRegistry_num();
        this.bac_do_count = c.getBac_do_count();
        this.origine_bfem = null;
        this.year_bfem = c.getYear_bfem();
        this.subject = c.getSubject();
        this.handicap = c.isHandicap();
        this.type_handicap = c.getType_handicap();
        this.eps = c.getEps();
        this.cdt_is_cgs = c.isCdt_is_cgs();
        this.decision = c.getDecision();
        this.matiere1 = c.getMatiere1();
        this.matiere2 = c.getMatiere2();
        this.matiere3 = c.getMatiere3();
        this.eprFacListA = c.getEprFacListA();
        this.eprFacListB = c.getEprFacListB();
        this.typeCandidat = c.getTypeCandidat();
        this.etablissement = c.getEtablissement();
        this.centreEtatCivil = c.getCentreEtatCivil();
        this.serie = c.getSerie();
        this.nationality = c.getNationality();
        this.countryBirth = c.getCountryBirth();
        this.concoursGeneral = c.getConcoursGeneral();
        this.centreExamen = c.getCentreExamen();
        this.rejets = (c.getRejets() != null) ? new ArrayList<>(c.getRejets()) : null;
        this.alreadyBac = c.isAlreadyBac();
        this.operator = c.getOperator();
        this.dateOperation = c.getDateOperation();
        this.codeEnrolementEC = c.getCodeEnrolementEC();
    }

}
