package com.officedubac.project.dto;

import com.officedubac.project.models.Ville;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "centre_examen")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CentreExamenDTO
{
    private String id;
    private String name;
    private Ville ville;
}
