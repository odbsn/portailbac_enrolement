package com.officedubac.project.repository;

import com.officedubac.project.models.CompteDroitsInscription;
import com.officedubac.project.models.EtatDeVersement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompteDroitInscriptionRepository extends MongoRepository<CompteDroitsInscription, String>
{
    CompteDroitsInscription findByEtablissementIdAndSession(String etablissementId, Long session);
    CompteDroitsInscription findByEtablissementNameAndSession(String etablissementName, Long session);
    CompteDroitsInscription findByEtablissementCodeAndSession(String etablissementCode, Long session);

    List<CompteDroitsInscription> findBySession(Long session);

    List<CompteDroitsInscription> findBySessionOrderByEtablissementInspectionAcademieNameAsc(Long session);

}

