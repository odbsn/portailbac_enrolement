package com.officedubac.project.module.epreuve;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class EpreuveSorter {

    /**
     * Trie les épreuves par code du jour (J01, J02, etc.) puis par code de l'heure (H01, H02, etc.)
     */
    public List<Epreuve> sortByJourAndHeure(List<Epreuve> epreuves) {
        if (epreuves == null || epreuves.isEmpty()) {
            return epreuves;
        }

        return epreuves.stream()
                .sorted(Comparator
                        .comparingInt((Epreuve e) -> extractNumber(e.getJourDebut() != null ? e.getJourDebut().getCode() : null))
                        .thenComparingInt(e -> extractNumber(e.getHeureDebut() != null ? e.getHeureDebut().getCode() : null))
                )
                .collect(Collectors.toList());
    }

    /**
     * Extrait le nombre du code (J01 -> 1, J02 -> 2, H01 -> 1, H02 -> 2)
     */
    private int extractNumber(String code) {
        if (code == null) return 999;
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return 999;
    }
}