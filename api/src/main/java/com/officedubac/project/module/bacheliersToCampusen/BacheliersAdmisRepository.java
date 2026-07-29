package com.officedubac.project.module.bacheliersToCampusen;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BacheliersAdmisRepository extends MongoRepository<BacheliersToCampusen, String>
{
    Page<BacheliersToCampusen> findByAnnee(int annee, Pageable pageable);
    Page<BacheliersToCampusen> findByAnneeAndSerie(int annee, String serie, Pageable pageable);
    Optional<BacheliersToCampusen> findFirstByAnneeAndNumeroTable(int annee, String numeroTable);
    long countByAnnee(int annee);
    long deleteByAnnee(int annee);
    boolean existsByAnnee(int annee);
}
