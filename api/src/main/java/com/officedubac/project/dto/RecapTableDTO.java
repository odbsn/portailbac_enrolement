package com.officedubac.project.dto;

import java.util.List;

/**
 * Réponse complète du tableau récapitulatif, équivalent du tableau
 * "EFFECTIFS ET PERFORMANCES DES CANDIDATS AU BACCALAUREAT" du PDF.
 *
 * @param academie code académie filtré ("TOUTES" si aucun filtre)
 * @param series   colonnes (séries) dans l'ordre officiel d'affichage
 * @param blocs    blocs de lignes, dans l'ordre : Inscrits, Présents,
 *                 Admis d'Office, Mention TBien, Mention Bien, Mention ABien,
 *                 Total Mentions, Autorisés 2nd groupe, Admis 2nd Groupe,
 *                 Total Admis
 */
public record RecapTableDTO(String academie, List<String> series, List<StatBlock> blocs) {
}
