package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "type_serie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeSerie
{
    @Id
    private String id;
    //General, Technique...
    private String name;
}
