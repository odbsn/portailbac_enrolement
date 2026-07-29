package com.officedubac.project.config;

import java.util.Set;


public final class ResultatLabels {

    private ResultatLabels()
    {

    }

    public static final String ADMIS = "Admis(e)";
    public static final String AUTORISE_2ND_GROUPE = "Autorisé(s) au 2ème groupe d'épreuves";
    public static final String ADMIS_2ND_GROUPE = "Admis(e) au 2nd groupe";
    public static final String EN_ATTENTE = "--------";
    public static final String MENTION_TRES_BIEN = "TBien";
    public static final String MENTION_BIEN = "Bien";
    public static final String MENTION_ASSEZ_BIEN = "ABien";

    public static final Set<String> MENTIONS_CONNUES = Set.of(
            MENTION_TRES_BIEN, MENTION_BIEN, MENTION_ASSEZ_BIEN
    );
}
