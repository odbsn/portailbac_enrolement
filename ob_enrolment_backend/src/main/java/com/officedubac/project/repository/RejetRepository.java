package com.officedubac.project.repository;

import com.officedubac.project.models.Rejet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RejetRepository extends MongoRepository<Rejet, String>
{
    Rejet findByName(String name);
}

