package com.officedubac.project.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OperatorDailyCountDTO {
    private String operator;
    private LocalDate dateOperation;
    private int accepted; // nombre de dossiers acceptés
    private int rejected; // nombre de dossiers rejetés
}