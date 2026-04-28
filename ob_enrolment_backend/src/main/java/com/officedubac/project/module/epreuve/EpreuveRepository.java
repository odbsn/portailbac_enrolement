package com.officedubac.project.module.epreuve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpreuveRepository extends MongoRepository<Epreuve, String> {

    Optional<Epreuve> findByMatiereIdAndSerieIdAndType(String matiereId, String serieId, String type);

    Page<Epreuve> findByMatiereId(String matiereId, Pageable pageable);

    Page<Epreuve> findBySerieId(String serieId, Pageable pageable);

    Page<Epreuve> findByType(String type, Pageable pageable);

    @Query("{ $or: [ " +
            "{ 'matiere.nom': { $regex: ?0, $options: 'i' } }, " +
            "{ 'matiere.code': { $regex: ?0, $options: 'i' } }, " +
            "{ 'serie.nom': { $regex: ?0, $options: 'i' } }, " +
            "{ 'serie.code': { $regex: ?0, $options: 'i' } } ] }")
    Page<Epreuve> search(String keyword, Pageable pageable);

    boolean existsByMatiereIdAndSerieIdAndType(String matiereId, String serieId, String type);
    // Trouver toutes les épreuves qui utilisent un jour spécifique (par son ID)
    List<Epreuve> findByJourDebutId(String jourId);

    // Trouver toutes les épreuves par code de jour
    List<Epreuve> findByJourDebutCode(String jourCode);

    // Compter les épreuves par jour
    long countByJourDebutId(String jourId);
}