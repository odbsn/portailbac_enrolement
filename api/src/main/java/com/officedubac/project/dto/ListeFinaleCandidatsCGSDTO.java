package com.officedubac.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ListeFinaleCandidatsCGSDTO
{
    private Integer ordre;
    private String discipline;
    private String firstname;
    private String lastname;
    private String sexe;
    private String place_birth;
    private String serie;
    private String etablissementOrigine;
    private String centreComposition;
    private String academia;
    private String level;
    private Integer session;
}
