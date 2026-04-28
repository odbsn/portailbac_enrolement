package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "grade")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Grade
{
    @Id
    private String id;
    private String name;
}
