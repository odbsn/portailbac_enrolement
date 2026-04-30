package com.officedubac.project.module.nouveauBachelier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NouveauBachelierResponse {
    private String id;
    private String telephone;
    private String prenoms;
    private String nom;
    private String numeroTable;
    private String resultat;
    private String mention;
    private Long utiCree;
    @CreatedDate
    private LocalDateTime dateCreation;
    private Long utiModifie;
    @LastModifiedDate
    private LocalDateTime dateModification;
}
