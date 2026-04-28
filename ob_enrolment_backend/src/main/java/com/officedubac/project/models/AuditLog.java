package com.officedubac.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    private String id;
    private String natureOperation;     // "Candidat"
    private String idCandidate;       // ex: 687e28525868513e0bce8c97
    private String operationType;  // POST, PUT, PATCH, DELETE
    private Map<String, Object> fieldValues; // valeurs avant modif
    private String login;
    private String ipAddress;
    private LocalDateTime timestamp = LocalDateTime.now();
}
