package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "sujet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sujet
{
    @Id
    private String id;
    private String wording;
    private int numSujet;
    private Etablissement etablissement;
    private Specialite specialite;
    private long session;

    //List<Candidat> candidats;
}
