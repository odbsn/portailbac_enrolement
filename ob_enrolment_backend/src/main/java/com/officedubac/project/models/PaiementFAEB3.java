package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "paiements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementFAEB3
{
    @Id
    private String id;
    private String orderNumber;
    private String paymentMode;
    private Double paidSum;
    private String paidAmount;
    private String paymentToken;
    private String paymentStatus;
    private String commandNumber;
    private LocalDateTime paymentValidationDate;
}
