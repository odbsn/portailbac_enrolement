package com.officedubac.project.repository;

import com.officedubac.project.models.CentreExamen;
import com.officedubac.project.models.InspectionAcademie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspectionAcademieRepository extends MongoRepository<InspectionAcademie, String>
{
    InspectionAcademie findByName(String nameInsp);
}