package com.officedubac.project.module.epreuve;

import com.officedubac.project.module.jour.Jour;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpreuveCascadeService {

    private final MongoTemplate mongoTemplate;
    private final EpreuveRepository epreuveRepository;

    /**
     * Met à jour en cascade toutes les épreuves avec le jour complet
     */
    public void updateEpreuvesWithJour(Jour jour) {
        log.info("🔄 Mise à jour en cascade des épreuves pour le jour: {} - date: {}", jour.getCode(), jour.getDate());

        // Requête: trouver toutes les épreuves qui ont ce jour
        Query query = Query.query(Criteria.where("jourDebut._id").is(jour.getId()));

        // Mise à jour: remplacer l'objet jour complet (code, name, date, ordre, type)
        Update update = new Update()
                .set("jourDebut", jour)
                .set("dateModification", LocalDateTime.now());

        // Exécuter la mise à jour
        long updatedCount = mongoTemplate.updateMulti(query, update, Epreuve.class)
                .getModifiedCount();

        log.info("✅ {} épreuve(s) mise(s) à jour avec le jour: {} (date: {})",
                updatedCount, jour.getCode(), jour.getDate());
    }

    /**
     * Met à jour uniquement la date du jour dans les épreuves
     */
    public void updateEpreuvesJourDate(String jourId, LocalDate newDate, String newName) {
        log.info("🔄 Mise à jour de la date du jour dans les épreuves: {} -> {}", jourId, newDate);

        Query query = Query.query(Criteria.where("jourDebut._id").is(jourId));
        Update update = new Update()
                .set("jourDebut.date", newDate)
                .set("jourDebut.name", newName)
                .set("dateModification", LocalDateTime.now());

        long updatedCount = mongoTemplate.updateMulti(query, update, Epreuve.class)
                .getModifiedCount();

        log.info("✅ {} épreuve(s) mise(s) à jour avec la nouvelle date: {}", updatedCount, newDate);
    }

    /**
     * Met à jour toutes les épreuves avec les nouvelles informations du jour
     * (nom, code, ordre, type, date)
     */
    public void syncJourToEpreuves(Jour jour) {
        log.info("🔄 Synchronisation du jour {} vers toutes les épreuves", jour.getCode());

        // Récupérer toutes les épreuves qui utilisent ce jour
        List<Epreuve> epreuves = epreuveRepository.findByJourDebutId(jour.getId());

        if (epreuves.isEmpty()) {
            log.info("Aucune épreuve ne référence ce jour");
            return;
        }

        // Mettre à jour chaque épreuve
        for (Epreuve epreuve : epreuves) {
            epreuve.setJourDebut(jour);  // L'objet jour complet est mis à jour
            epreuveRepository.save(epreuve);
        }

        log.info("✅ {} épreuve(s) synchronisée(s) avec le jour {} (date: {})",
                epreuves.size(), jour.getCode(), jour.getDate());
    }

    /**
     * Mise à jour en cascade complète quand un jour est modifié
     */
    public void cascadeJourUpdate(Jour jour) {
        log.info("🚀 Cascade complète pour le jour: {} - {} - date: {}",
                jour.getCode(), jour.getName(), jour.getDate());

        updateEpreuvesWithJour(jour);

        log.info("✅ Cascade terminée pour le jour: {}", jour.getCode());
    }
}