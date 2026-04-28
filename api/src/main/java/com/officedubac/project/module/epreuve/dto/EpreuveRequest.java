package com.officedubac.project.module.epreuve.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpreuveRequest {

    @NotBlank(message = "Le code matière est obligatoire")
    private String matiere;

    @NotBlank(message = "Le code série est obligatoire")
    private String serie;

    private Integer coefficient;

    private Boolean autorisation;

    private Boolean estDominant;

    private Integer nombrePoints;

    private String jourDebut;

    private String heureDebut;

    private String duree;

    @NotBlank(message = "Le type (Ecrit/Oral) est obligatoire")
    private String type;
}
