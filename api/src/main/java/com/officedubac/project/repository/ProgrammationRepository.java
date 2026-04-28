package com.officedubac.project.repository;

import com.officedubac.project.models.Acteurs;
import com.officedubac.project.models.Programmation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgrammationRepository extends MongoRepository<Programmation, String>
{
    Programmation findTopByOrderByIdDesc();

    Programmation findByEdition(Long session);

    Optional<Programmation> findByPublicKeyAndSecretKey(String publicKey, String secretKey);
}

