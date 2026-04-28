package com.officedubac.project.repository;

import com.officedubac.project.models.BaseMorte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseMorteRepository extends MongoRepository<BaseMorte, String>
{
    BaseMorte findByTableNumAndExYearBac(int tableNum, int exYearBac);

    BaseMorte findFirstByCodeEnrolementOrderByExYearBacDesc(String codeEnrolement);

    Page<BaseMorte> findByTableNum(int numTable, Pageable pageable);
}
