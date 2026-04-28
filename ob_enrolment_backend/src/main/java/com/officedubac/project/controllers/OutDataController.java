package com.officedubac.project.controllers;

import com.officedubac.project.models.BaseMorte;
import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.CandidatToCampusen;
import com.officedubac.project.services.CandidatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/office-du-bac")
@RequiredArgsConstructor
@Tag(name="Data Controller", description = "Endpoints responsables du partage des données")
public class OutDataController
{
    @Autowired
    private final CandidatService candidatService;
    @Operation(summary="Service de partage des données de candidature au BAC avec CAMPUSEN")
    @GetMapping("/portailbac-to-campusen/{page}/{size}")
    public ResponseEntity<?> dataForCampusen(
            @PathVariable int page,
            @PathVariable int size,
            @RequestParam Long session
    ) {
        Page<CandidatToCampusen> p = this.candidatService.getCandidatsValidesPourCampusen(session, page, size);

        Map<String, Object> res = new HashMap<>();
        res.put("content", p.getContent());
        res.put("totalElements", p.getTotalElements());
        res.put("totalPages", p.getTotalPages());
        res.put("size", p.getSize());
        res.put("page", p.getNumber());

        return ResponseEntity.ok(res);
    }

}
