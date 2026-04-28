package com.officedubac.project.module.jour;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JourRepository extends MongoRepository<Jour, String> {

    Optional<Jour> findByCode(String code);

    Optional<Jour> findByTypeAndCode(String type, String code);

    List<Jour> findByType(String type);

    List<Jour> findAllByOrderByOrdreAsc();

    boolean existsByCode(String code);
}
