package com.officedubac.project.module.jour;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jour")
public class Jour {
    @Id
    private String id;

    private String code;  // J01, J02, J03, J-14, J-38, etc.

    private String name;   // Mar 01 Juil 2025, Sam 14 Juin 2025, etc.
    private LocalDate date;

    private Integer ordre;  // Pour trier chronologiquement
    private String type;   // BAC_GENERAL, BAC_TECHNIQUE, EPS, LAFAC, LBFAC
}
