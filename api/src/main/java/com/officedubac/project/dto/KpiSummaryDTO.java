package com.officedubac.project.dto;

import java.util.Map;

/**
 * Indicateurs clés affichés en haut du dashboard, équivalents des encarts
 * rouge/bleu du PDF ("TOTAL INSCRITS = 3354", "TAUX DE REUSSITE = 49,47%"...).
 *
 * @param academie        académie filtrée ("TOUTES" si pas de filtre)
 * @param totalInscrits   nombre total de candidats inscrits
 * @param totalPresents   nombre total de présents (inscrits - absents)
 * @param totalAbsents    nombre total d'absents
 * @param totalAdmis      Admis d'Office + Admis 2nd Groupe
 * @param totalMentions   nombre d'admis ayant obtenu une mention (TB+B+AB)
 * @param tauxReussite    totalAdmis / totalPresents * 100, arrondi à 2 décimales
 * @param repartitionParFiliere effectifs inscrits par filière (S&T, Tertiaire, Littéraire)
 */
public record KpiSummaryDTO(
        String academie,
        long totalInscrits,
        long totalPresents,
        long totalAbsents,
        long totalAdmis,
        long totalMentions,
        double tauxReussite,
        Map<String, Long> repartitionParFiliere
) {
}
