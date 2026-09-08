package com.officedubac.project.module.bacheliersToCampusen;

/** Détail d'un import pour une feuille (série) donnée : total traité, nouveaux, mis à jour. */
public record SerieImportStat(long total, long nouveaux, long misAJour) { }
