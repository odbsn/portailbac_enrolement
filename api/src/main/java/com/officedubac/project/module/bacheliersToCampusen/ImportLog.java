package com.officedubac.project.module.bacheliersToCampusen;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "importsLog")
@CompoundIndex(name = "uk_annee_typeSession", def = "{'annee': 1, 'typeSession': 1}", unique = true)
public class ImportLog
{
    @Id
    private String id;

    private int annee;
    /** Un journal par (annee, typeSession) : "Normale" ou "Remplacement". */
    private String typeSession;

    private String nomFichier;
    private int nombreFeuilles;
    private long nombreCandidats;
    private long nombreNouveaux;
    private long nombreMisAJour;
    private Map<String, SerieImportStat> candidatsParSerie;
    private Instant dateImport;

}
