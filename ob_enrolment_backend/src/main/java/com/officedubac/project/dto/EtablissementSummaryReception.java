package com.officedubac.project.dto;

import com.officedubac.project.models.Etablissement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EtablissementSummaryReception
{
    private Etablissement etablissement;
    private int decision0 = 0;
    private int decision1 = 0;
    private int decision2 = 0;
    private List<String> operators = new ArrayList<>();

    // Incrémentation des décisions
    public void incrementDecision0() {
        this.decision0++;
    }

    public void incrementDecision1() {
        this.decision1++;
    }

    public void incrementDecision2() {
        this.decision2++;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

    /** Fusionner les compteurs d'un autre summary dans celui-ci */
    public void copyInto(EtablissementSummaryReception other) {
        other.decision0 += this.decision0;
        other.decision1 += this.decision1;
        other.decision2 += this.decision2;
    }
}
