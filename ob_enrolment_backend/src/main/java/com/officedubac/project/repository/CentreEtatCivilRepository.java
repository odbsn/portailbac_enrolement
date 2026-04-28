package com.officedubac.project.repository;

import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.CentreEtatCivil;
import com.officedubac.project.models.CentreExamen;
import com.officedubac.project.models.Etablissement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentreEtatCivilRepository extends MongoRepository<CentreEtatCivil, String>
{
    CentreEtatCivil findByName(String nameCE);
}
