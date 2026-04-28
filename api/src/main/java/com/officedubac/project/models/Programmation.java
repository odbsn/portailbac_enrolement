package com.officedubac.project.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Document(collection = "programmation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Programmation
{
    @Id
    private String id;
    private int edition;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date_start;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date_end;
    private int bfem_IfEPI;
    private int bfem_IfI;
    private String codeSup1;
    private String codeSup2;
    @Field("public_key")
    private String publicKey;
    @Field("secret_key")
    private String secretKey;
}
