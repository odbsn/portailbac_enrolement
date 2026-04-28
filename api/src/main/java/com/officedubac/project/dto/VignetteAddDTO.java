package com.officedubac.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class VignetteAddDTO
{
    //1 - En cours, 2 - Validé, 3 - Rejeté
    private int v1000;
    private int v5000;
}
