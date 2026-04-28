package com.officedubac.project.repository;

import com.officedubac.project.models.TypeEtablissement;
import com.officedubac.project.models.User;
import com.officedubac.project.models.Ville;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VilleRepository extends MongoRepository<Ville, String>
{
    Ville findByName(String vil);
}
