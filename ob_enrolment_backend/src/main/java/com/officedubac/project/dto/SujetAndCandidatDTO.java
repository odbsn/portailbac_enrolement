package com.officedubac.project.dto;

import com.officedubac.project.models.Sujet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.security.auth.Subject;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SujetAndCandidatDTO
{
    private Sujet subject;
    private String etablissementId;
    private Long session;
    private List<String> candidats;
}
