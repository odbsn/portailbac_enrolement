package com.officedubac.project.repository;

import com.officedubac.project.models.Option;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends MongoRepository<Option, String>
{
}
