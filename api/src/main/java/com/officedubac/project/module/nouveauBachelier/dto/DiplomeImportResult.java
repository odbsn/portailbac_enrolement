package com.officedubac.project.module.nouveauBachelier.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class DiplomeImportResult {
    private String fichier;
    private int total;
    private int misAJour;
    private int nonTrouves;       // numeroTable absent de la collection nouveauBachelier
    private int colonneAcademieAbsente; // fichier ignoré : pas de colonne "N° Academie"
    private List<String> warnings;

    public static DiplomeImportResult ignoreSansColonneAcademie(String fichier, int total) {
        return DiplomeImportResult.builder()
                .fichier(fichier)
                .total(total)
                .misAJour(0)
                .nonTrouves(0)
                .colonneAcademieAbsente(total)
                .warnings(new ArrayList<>(List.of(
                        "⚠️ Colonne \"N° Academie\" absente de ce fichier — fichier ignoré.")))
                .build();
    }

    public String toSummary() {
        return String.format(
                "📁 %s → Total: %d | ✅ Mis à jour: %d | ❌ Introuvables: %d | ⏭️ Sans colonne académie: %d",
                fichier, total, misAJour, nonTrouves, colonneAcademieAbsente
        );
    }
}
