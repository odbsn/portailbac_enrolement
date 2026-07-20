package com.officedubac.project.module.bacheliersToCampusen.v1;

import com.officedubac.project.module.bacheliersToCampusen.AdmisService;
import com.officedubac.project.module.bacheliersToCampusen.BacheliersToCampusen;
import com.officedubac.project.module.bacheliersToCampusen.dto.SerieStat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/office-du-bac/bacheliers-to-campusen")
public class AdmisController {

    private final AdmisService admisService;

    @GetMapping("/{annee}")
    public Page<BacheliersToCampusen> parAnnee(@PathVariable int annee,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "50") int taille) {
        return admisService.parAnnee(annee, page, taille);
    }

    @GetMapping("/{annee}/series")
    public List<SerieStat> series(@PathVariable int annee)
    {
        return admisService.statistiquesSeries(annee);
    }

    @GetMapping("/{annee}/series/{serie}")
    public Page<BacheliersToCampusen> parSerie(@PathVariable int annee,
                                   @PathVariable String serie,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int taille) {
        return admisService.parAnneeEtSerie(annee, serie, page, taille);
    }

    @GetMapping("/{annee}/candidats/{numeroTable}")
    public BacheliersToCampusen parNumeroTable(@PathVariable int annee, @PathVariable String numeroTable) {
        return admisService.parNumeroTable(annee, numeroTable);
    }
}
