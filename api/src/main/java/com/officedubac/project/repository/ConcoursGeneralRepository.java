package com.officedubac.project.repository;

import com.officedubac.project.dto.EtabSummaryDTO;
import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.ConcoursGeneral;
import com.officedubac.project.models.Departement;
import com.officedubac.project.models.VilleProjection;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcoursGeneralRepository extends MongoRepository<ConcoursGeneral, String>
{
    @Query("{ 'level': ?0, 'specialite': ?1}")
    List<ConcoursGeneral> findByLevelAndSpecialite(String level, String specialite);

    List<ConcoursGeneral> findByEtablissementIdAndSession(String etablissementId, Long session);

    List<ConcoursGeneral> findByEtablissementIdAndSessionAndSpecialiteAndLevel(String etablissementId, Long session, String specialite, String level);

    List<ConcoursGeneral> findBySession(Long session);

    List<ConcoursGeneral> findBySessionAndDecision(Long session, Integer decision);


    int countBySessionAndDecision(Long session, int decision);


    @Aggregation(pipeline = {
            "{ $match: { 'operator': ?0, 'session': ?1, 'etablissement.code': ?2 } }",
            "{ $addFields: { dosNumberInt: { $toInt: '$dosNumber' } } }",
            "{ $sort: { 'dosNumberInt': 1 } }"
    })
    List<ConcoursGeneral> findByOperatorAndSessionSorted(String operator, Long session, String code);


    List<ConcoursGeneral> findByEtablissementCodeAndSession(String code, Long session);


    @Aggregation(pipeline = {
            "{ $match: { session: ?0, 'etablissement.centreExamen.name': { $ne: null } } }",
            "{ $group: { _id: '$etablissement.centreExamen.name' } }",
            "{ $sort: { _id: 1 } }"
    })
    List<String> findDistinctVilleNamesBySession(Long session);


    @Aggregation(pipeline = {
            // 1. Filtrer candidats
            "{ $match: { session: ?0, decision: 0 } }",

            // 2. Grouper par établissement
            "{ $group: { _id: '$etablissement._id', count: { $sum: 1 }, etablissement: { $first: '$etablissement' } } }",

            // 3. Jointure avec User
            "{ $lookup: { " +
                    "from: 'user', " +
                    "localField: 'etablissement.code', " +
                    "foreignField: 'acteur.etablissement.code', " +
                    "as: 'user' " +
                    "} }",

            // 4. Prendre un seul user (ex: admin établissement)
            "{ $unwind: { path: '$user', preserveNullAndEmptyArrays: true } }",

            // 5. Projection finale
            "{ $project: { " +
                    "etablissement: '$etablissement.name', " +
                    "telephone: '$user.phone', " +
                    "count: 1, " +
                    "_id: 0 " +
                    "} }"
    })
    List<EtabSummaryDTO> findEtablissementsWithDecisionZero(Long session);




}
