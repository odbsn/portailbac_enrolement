package com.officedubac.project.module.epreuve.dto;

import com.officedubac.project.models.Matiere;
import com.officedubac.project.models.Serie;
import com.officedubac.project.module.heure.Heure;
import com.officedubac.project.module.jour.Jour;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpreuveResponse {
    private String id;
    private Matiere matiere;
    private Serie serie;
    private Integer coefficient;
    private Boolean autorisation;
    private Boolean estDominant;
    private Integer nombrePoints;
    private Jour jourDebut;
    private Heure heureDebut;
    private String duree;
    private String type;

}
