package com.officedubac.project.repository;

import com.officedubac.project.dto.EtablissementSummaryReception;
import com.officedubac.project.models.*;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatRepository extends MongoRepository<Candidat, String>
{
    @Query("{ 'registry_num': ?0 }")
    Candidat findByRegistryNum(int registryNum);

    @Query("{ 'subject': ?0 }")
    List<Candidat> findBySubject(String subject);

    List<Candidat> findByEtablissementId(String etablissementId);

    List<Candidat> findByEtablissementIdAndSerieCodeAndSession(String etablissementId, String serieCode, Long session);

    List<Candidat> findByEtablissementIdAndSessionAndSerieCode(String etablissementId, Long session, String serieCode);

    List<Candidat> findByEtablissementIdAndSessionAndSerieCodeAndDosNumber(String etablissementId, Long session, String serieCode, String dosN);

    List<Candidat> findByEtablissementIdAndSessionAndSerieCodeAndDecision(String etablissementId, Long session, String serieCode, int decision);

    @Aggregation(pipeline = {
            "{ $match: { 'etablissement.id': ?0, session: ?1, 'serie.code': ?2 } }",
            "{ $addFields: { dosNumberInt: { $toLong: '$dosNumber' } } }",
            "{ $match: { dosNumberInt: { $gte: ?3, $lte: ?4 } } }"
    })
    List<Candidat> findByDosNumberIntervalAgg(
            String etablissementId,
            Long session,
            String serieCode,
            Long debut,
            Long fin
    );

    List<Candidat> findByEtablissementIdAndSessionAndDecision(String etablissementId, Long session, int decision);

    List<Candidat> findByEtablissementIdAndSessionAndSubject(String etablissementId, Long session, String subject);

    List<Candidat> findByEtablissementIdAndSession(String etablissementId, Long session);

    // Dans CandidatRepository.java
    List<Candidat> findBySessionAndDecision(Long session, Integer decision);

    Long countBySessionAndEtablissement_IdAndEprFacListANotNull(Long session, String etablissementId);

    Long countBySessionAndEtablissement_IdAndEprFacListBNotNull(Long session, String etablissementId);

    Long countBySessionAndEtablissement_Id(Long session, String etablissementId);

    List<Candidat> findBySessionAndEtablissement_Id(Long session, String etablissementId);

    @Query("{ 'year_registry_num': ?0, 'registry_num': ?1, 'centreEtatCivil.name': ?2, 'session': ?3 }")
    Candidat findCandidate(int yearRegistryNum, String registryNum, String centreEtatCivilName, Long session);

    Candidat findByPhone1AndSession(String phone1, Long session);
    Candidat findByEmailAndSession(String email, Long session);

    Candidat findByDosNumberAndSessionAndEtablissement_Id(String dosNumber, Long session, String etablissementId);

    boolean existsBySubject(String wording);

    List<Candidat> findBySession(Long session);

    List<Candidat> findBySessionAndOperator(Long session, String ops);

    Page<Candidat> readBySession(Long session, Pageable pageable);

    int countBySessionAndDecision(Long session, int decision);

    @Aggregation(pipeline = {
            "{ $match: { session: ?0 } }",
            "{ $group: { "
                    + "_id: '$etablissement', "
                    + "decision1: { $sum: { $cond: [ { $eq: ['$decision', 1] }, 1, 0 ] } }, "
                    + "decision2: { $sum: { $cond: [ { $eq: ['$decision', 2] }, 1, 0 ] } }, "
                    + "decision0: { $sum: { $cond: [ { $eq: ['$decision', 0] }, 1, 0 ] } } "
                    + "} }"
    })
    List<EtablissementSummaryReception[]> summarizeDecisions(Long session);

    @Aggregation(pipeline = {
            "{ $match: { session: ?0, operator: { $ne: null } } }",
            "{ $group: { "
                    + "_id: '$etablissement', "
                    + "operators: { $addToSet: '$operator' } "
                    + "} }"
    })
    List<Object[]> findOperatorsByEtablissement(Long session);

    List<Candidat> findBySessionAndEtablissementInspectionAcademieCode(Long session, String ia);

    @Aggregation(pipeline = {
            "{ $match: { 'operator': ?0, 'session': ?1, 'etablissement.code': ?2 } }",
            "{ $addFields: { dosNumberInt: { $toInt: '$dosNumber' } } }",
            "{ $sort: { 'dosNumberInt': 1 } }"
    })
    List<Candidat> findByOperatorAndSessionSorted(String operator, Long session, String code);

    @Aggregation(pipeline = {
            "{ $match: { 'etablissement._id': ?0, 'session': ?1, 'centreExamen._id': { $exists: true } } }",
            "{ $group: { _id: '$centreExamen._id', centreExamen: { $first: '$centreExamen' } } }",
            "{ $replaceRoot: { newRoot: '$centreExamen' } }"
    })
    List<CentreExamen> findCentresExamenByEtablissementAndSession(
            ObjectId etablissementId,
            Integer session
    );

    @Query("{ 'session': ?0, 'etablissement.inspectionAcademie.code': ?1 }")
    Page<Candidat> findBySessionAndIaCode(Integer session, String iaCode, Pageable pageable);

    @Query("{ 'session': ?0, 'etablissement.inspectionAcademie.code': ?1 }")
    List<Candidat> findBySessionAndIaCode_(Integer session, String iaCode);

    @Query("{ 'session': ?0, 'etablissement.inspectionAcademie.code': ?1, 'serie.code': ?2 }")
    List<Candidat> findBySessionAndIaCodeAndSerieCode(Long session, String iaCode, String serieCode);


    @Query("{ 'session': ?0, 'etablissement.inspectionAcademie.code': ?1 }")
    List<Candidat> findBySessionAndIaCode(Long session, String iaCode);


}
