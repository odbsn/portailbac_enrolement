package com.officedubac.project.module.nouveauBachelier.v1;

import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.models.Jury;
import com.officedubac.project.module.nouveauBachelier.NouveauBachelierService;
import com.officedubac.project.module.nouveauBachelier.dto.ImportResult;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierRequest;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/nouveauBacheliers")
@RequiredArgsConstructor
@Slf4j
public class NouveauBachelierResource {
    private final NouveauBachelierService service;
    @GetMapping("/all")
    // @PreAuthorize("hasRole('USER_LISTE') or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<NouveauBachelierResponse>> all(){
        List<NouveauBachelierResponse> response = service.all();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping(value = "/")
    // @PreAuthorize("hasRole('USER_ADD') or hasRole('ADMIN')")
    public ResponseEntity<NouveauBachelierResponse> add(@RequestBody @Valid NouveauBachelierRequest request) {
        NouveauBachelierResponse response = service.add(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    // @PreAuthorize("hasRole('USER_MAJ') or hasRole('ADMIN')")
    public ResponseEntity<NouveauBachelierResponse> maj(@PathVariable(value="id") String id,
                                             @RequestBody @Valid NouveauBachelierRequest request) {
        NouveauBachelierResponse response = service.maj(request, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping(value = "/{id}")
    public ResponseEntity<Optional<NouveauBachelierResponse>> one(@PathVariable(value = "id") String id) {
        Optional<NouveauBachelierResponse> response = service.oneById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    // @PreAuthorize("hasRole('USER_DEL') or hasRole('ADMIN')")
    public ResponseEntity<Void> del(@PathVariable(value="id") String id) {
        service.del(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
//    @ValidateCloudflare
//    @GetMapping("/resultat")
//    public ResponseEntity<Optional<NouveauBachelierResponse>> resultat(
//            @RequestParam String numeroTable) {
//        try {
//            Optional<NouveauBachelierResponse> response = service.searchSimple(numeroTable);
//            return ResponseEntity.ok().body(response);
//        } catch (BusinessResourceException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        }
//    }
//    @PostMapping("/import")
//    public ResponseEntity<List<String>> importerPlusieursFichiersExcel(
//            @RequestParam("files") List<MultipartFile> files) {
//
//        List<String> logsTotaux = new ArrayList<>();
//
//        for (MultipartFile file : files) {
//            if (file.isEmpty()) {
//                logsTotaux.add("Le fichier " + file.getOriginalFilename() + " est vide. Ignoré.");
//                continue;
//            }
//
//            try (InputStream is = file.getInputStream()) {
//                logsTotaux.add("== Fichier : " + file.getOriginalFilename() + " ==");
//                List<String> logs = service.importerDepuisExcel(is);
//                logsTotaux.addAll(logs);
//            } catch (IOException e) {
//                logsTotaux.add("Erreur sur le fichier " + file.getOriginalFilename() + " : " + e.getMessage());
//            }
//        }
//        return ResponseEntity.ok(logsTotaux);
//    }

    @PostMapping("/import/excel")
    public ResponseEntity<ImportResult> importerExcel(
            @RequestParam("file") MultipartFile file) throws IOException {
        ImportResult result = service.importerDepuisExcel(file.getInputStream());
        log.info(result.toSummary());
        return ResponseEntity.ok(result);
    }
    @PostMapping("/import/excel/multiple")
    public ResponseEntity<List<ImportResult>> importerExcelMultiple(
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        log.info("Réception de {} fichier(s) Excel à importer", files.size());
        List<ImportResult> results = service.importerDepuisExcelMultiple(files);
        results.forEach(r -> log.info(r.toSummary()));
        return ResponseEntity.ok(results);
    }
    @PostMapping("/csv")
    public ResponseEntity<List<String>> uploadCsv(@RequestParam("file") MultipartFile file) {
        List<String> logs;

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of("❌ Fichier vide"));
        }

        try {
            logs = service.importerDepuisCsv(file.getInputStream());
            return ResponseEntity.ok(logs);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(List.of("❌ Erreur lors de l'import : " + e.getMessage()));
        }
    }
    @GetMapping("/resultat/{numeroTable}")
    public ResponseEntity<NouveauBachelierResponse> getResultatByNumeroTable(@PathVariable String numeroTable) {
        log.info("Recherche du résultat pour le numéro table: {}", numeroTable);
        try {
            Optional<NouveauBachelierResponse> optional = service.getBachelierByNumeroTable(numeroTable);
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(optional.get());

        } catch (BusinessResourceException e) {
            log.error("Erreur lors de la recherche: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // ✅ NOUVEAU : pagination serveur — GET /api/v1/nouveauBacheliers?page=0&size=10&search=...&sortBy=dateCreation&sortDir=desc
    @GetMapping
    public ResponseEntity<Page<NouveauBachelierResponse>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        log.info("Requête paginée: page={}, size={}, search='{}', sortBy={}, sortDir={}",
                page, size, search, sortBy, sortDir);

        Page<NouveauBachelierResponse> response = service.findAllPaginated(pageable, search);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ✅ NOUVEAU : jurys n'ayant encore aucun bachelier chargé
    @GetMapping("/jurys-non-charges")
    public ResponseEntity<List<Jury>> jurysNonCharges(
            @RequestParam(required = false) Boolean technique
    ) {
        List<Jury> response = service.findJurysNonCharges(technique);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

