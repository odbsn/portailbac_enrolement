package com.officedubac.project.repository;

import com.officedubac.project.models.Matiere;
import com.officedubac.project.models.Serie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatiereRepository extends MongoRepository<Matiere, String>
{
    List<Matiere> findBySerie_Id(String serieId);
    Matiere findByCode(String codeSerie);
    Matiere findByName(String mat);
}
