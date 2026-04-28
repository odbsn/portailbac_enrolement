package com.officedubac.project.module.epreuve.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class EpreuveExcelDto {

    @ExcelProperty("Code Matière")
    private String matiere;

    @ExcelProperty("Code Série")
    private String serie;

    @ExcelProperty("coef")
    private String coefficient;

    @ExcelProperty("autorisation")
    private String autorisation;

    @ExcelProperty("dominant")
    private String dominant;

    @ExcelProperty("Nombre de points")
    private String nombrePoints;

    @ExcelProperty("Code Jour")
    private String jour;

    @ExcelProperty("Code Heure")
    private String heure;

    @ExcelProperty("Durée")
    private String duree;

    @ExcelProperty("Type (Ecrit/Oral)")
    private String type;
}