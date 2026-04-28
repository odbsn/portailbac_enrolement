package com.officedubac.project.repository;

import com.officedubac.project.models.Departement;
import com.officedubac.project.models.Nationality;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NationalityRepository extends MongoRepository<Nationality, String>
{
    Nationality findByName(String nameNat);
}
