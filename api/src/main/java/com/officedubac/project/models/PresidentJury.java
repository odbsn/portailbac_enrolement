package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "president_jury")
public class PresidentJury
{
    @Id
    private String id;
    private String firstname;
    private String lastname;
    private String phone;
    private String email;
    private String matricule;
    private Civilite civilite;
    private String indice_sal;
    private Grade grade;
    private int anc;
    private boolean decision;
    private Matiere matiere;
    private int bonus;
    private int pond_bonus;
    //Dernier centre d'examen
    private CentreExamen last_ce;
    //Etablissement de provenance
    private Structure structure;
    private Etablissement etablissement;
    private InspectionAcademie inspectionAcademie;
    private Universite universite;
    //Centre d'examen
    private CentreExamen ce;
    //Lieu d'activité
    private Etablissement place_of_activity;
    //Académie de provenance
    private InspectionAcademie aca_of_prov;
    //Académie du lieu de l'activité
    private InspectionAcademie aca_place_of_activity;
    private String numb_jury;
    private String bank;
    private String code_bank;
    private String code_agc;
    private String num_compte;
    private String key_rib;
    private boolean key_rib_correct;
    private boolean duplicate;
    private boolean not_eligible;
}
