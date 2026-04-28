package com.officedubac.project.dto;

import com.officedubac.project.models.PorteeMatiere;
import com.officedubac.project.models.Serie;
import com.officedubac.project.models.TypeMatiere;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MatiereDTO {
    private String name;
    private String code;
    private double coef_princ;
    private double coef_prat;
    private double memo;
    private Serie serie;

}