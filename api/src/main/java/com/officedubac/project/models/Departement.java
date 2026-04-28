package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "departement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departement
{
    @Id
    private String id;
    private String name;
    private Region region;

    //private List<Etablissement> etablissements = new ArrayList<>();
    //private List<CentreEtatCivil> centreEtatCivils = new ArrayList<>();
}