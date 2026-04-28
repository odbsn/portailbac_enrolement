package com.officedubac.project.dto;

import com.officedubac.project.models.Role;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProfilDTO {
    public String name;

    //Manage User
    private boolean add_user;
    private boolean update_password;
    private boolean grant_user;
    private boolean revoke_user;

    //Le droit Manage Settings contient :
    /***
     * Matiere
     * Serie
     * Options
     * Types (Filières, Matières, Enseignements...)
     * Etablissements
     * Universités
     * Centres Etat Civil
     * Regions
     * Département
     * IA
     * Ville
     * Centre Examen
     */
    private boolean manage_settings;

    //Manage Plan
    private boolean add_date;
    private boolean update_date;
    private boolean delete_date;
    private boolean share_date;
    private boolean view_plan;

    //Manage Candidate
    private boolean add_candidate;
    private boolean view_candidate;
    private boolean update_candidate;
    private boolean delete_candidate;
    private boolean accept_candidate;
    private boolean reject_candidate;

    //Manage Examinator
    private boolean add_examinator;
    private boolean update_examinator;

    //Manage SJ
    private boolean add_SJ;
    private boolean update_SJ;

    //Manage PJ
    private boolean add_PJ;
    private boolean update_PJ;

    //Manage Intrant
    private boolean plan_Intrant;
}
