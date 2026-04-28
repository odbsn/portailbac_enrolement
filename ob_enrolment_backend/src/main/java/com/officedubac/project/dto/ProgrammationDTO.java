package com.officedubac.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProgrammationDTO
{
    private int edition;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date_start;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date_end;
    private int bfemEPI;
    private int bfemI;
    private String codeSup1;
    private String codeSup2;
    private String publicKey;
    private String secretKey;
}
