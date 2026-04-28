package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "serie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Serie
{
    @Id
    private String id;
    private String name;
    private String code;

    private TypeFiliere typeFiliere;
    private TypeSerie typeSerie;

    List<Option> options;
}
