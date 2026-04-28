package com.officedubac.project.dto;

import com.officedubac.project.models.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UniversiteDTO
{
    @Id
    private String id;
    private String name;
    private String code;
    private Region region;
}
