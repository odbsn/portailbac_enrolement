package com.officedubac.project.repository;

import com.officedubac.project.models.Departement;
import com.officedubac.project.models.TypeCandidat;
import com.officedubac.project.models.TypeEnseignement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeEnseignementRepository extends MongoRepository<TypeEnseignement, String>
{
    TypeEnseignement findByName(String te);

}