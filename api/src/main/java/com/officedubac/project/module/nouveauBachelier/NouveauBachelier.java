package com.officedubac.project.module.nouveauBachelier;

import com.officedubac.project.models.Jury;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection ="nouveauBachelier")
public class NouveauBachelier {
   @Id
   private String id;
   private String telephone;
   private String prenoms;
   private String nom;
   private String numeroTable;
   private Jury jury;
   private String resultat;
   private String mention;
   private Long utiCree;
   @CreatedDate
   private LocalDateTime dateCreation;
   private Long utiModifie;
   @LastModifiedDate
   private LocalDateTime dateModification;
   private String fichierSource;       // "1111.xlsx"
   private LocalDateTime dateImport;   // dernière date d'import
   private String hashResultat;

}
