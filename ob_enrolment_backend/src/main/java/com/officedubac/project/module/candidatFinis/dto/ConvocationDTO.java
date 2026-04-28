package com.officedubac.project.module.candidatFinis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvocationDTO {

    private String prenoms;
    private String nom;
    private String dateNaissance;
    private String lieuNaissance;
    private String nationalite;

    private String numeroTable;
    private String jury;
    private String serie;
    private String sexe;

    private String typeCandidat;
    private String eps;

    // établissement
    private String etablissementName;
    private String codeEtab;


    // centres
    private String centreEcritName;
    private String centreCode;
    private String centreEcritParticulier;
    private String centreActEPSName;

    // matières
    private String mo1;
    private String mo2;
    private String mo3;
    private String ef1;
    private String ef2;

    private String centreMatFac1;
    private String libMatFac1;
    private String centreMatFac2;
    private String libMatFac2;
}
