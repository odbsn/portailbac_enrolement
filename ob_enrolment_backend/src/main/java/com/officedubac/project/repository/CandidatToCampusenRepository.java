package com.officedubac.project.repository;

import com.officedubac.project.dto.EtablissementSummaryReception;
import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.CandidatToCampusen;
import com.officedubac.project.models.CentreExamen;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatToCampusenRepository extends MongoRepository<CandidatToCampusen, String>
{
    Page<CandidatToCampusen> findBySession(Long session, Pageable pageable);
}
