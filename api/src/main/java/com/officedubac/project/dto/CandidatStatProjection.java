package com.officedubac.project.dto;

/**
 * Projection "à plat" issue du pipeline d'agrégation (CandidatFinis +
 * $lookup sur la collection résultats). Une ligne par candidat.
 *
 * On reste volontairement simple ici : Mongo se contente de joindre et
 * d'aplatir, tout le calcul des statistiques (comptages, pivot par série/sexe)
 * est ensuite fait côté Java dans {@code DashboardStatsService}, ce qui est
 * largement assez performant pour un volume de quelques dizaines de milliers
 * de candidats, et beaucoup plus simple à maintenir/déboguer qu'un pipeline
 * Mongo avec des $group conditionnels imbriqués.
 */
public class CandidatStatProjection {

    private String serie;
    private String sexe;

    /** "0" = présent, "1" = absent (tel qu'observé dans CandidatFinis). */
    private String absence;

    /** Peut être null si aucun résultat encore publié pour ce candidat. */
    private String resultat;

    /** Peut être null/vide si admis sans mention, ou pas encore admis. */
    private String mention;

    public CandidatStatProjection() {
    }

    public CandidatStatProjection(String serie, String sexe, String absence, String resultat, String mention) {
        this.serie = serie;
        this.sexe = sexe;
        this.absence = absence;
        this.resultat = resultat;
        this.mention = mention;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getAbsence() {
        return absence;
    }

    public void setAbsence(String absence) {
        this.absence = absence;
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }
}
