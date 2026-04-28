package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "nationnality")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nationality
{
    @Id
    private String id;
    private String name;
    private String code;
}
