package com.officedubac.project.repository;

import com.officedubac.project.models.Specialite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialiteRepository extends MongoRepository<Specialite, String>
{

}
