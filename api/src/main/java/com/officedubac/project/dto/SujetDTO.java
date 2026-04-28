package com.officedubac.project.dto;

import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.models.Specialite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SujetDTO
{
        @Id
        private String id;
        private String wording;
        private int numSujet;
        private String etab_id;
        private String spec_id;
        private long session;
        //List<Candidat> candidats;
}
