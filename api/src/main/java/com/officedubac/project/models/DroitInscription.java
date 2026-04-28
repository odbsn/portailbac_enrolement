package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection = "droits_inscription")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DroitInscription
{
    @Id
    private String id;
    private String orderNumber;
    private String establishment;
    private LocalDateTime dateTransaction;
    private Long session;
    private int nbCdtsInscrits;
    private double montantAVerser;
    private String phoneNumber;
    private boolean paid;
    private double montantVerser=0;
    private List<PaiementFAEB3> paiements;

}
