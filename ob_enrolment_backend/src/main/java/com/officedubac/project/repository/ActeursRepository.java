package com.officedubac.project.repository;

import com.officedubac.project.models.Acteurs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActeursRepository extends MongoRepository<Acteurs, String>
{
}

