package com.officedubac.project.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SerieReferentiel {

    private SerieReferentiel() {
    }
    public static final List<String> ORDRE_SERIES = List.of(
            "S1", "S1A", "S2", "S2A", "S3", "S4", "S5",
            "STIDD", "F6", "T1", "T2", "STEG",
            "L'1", "L1a", "L1b", "L2", "LA", "L-AR"
    );

    public static final Map<String, String> FILIERE_PAR_SERIE = buildFiliereMap();

    public static final String FILIERE_ST = "S&T";
    public static final String FILIERE_TERTIAIRE = "Tertiaire";
    public static final String FILIERE_LITTERAIRE = "Littéraire";

    private static Map<String, String> buildFiliereMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String serie : List.of("S1", "S1A", "S2", "S2A", "S3", "S4", "S5", "STIDD", "F6", "T1", "T2")) {
            map.put(serie, FILIERE_ST);
        }
        map.put("STEG", FILIERE_TERTIAIRE);
        for (String serie : List.of("L'1", "L1a", "L1b", "L2", "LA", "L-AR")) {
            map.put(serie, FILIERE_LITTERAIRE);
        }
        return Map.copyOf(map);
    }
}
