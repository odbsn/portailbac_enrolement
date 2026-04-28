package com.officedubac.project.dto;

import com.officedubac.project.models.Departement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class VilleDTO
{
    private String name;
    private Departement departement;
}
