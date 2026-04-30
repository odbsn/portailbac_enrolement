package com.officedubac.project.module.nouveauBachelier;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NouveauBachelierDao extends MongoRepository<NouveauBachelier,String> {

}
