package com.officedubac.project.module.bacheliersToCampusen;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ImportLogRepository extends MongoRepository<ImportLog, String>
{
    Optional<ImportLog> findByAnnee(int annee);
    void deleteByAnnee(int annee);
}
