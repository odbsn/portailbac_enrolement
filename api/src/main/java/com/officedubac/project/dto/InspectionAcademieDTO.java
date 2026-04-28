package com.officedubac.project.dto;

import com.officedubac.project.models.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InspectionAcademieDTO
{
    @Id
    private String id;
    private String name;
    private String code;
    private Region region;
}
