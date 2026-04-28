package com.officedubac.project.repository;

import com.officedubac.project.models.Acteurs;
import com.officedubac.project.models.ConcoursGeneral;
import com.officedubac.project.models.RegleCentre;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegleCentreRepository extends MongoRepository<RegleCentre, String>
{
    List<RegleCentre> findBySession(Long session);
}

