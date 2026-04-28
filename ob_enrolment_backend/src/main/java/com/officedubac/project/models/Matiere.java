package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "matiere")
@Builder
public class Matiere
{
    @Id
    private String id;
    private String name;
    private String code;
    private double coef_princ;
    private double coef_prat;
    private double memo;
    private Serie serie;
    private TypeMatiere typeMatiere;
    //Option
    private PorteeMatiere porteeMatiere;
}
