package com.officedubac.project.module.nouveauBachelier;

import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.models.Jury;
import com.officedubac.project.module.nouveauBachelier.dto.ImportResult;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierAudit;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierRequest;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface NouveauBachelierService {
    public List<NouveauBachelierResponse> all() throws BusinessResourceException;

    // ✅ Pagination serveur : recherche sur nom, prénoms, numeroTable, téléphone, jury.numero
    public Page<NouveauBachelierResponse> findAllPaginated(Pageable pageable, String search) throws BusinessResourceException;

    public Optional<NouveauBachelierAudit> auditOneById(String id) throws NumberFormatException, BusinessResourceException;
    public Optional<NouveauBachelierResponse> oneById(String id) throws NumberFormatException, BusinessResourceException;
    public NouveauBachelierResponse add(NouveauBachelierRequest req) throws BusinessResourceException;
    public NouveauBachelierResponse maj(NouveauBachelierRequest req, String id) throws NumberFormatException, NoSuchElementException, BusinessResourceException;
    public String del(String id) throws NumberFormatException, BusinessResourceException;
    public Optional <NouveauBachelierResponse> searchSimple(String numeroTable) throws BusinessResourceException;

    // Import fichier unique (conservé pour compatibilité)
    public ImportResult importerDepuisExcel(InputStream inputStream) throws IOException;

    // ✅ NOUVEAU : import multi-fichiers — parsing parallèle, 1 seul batch MongoDB fusionné,
    //    résultats répartis par fichier source
    public List<ImportResult> importerDepuisExcelMultiple(List<MultipartFile> files) throws IOException;

    List<String> importerDepuisCsv(InputStream inputStream) throws IOException;
    Optional<NouveauBachelierResponse> getBachelierByNumeroTable(String numeroTable) throws BusinessResourceException;

    // ✅ Liste des jurys (collection "jury") qui n'ont encore AUCUN bachelier chargé dans "nouveauBachelier"
    List<Jury> findJurysNonCharges(Boolean technique) throws BusinessResourceException;
}