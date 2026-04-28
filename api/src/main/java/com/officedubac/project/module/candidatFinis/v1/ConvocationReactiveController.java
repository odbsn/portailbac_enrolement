package com.officedubac.project.module.candidatFinis.v1;

import com.officedubac.project.module.candidatFinis.CandidatFinisService;
import com.officedubac.project.module.candidatFinis.ConvocationReactiveService;
import com.officedubac.project.module.candidatFinis.dto.ConvocationDTO;
import com.officedubac.project.module.convocations.ConvocationPdfService;
import com.officedubac.project.module.convocations.kafka.ConvocationStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/convocations")
@RequiredArgsConstructor
public class ConvocationReactiveController {

    private final ConvocationReactiveService service;
    private final ConvocationPdfService convocationService;
    private final Path basePath = Paths.get("./convocations");
    private final ConvocationStorageConfig storageConfig;
    private  final CandidatFinisService  candidatFinisService;

    @GetMapping("/candidat")
    public Mono<ResponseEntity<ConvocationDTO>> getConvocation(
            @RequestParam String codeEtab,
            @RequestParam String numeroTable,
            @RequestParam String dateNaissance) {

        log.info("📄 Convocation request - Etab: {}, Table: {}, Date: {}",
                codeEtab, numeroTable, dateNaissance);

        return service.findConvocation(codeEtab, numeroTable, dateNaissance)
                .map(dto -> ResponseEntity.ok()
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                        .header("X-Cache-Status", "MISS")
                        .body(dto))
                .onErrorResume(error -> {
                    log.error("Error: {}", error.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

//    // Endpoint pour invalider le cache
//    @DeleteMapping("/cache")
//    public Mono<ResponseEntity<Void>> invalidateCache(
//            @RequestParam String codeEtab,
//            @RequestParam String numeroTable,
//            @RequestParam String dateNaissance) {
//
//        String cacheKey = String.format("conv:%s:%s:%s", codeEtab, numeroTable, dateNaissance);
//        return service.invalidateCache(cacheKey)
//                .then(Mono.just(ResponseEntity.ok().build()));
//    }

    @GetMapping("/{numeroTable}")
    public Mono<ResponseEntity<byte[]>> generate(@PathVariable String numeroTable) {

        return convocationService.generateConvocation(numeroTable)
                .map(pdf -> ResponseEntity.ok()
                        .header("Content-Type", "application/pdf")
                        .header("Content-Disposition", "inline; filename=convocation.pdf")
                        .body(pdf));
    }
    @GetMapping("/download-attachment")
    public Mono<ResponseEntity<Resource>> downloadConvocationAsAttachment(
            @RequestParam String centreCode,
            @RequestParam String numeroTable) {
        try {
            Path filePath = storageConfig.getBasePath()
                    .resolve(sanitizeCode(centreCode))
                    .resolve(numeroTable + ".pdf");

            log.info("📥 Téléchargement demande: {}", filePath.toAbsolutePath());

            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                Resource resource = new FileSystemResource(filePath.toFile());

                return Mono.just(ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=convocation_" + numeroTable + ".pdf")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(filePath)))
                        .body(resource));
            }

            log.warn("⚠️ Fichier non trouvé: {}", filePath);
            return Mono.just(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("❌ Erreur téléchargement: {}", e.getMessage(), e);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }
    private String sanitizeCode(String code) {
        if (code == null) return "unknown";
        // Remplacer les caractères problématiques
        return code.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}