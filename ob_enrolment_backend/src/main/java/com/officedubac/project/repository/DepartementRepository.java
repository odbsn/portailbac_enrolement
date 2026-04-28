package com.officedubac.project.repository;

import com.officedubac.project.models.CentreEtatCivil;
import com.officedubac.project.models.Departement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartementRepository extends MongoRepository<Departement, String>
{
    Departement findByName(String nameDept);
}