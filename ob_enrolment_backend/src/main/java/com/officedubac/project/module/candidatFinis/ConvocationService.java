package com.officedubac.project.module.candidatFinis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConvocationService {

    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private String buildKey(String codeEtab, String numeroTable, String dateNaissance) {
        return "convocation:" + codeEtab + ":" + numeroTable + ":" + dateNaissance;
    }

//    public ConvocationDTO findConvocation(String codeEtab, String numeroTable, String dateNaissance) {
//
//        String key = buildKey(codeEtab, numeroTable, dateNaissance);
//
//        // 🚀 1. CACHE HIT (ULTRA FAST - RAM)
//        ConvocationDTO cached = (ConvocationDTO) redisTemplate.opsForValue().get(key);
//        if (cached != null) {
//            return cached;
//        }
//
//        // 🚀 2. MONGO QUERY OPTIMISÉE
//        Query query = new Query();
//
//        query.addCriteria(Criteria.where("etablissement.code").is(codeEtab)
//                .and("numeroTable").is(numeroTable)
//                .and("dateNaissance").is(dateNaissance));
//
//        query.fields()
//                .include("prenoms")
//                .include("nom")
//                .include("dateNaissance")
//                .include("lieuNaissance")
//                .include("nationalite")
//                .include("numeroTable")
//                .include("jury")
//                .include("serie")
//                .include("sexe")
//                .include("typeCandidat")
//                .include("eps")
//                .include("mo1").include("mo2").include("mo3")
//                .include("ef1").include("ef2")
//                .include("centreMatFac1").include("libMatFac1")
//                .include("centreMatFac2").include("libMatFac2")
//                .include("centreEcrit.name")
//                .include("centreEcritParticulier")
//                .include("centreActEPS.name")
//                .include("etablissement.name");
//
//        Document doc = mongoTemplate.findOne(query, Document.class, "candidat_finis");
//
//        if (doc == null) {
//            throw new RuntimeException("Candidat non trouvé");
//        }
//
//        // 🚀 3. MAPPING ULTRA LIGHT (manuel, pas de mapper)
//        ConvocationDTO result = new ConvocationDTO(
//                doc.getString("prenoms"),
//                doc.getString("nom"),
//                doc.getString("dateNaissance"),
//                doc.getString("lieuNaissance"),
//                doc.getString("nationalite"),
//
//                doc.getString("numeroTable"),
//                doc.getString("jury"),
//                doc.getString("serie"),
//                doc.getString("sexe"),
//
//                doc.getString("typeCandidat"),
//                doc.getString("eps"),
//
//                doc.get("etablissement") != null
//                        ? ((Document) doc.get("etablissement")).getString("name")
//                        : null,
//
//                doc.get("centreEcrit") != null
//                        ? ((Document) doc.get("centreEcrit")).getString("name")
//                        : null,
//
//                doc.getString("centreEcritParticulier"),
//
//                doc.get("centreActEPS") != null
//                        ? ((Document) doc.get("centreActEPS")).getString("name")
//                        : null,
//
//                doc.getString("mo1"),
//                doc.getString("mo2"),
//                doc.getString("mo3"),
//                doc.getString("ef1"),
//                doc.getString("ef2"),
//
//                doc.getString("centreMatFac1"),
//                doc.getString("libMatFac1"),
//                doc.getString("centreMatFac2"),
//                doc.getString("libMatFac2")
//        );
//
//        // 🚀 4. STORE CACHE (TTL 10–30 min)
//        redisTemplate.opsForValue().set(key, result, Duration.ofMinutes(15));
//
//        return result;
//    }
}
