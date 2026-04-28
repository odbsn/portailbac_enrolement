package com.officedubac.project.dto;

import com.officedubac.project.models.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DepartementDTO
{
    private String name;
    private Region region;
}
