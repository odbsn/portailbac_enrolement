package com.officedubac.project.module.heure.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class HeureExcelDto {

    @ExcelProperty("Code Heure")
    private String code;

    @ExcelProperty("heure_exament")
    private String heure;
}