package com.officedubac.project.module.nouveauBachelier.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DiplomeImportJob {

    public enum Statut { EN_ATTENTE, EN_COURS, TERMINE, ECHEC }

    private String id;
    private Statut statut;
    private int totalFichiers;
    private List<DiplomeImportResult> resultats;
    private String erreur;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}
