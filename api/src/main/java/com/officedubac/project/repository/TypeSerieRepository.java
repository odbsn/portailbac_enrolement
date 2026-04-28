package com.officedubac.project.repository;

import com.officedubac.project.models.TypeFiliere;
import com.officedubac.project.models.TypeMatiere;
import com.officedubac.project.models.TypeSerie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeSerieRepository extends MongoRepository<TypeSerie, String>
{
    TypeSerie findByName(String ts);
}
