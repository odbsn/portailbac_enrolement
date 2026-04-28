package com.officedubac.project.dto;

import com.officedubac.project.models.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EtablissementDTO
{
    private String name;
    private String code;
    private int capacity;
    private int nb_of_jury;
    private int capacity_eps;
    private int nb_act_sur_site;
    private int zone;

    private boolean can_have_cdt;
    private boolean etb_with_actor;
    private boolean etb_was_ce;
    private boolean etb_with_other_actor;
    //ce : Centre d'examen
    private boolean is_ce;
    private boolean etab_have_cdt;
    private boolean ce_for_other;

    private InspectionAcademie insp_aca;
    private TypeEtablissement type_etab;
    private Departement dep;
    private Ville ville;
    private CentreExamen centre_exam;
    private TypeEnseignement type_ens;
    private TypeCandidat type_cdts;
}
