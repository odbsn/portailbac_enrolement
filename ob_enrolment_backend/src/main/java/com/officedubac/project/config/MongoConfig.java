package com.officedubac.project.config;

import com.officedubac.project.models.Candidat;
import com.officedubac.project.models.Etablissement;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Collation;


@EnableMongoAuditing
@Configuration
public class MongoConfig {
    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initIndexes() {
        // Numéro d'enrôlement unique
        mongoTemplate.indexOps(Candidat.class)
                .ensureIndex(new Index().on("numEnrolement", Sort.Direction.ASC).unique());

        // Dos number unique par session et par établissement
        mongoTemplate.indexOps(Candidat.class)
                .ensureIndex(new Index()
                        .on("dosNumber", Sort.Direction.ASC)
                        .on("session", Sort.Direction.ASC)
                        .on("etablissement.code", Sort.Direction.ASC)
                        .unique());

        // Téléphone unique
        mongoTemplate.indexOps(Candidat.class)
                .ensureIndex(new Index().on("phone1", Sort.Direction.ASC).unique());

        // Email unique
        mongoTemplate.indexOps(Candidat.class)
                .ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());

        mongoTemplate.indexOps(Etablissement.class)
                .ensureIndex(new Index()
                        .on("code", Sort.Direction.ASC)
                        .unique());


    }
}
