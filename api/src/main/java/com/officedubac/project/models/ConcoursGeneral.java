package com.officedubac.project.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "concours_general")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcoursGeneral
{
    @Id
    private String id;
    private String firstname;
    private String lastname;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date_birth;
    private String place_birth;
    private Gender gender;
    private String phone;
    private String classe_0;
    private Double note_student_disc;
    private String classe_1;
    private Double note_classe_disc;
    private String firstname_prof;
    private String lastname_prof;
    private String specialite;
    private Serie serie;
    private Integer session;
    private Etablissement etablissement;
    private String level;

    private int decision;

    private List<Rejet> rejets;

    private String operator;
    private LocalDateTime dateOperation;
}
