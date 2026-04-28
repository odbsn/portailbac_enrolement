package com.officedubac.project.module.candidatFinis.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(20)
public class CandidatExportDTO {

    @ExcelProperty("Nom")
    @ColumnWidth(15)
    private String nom;

    @ExcelProperty("Prénoms")
    @ColumnWidth(20)
    private String prenoms;

    @ExcelProperty("Série")
    @ColumnWidth(10)
    private String serie;

    @ExcelProperty("Jury")
    @ColumnWidth(10)
    private String jury;

    @ExcelProperty("N° Table")
    @ColumnWidth(12)
    private String numeroTable;

    @ExcelProperty("N° Dossier")
    @ColumnWidth(12)
    private String numeroDossier;

    @ExcelProperty("Sexe")
    @ColumnWidth(8)
    private String sexe;

    @ExcelProperty("Date Naissance")
    @ColumnWidth(15)
    private String dateNaissance;

    @ExcelProperty("Lieu Naissance")
    @ColumnWidth(20)
    private String lieuNaissance;

    @ExcelProperty("Nationalité")
    @ColumnWidth(15)
    private String nationalite;

    @ExcelProperty("EPS")
    @ColumnWidth(8)
    private String eps;

    @ExcelProperty("Téléphone")
    @ColumnWidth(15)
    private String telephone;

    @ExcelProperty("Établissement")
    @ColumnWidth(30)
    private String etablissement;

    @ExcelProperty("Centre Examen")
    @ColumnWidth(20)
    private String centreExamen;

    @ExcelProperty("Centre Écrit")
    @ColumnWidth(20)
    private String centreEcrit;

    @ExcelProperty("Centre EPS")
    @ColumnWidth(25)
    private String centreActEPS;

    @ExcelProperty("Matières Optionnelles")
    @ColumnWidth(40)
    private String matieresOptionnelles;

    @ExcelProperty("Matières Facultatives")
    @ColumnWidth(40)
    private String matieresFacultatives;

    @ExcelProperty("Type Candidat")
    @ColumnWidth(15)
    private String typeCandidat;

    @ExcelProperty("Statut")
    @ColumnWidth(12)
    private String statutResultat;
}
