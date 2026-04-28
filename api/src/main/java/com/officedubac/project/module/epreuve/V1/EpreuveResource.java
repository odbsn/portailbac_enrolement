package com.officedubac.project.module.epreuve.V1;
import com.officedubac.project.module.epreuve.EpreuveImportService;
import com.officedubac.project.module.epreuve.EpreuveReactiveService;
import com.officedubac.project.module.epreuve.EpreuveService;
import com.officedubac.project.module.epreuve.dto.EpreuveRequest;
import com.officedubac.project.module.epreuve.dto.EpreuveResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/epreuves")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Épreuves", description = "API de gestion des épreuves")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EpreuveResource {

    private final EpreuveService epreuveService;
    private final EpreuveImportService epreuveImportService;
    private final EpreuveReactiveService epreuveReactiveService;

    @PostMapping("/")
    @Operation(summary = "Créer une nouvelle épreuve", description = "Crée une nouvelle épreuve avec les informations fournies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Épreuve créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "409", description = "Épreuve déjà existante")
    })
    public ResponseEntity<EpreuveResponse> create(@Valid @RequestBody EpreuveRequest request) {
        log.info("POST /api/v1/epreuves - Create epreuve: matiereId={}, serieId={}, type={}",
                request.getMatiere(), request.getSerie(), request.getType());
        EpreuveResponse response = epreuveService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une épreuve", description = "Modifie les informations d'une épreuve existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Épreuve modifiée avec succès"),
            @ApiResponse(responseCode = "404", description = "Épreuve non trouvée"),
            @ApiResponse(responseCode = "409", description = "Conflit avec une autre épreuve")
    })
    public ResponseEntity<EpreuveResponse> update(
            @Parameter(description = "ID de l'épreuve") @PathVariable String id,
            @Valid @RequestBody EpreuveRequest request) {
        log.info("PUT /api/v1/epreuves/{} - Update epreuve", id);
        EpreuveResponse response = epreuveService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une épreuve", description = "Supprime une épreuve existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Épreuve supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Épreuve non trouvée")
    })
    public ResponseEntity<Void> delete(@Parameter(description = "ID de l'épreuve") @PathVariable String id) {
        log.info("DELETE /api/v1/epreuves/{} - Delete epreuve", id);
        epreuveService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une épreuve par son ID", description = "Récupère les informations d'une épreuve par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Épreuve trouvée"),
            @ApiResponse(responseCode = "404", description = "Épreuve non trouvée")
    })
    public ResponseEntity<EpreuveResponse> getById(@Parameter(description = "ID de l'épreuve") @PathVariable String id) {
        log.info("GET /api/v1/epreuves/{} - Get epreuve by id", id);
        EpreuveResponse response = epreuveService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Lister toutes les épreuves", description = "Récupère la liste paginée de toutes les épreuves")
    public ResponseEntity<Page<EpreuveResponse>> getAll(
            @PageableDefault(size = 20, sort = "matiere.name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/epreuves - Get all epreuves with pagination");
        Page<EpreuveResponse> responses = epreuveService.getAll(pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des épreuves", description = "Recherche des épreuves par mot-clé (matière, série, type)")
    public ResponseEntity<Page<EpreuveResponse>> search(
            @Parameter(description = "Mot-clé de recherche") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "matiere.nom", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/epreuves/search - Search epreuves with keyword: {}", keyword);
        Page<EpreuveResponse> responses = epreuveService.search(keyword, pageable);
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/filters")
    @Operation(summary = "Rechercher des épreuves avec filtres", description = "Recherche des épreuves avec plusieurs filtres combinés")
    public ResponseEntity<Page<EpreuveResponse>> getWithFilters(
            @Parameter(description = "Mot-clé de recherche") @RequestParam(required = false) String keyword,
            @Parameter(description = "ID de la matière") @RequestParam(required = false) String matiereId,
            @Parameter(description = "ID de la série") @RequestParam(required = false) String serieId,
            @Parameter(description = "Type (Ecrit/Oral/Pratique)") @RequestParam(required = false) String type,
            @Parameter(description = "Autorisation (OUI/NON)") @RequestParam(required = false) Boolean autorisation,
            @Parameter(description = "Matière dominante") @RequestParam(required = false) Boolean estDominant,
            @PageableDefault(size = 20, sort = "matiere.nom", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("GET /api/v1/epreuves/filters - Get epreuves with filters");

        Page<EpreuveResponse> responses = epreuveService.getWithFilters(
                keyword, matiereId, serieId, type, autorisation, estDominant, pageable);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/matiere/{matiereId}")
    @Operation(summary = "Épreuves par matière", description = "Récupère les épreuves d'une matière spécifique")
    public ResponseEntity<Page<EpreuveResponse>> getByMatiere(
            @Parameter(description = "ID de la matière") @PathVariable String matiereId,
            @PageableDefault(size = 20, sort = "type", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/epreuves/matiere/{} - Get epreuves by matiere", matiereId);
        Page<EpreuveResponse> responses = epreuveService.getWithFilters(
                null, matiereId, null, null, null, null, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/serie/{serieId}")
    @Operation(summary = "Épreuves par série", description = "Récupère les épreuves d'une série spécifique")
    public ResponseEntity<Page<EpreuveResponse>> getBySerie(
            @Parameter(description = "ID de la série") @PathVariable String serieId,
            @PageableDefault(size = 20, sort = "matiere.nom", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/epreuves/serie/{} - Get epreuves by serie", serieId);
        Page<EpreuveResponse> responses = epreuveService.getWithFilters(
                null, null, serieId, null, null, null, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Épreuves par type", description = "Récupère les épreuves par type (Ecrit/Oral/Pratique)")
    public ResponseEntity<Page<EpreuveResponse>> getByType(
            @Parameter(description = "Type (Ecrit/Oral/Pratique)") @PathVariable String type,
            @PageableDefault(size = 20, sort = "matiere.nom", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/epreuves/type/{} - Get epreuves by type", type);
        Page<EpreuveResponse> responses = epreuveService.getWithFilters(
                null, null, null, type, null, null, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/check")
    @Operation(summary = "Vérifier si une épreuve existe", description = "Vérifie l'existence d'une épreuve par matière, série et type")
    public ResponseEntity<Boolean> exists(
            @Parameter(description = "ID de la matière") @RequestParam String matiereId,
            @Parameter(description = "ID de la série") @RequestParam String serieId,
            @Parameter(description = "Type (Ecrit/Oral/Pratique)") @RequestParam String type) {

        log.info("GET /api/v1/epreuves/check - Check if epreuve exists: matiereId={}, serieId={}, type={}",
                matiereId, serieId, type);

        Page<EpreuveResponse> result = epreuveService.getWithFilters(
                null, matiereId, serieId, type, null, null, Pageable.unpaged());

        boolean exists = result.getTotalElements() > 0;
        return ResponseEntity.ok(exists);
    }
    @PostMapping("/import-excel")
    @Operation(summary = "Importer des épreuves depuis Excel")
    public ResponseEntity<EpreuveImportService.ImportResult> importFromExcel(
            @RequestParam("file") MultipartFile file) {
        try {
            EpreuveImportService.ImportResult result = epreuveImportService.importFromExcel(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur import: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyser les lignes ignorées")
    public ResponseEntity<?> analyzeIgnoredRows(@RequestParam("file") MultipartFile file) {
        try {
            EpreuveImportService.IgnoredAnalysisResult analysis =
                    epreuveImportService.analyzeIgnoredRows(file);
            String baseFileName = "epreuve_ignored_analysis";
            epreuveImportService.exportAnalysisToFile(analysis, baseFileName);

            Map<String, Object> response = new HashMap<>();
            response.put("totalAnalyzed", analysis.getTotalAnalyzed());
            response.put("totalIgnored", analysis.getTotalIgnored());
            response.put("ignoredPercentage", String.format("%.2f%%",
                    (analysis.getTotalIgnored() * 100.0) / analysis.getTotalAnalyzed()));
            response.put("reasons", analysis.getReasonCounts());
            response.put("topMissingMatieres", analysis.getMissingMatieres().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse", e);
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }
    @PutMapping("/update-types")
    public ResponseEntity<Map<String, Object>> updateEpreuvesTypes() {
        log.info("Requête reçue pour mettre à jour les types des épreuves");

        try {
            int updatedCount = epreuveService.updateEpreuvesTypeDirectly();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mise à jour terminée avec succès");
            response.put("updatedCount", updatedCount);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            if (updatedCount == 0) {
                response.put("warning", "Aucune épreuve n'a été mise à jour. Vérifiez que les matières LAFAC, LBFAC, EPS existent et ont des épreuves associées.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour des types", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/serie/{codeSerie}")
    public Mono<ResponseEntity<List<EpreuveResponse>>> getEpreuvesBySerie(
            @PathVariable String codeSerie) {

        log.info("📚 Récupération des épreuves pour la série: {}", codeSerie);

        return epreuveReactiveService.getBySerie(codeSerie)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(error -> {
                    log.error("Erreur lors de la récupération des épreuves: {}", error.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
