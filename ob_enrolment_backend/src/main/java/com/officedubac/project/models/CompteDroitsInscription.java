package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "compte_droits_inscription")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompteDroitsInscription
{
    @Id
    private String id;
    private Etablissement etablissement;
    private Long session;
    private int count_1000_OB;
    private int count_5000;
    private int count_1000_EF;
    private String representative;
    private String phone;
    private boolean enabled;
    private LocalDateTime dateDepot;
}
