package com.officedubac.project.repository;

import com.officedubac.project.models.Serie;
import com.officedubac.project.models.SpecialiteCGS;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialiteCGSRepository extends MongoRepository<SpecialiteCGS, String>
{

}
