package com.officedubac.project.module.epreuve;

import com.officedubac.project.module.epreuve.dto.EpreuveRequest;
import com.officedubac.project.module.epreuve.dto.EpreuveResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EpreuveService {

    EpreuveResponse create(EpreuveRequest request);

    EpreuveResponse update(String id, EpreuveRequest request);

    void delete(String id);

    EpreuveResponse getById(String id);

    Page<EpreuveResponse> getAll(Pageable pageable);

    Page<EpreuveResponse> search(String keyword, Pageable pageable);

    Page<EpreuveResponse> getWithFilters(
            String keyword,
            String matiereId,
            String serieId,
            String type,
            Boolean autorisation,
            Boolean estDominant,
            Pageable pageable);
    public int updateEpreuvesTypeDirectly();
}