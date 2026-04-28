package com.officedubac.project.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "candidat_to_campusen")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatToCampusen
{
    @Id
    private String id;
    private Long session;
    private int tableNum;
    private int jury;
    private String dosNumber;
    private String firstname;
    private String lastname;
    private LocalDate date_birth;
    private String place_birth;
    private String gender;
    private String phone;
    private String email;
    private int year_registry_num;
    private String registry_num;
    private String type_handicap;
    private String eps;

    private String matiere1;
    private String matiere2;
    private String matiere3;

    // Epreuve Facultative Liste A
    private String eprFacListA;

    // Epreuve Facultative Liste B
    private String eprFacListB;

    private String typeCandidat;
    private String etablissement;
    private String centreEtatCivil;
    private String serie;
    private String nationality;
}
