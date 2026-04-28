package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "type_matiere")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeMatiere
{
    @Id
    private String id;
    //General, Fondamental|Professionnel...
    private String name;
}
