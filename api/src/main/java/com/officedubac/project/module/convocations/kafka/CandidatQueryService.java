package com.officedubac.project.module.convocations.kafka;

import com.officedubac.project.module.candidatFinis.CandidatFinis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidatQueryService {

    private final MongoTemplate mongoTemplate;

    /**
     * Récupérer tous les codes centres distincts
     */
    public List<String> findAllDistinctCentreCodes() {
        // Requête pour trouver tous les centres distincts
        List<CandidatFinis> candidats = mongoTemplate.findAll(CandidatFinis.class);

        List<String> centres = candidats.stream()
                .map(c -> {
                    if (c.getCentreEcrit() != null && c.getCentreEcrit().getCode() != null) {
                        return c.getCentreEcrit().getCode();
                    }
                    return null;
                })
                .filter(code -> code != null && !code.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        log.info("📊 {} centres distincts trouvés: {}", centres.size(), centres);
        return centres;
    }

    /**
     * Récupérer les candidats par centre
     */
    public List<CandidatFinis> findCandidatsByCentreCode(String centreCode) {
        Query query = new Query();
        query.addCriteria(where("centreEcrit.code").is(centreCode));

        return mongoTemplate.find(query, CandidatFinis.class);
    }

    /**
     * Compter les candidats par centre
     */
    public long countCandidatsByCentreCode(String centreCode) {
        Query query = new Query();
        query.addCriteria(where("centreEcrit.code").is(centreCode));

        return mongoTemplate.count(query, CandidatFinis.class);
    }
}
