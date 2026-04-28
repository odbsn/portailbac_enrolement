package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "editions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Edition
{
    @Id
    private String id;
    private String name;
    private String open;
    private String close;
    private boolean enabled;
    List<Candidat> candidats;
}
