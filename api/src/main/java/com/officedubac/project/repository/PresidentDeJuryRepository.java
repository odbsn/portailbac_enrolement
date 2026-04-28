package com.officedubac.project.repository;

import com.officedubac.project.models.PresidentJury;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PresidentDeJuryRepository extends MongoRepository<PresidentJury, String>
{
}
