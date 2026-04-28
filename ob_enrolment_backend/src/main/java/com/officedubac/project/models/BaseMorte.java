package com.officedubac.project.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Document(collection = "base_morte")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseMorte
{
    @Id
    private String id;
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
