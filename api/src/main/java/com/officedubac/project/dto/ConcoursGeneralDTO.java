package com.officedubac.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.officedubac.project.models.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ConcoursGeneralDTO
{
    private String id;
    private String firstname;
    private String lastname;
    private String date_birth;
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
    private String etablissement;
    private String level;

    private int decision;

    private List<String> rejets;

    private String operator;

}
