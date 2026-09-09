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

    private LocalDate dateBacGeneralStart;      // Date de début du bac général (1er groupe - J1...)
    private LocalDate dateBacTechniqueStart;    // Date de début du bac technique (1er groupe - J01...)
    private LocalDate dateBacGeneralDTour;      // Date de début du bac général (2ème groupe - Y1...)
    private LocalDate dateBacTechniqueDTour;    // Date de début du bac technique (2ème groupe - Y01...)
    private LocalDate dateEPS;                  // Date de l'épreuve EPS
    private LocalDate dateLAFAC;                // Date de l'épreuve LAFAC
    private LocalDate dateLBFAC;                // Date de l'épreuve LBFAC
    private LocalDate dateJPRJT;                // Date de l'épreuve JPRJT
}
