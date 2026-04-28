package com.officedubac.project.module.epreuve;

import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.models.Matiere;
import com.officedubac.project.models.Serie;
import com.officedubac.project.module.heure.Heure;
import com.officedubac.project.module.heure.HeureRepository;
import com.officedubac.project.module.jour.Jour;
import com.officedubac.project.module.jour.JourRepository;
import com.officedubac.project.repository.MatiereRepository;
import com.officedubac.project.repository.SerieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Named;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EpreuveMapperUtil {

    private final MatiereRepository matiereRepository;
    private final SerieRepository serieRepository;
    private final JourRepository jourRepository;
    private final HeureRepository heureRepository;

    @Named("getMatiereById")
    public Matiere getMatiereById(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                log.info("Matière ID est null ou vide, retourne null");
                return null;
            }

            Matiere response = matiereRepository.findById(id)
                    .orElseThrow(() -> new BusinessResourceException(
                            "matiere-not-found",
                            "Aucune matière trouvée avec l'ID: " + id,
                            HttpStatus.NOT_FOUND
                    ));
            log.info("Matière trouvée avec ID: {}", id);
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramètre id {} non autorisé. <EpreuveMapperUtil::getMatiereById>.", id);
            throw new BusinessResourceException(
                    "not-valid-param",
                    "Paramètre " + id + " non autorisé.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @Named("getSerieById")
    public Serie getSerieById(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                log.info("Série ID est null ou vide, retourne null");
                return null;
            }

            Serie response = serieRepository.findById(id)
                    .orElseThrow(() -> new BusinessResourceException(
                            "serie-not-found",
                            "Aucune série trouvée avec l'ID: " + id,
                            HttpStatus.NOT_FOUND
                    ));
            log.info("Série trouvée avec ID: {}", id);
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramètre id {} non autorisé. <EpreuveMapperUtil::getSerieById>.", id);
            throw new BusinessResourceException(
                    "not-valid-param",
                    "Paramètre " + id + " non autorisé.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @Named("getJourById")
    public Jour getJourById(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                log.info("Jour ID est null ou vide, retourne null");
                return null;
            }

            Jour response = jourRepository.findById(id)
                    .orElseThrow(() -> new BusinessResourceException(
                            "jour-not-found",
                            "Aucun jour trouvé avec l'ID: " + id,
                            HttpStatus.NOT_FOUND
                    ));
            log.info("Jour trouvé avec ID: {}", id);
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramètre id {} non autorisé. <EpreuveMapperUtil::getJourById>.", id);
            throw new BusinessResourceException(
                    "not-valid-param",
                    "Paramètre " + id + " non autorisé.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @Named("getHeureById")
    public Heure getHeureById(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                log.info("Heure ID est null ou vide, retourne null");
                return null;
            }

            Heure response = heureRepository.findById(id)
                    .orElseThrow(() -> new BusinessResourceException(
                            "heure-not-found",
                            "Aucune heure trouvée avec l'ID: " + id,
                            HttpStatus.NOT_FOUND
                    ));
            log.info("Heure trouvée avec ID: {}", id);
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramètre id {} non autorisé. <EpreuveMapperUtil::getHeureById>.", id);
            throw new BusinessResourceException(
                    "not-valid-param",
                    "Paramètre " + id + " non autorisé.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
