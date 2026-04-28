package com.officedubac.project.repository;

import com.officedubac.project.models.Jury;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JuryRepository extends MongoRepository<Jury, String>
{

}
