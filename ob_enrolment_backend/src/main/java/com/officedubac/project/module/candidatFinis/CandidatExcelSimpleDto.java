package com.officedubac.project.module.candidatFinis;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
public class CandidatExcelSimpleDto {

    @ExcelProperty("Crt. Ecrit")
    @ColumnWidth(15)
    private String centreEcrit;

    @ExcelProperty("Jury")
    @ColumnWidth(10)
    private String jury;

    @ExcelProperty("N° Table")
    @ColumnWidth(10)
    private String numeroTable;

    @ExcelProperty("Série")
    @ColumnWidth(10)
    private String serie;

    @ExcelProperty("Matière(s) Optionnelles")
    @ColumnWidth(25)
    private String matieresOptionnelles;

    @ExcelProperty("Prénom(s)")
    @ColumnWidth(20)
    private String prenoms;

    @ExcelProperty("Nom")
    @ColumnWidth(15)
    private String nom;

    @ExcelProperty("Sexe")
    @ColumnWidth(6)
    private String sexe;

    @ExcelProperty("Date naiss.")
    @DateTimeFormat("dd/MM/yyyy")
    @ColumnWidth(12)
    private String dateNaissance;

    @ExcelProperty("Lieu naiss.")
    @ColumnWidth(20)
    private String lieuNaissance;

    @ExcelProperty("Nationalité")
    @ColumnWidth(12)
    private String nationalite;

    @ExcelProperty("EPS")
    @ColumnWidth(10)
    private String eps;

    @ExcelProperty("Matière(s) Facultatives")
    @ColumnWidth(25)
    private String matieresFacultatives;
}
