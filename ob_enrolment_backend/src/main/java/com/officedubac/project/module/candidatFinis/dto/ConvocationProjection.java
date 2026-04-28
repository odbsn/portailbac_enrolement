package com.officedubac.project.module.candidatFinis.dto;

import org.springframework.beans.factory.annotation.Value;

public interface ConvocationProjection {

    String getPrenoms();
    String getNom();
    String getDateNaissance();
    String getLieuNaissance();
    String getNationalite();

    String getNumeroTable();
    String getJury();
    String getSerie();
    String getSexe();

    String getTypeCandidat();
    String getEps();

    @Value("#{target.etablissement.name}")
    String getEtablissementName();

    @Value("#{target.etablissement.code}")
    String getCodeEtab();


    @Value("#{target.centreEcrit.name}")
    String getCentreEcritName();
    @Value("#{target.centreEcrit.code}")
    String getCentreCode();
//    CentreProj getCentreEcritName();
    String getCentreEcritParticulier();
    CentreProj getCentreActEPS();

    String getMo1();
    String getMo2();
    String getMo3();
    String getEf1();
    String getEf2();

    String getCentreMatFac1();
    String getLibMatFac1();
    String getCentreMatFac2();
    String getLibMatFac2();

    interface EtablissementProj {
        String getName();
        String getCode(); // 👈 ajoute ça ici
    }
    interface CentreProj { String getName(); }
}
