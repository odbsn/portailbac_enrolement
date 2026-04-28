package com.officedubac.project.repository;

import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.ConcoursGeneral;
import com.officedubac.project.models.ListeFinaleCandidatsCGS;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListeFinaleCndidatsCGSRepository extends MongoRepository<ListeFinaleCandidatsCGS, String>
{
    List<ListeFinaleCandidatsCGS> findByCentreCompositionAndSessionAndDisciplineAndLevelOrderByLastnameAsc(String centreCompo, Long session, String discipline, String level);

    @Query(value = "{ 'academia': ?0, 'session': ?1 }", fields = "{ 'centreComposition': 1 }")
        List<ListeFinaleCandidatsCGS> findCentresByAcademiaAndSession(String academia, Long session);

    @Query(value = "{ 'centreComposition': ?0, 'session': ?1 }", fields = "{ 'level': 1 }")
    List<ListeFinaleCandidatsCGS> findNiveauxByCentreAndSession(String centre, Long session);

    @Query(value = "{ 'centreComposition': ?0, 'level': ?1, 'session': ?2 }", fields = "{ 'discipline': 1 }")
    List<ListeFinaleCandidatsCGS> findDisciplinesByCentreAndNiveauAndSession(String centre, String niveau, Long session);

    List<ListeFinaleCandidatsCGS> findBySessionOrderByDisciplineAscLevelAsc(Long session);

}
