package com.officedubac.project.module.bacheliersToCampusen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;
import java.util.Map;

@Document(collection = "bacheliers_to_campusen")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "idx_annee_serie", def = "{'annee': 1, 'serie': 1}"),
        @CompoundIndex(name = "idx_annee_table", def = "{'annee': 1, 'numeroTable': 1}")
})
public class BacheliersToCampusen
{

    @Id
    private String id;
    private int annee;
    private String serie;

    private String numeroTable;
    private String prenoms;
    private String nom;
    private String dateNaissance;
    private String anneeNaissance;
    private String lieuNaissance;
    private String sexe;
    private String etsProvenance;
    private String typeCandidature;
    private String academieProvenance;
    private String residence;
    private String centreEcrit;
    private String numeroJury;
    private String nombreFois;
    private String nationalite;
    private String matiereOptionnelle1;
    private String matiereOptionnelle2;
    private String matiereOptionnelle3;
    private String epreuveFacultativeListeA;
    private String epreuveFacultativeListeB;
    private String noteEpreuveFacultativeA;
    private String noteEpreuveFacultativeB;
    private String noteEps;
    private String present;
    private String mention;
    private String resultat;
    private String groupeResultat;
    private String dateDeliberation;
    private String paysNaissance;
    private String cec;
    private String numeroAec;
    private String anneeExtraitEc;
    private String typeAec;
    private String moyenneSeconde;
    private String moyennePremiere;
    private String moyenneS1Terminale;
    private String moyenneS2Terminale;
    private String totalPointsGroupe1;
    private String moyenneGroupe1;
    private String totalPointsG1G2;
    private String moyenneGenerale;
    private String moyenneMatieresFondamentales;
    private String moyenneRetenue;
    private String moyenneDefinitive;

    /** Colonnes specifiques a la serie : ex "MATH C=5/Ec" -> "08".
     * Ordre des colonnes preserve. */
    private Map<String, String> notes = new LinkedHashMap<>();
}
