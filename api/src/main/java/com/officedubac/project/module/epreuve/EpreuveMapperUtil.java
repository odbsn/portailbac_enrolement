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

    // ⚠️ EpreuveRequest (matiere/serie/jourDebut/heureDebut) transporte des
    // CODES (voir ses messages de validation : "Le code matière est
    // obligatoire", etc.), pas des ObjectId Mongo. Ces résolveurs doivent
    // donc chercher par code (findByCode), jamais par id (findById) —
    // sinon toute création/modification d'épreuve échoue avec un 404
    // ("...-not-found") dès que le code n'est pas lui-même un ObjectId valide.
    @Named("getMatiereById")
    public Matiere getMatiereById(String code) {
        if (code == null || code.trim().isEmpty()) {
            log.info("Code matière est null ou vide, retourne null");
            return null;
        }

        Matiere response = matiereRepository.findByCode(code);
        if (response == null) {
            throw new BusinessResourceException(
                    "matiere-not-found",
                    "Aucune matière trouvée avec le code: " + code,
                    HttpStatus.NOT_FOUND
            );
        }
        log.info("Matière trouvée avec le code: {}", code);
        return response;
    }

    @Named("getSerieById")
    public Serie getSerieById(String code) {
        if (code == null || code.trim().isEmpty()) {
            log.info("Code série est null ou vide, retourne null");
            return null;
        }

        Serie response = serieRepository.findByCode(code);
        if (response == null) {
            throw new BusinessResourceException(
                    "serie-not-found",
                    "Aucune série trouvée avec le code: " + code,
                    HttpStatus.NOT_FOUND
            );
        }
        log.info("Série trouvée avec le code: {}", code);
        return response;
    }

    @Named("getJourById")
    public Jour getJourById(String code) {
        if (code == null || code.trim().isEmpty()) {
            log.info("Code jour est null ou vide, retourne null");
            return null;
        }

        return jourRepository.findByCode(code)
                .map(jour -> {
                    log.info("Jour trouvé avec le code: {}", code);
                    return jour;
                })
                .orElseThrow(() -> new BusinessResourceException(
                        "jour-not-found",
                        "Aucun jour trouvé avec le code: " + code,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Named("getHeureById")
    public Heure getHeureById(String code) {
        if (code == null || code.trim().isEmpty()) {
            log.info("Code heure est null ou vide, retourne null");
            return null;
        }

        return heureRepository.findByCode(code)
                .map(heure -> {
                    log.info("Heure trouvée avec le code: {}", code);
                    return heure;
                })
                .orElseThrow(() -> new BusinessResourceException(
                        "heure-not-found",
                        "Aucune heure trouvée avec le code: " + code,
                        HttpStatus.NOT_FOUND
                ));
    }
}
