package com.officedubac.project.repository;

import com.officedubac.project.models.Structure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StructureRepository extends MongoRepository<Structure, String>
{
}
