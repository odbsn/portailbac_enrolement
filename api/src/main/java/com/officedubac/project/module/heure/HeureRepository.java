package com.officedubac.project.module.heure;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HeureRepository extends MongoRepository<Heure, String>
{
    Optional<Heure> findByCode(String code);

    boolean existsByCode(String code);
}
