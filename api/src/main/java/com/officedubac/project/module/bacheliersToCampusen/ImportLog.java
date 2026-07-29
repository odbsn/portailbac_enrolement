package com.officedubac.project.module.bacheliersToCampusen;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
public class ImportLog
{
    @Id
    private String id;

    @Indexed(unique = true)
    private int annee;

    private String nomFichier;
    private int nombreFeuilles;
    private long nombreCandidats;
    private Map<String, Long> candidatsParSerie;
    private Instant dateImport;

}
