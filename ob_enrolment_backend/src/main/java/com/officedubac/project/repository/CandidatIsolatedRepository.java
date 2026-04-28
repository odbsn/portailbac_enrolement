package com.officedubac.project.repository;

import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.CandidateIsolated;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatIsolatedRepository extends MongoRepository<CandidateIsolated, String>
{
    List<CandidateIsolated> findByEtablissementIdAndSession(String etablissementId, Long session);


    @Query("{ 'session': ?0, 'etablissement.inspectionAcademie.code': ?1, 'serie.code': ?2 }")
    List<CandidateIsolated> findIsoBySessionAndIaCodeAndSerieCode(Long session, String iaCode, String serieCode);

    @Query("{ 'session': ?0, 'etablissement.inspectionAcademie.code': ?1 }")
    List<CandidateIsolated> findIsoBySessionAndIaCode(Long session, String iaCode);

}
