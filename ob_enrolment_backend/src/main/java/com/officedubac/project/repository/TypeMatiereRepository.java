package com.officedubac.project.repository;

import com.officedubac.project.models.PorteeMatiere;
import com.officedubac.project.models.TypeMatiere;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeMatiereRepository extends MongoRepository<TypeMatiere, String>
{
    TypeMatiere findByName(String nameTM);

}
