package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "etat_de_versement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtatDeVersement
{
    @Id
    private String id;
    private Etablissement etablissement;
    private Long session;
    private String file_id;
    private Integer count_5000;
    private Integer count_1000_EF;
    private LocalDateTime date_deposit;
    private String operator;
    private LocalDateTime date_ops;
    private String motif_correction_vignettes;
    private String correcteur;
    private LocalDateTime date_correction;
    private boolean state;
    private boolean invalid_file;
}
