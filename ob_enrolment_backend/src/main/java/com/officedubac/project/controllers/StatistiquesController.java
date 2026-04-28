package com.officedubac.project.controllers;

import com.officedubac.project.dto.*;
import com.officedubac.project.models.*;
import com.officedubac.project.services.AuthenticationService;
import com.officedubac.project.services.ParametrageService;
import com.officedubac.project.services.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name="Statistiques Controller", description = "Endpoints responsables de la gestion des statistiques de la plateforme")
public class StatistiquesController
{
    @Autowired
    private final StatsService statsService;

    @Operation(summary="Service de recupération des statistiques")
    @GetMapping("/stats-globales/{session}")
    public ResponseEntity<GlobalStatDTO> statsGlobales(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getGlobalStat(session));
    }


    @Operation(summary="Service de recupération des statistiques CGS")
    @GetMapping("/stats-globales-cgs/{session}")
    public ResponseEntity<GlobalStatDTO> statsGlobales_(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getGlobalStat_(session));
    }

    @Operation(summary="Service de recupération des statistiques par IA")
    @GetMapping("/stats-globales-by-IA/{session}/{ia}")
    public ResponseEntity<GlobalStatDTO> statsGlobalesIA(@PathVariable int session, @PathVariable String ia)
    {
        return ResponseEntity.ok(statsService.getGlobalStatIA(session, ia));
    }

    @Operation(summary="Service de recupération des statistiques par IA")
    @GetMapping("/stats-globales-etab-by-IA/{session}/{ia}")
    public ResponseEntity<List<EtablissementSummaryReception>> statsGlobalesEtabIA(@PathVariable long session, @PathVariable String ia)
    {
        return ResponseEntity.ok(statsService.summarizeByIA(session, ia));
    }

    @GetMapping("/stats-globales-vignettes/{session}")
    public ResponseEntity<GlobalStatVCDTO> statsGlobalesVC(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getGlobalStatVC(session));
    }
    @GetMapping("/stats-nationales/{session}")
    public ResponseEntity<List<MapDTO>> statsNationales(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getStatsParDepartement(session));
    }

    @GetMapping("/stats-by-academie/{session}")
    public ResponseEntity<List<StatAcademieDTO>> statsByAcademie(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getStatsParAcademie(session));
    }


    @GetMapping("/statsCGS-by-academie/{session}")
    public ResponseEntity<List<StatAcademieDTO>> statsCGSByAcademie_(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getStatsCGSParAcademie_(session));
    }

    @GetMapping("/statsCGS-by-type-etab/{session}")
    public ResponseEntity<List<StatAcademieDTO>> statsCGSByTypeEtab(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getStatsCGSTypeEtab(session));
    }

    @GetMapping("/stats-by-type-etab/{session}")
    public ResponseEntity<List<StatAcademieDTO>> statsCGSByTypeEtab_(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getStatsTypeEtab(session));
    }

    @GetMapping("/stats-by-handicap/{session}")
    public ResponseEntity<List<StatHandicapDTO>> statsByHandicap(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getStatsParHandicap(session));
    }

    @GetMapping("/stats-by-discipline")
    public ResponseEntity<List<StatDisciplineDTO>> statsByHandicap(@RequestParam int session, @RequestParam String level)
    {
        return ResponseEntity.ok(statsService.getStatsSpeciaNiveau(session, level));
    }

    @GetMapping("/stats-by-serie/{session}")
    public ResponseEntity<List<StatSerieDTO>> statsBySerie(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getStatsParSerie(session));
    }

    @GetMapping("/stats-for-litteraire/{session}")
    public ResponseEntity<List<StatSerieDTO>> statsByTypeSerie(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getStatsForLitteraire(session));
    }

    @GetMapping("/stats-for-science/{session}")
    public ResponseEntity<List<StatSerieDTO>> statsByTypeSerie_(@PathVariable int session)
    {
        return ResponseEntity.ok(statsService.getStatsForScience(session));
    }





}
