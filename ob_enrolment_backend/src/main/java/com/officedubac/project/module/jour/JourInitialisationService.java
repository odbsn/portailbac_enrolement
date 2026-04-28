package com.officedubac.project.module.jour;

import com.officedubac.project.module.epreuve.EpreuveCascadeService;
import com.officedubac.project.module.jour.dto.JourInitialisationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class JourInitialisationService {

    private final JourRepository jourRepository;
    private final EpreuveCascadeService epreuveCascadeService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE dd MMM yyyy", Locale.FRENCH);

    /**
     * Initialise tous les jours avec leurs types
     */
    public void initializeEmptyJours() {
        log.info("Initialisation des jours vides...");

        if (jourRepository.count() > 0) {
            log.info("Les jours sont déjà initialisés. Count: {}", jourRepository.count());
            return;
        }

        // ==================== BAC GÉNÉRAL (J1 à J10) ====================
        for (int i = 1; i <= 10; i++) {
            String code = "J" + i;
            Jour jour = Jour.builder()
                    .code(code)
                    .name("")
                    .date(null)
                    .ordre(i)
                    .type("BAC_GENERAL")
                    .build();
            jourRepository.save(jour);
            log.debug("Jour créé: {} - type: BAC_GENERAL", code);
        }

        // ==================== BAC TECHNIQUE (J01 à J10) ====================
        for (int i = 1; i <= 10; i++) {
            String code = String.format("J%02d", i);
            Jour jour = Jour.builder()
                    .code(code)
                    .name("")
                    .date(null)
                    .ordre(100 + i)
                    .type("BAC_TECHNIQUE")
                    .build();
            jourRepository.save(jour);
            log.debug("Jour créé: {} - type: BAC_TECHNIQUE", code);
        }

        // ==================== EPS ====================
        Jour eps = Jour.builder()
                .code("JEPS")
                .name("")
                .date(null)
                .ordre(1000)
                .type("EPS")
                .build();
        jourRepository.save(eps);
        log.debug("Jour créé: JEPS - type: EPS");

        // ==================== FACULTATIVES ====================
        Jour lafac = Jour.builder()
                .code("JLAFAC")
                .name("")
                .date(null)
                .ordre(2000)
                .type("FACULTATIVE")
                .build();
        jourRepository.save(lafac);
        log.debug("Jour créé: JLAFAC - type: FACULTATIVE");

        Jour lbfac = Jour.builder()
                .code("JLBFAC")
                .name("")
                .date(null)
                .ordre(3000)
                .type("FACULTATIVE")
                .build();
        jourRepository.save(lbfac);
        log.debug("Jour créé: JLBFAC - type: FACULTATIVE");

        log.info("{} jours initialisés avec succès", jourRepository.count());
    }

    /**
     * Met à jour les noms des jours à partir des dates fournies
     * AVEC CASCADE VERS LES ÉPREUVES
     */
    public void updateJoursNames(JourInitialisationRequest request) {
        log.info("Mise à jour des noms des jours...");

        int updatedCount = 0;

        // 1. Mettre à jour les jours du bac général (type = BAC_GENERAL)
        updatedCount += updateJoursByType("BAC_GENERAL", request.getDateBacGeneralStart());

        // 2. Mettre à jour les jours du bac technique (type = BAC_TECHNIQUE)
        updatedCount += updateJoursByType("BAC_TECHNIQUE", request.getDateBacTechniqueStart());

        // 3. Mettre à jour EPS (type = EPS)
        updatedCount += updateJourByTypeAndCode("EPS", "JEPS", request.getDateEPS());

        // 4. Mettre à jour LAFAC (type = FACULTATIVE)
        updatedCount += updateJourByTypeAndCode("FACULTATIVE", "JLAFAC", request.getDateLAFAC());

        // 5. Mettre à jour LBFAC (type = FACULTATIVE)
        updatedCount += updateJourByTypeAndCode("FACULTATIVE", "JLBFAC", request.getDateLBFAC());

        log.info("{} jours mis à jour avec succès", updatedCount);
    }

    /**
     * Met à jour tous les jours d'un type spécifique
     */
    private int updateJoursByType(String type, LocalDate startDate) {
        if (startDate == null) {
            log.warn("Date de début non fournie pour le type: {}", type);
            return 0;
        }

        List<Jour> jours = jourRepository.findByType(type);

        if (jours.isEmpty()) {
            log.warn("Aucun jour trouvé pour le type: {}", type);
            return 0;
        }

        int count = 0;
        for (Jour jour : jours) {
            String code = jour.getCode();
            int numero = extractNumberFromCode(code, type);
            LocalDate date = startDate.plusDays(numero - 1);
            String name = formatDate(date);

            jour.setName(name);
            jour.setDate(date);  // ✅ Ajout de la date
            jourRepository.save(jour);

            // CASCADE: Mettre à jour les épreuves qui utilisent ce jour
            epreuveCascadeService.updateEpreuvesWithJour(jour);

            count++;
            log.debug("Jour {} mis à jour: {} -> {} (cascade vers épreuves)", code, name, date);
        }

        log.info("{} jours de type {} mis à jour", count, type);
        return count;
    }

    /**
     * Met à jour un jour spécifique par son type et son code
     */
    private int updateJourByTypeAndCode(String type, String code, LocalDate date) {
        if (date == null) {
            log.warn("Date non fournie pour le jour: {}", code);
            return 0;
        }

        return jourRepository.findByTypeAndCode(type, code).map(jour -> {
            String name = formatDate(date);
            jour.setName(name);
            jour.setDate(date);  // ✅ Ajout de la date
            jourRepository.save(jour);

            // CASCADE: Mettre à jour les épreuves qui utilisent ce jour
            epreuveCascadeService.updateEpreuvesWithJour(jour);

            log.debug("Jour {} mis à jour: {} -> {} (cascade vers épreuves)", code, name, date);
            return 1;
        }).orElse(0);
    }

    /**
     * Extrait le numéro du code en fonction du type
     */
    private int extractNumberFromCode(String code, String type) {
        if (type.equals("BAC_GENERAL")) {
            // J1 -> 1, J2 -> 2
            return Integer.parseInt(code.substring(1));
        } else if (type.equals("BAC_TECHNIQUE")) {
            // J01 -> 1, J02 -> 2
            return Integer.parseInt(code.substring(2));
        }
        return 1;
    }

    /**
     * Met à jour un jour spécifique avec son nom (par code)
     * AVEC CASCADE VERS LES ÉPREUVES
     */
    public boolean updateJourName(String code, String name) {
        return jourRepository.findByCode(code).map(jour -> {
            jour.setName(name);
            // Note: la date reste inchangée, seulement le nom d'affichage
            jourRepository.save(jour);

            // CASCADE: Mettre à jour les épreuves qui utilisent ce jour
            epreuveCascadeService.updateEpreuvesWithJour(jour);

            log.info("Jour {} mis à jour: {} (cascade vers épreuves)", code, name);
            return true;
        }).orElse(false);
    }

    /**
     * Met à jour un jour spécifique avec sa date (par code)
     * AVEC CASCADE VERS LES ÉPREUVES
     */
    public boolean updateJourDate(String code, LocalDate date) {
        if (date == null) return false;

        return jourRepository.findByCode(code).map(jour -> {
            String name = formatDate(date);
            jour.setName(name);
            jour.setDate(date);  // ✅ Ajout de la date
            jourRepository.save(jour);

            // CASCADE: Mettre à jour les épreuves qui utilisent ce jour
            epreuveCascadeService.updateEpreuvesWithJour(jour);

            log.info("Jour {} mis à jour: {} -> {} (cascade vers épreuves)", code, name, date);
            return true;
        }).orElse(false);
    }

    /**
     * Vérifie si tous les jours ont leurs noms renseignés
     */
    public boolean isFullyInitialized() {
        List<Jour> jours = jourRepository.findAll();
        return jours.stream().allMatch(jour -> jour.getName() != null && !jour.getName().isEmpty());
    }

    /**
     * Récupère les jours qui n'ont pas encore de nom
     */
    public List<Jour> getUninitializedJours() {
        return jourRepository.findAll().stream()
                .filter(jour -> jour.getName() == null || jour.getName().isEmpty())
                .toList();
    }

    /**
     * Récupère tous les jours
     */
    public List<Jour> getAllJours() {
        return jourRepository.findAllByOrderByOrdreAsc();
    }

    /**
     * Récupère un jour par son code
     */
    public Jour getJourByCode(String code) {
        return jourRepository.findByCode(code).orElse(null);
    }

    /**
     * Récupère les jours par type
     */
    public List<Jour> getJoursByType(String type) {
        return jourRepository.findByType(type);
    }

    /**
     * Formate une date en français
     */
    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }
}