package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "centre_etat_civil")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CentreEtatCivil
{
    @Id
    private String id;
    private String name;
    private String code;
    private Departement departement;
}
