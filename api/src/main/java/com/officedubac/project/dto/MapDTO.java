package com.officedubac.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MapDTO
{
    private String departement;
    private long totalCandidats;
    private long totalM;
    private long totalF;
    private long enAttente;
    private long valider;
    private long rejeter;
}
