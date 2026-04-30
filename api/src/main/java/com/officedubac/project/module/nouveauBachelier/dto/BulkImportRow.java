package com.officedubac.project.module.nouveauBachelier.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BulkImportRow {
    private String telephone;
    private String prenoms;
    private String nom;
    private String numeroTable;
    private String resultat;
    private String mention;
    private String juryCode;
    private int rowNum;
}
