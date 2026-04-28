package com.officedubac.project.repository;

import com.officedubac.project.models.Departement;
import com.officedubac.project.models.Option;
import com.officedubac.project.models.PorteeMatiere;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PorteeMatiereRepository extends MongoRepository<PorteeMatiere, String>
{
    PorteeMatiere findByName(String namePortee);
}
