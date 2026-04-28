package com.officedubac.project.dto;

import com.officedubac.project.models.Option;
import com.officedubac.project.models.TypeFiliere;
import com.officedubac.project.models.TypeSerie;
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
public class SerieDTO
{
        @Id
        private String id;
        private String name;
        private String code;

        private TypeFiliere type_filiere;
        private TypeSerie type_serie;

}
