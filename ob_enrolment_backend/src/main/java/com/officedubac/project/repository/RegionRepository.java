package com.officedubac.project.repository;

import com.officedubac.project.models.Departement;
import com.officedubac.project.models.Profil;
import com.officedubac.project.models.Region;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends MongoRepository<Region, String>
{
    Region findByName(String nameReg);
}
