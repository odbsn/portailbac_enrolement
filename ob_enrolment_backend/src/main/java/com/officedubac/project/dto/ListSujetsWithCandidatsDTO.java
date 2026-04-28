package com.officedubac.project.dto;

import com.officedubac.project.models.Candidat;
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
public class ListSujetsWithCandidatsDTO
{
    private Sujet subject;
    private List<Candidat> candidats;

}
