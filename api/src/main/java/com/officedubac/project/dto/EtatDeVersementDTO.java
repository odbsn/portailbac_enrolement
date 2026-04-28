package com.officedubac.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EtatDeVersementDTO
{
    private String id;
    private String etablissement;
    private Long session;
    private String file_id;
    private Integer count_5000;
    private Integer count_1000_EF;
    private LocalDateTime date_deposit;
}
