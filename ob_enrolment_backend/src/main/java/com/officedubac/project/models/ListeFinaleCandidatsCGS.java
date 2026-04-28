package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "liste_finale_candidats_CGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListeFinaleCandidatsCGS
{
    @Id
    private String id;
    private Integer ordre;
    private String discipline;
    private String firstname;
    private String lastname;
    private String sexe;
    private String date_birth;
    private String place_birth;
    private String serie;
    private String etablissementOrigine;
    private String centreComposition;
    private String academia;
    private String level;
    private Integer session;
}
