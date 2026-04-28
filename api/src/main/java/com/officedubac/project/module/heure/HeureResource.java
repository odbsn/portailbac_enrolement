package com.officedubac.project.module.heure;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/heures")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Heures", description = "API de gestion des heures")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HeureResource {

    private final HeureRepository heureRepository;
    private final HeureImportService heureImportService;

    @PostMapping("/import-excel")
    @Operation(summary = "Importer des heures depuis Excel")
    public ResponseEntity<HeureImportService.ImportResult> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            HeureImportService.ImportResult result = heureImportService.importFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur import: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    @PostMapping("/initialize-default")
    @Operation(summary = "Initialiser les heures par défaut")
    public ResponseEntity<Map<String, Object>> initializeDefaultHeures() {
        heureImportService.initializeDefaultHeures();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Heures par défaut initialisées");
        response.put("total", heureRepository.count());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les heures")
    public ResponseEntity<List<Heure>> getAllHeures() {
        List<Heure> heures = heureRepository.findAll();
        return ResponseEntity.ok(heures);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Récupérer une heure par son code")
    public ResponseEntity<Heure> getHeureByCode(@PathVariable String code) {
        return heureRepository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
