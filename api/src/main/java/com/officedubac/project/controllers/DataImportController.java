package com.officedubac.project.controllers;

import com.officedubac.project.dto.BaseMorteDTO;
import com.officedubac.project.models.BaseMorte;
import com.officedubac.project.services.ImportDataService;
import com.officedubac.project.services.ParametrageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/import-data")
@RequiredArgsConstructor
@Tag(name="Data Import Controller", description = "Endpoints responsables de la gestion des imports de données")

public class DataImportController
{
    @Autowired
    private final ImportDataService importDataService;
    @Autowired
    private final ParametrageService parametrageService;

    @PostMapping("/base-morte")
    public String importBaseMorte(@RequestParam("file") MultipartFile file)
    {
        try {
            // Sauvegarder le fichier temporairement
            File tempFile = File.createTempFile("base_morte_", ".xlsx");
            file.transferTo(tempFile);
            // Appeler le service
            importDataService.importFromExcel(tempFile.getAbsolutePath());
            // Supprimer le fichier temporaire après import
            tempFile.delete();
            return "Import effectué avec succès !";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "Erreur lors de l'import : " + e.getMessage();
        }
    }

    @PostMapping("/codification-etab")
    public String importUserByFile(@RequestParam("file") MultipartFile file)
    {
        String message;
        try
        {
            // Sauvegarder le fichier temporairement
            File tempFile = File.createTempFile("codif_etab_", ".xlsx");
            file.transferTo(tempFile);
            // Appeler le service
            boolean ok = parametrageService.importUserByFile(tempFile.getAbsolutePath());
            // Supprimer le fichier temporaire après import
            tempFile.delete();
            if (ok)
            {
                message = "Les accés ont été créés et transmis avec succés.";
            }
            else
            {
                message = "Aucun accés n\'a été transmis, veuillez vérifier le SMTP ou la Base de données";
            }
            return message;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "Erreur lors de l'import : " + e.getMessage();
        }
    }

    @PostMapping("/checkRedoublantOrFraude/{tableNum}/{exYearBac}")
    public ResponseEntity<BaseMorte> checkRedOrFraud(@PathVariable int tableNum, @PathVariable int exYearBac)
    {
        return ResponseEntity.ok(this.importDataService.checkRedoublantOrFraude(tableNum, exYearBac));
    }

    @PatchMapping("/updateDureeMentionAndEtatCivil")
    public ResponseEntity<BaseMorte> checkRedoublantByEtatCivil_(@RequestParam String idBm, @RequestBody BaseMorteDTO baseMorteDTO)
    {
        return ResponseEntity.ok(this.importDataService.updateArchive(idBm, baseMorteDTO));
    }

    @DeleteMapping(value = "/delete-archive")
    public ResponseEntity<Void> deleteArchive(@RequestParam String idCdt) throws Exception
    {
        this.importDataService.deleteArchive(idCdt);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add-archive")
    public ResponseEntity<BaseMorte> addArchive(@RequestBody BaseMorteDTO baseMorteDTO)
    {
        return ResponseEntity.ok(this.importDataService.createArchive(baseMorteDTO));
    }

    @PostMapping("/checkRedoublantByEtatCivil/{codeCentreEtatCivil}/{yearRegistryNum}")
    public ResponseEntity<BaseMorte> checkRedoublantByEtatCivil(@PathVariable String codeCentreEtatCivil, @PathVariable int yearRegistryNum, @RequestParam String registryNum)
    {
        return ResponseEntity.ok(this.importDataService.checkRedoublantByEtatCivil(codeCentreEtatCivil, yearRegistryNum, registryNum));
    }

    @GetMapping("/get-archives/{page}/{size}")
    public ResponseEntity<?> getArchives(
            @PathVariable int page,
            @PathVariable int size,
            @RequestParam(required = false) String search
    ) {
        Page<BaseMorte> p = this.importDataService.getDataBaseMorte(page, size, search);

        Map<String, Object> res = new HashMap<>();
        res.put("content", p.getContent());
        res.put("totalElements", p.getTotalElements());
        res.put("totalPages", p.getTotalPages());
        res.put("size", p.getSize());
        res.put("page", p.getNumber());

        return ResponseEntity.ok(res);
    }

}
