package com.officedubac.project.module.candidatFinis;

import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.repository.EtablissementRepository;
import com.officedubac.project.repository.VilleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Named;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
@Slf4j
public class CandidatFinisMapperUtil {
    private final EtablissementRepository etablissementRepository;
    private final VilleRepository villeRepository;


    @Named("getEtablissementById")
    public Etablissement getEtablissementById(String etablissementId) throws NumberFormatException {
        try {
            if (etablissementId == null || etablissementId.trim().isEmpty()) {
                log.info("Etablissement ID est null ou vide, retourne null");
                return null;
            }
            Etablissement response = etablissementRepository.findById(etablissementId)
                    .orElseThrow(
                            () -> new BusinessResourceException("not-found", "Aucune etablissement avec " + etablissementId + " trouvée.", HttpStatus.NOT_FOUND)
                    );
            log.info("Etablissement trouvé avec ID: {}", etablissementId);
            return response;
        } catch (NumberFormatException e) {
            log.warn("Paramétre id {} non autorisé. <ActeursMapperUtil::getEtablissementById>.", etablissementId);
            throw new BusinessResourceException("not-valid-param", "Paramétre " + etablissementId + " non autorisé.", HttpStatus.BAD_REQUEST);
        }
    }
}
