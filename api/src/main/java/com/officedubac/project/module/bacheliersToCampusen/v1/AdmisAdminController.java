package com.officedubac.project.module.bacheliersToCampusen.v1;

import com.officedubac.project.module.bacheliersToCampusen.AdmisService;
import com.officedubac.project.module.bacheliersToCampusen.BacheliersToCampusen;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/** Endpoint interne pour l'interface d'administration (JWT + rôle ADMIN, cf. SecurityConfig).
 *  À distinguer de {@link AdmisController} (/api/v1/office-du-bac/**), exposé à Campusen via clé API.
 *  Liste simple paginée, sans notion de session : les données sont sessionnières
 *  (une seule session en base à la fois), même logique que /api/v1/nouveauBacheliers. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bacheliers-to-campusen")
public class AdmisAdminController {

    private final AdmisService admisService;

    @GetMapping
    public Page<BacheliersToCampusen> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return admisService.findAllPaginated(page, size, search);
    }
}
