package com.officedubac.project.repository;

import com.officedubac.project.models.Departement;
import com.officedubac.project.models.TypeEtablissement;
import com.officedubac.project.models.TypeFiliere;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeFiliereRepository extends MongoRepository<TypeFiliere, String>
{
    TypeFiliere findByName(String tf);
}