package com.officedubac.project.repository;

import com.officedubac.project.models.CentreEtatCivil;
import com.officedubac.project.models.CentreExamen;
import com.officedubac.project.models.TypeMatiere;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentreExamenRepository extends MongoRepository<CentreExamen, String>
{
    CentreExamen findByName(String nameCE);
}