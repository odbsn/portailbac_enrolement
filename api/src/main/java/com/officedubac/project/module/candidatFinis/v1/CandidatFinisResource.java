package com.officedubac.project.module.candidatFinis.v1;

import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.module.candidatFinis.CandidatFinisService;
import com.officedubac.project.module.candidatFinis.CandidatImportService;
import com.officedubac.project.module.candidatFinis.ListeCandidatsPdfService;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisRequest;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisResponse;
import com.officedubac.project.module.candidatFinis.dto.PageResponse;
import com.officedubac.project.module.convocations.ConvocationPdfService;
import com.officedubac.project.module.jour.Jour;
import com.officedubac.project.services.ParametrageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/candidats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidat Finis", description = "API de gestion des candidats finis")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CandidatFinisResource {

    private final CandidatFinisService candidatFinisService;
    private final CandidatImportService candidatImportService;
    private final ParametrageService parametrageService;
    private final ListeCandidatsPdfService pdfService;
    private  final  ConvocationPdfService convocationPdfService;

    @GetMapping("/export-pdf")
    ResponseEntity<byte[]> exportPdf() throws IOException {

        // 🔥 Récupérer les candidats (utilisateur connecté)
        List<CandidatFinisResponse> candidats = candidatFinisService.getAllByUtilisateurConnecte();

        // 🔥 Infos dynamiques
        var etablissement = candidatFinisService.getEtablissementUtilisateurConnecte();
        String etablissementNom = etablissement.getName();
        String etablissementCode = etablissement.getCode();
        String centre = candidatFinisService.getEtablissementUtilisateurConnecte().getVille().getName();
        String session = "NORMALE 2026";

        // 🔥 Génération PDF
        byte[] pdf = pdfService.generate(candidats, etablissementNom, etablissementCode, centre, session);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=liste_candidats.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    @GetMapping("/export-zip-by-serie")
    ResponseEntity<byte[]> exportZipBySerie() throws IOException {
        List<CandidatFinisResponse> candidats = candidatFinisService.getAllByUtilisateurConnecte();
        var etablissement = candidatFinisService.getEtablissementUtilisateurConnecte();

        byte[] zip = pdfService.generateZipBySerie(
                candidats,
                etablissement.getName(),
                etablissement.getCode(),
                etablissement.getVille().getName(),
                "NORMALE 2026"
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=listes_candidats_par_serie.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
    // ==================== CRUD ====================
    @PostMapping("/")
    @Operation(summary = "Créer un nouveau candidat", description = "Crée un nouveau candidat avec les informations fournies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Candidat créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide"),
            @ApiResponse(responseCode = "409", description = "Candidat déjà existant")
    })
    public ResponseEntity<CandidatFinisResponse> create(@Valid @RequestBody CandidatFinisRequest request) {
        log.info("POST /api/v1/candidats - Create candidat: {}", request.getNom());
        CandidatFinisResponse response = candidatFinisService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un candidat", description = "Modifie les informations d'un candidat existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidat modifié avec succès"),
            @ApiResponse(responseCode = "404", description = "Candidat non trouvé"),
            @ApiResponse(responseCode = "409", description = "Conflit avec un autre candidat")
    })
    public ResponseEntity<CandidatFinisResponse> update(
            @Parameter(description = "ID du candidat") @PathVariable String id,
            @Valid @RequestBody CandidatFinisRequest request) {
        log.info("PUT /api/v1/candidats/{} - Update candidat", id);
        CandidatFinisResponse response = candidatFinisService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un candidat", description = "Supprime un candidat existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Candidat supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Candidat non trouvé")
    })
    public ResponseEntity<Void> delete(@Parameter(description = "ID du candidat") @PathVariable String id) {
        log.info("DELETE /api/v1/candidats/{} - Delete candidat", id);
        candidatFinisService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un candidat", description = "Récupère les informations d'un candidat par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidat trouvé"),
            @ApiResponse(responseCode = "404", description = "Candidat non trouvé")
    })
    public ResponseEntity<CandidatFinisResponse> getById(@Parameter(description = "ID du candidat") @PathVariable String id) {
        log.info("GET /api/v1/candidats/{} - Get candidat by id", id);
        CandidatFinisResponse response = candidatFinisService.getById(id);
        return ResponseEntity.ok(response);
    }

    // ==================== LISTES AVEC FILTRES (TOUTES AVEC ÉPREUVES) ====================

    // ✅ CORRECTION ICI - Ajouter les paramètres de filtre
    @GetMapping("/all")
    public ResponseEntity<PageResponse<CandidatFinisResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String serie,
            @RequestParam(required = false) String jury,
            @RequestParam(required = false) String numeroDossier,
            @RequestParam(required = false) String typeCandidat,
            @RequestParam(required = false) String statutResultat,
            @RequestParam(required = false) String sexe,
            @RequestParam(required = false) String nationalite,
            @RequestParam(required = false) String etablissementCode,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {

        PageResponse<CandidatFinisResponse> responses = candidatFinisService.getWithFilters(
                keyword, serie, jury, numeroDossier, typeCandidat,
                statutResultat, sexe, nationalite, etablissementCode, pageable);
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/search")
    @Operation(summary = "Rechercher des candidats", description = "Recherche des candidats par mot-clé avec leurs épreuves")
    public ResponseEntity<PageResponse<CandidatFinisResponse>> search(
            @Parameter(description = "Mot-clé de recherche") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/candidats/search - Search candidats with keyword: {}", keyword);
        PageResponse<CandidatFinisResponse> responses = candidatFinisService.search(keyword, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/filters")
    public ResponseEntity<PageResponse<CandidatFinisResponse>> getWithFilters(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String serie,
            @RequestParam(required = false) String jury,
            @RequestParam(required = false) String numeroDossier,
            @RequestParam(required = false) String typeCandidat,
            @RequestParam(required = false) String statutResultat,
            @RequestParam(required = false) String sexe,
            @RequestParam(required = false) String nationalite,
            @RequestParam(required = false) String etablissementCode,  // ← AJOUT
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {

        PageResponse<CandidatFinisResponse> responses = candidatFinisService.getWithFilters(
                keyword, serie, jury, numeroDossier, typeCandidat, statutResultat, sexe, nationalite,
                etablissementCode, pageable);  // ← AJOUT
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/serie/{serieCode}")
    @Operation(summary = "Candidats par série", description = "Récupère les candidats d'une série avec leurs épreuves")
    public ResponseEntity<PageResponse<CandidatFinisResponse>> getBySerie(
            @Parameter(description = "Code de la série") @PathVariable String serieCode,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/candidats/serie/{} - Get candidats by serie with epreuves", serieCode);
        PageResponse<CandidatFinisResponse> responses = candidatFinisService.getBySerie(serieCode, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/jury/{jury}")
    @Operation(summary = "Candidats par jury", description = "Récupère les candidats d'un jury avec leurs épreuves")
    public ResponseEntity<PageResponse<CandidatFinisResponse>> getByJury(
            @Parameter(description = "Jury") @PathVariable String jury,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/candidats/jury/{} - Get candidats by jury with epreuves", jury);
        PageResponse<CandidatFinisResponse> responses = candidatFinisService.getByJury(jury, pageable);
        return ResponseEntity.ok(responses);
    }

    // ==================== MÉTHODES POUR UTILISATEUR CONNECTÉ ====================

    // CandidatFinisResource.java
    @GetMapping("/me")
    public ResponseEntity<PageResponse<CandidatFinisResponse>> getAllByUtilisateurConnecte(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String serie,
            @RequestParam(required = false) String jury,
            @RequestParam(required = false) String numeroDossier,
            @RequestParam(required = false) String typeCandidat,
            @RequestParam(required = false) String statutResultat,
            @RequestParam(required = false) String sexe,
            @RequestParam(required = false) String nationalite,
            @RequestParam(required = false) String etablissementCode,  // ← AJOUT
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {

        PageResponse<CandidatFinisResponse> responses = candidatFinisService.getWithFiltersByUtilisateurConnecte(
                keyword, serie, jury, numeroDossier, typeCandidat, statutResultat, sexe, nationalite,
                etablissementCode, pageable);  // ← AJOUT
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/me/all")
    @Operation(summary = "Lister tous mes candidats sans pagination (option série)")
    public ResponseEntity<List<CandidatFinisResponse>> getAllNoPagination(
            @RequestParam(required = false) String serie) {

        log.info("GET /api/v1/candidats/me/all - serie: {}", serie);

        List<CandidatFinisResponse> responses =
                candidatFinisService.getAllByUtilisateurConnecteNoPagination(serie);

        return ResponseEntity.ok(responses);
    }
    @GetMapping("/me/filters")
    public ResponseEntity<PageResponse<CandidatFinisResponse>> getWithFiltersByUtilisateurConnecte(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String serie,
            @RequestParam(required = false) String jury,
            @RequestParam(required = false) String numeroDossier,
            @RequestParam(required = false) String typeCandidat,
            @RequestParam(required = false) String statutResultat,
            @RequestParam(required = false) String sexe,
            @RequestParam(required = false) String nationalite,
            @RequestParam(required = false) String etablissementCode,  // ← AJOUT
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {

        PageResponse<CandidatFinisResponse> responses = candidatFinisService.getWithFiltersByUtilisateurConnecte(
                keyword, serie, jury, numeroDossier, typeCandidat, statutResultat, sexe, nationalite,
                etablissementCode, pageable);  // ← AJOUT
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/me/export")
    @Operation(summary = "Exporter mes candidats", description = "Exporte la liste des candidats de mon établissement au format Excel")
    public ResponseEntity<byte[]> exportMyCandidats() {
        log.info("GET /api/v1/candidats/me/export - Export tous les candidats de mon établissement");

        ByteArrayInputStream excelFile = candidatFinisService.exportAllCandidatsToExcel();

        String filename = String.format("candidats_%s.xlsx",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile.readAllBytes());
    }
    @GetMapping("/me/{id}")
    @Operation(summary = "Récupérer un de mes candidats", description = "Récupère un candidat de mon établissement par son ID avec ses épreuves")
    public ResponseEntity<CandidatFinisResponse> getByIdByUtilisateurConnecte(
            @Parameter(description = "ID du candidat") @PathVariable String id) {
        log.info("GET /api/v1/candidats/me/{} - Get candidat by id of connected user with epreuves", id);
        CandidatFinisResponse response = candidatFinisService.getByIdByUtilisateurConnecte(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/serie/{serieCode}")
    @Operation(summary = "Mes candidats par série", description = "Récupère les candidats de mon établissement par série avec leurs épreuves")
    public ResponseEntity<PageResponse<CandidatFinisResponse>> getBySerieByUtilisateurConnecte(
            @Parameter(description = "Code de la série") @PathVariable String serieCode,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/candidats/me/serie/{} - Get candidats by serie of connected user with epreuves", serieCode);
        PageResponse<CandidatFinisResponse> responses = candidatFinisService.getBySerieByUtilisateurConnecte(serieCode, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/me/jury/{jury}")
    @Operation(summary = "Mes candidats par jury", description = "Récupère les candidats de mon établissement par jury avec leurs épreuves")
    public ResponseEntity<PageResponse<CandidatFinisResponse>> getByJuryByUtilisateurConnecte(
            @Parameter(description = "Jury") @PathVariable String jury,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/v1/candidats/me/jury/{} - Get candidats by jury of connected user with epreuves", jury);
        PageResponse<CandidatFinisResponse> responses = candidatFinisService.getByJuryByUtilisateurConnecte(jury, pageable);
        return ResponseEntity.ok(responses);
    }

    // ==================== IMPORT EXCEL ====================

    @PostMapping("/import-excel")
    @Operation(summary = "Importer des candidats depuis Excel")
    public ResponseEntity<CandidatImportService.ImportResult> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            CandidatImportService.ImportResult result = candidatImportService.importFromExcel(file);
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
            CandidatImportService.IgnoredAnalysisResult analysis = candidatImportService.analyzeIgnoredRows(file);

            String baseFileName = "ignored_analysis";
            candidatImportService.exportAnalysisToFile(analysis, baseFileName);

            Map<String, Object> response = new HashMap<>();
            response.put("totalAnalyzed", analysis.getTotalAnalyzed());
            response.put("totalIgnored", analysis.getTotalIgnored());
            response.put("ignoredPercentage", String.format("%.2f%%",
                    (analysis.getTotalIgnored() * 100.0) / analysis.getTotalAnalyzed()));
            response.put("reasons", analysis.getReasonCounts());
            response.put("topMissingEstablishments", analysis.getMissingEtablissementsPrincipaux().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(100)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            response.put("topMissingCentresEcrit", analysis.getMissingCentresEcrit().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(100)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            response.put("topMissingCentresEPS", analysis.getMissingCentresEPS().entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(100)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            response.put("message", "Analyse terminée. Les fichiers détaillés ont été générés.");
            response.put("files", new String[]{
                    baseFileName + "_ignored_lines_*.csv",
                    baseFileName + "_missing_establishments_*.csv",
                    baseFileName + "_statistics_*.txt"
            });
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de l'analyse", e);
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }
    // CandidatFinisResource.java
    @GetMapping("/series")
    @Operation(summary = "Liste des séries", description = "Récupère la liste de toutes les séries distinctes")
    public ResponseEntity<List<String>> getAllSeries() {
        log.info("GET /api/v1/candidats/series - Get all distinct series");
        List<String> series = candidatFinisService.getAllDistinctSeries();
        return ResponseEntity.ok(series);
    }
    @GetMapping("/me/series")
    @Operation(summary = "Liste des séries de mon établissement", description = "Récupère la liste des séries distinctes des candidats de l'établissement de l'utilisateur connecté")
    public ResponseEntity<List<String>> getMyDistinctSeries() {
        log.info("GET /api/v1/candidats/me/series - Get distinct series from my establishment");
        List<String> series = candidatFinisService.getMyDistinctSeries();
        log.info("Séries trouvées: {}", series);
        return ResponseEntity.ok(series);
    }
    @GetMapping("/jour-eps")
    public ResponseEntity<Jour> getJourEPS() {
        log.info("📅 Requête: Récupération du jour EPS");

        try {
            Jour jourEPS = candidatFinisService.getJourEPS();

            if (jourEPS == null) {
                log.warn("⚠️ Jour EPS non trouvé");
                return ResponseEntity.notFound().build();
            }
            log.info("✅ Jour EPS trouvé: {} - {}", jourEPS.getCode(), jourEPS.getName());
            return ResponseEntity.ok(jourEPS);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération du jour EPS: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/zip/by-series")
    public ResponseEntity<byte[]> downloadAllConvocationsZip() {
        try {
            // Récupérer toutes les séries distinctes de l'établissement
            List<String> series = candidatFinisService.getMyDistinctSeries();

            if (series.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            // Grouper les candidats par série
            Map<String, List<CandidatFinisResponse>> candidatsParSerie = new LinkedHashMap<>();

            for (String serieCode : series) {
                List<CandidatFinisResponse> candidats = candidatFinisService
                        .getAllByUtilisateurConnecteNoPagination(serieCode);
                if (!candidats.isEmpty()) {
                    candidatsParSerie.put(serieCode, candidats);
                }
            }

            byte[] zipBytes = convocationPdfService.generateConvocationsZipBySeries(candidatsParSerie);

            String filename = "convocations_toutes_series_" + System.currentTimeMillis() + ".zip";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .body(zipBytes);

        } catch (Exception e) {
            log.error("Erreur génération ZIP: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/pdf/serie/{serieCode}")
    public ResponseEntity<byte[]> downloadConvocationsBySerie(@PathVariable String serieCode) {
        try {
            List<CandidatFinisResponse> candidats = candidatFinisService
                    .getAllByUtilisateurConnecteNoPagination(serieCode);

            if (candidats.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            byte[] pdfBytes = convocationPdfService.generateConvocationsPdfBySerie(candidats, serieCode);

            String filename = "convocations_serie_" + serieCode + "_" + System.currentTimeMillis() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("Erreur génération PDF: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    @PostMapping("/regenerate/{numeroTable}")
    public Mono<ResponseEntity<Map<String, String>>> regenerateConvocation(@PathVariable String numeroTable) {
        log.info("🔄 Demande de régénération pour: {}", numeroTable);
        return candidatFinisService.regenerateConvocation(numeroTable)
                .map(filePath -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Convocation régénérée avec succès");
                    response.put("path", filePath);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(error -> {
                    log.error("❌ Erreur: {}", error.getMessage());
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", error.getMessage());

                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    if (error instanceof BusinessResourceException) {
                        status = ((BusinessResourceException) error).getStatus();
                    }

                    return Mono.just(ResponseEntity.status(status).body(response));
                });
    }
}