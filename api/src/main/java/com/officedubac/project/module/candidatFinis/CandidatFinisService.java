package com.officedubac.project.module.candidatFinis;
import com.officedubac.project.models.Etablissement;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisRequest;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisResponse;
import com.officedubac.project.module.candidatFinis.dto.PageResponse;
import com.officedubac.project.module.jour.Jour;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface CandidatFinisService {

    // CRUD
    CandidatFinisResponse create(CandidatFinisRequest request);
    CandidatFinisResponse update(String id, CandidatFinisRequest request);
    void delete(String id);
    CandidatFinisResponse getById(String id);

    // Listes avec filtres (toutes incluent les épreuves)
    PageResponse<CandidatFinisResponse> getAll(Pageable pageable);
    PageResponse<CandidatFinisResponse> search(String keyword, Pageable pageable);
    PageResponse<CandidatFinisResponse> getWithFilters(
            String keyword,
            String serie,
            String jury,
            String typeCandidat,
            String numeroDossier,
            String statutResultat,
            String sexe,
            String nationalite,
            Pageable pageable);

    // Méthodes pour l'utilisateur connecté
    PageResponse<CandidatFinisResponse> getAllByUtilisateurConnecte(Pageable pageable);
    PageResponse<CandidatFinisResponse> getWithFiltersByUtilisateurConnecte(
            String keyword,
            String serie,
            String jury,
            String typeCandidat,
            String numeroDossier,
            String statutResultat,
            String sexe,
            String nationalite,
            Pageable pageable);
    CandidatFinisResponse getByIdByUtilisateurConnecte(String id);
    PageResponse<CandidatFinisResponse> getBySerieByUtilisateurConnecte(String serieCode, Pageable pageable);
    PageResponse<CandidatFinisResponse> getByJuryByUtilisateurConnecte(String jury, Pageable pageable);
    PageResponse<CandidatFinisResponse> getByJury(String jury, Pageable pageable);
    PageResponse<CandidatFinisResponse> getBySerie(String serieCode, Pageable pageable);
    List<String> getAllDistinctSeries();
    List<String> getMyDistinctSeries();
    public ByteArrayInputStream exportAllCandidatsToExcel();
    List<CandidatFinisResponse> getAllByUtilisateurConnecteNoPagination(String serie);
    List<CandidatFinisResponse> getAllByUtilisateurConnecte();
    public Etablissement getEtablissementUtilisateurConnecte();
    Jour getJourEPS();
//    public ConvocationDTO getCandidatConvocation(
//            String codeEtablissement,
//            String numeroTable,
//            String dateNaissance
//    );
Mono<String> regenerateConvocation(String numeroTable);
}