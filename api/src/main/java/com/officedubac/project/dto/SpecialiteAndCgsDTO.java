package com.officedubac.project.dto;

import com.officedubac.project.models.SpecialiteCGS;
import com.officedubac.project.models.Sujet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SpecialiteAndCgsDTO
{
    private SpecialiteCGS specialite;
    private List<String> candidats;
}
