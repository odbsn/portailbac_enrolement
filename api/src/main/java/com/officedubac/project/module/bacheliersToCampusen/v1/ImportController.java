package com.officedubac.project.module.bacheliersToCampusen.v1;

import com.officedubac.project.module.bacheliersToCampusen.ImportBacheliersService;
import com.officedubac.project.module.bacheliersToCampusen.ImportLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/imports-bacheliers")
public class ImportController {

    private final ImportBacheliersService importBacheliersService;
    @PostMapping("/load")
    public ResponseEntity<ImportLog> importer(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam("annee") int annee,
            @RequestParam(value = "remplacer", defaultValue = "false") boolean remplacer)
    {

        if (fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }
        String nom = fichier.getOriginalFilename();
        if (nom == null || !nom.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Seuls les fichiers .xlsx sont acceptés");
        }
        ImportLog resultat = importBacheliersService.importer(fichier, annee, remplacer);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultat);
    }
}
