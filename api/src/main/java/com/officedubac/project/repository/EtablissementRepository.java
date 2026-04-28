package com.officedubac.project.repository;

import com.officedubac.project.models.Departement;
import com.officedubac.project.models.Etablissement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtablissementRepository extends MongoRepository<Etablissement, String>
{
    Etablissement findByName(String nameEtab);
    Etablissement findByCode(String codeEtab);

    List<Etablissement> findByInspectionAcademieCode(String ia);

    Optional<Etablissement> findByNameIgnoreCase(String nameEtab);

}
