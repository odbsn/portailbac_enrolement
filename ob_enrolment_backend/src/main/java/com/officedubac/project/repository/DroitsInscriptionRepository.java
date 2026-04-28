package com.officedubac.project.repository;

import com.officedubac.project.models.DroitInscription;
import com.officedubac.project.models.EtatDeVersement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DroitsInscriptionRepository extends MongoRepository<DroitInscription, String>
{
    List<DroitInscription> findByEstablishmentAndSession(String etabCode, Long session);

    List<DroitInscription> findBySession(Long session);

}

