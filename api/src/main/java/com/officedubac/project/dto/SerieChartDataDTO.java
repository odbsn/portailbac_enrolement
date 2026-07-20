package com.officedubac.project.dto;

/**
 * Une ligne de données pour le graphique en barres "Inscrits / Admis par série".
 */
public record SerieChartDataDTO(String serie, long inscrits, long presents, long admis) {
}
