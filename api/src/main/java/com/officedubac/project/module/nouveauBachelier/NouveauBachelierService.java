package com.officedubac.project.module.nouveauBachelier;

import com.officedubac.project.exception.BusinessResourceException;
import com.officedubac.project.module.nouveauBachelier.dto.ImportResult;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierAudit;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierRequest;
import com.officedubac.project.module.nouveauBachelier.dto.NouveauBachelierResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface NouveauBachelierService {
    public List<NouveauBachelierResponse> all() throws BusinessResourceException;
    public Optional<NouveauBachelierAudit> auditOneById(String id) throws NumberFormatException, BusinessResourceException;
    public Optional<NouveauBachelierResponse> oneById(String id) throws NumberFormatException, BusinessResourceException;
    public NouveauBachelierResponse add(NouveauBachelierRequest req) throws BusinessResourceException;
    public NouveauBachelierResponse maj(NouveauBachelierRequest req, String id) throws NumberFormatException, NoSuchElementException, BusinessResourceException;
    public String del(String id) throws NumberFormatException, BusinessResourceException;
    public Optional <NouveauBachelierResponse> searchSimple(String numeroTable) throws BusinessResourceException;
//    public List<String> importerDepuisExcel(InputStream inputStream) throws IOException;
    public ImportResult importerDepuisExcel(InputStream inputStream) throws IOException;
    List<String> importerDepuisCsv(InputStream inputStream) throws IOException;
}
