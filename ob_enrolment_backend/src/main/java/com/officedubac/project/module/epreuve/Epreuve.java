package com.officedubac.project.module.epreuve;

import com.officedubac.project.models.Matiere;
import com.officedubac.project.models.Serie;
import com.officedubac.project.module.heure.Heure;
import com.officedubac.project.module.jour.Jour;
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
@Document(collection = "epreuve")
public class Epreuve {
    @Id
    private String id;

    private Matiere matiere;

    private Serie serie;

    private Integer coefficient;

    private Boolean autorisation;  // OUI/NON

    private Boolean estDominant;

    private Integer nombrePoints;

    private Jour jourDebut;  // Jour de début (pour les épreuves sur plusieurs jours)

    private Heure heureDebut;

    private String duree;  // 04:00:00, 03:00:00, etc.

    private String type;  // "Ecrit" ou "Oral/Pratique"
}
