package com.officedubac.project.repository;

import com.officedubac.project.models.Serie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SerieRepository extends MongoRepository<Serie, String>
{
    Serie findByName(String nameSerie);
    Serie findByCode(String codeSerie);
}
