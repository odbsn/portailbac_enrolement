package com.officedubac.project.module.nouveauBachelier.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ImportResult {
    private String fichier;
    private int total;
    private int nouveaux;
    private int modifies;
    private int inchanges;
    private int juryIntrouvable;       // 🆕
    private List<String> warnings;    // 🆕 détail des lignes ignorées

    public static ImportResult vide() {
        return ImportResult.builder()
                .nouveaux(0).modifies(0).inchanges(0)
                .juryIntrouvable(0).warnings(new ArrayList<>()).total(0)
                .build();
    }

    public String toSummary() {
        return String.format(
                "📁 %s → Total: %d | ✅ Nouveaux: %d | ♻️ Modifiés: %d | ⏭️ Inchangés: %d | ❌ Jury inconnu: %d",
                fichier, total, nouveaux, modifies, inchanges, juryIntrouvable
        );
    }
}
