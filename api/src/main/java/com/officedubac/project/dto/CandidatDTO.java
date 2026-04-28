package com.officedubac.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.officedubac.project.models.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CandidatDTO
{
    private String dosNumber;
    private Long session;
    private String firstname;
    private String lastname;
    private String date_birth;
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
    //1 - En cours, 2 - Validé, 3 - Rejeté
    private int decision;
    private String motif;

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

    private boolean alreadyBac;
    private String codeEnrolementEC;

}
