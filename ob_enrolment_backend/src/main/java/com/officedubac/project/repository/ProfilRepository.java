package com.officedubac.project.repository;

import com.officedubac.project.models.Profil;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfilRepository extends MongoRepository<Profil, String>
{
    Profil findByName(String name);
}
