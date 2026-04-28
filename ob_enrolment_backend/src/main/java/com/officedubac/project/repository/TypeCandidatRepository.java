package com.officedubac.project.repository;

import com.officedubac.project.models.TypeCandidat;
import com.officedubac.project.models.TypeEtablissement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeCandidatRepository extends MongoRepository<TypeCandidat, String>
{
    TypeCandidat findByName(String tc);

}