package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "jury")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jury
{
    @Id
    private String id;
    private String code;
}
