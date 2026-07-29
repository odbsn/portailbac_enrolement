package com.officedubac.project.dto;

import java.util.Map;

/**
 * Une ligne du tableau récapitulatif (ex: "Filles" dans le bloc "Inscrits").
 *
 * @param label      libellé de la ligne (ex: "Filles", "Garçons", "Total inscrits")
 * @param valeursParSerie comptage pour chaque série (clé = code série, ex: "STEG")
 * @param total      somme toutes séries confondues (colonne "TOTAL" du PDF)
 */
public record StatRow(String label, Map<String, Long> valeursParSerie, long total) {
}
