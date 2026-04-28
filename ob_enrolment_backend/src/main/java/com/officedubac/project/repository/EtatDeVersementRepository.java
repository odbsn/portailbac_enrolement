package com.officedubac.project.repository;

import com.officedubac.project.models.Acteurs;
import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.EtatDeVersement;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtatDeVersementRepository extends MongoRepository<EtatDeVersement, String>
{
    List<EtatDeVersement> findByEtablissementIdAndSession(String etablissementId, Long session, Sort sort);

    List<EtatDeVersement> findBySession(Long session, Sort sort);


}

