package com.officedubac.project.module.nouveauBachelier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NouveauBachelierRequest {
    private String id;
    private String telephone;
    private String prenoms;
    private String nom;
    private String numeroTable;
    private String resultat;
    private String mention;
}
