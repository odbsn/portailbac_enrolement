package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "type_filiere")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeFiliere
{
    @Id
    private String id;
    //Littéraire, Science & Technique, Tertiaire, Science, Technique
    private String name;
}
