package com.officedubac.project.dto;

import java.util.List;

/**
 * Un bloc du tableau récapitulatif (ex: "Admis d'Office") regroupant
 * une ou plusieurs lignes ("Filles", "Garçons", "Total Admis"...).
 *
 * Certains blocs n'ont qu'une seule ligne (ex: "Total Mentions",
 * "Autorisés à faire le 2nd groupe") : on garde tout de même la même
 * structure générique pour que le front n'ait qu'un seul type de rendu
 * à gérer.
 */
public record StatBlock(String titre, List<StatRow> lignes) {
}
