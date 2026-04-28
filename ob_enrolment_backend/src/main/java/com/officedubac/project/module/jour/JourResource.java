package com.officedubac.project.module.jour;
import com.officedubac.project.module.jour.dto.JourInitialisationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/jours")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Jours", description = "API de gestion des jours du calendrier")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class JourResource {

    private final JourInitialisationService jourInitialisationService;
    @PostMapping("/initialize")
    @Operation(summary = "Initialiser les jours vides")
    public ResponseEntity<Map<String, Object>> initializeJours() {
        log.info("POST /api/v1/jours/initialize - Initialisation des jours vides");
        jourInitialisationService.initializeEmptyJours();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Jours initialisés avec succès");
        response.put("total", jourInitialisationService.getAllJours().size());

        return ResponseEntity.ok(response);
    }
    @PutMapping("/initialiser")
    @Operation(summary = "Initialiser les noms des jours")
    public ResponseEntity<Map<String, Object>> initialiserJours(@Valid @RequestBody JourInitialisationRequest request) {
        log.info("PUT /api/v1/jours/initialiser - Initialisation des jours");
        log.info("Bac Général début: {}", request.getDateBacGeneralStart());
        log.info("Bac Technique début: {}", request.getDateBacTechniqueStart());
        log.info("EPS: {}", request.getDateEPS());
        log.info("LAFAC: {}", request.getDateLAFAC());
        log.info("LBFAC: {}", request.getDateLBFAC());

        jourInitialisationService.updateJoursNames(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Jours initialisés avec succès");
        response.put("totalJours", jourInitialisationService.getAllJours().size());
        response.put("fullyInitialized", jourInitialisationService.isFullyInitialized());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lister tous les jours")
    public ResponseEntity<List<Jour>> getAllJours() {
        log.info("GET /api/v1/jours - Récupération de tous les jours");
        List<Jour> jours = jourInitialisationService.getAllJours();
        return ResponseEntity.ok(jours);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Lister les jours par type")
    public ResponseEntity<List<Jour>> getJoursByType(@PathVariable String type) {
        log.info("GET /api/v1/jours/type/{} - Récupération des jours par type", type);
        List<Jour> jours = jourInitialisationService.getJoursByType(type);
        return ResponseEntity.ok(jours);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Récupérer un jour par son code")
    public ResponseEntity<Jour> getJourByCode(@PathVariable String code) {
        log.info("GET /api/v1/jours/{} - Récupération du jour", code);
        Jour jour = jourInitialisationService.getJourByCode(code);
        if (jour == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(jour);
    }

    @GetMapping("/non-initialises")
    @Operation(summary = "Jours non initialisés")
    public ResponseEntity<List<Jour>> getUninitializedJours() {
        log.info("GET /api/v1/jours/non-initialises - Récupération des jours non initialisés");
        List<Jour> jours = jourInitialisationService.getUninitializedJours();
        return ResponseEntity.ok(jours);
    }

    @GetMapping("/statut")
    @Operation(summary = "Statut d'initialisation")
    public ResponseEntity<Map<String, Object>> getInitialisationStatut() {
        log.info("GET /api/v1/jours/statut - Vérification du statut");

        Map<String, Object> response = new HashMap<>();
        response.put("fullyInitialized", jourInitialisationService.isFullyInitialized());
        response.put("totalJours", jourInitialisationService.getAllJours().size());
        response.put("nonInitialises", jourInitialisationService.getUninitializedJours().size());

        return ResponseEntity.ok(response);
    }
}
