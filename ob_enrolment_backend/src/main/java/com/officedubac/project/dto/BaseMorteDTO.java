package com.officedubac.project.dto;

import com.officedubac.project.models.TypeFiliere;
import com.officedubac.project.models.TypeSerie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BaseMorteDTO
{
        private int tableNum;
        private int exYearBac;
        private String firstname;
        private String lastname;
        private LocalDate date_birth;
        private String place_birth;
        private String gender;
        private String countryBirth;
        private String etablissement;
        private int bac_do_count;
        private String codeCentreEtatCivil;
        private int yearRegistryNum;
        private String registryNum;
        private int exclusionDuree;
        private String codeEnrolement;
}
