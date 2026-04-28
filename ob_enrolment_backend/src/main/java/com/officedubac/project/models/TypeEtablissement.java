package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "type_etablissement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeEtablissement
{
    @Id
    private String id;
    //EPI, PU, I, PRC, PRL
    private String name;
    private String code;
}