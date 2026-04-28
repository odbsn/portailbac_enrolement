package com.officedubac.project.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "profil")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profil
{
    @Id
    private String id;
    public Role name;

    //Manage User
    private boolean addUser;
    private boolean updatePassword;
    private boolean grantUser;
    private boolean revokeUser;

    //Manage Settings
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
    private boolean manageSettings;

    //Manage Plan
    private boolean addDate;
    private boolean updateDate;
    private boolean deleteDate;
    private boolean shareDate;
    private boolean viewPlan;

    //Manage Scolarité
    private boolean addCandidate;
    private boolean viewCandidate;
    private boolean updateCandidate;
    private boolean deleteCandidate;
    private boolean acceptCandidate;
    private boolean rejectCandidate;

    //Manage Examinator
    private boolean addExaminator;
    private boolean updateExaminator;

    //Manage SJ
    private boolean addSJ;
    private boolean updateSJ;

    //Manage PJ
    private boolean addPJ;
    private boolean updatePJ;

    //Manage Intrant
    private boolean planIntrant;
}
