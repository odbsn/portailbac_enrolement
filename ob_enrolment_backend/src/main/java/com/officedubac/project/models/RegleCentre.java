package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "regle_centre")
@Builder
public class RegleCentre
{
    @Id
    private String id;
    private Long session;
    private List<String> provenanceVille;
    private InspectionAcademie provenanceAcademie;
    private List<String> discipline;
    private List<String> classes;
    private Etablissement centreDeComposition;
}
