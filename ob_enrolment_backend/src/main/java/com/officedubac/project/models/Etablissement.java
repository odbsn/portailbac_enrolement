package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "etablissement")
public class Etablissement
{
    @Id
    private String id;
    private String name;
    private String code;
    private String sigle;
    private int capacity;
    private int nb_of_jury;
    private int capacity_eps;
    private int nb_act_sur_site;
    private int zone;
    //Peut avoir des candidats
    private boolean can_have_cdt;
    //Ets. avec acteurs
    private boolean etb_with_actor;
    //Ets servi C. Ecrit
    private boolean etb_was_ce;
    //Ets. avec autre act.
    private boolean etb_with_other_actor;
    //Ets. Avec Candidats"
    private boolean etb_is_ce;
    //Ets de prov. Acteurs"
    private boolean etb_prov_actor;
    //Ets. Avec candidats
    private boolean etab_have_cdt;
    //Centre pour autre activité
    private boolean ce_for_other;

    private InspectionAcademie inspectionAcademie;
    private TypeEtablissement typeEtablissement;
    private TypeEnseignement typeEnseignement;
    private TypeCandidat typeCandidat;
    private CentreExamen centreExamen;
    private Ville ville;
    private Departement departement;
}
