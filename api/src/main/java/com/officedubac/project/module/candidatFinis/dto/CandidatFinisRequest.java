package com.officedubac.project.module.candidatFinis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatFinisRequest {

    private String id;
    private String prenoms;
    private String nom;
    private String dateNaissance;
    private String lieuNaissance;
    private String nationalite;
    private String numeroTable;
    private String jury;
    private String serie;
    private String sexe;
    private Integer age;
    private String eps;
    private String etablissement;
    private String centreActEPS;
    private String mo1;
    private String mo2;
    private String mo3;
    private String ef1;
    private String ef2;
    private String centreEcrit;
    private String typeCandidat;
    private String codeEtatCivil;
    private String libEtatCivil;
    private String anneeActe;
    private String refActeNaissance;
    private String telephone;
    private String handicap;
}
