package com.officedubac.project.repository;

import com.officedubac.project.models.Universite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversiteRepository extends MongoRepository<Universite, String>
{

}
