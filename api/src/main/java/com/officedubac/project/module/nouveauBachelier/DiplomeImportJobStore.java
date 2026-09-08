package com.officedubac.project.module.nouveauBachelier;

import com.officedubac.project.module.nouveauBachelier.dto.DiplomeImportJob;
import com.officedubac.project.module.nouveauBachelier.dto.DiplomeImportResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Suivi en mémoire des jobs d'import asynchrone de numeroDiplome (pas de persistance :
// suffisant pour un usage admin ponctuel, perdu au redémarrage de l'application).
@Component
public class DiplomeImportJobStore {
    private final Map<String, DiplomeImportJob> jobs = new ConcurrentHashMap<>();

    public String creer(int totalFichiers) {
        String id = UUID.randomUUID().toString();
        jobs.put(id, DiplomeImportJob.builder()
                .id(id)
                .statut(DiplomeImportJob.Statut.EN_ATTENTE)
                .totalFichiers(totalFichiers)
                .dateDebut(LocalDateTime.now())
                .build());
        return id;
    }

    public void demarrer(String id) {
        jobs.computeIfPresent(id, (k, job) -> {
            job.setStatut(DiplomeImportJob.Statut.EN_COURS);
            return job;
        });
    }

    public void terminer(String id, List<DiplomeImportResult> resultats) {
        jobs.computeIfPresent(id, (k, job) -> {
            job.setStatut(DiplomeImportJob.Statut.TERMINE);
            job.setResultats(resultats);
            job.setDateFin(LocalDateTime.now());
            return job;
        });
    }

    public void echouer(String id, String message) {
        jobs.computeIfPresent(id, (k, job) -> {
            job.setStatut(DiplomeImportJob.Statut.ECHEC);
            job.setErreur(message);
            job.setDateFin(LocalDateTime.now());
            return job;
        });
    }

    public Optional<DiplomeImportJob> get(String id) {
        return Optional.ofNullable(jobs.get(id));
    }
}
