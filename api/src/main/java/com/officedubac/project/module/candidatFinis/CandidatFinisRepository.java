package com.officedubac.project.module.candidatFinis;

import com.officedubac.project.module.convocations.kafka.NumeroTableOnly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface CandidatFinisRepository extends MongoRepository<CandidatFinis, String>
{
    Optional<CandidatFinis> findByNumeroTable(String numeroTable);

    @Query("{ $or: [ " +
            "{ 'prenoms': { $regex: ?0, $options: 'i' } }, " +
            "{ 'nom': { $regex: ?0, $options: 'i' } }, " +
            "{ 'numeroTable': { $regex: ?0, $options: 'i' } }, " +
            "{ 'serie': { $regex: ?0, $options: 'i' } } " +
            "] }")
    Page<CandidatFinis> search(String keyword, Pageable pageable);

    boolean existsByNumeroTable(String numeroTable);
    @Query(value = "{ 'serie': ?0 }", fields = "{ 'numeroTable': 1, '_id': 0 }")
    List<NumeroTableOnly> findNumeroTableBySerie(String serie);

    @Query(value = "{}", fields = "{ 'numeroTable': 1, '_id': 0 }")
    Stream<CandidatFinis> streamAll();

    // 🔥 Méthode utilitaire (ici 👇)
    default Stream<String> streamAllNumeroTable() {
        return streamAll().map(CandidatFinis::getNumeroTable);
    }
    List<CandidatFinis> findByCentreEcritCode(String centreCode, Pageable pageable);

    // Méthode sans pagination (si besoin)
    List<CandidatFinis> findByCentreEcritCode(String centreCode);

    // Récupérer tous les centres distincts
    @Query(value = "{}", fields = "{ 'centreEcrit.code': 1 }")
    List<CentreProjection> findAllCentres();

}
