package com.officedubac.project.module.jour.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourInitialisationRequest {

    private LocalDate dateBacGeneralStart;   // Date de début du bac général
    private LocalDate dateBacTechniqueStart;  // Date de début du bac technique
    private LocalDate dateEPS;                // Date de l'épreuve EPS
    private LocalDate dateLAFAC;              // Date de l'épreuve LAFAC
    private LocalDate dateLBFAC;
    private LocalDate dateJPRJT;// Date de l'épreuve LBFAC
}
