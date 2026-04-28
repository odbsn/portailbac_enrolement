package com.officedubac.project.dto;

import com.officedubac.project.models.Etablissement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EtablissementSummaryReceptionScolarite
{
    private Etablissement etablissement;
    private int decision0 = 0;
    private int decision1 = 0;
    private int decision2 = 0;
    private String operator;
    private String representative;
    private String phone;
    private int count_5000 = 0;
    private String dateDepot;

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

}
