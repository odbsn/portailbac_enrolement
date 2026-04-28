package com.officedubac.project.module.heure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "heure")
public class Heure {
    @Id
    private String id;
    private String code;  // H1, H7, etc.
    private String heure;  // 07:30:00, 14:30:00, etc.

    private Integer ordre;  // Pour trier chronologiquement (1 = matin, 7 = après-midi)
}

