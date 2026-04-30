package com.officedubac.project.module.nouveauBachelier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NouveauBachelierAudit {
    private String id;
    private String nom;
    private String auteur;
    private LocalDateTime dateCreation;
    private String modificateur;
    private LocalDateTime dateModification;
}
