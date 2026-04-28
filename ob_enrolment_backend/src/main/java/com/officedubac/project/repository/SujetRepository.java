package com.officedubac.project.repository;

import com.officedubac.project.models.CentreExamen;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.models.Structure;
import com.officedubac.project.models.Sujet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SujetRepository extends MongoRepository<Sujet, String>
{
    List<Sujet> findByEtablissementIdAndSession(String etablissementId, Long session);

    Optional<Sujet> findTopBySessionAndEtablissementOrderByNumSujetDesc(Long session, Etablissement etablissement);

}
