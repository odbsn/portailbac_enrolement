package com.officedubac.project.repository;

import com.officedubac.project.models.TypeCandidat;
import com.officedubac.project.models.TypeEtablissement;
import com.officedubac.project.models.TypeFiliere;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeEtablissementRepository extends MongoRepository<TypeEtablissement, String>
{
    TypeEtablissement findByCode(String te);

}