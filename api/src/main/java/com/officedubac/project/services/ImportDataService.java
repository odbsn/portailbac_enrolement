package com.officedubac.project.services;

import com.officedubac.project.dto.BaseMorteDTO;
import com.officedubac.project.dto.SerieDTO;
import com.officedubac.project.models.*;
import com.officedubac.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportDataService
{
    @Autowired
    private BaseMorteRepository baseMorteRepository;


    public void importFromExcel(String filePath) {
        try (InputStream file = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<BaseMorte> batchList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // ignorer les en-têtes

                BaseMorte bm = new BaseMorte();

                // TableNum
                bm.setTableNum(safeParseInteger(row.getCell(0)));
                // ExYearBac
                bm.setExYearBac(safeParseInteger(row.getCell(1)));

                // Prénom / Nom
                bm.setFirstname(getCellValueSafe(row.getCell(2)));
                bm.setLastname(getCellValueSafe(row.getCell(3)));

                // Date de naissance
                String dateStr = getCellValueSafe(row.getCell(4));
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        bm.setDate_birth(LocalDate.parse(dateStr, formatter));
                    } catch (DateTimeParseException ex) {
                        System.err.println("Date invalide à la ligne " + (row.getRowNum() + 1) + ": " + dateStr);
                    }
                }

                bm.setPlace_birth(getCellValueSafe(row.getCell(5)));
                bm.setGender(getCellValueSafe(row.getCell(6)));
                bm.setCountryBirth(getCellValueSafe(row.getCell(7)));
                bm.setEtablissement(getCellValueSafe(row.getCell(8)));

                bm.setBac_do_count(Optional.ofNullable(safeParseInteger(row.getCell(9))).orElse(0));
                bm.setCodeCentreEtatCivil(getCellValueSafe(row.getCell(10)));
                bm.setRegistryNum(getCellValueSafe(row.getCell(11)));
                bm.setYearRegistryNum(Optional.ofNullable(safeParseInteger(row.getCell(12))).orElse(0));
                bm.setExclusionDuree(Optional.ofNullable(safeParseInteger(row.getCell(13))).orElse(0));
                bm.setCodeEnrolement(getCellValueSafe(row.getCell(14)));

                batchList.add(bm);
            }

            // Sauvegarder toutes les lignes d’un coup
            baseMorteRepository.saveAll(batchList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Méthode sécurisée pour récupérer la valeur d'une cellule en String */
    private String getCellValueSafe(Cell cell) {
        if (cell == null) return null;

        DataFormatter formatter = new DataFormatter(); // permet de garder la forme visible dans Excel

        switch (cell.getCellType()) {
            case STRING:
                String str = cell.getStringCellValue();
                return (str != null && !str.trim().isEmpty()) ? str.trim() : "";

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        return cell.getLocalDateTimeCellValue()
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e) {
                        System.err.println("Erreur lors du parsing de la date : " + e.getMessage());
                        return null;
                    }
                }
                return formatter.formatCellValue(cell);

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                try {
                    // Toujours formater via DataFormatter pour garder le visuel exact
                    return formatter.formatCellValue(cell);
                } catch (Exception e) {
                    System.err.println("Erreur lors du parsing de la formule : " + e.getMessage());
                    return null;
                }

            default:
                return null;
        }
    }

    private Integer safeParseInteger(Cell cell)
    {
        String val = getCellValueSafe(cell);
        try
        {
            return (val != null && !val.isEmpty()) ? Integer.parseInt(val) : 0;
        }
        catch (NumberFormatException ex) {
            System.err.println("Valeur entière invalide : " + val);
            return null;
        }
    }

    @Cacheable(value = "baseMorteCache", key = "#tableNum + '-' + #exYearBac")
    public BaseMorte checkRedoublantOrFraude(int tableNum, int exYearBac)
    {
        BaseMorte bm = baseMorteRepository.findByTableNumAndExYearBac(tableNum, exYearBac);
        if (bm != null)
        {
            log.info(bm.getTableNum() + " " + bm.getExYearBac());
        }
        else
        {
            log.info("Aucun enregistrement trouvé pour : " + tableNum + " " + exYearBac);
        }
        return bm;
    }

    @Cacheable(value = "baseMorteCache2", key = "#codeCentreEtatCivil + '-' + #yearRegistryNum + '-' + #registryNum")
    public BaseMorte checkRedoublantByEtatCivil(String codeCentreEtatCivil, int yearRegistryNum, String registryNum)
    {
        String codeEnrolement = codeCentreEtatCivil + yearRegistryNum + registryNum;

        log.info(codeEnrolement);

        BaseMorte bm2 = baseMorteRepository.findFirstByCodeEnrolementOrderByExYearBacDesc(codeEnrolement);
        if (bm2 != null)
        {
            log.info(bm2.getCodeCentreEtatCivil() + " " + bm2.getYearRegistryNum() + " " + bm2.getRegistryNum());
        }
        else
        {
            log.info("Aucun enregistrement trouvé pour : " + codeCentreEtatCivil + " " + yearRegistryNum + " " + registryNum);
        }

        return bm2;
    }

    public Integer convertToInt(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null; // pas de filtre
        }

        try {
            return Integer.parseInt(search.trim());
        } catch (NumberFormatException e) {
            // si la valeur n’est pas un entier valide, on peut soit :
            // - renvoyer null (pas de filtre)
            // - ou lever une exception
            return null;
        }
    }


    public Page<BaseMorte> getDataBaseMorte(int page, int size, String search)
    {
        Pageable pageable = PageRequest.of(page, size);

        if (search != null && !search.trim().isEmpty()) {
            return baseMorteRepository.findByTableNum(convertToInt(search), pageable);
        }

        return baseMorteRepository.findAll(pageable);
    }

    public BaseMorte createArchive(BaseMorteDTO baseMorteDTO)
    {
        BaseMorte bm = BaseMorte.builder()
                .tableNum(baseMorteDTO.getTableNum())
                .exYearBac(baseMorteDTO.getExYearBac())
                .firstname(baseMorteDTO.getFirstname())
                .lastname(baseMorteDTO.getLastname())
                .date_birth(baseMorteDTO.getDate_birth())
                .place_birth(baseMorteDTO.getPlace_birth())
                .gender(baseMorteDTO.getGender())
                .countryBirth(baseMorteDTO.getCountryBirth())
                .etablissement(baseMorteDTO.getEtablissement())
                .bac_do_count(baseMorteDTO.getBac_do_count())
                .codeCentreEtatCivil(baseMorteDTO.getCodeCentreEtatCivil())
                .yearRegistryNum(baseMorteDTO.getYearRegistryNum())
                .registryNum(baseMorteDTO.getRegistryNum())
                .exclusionDuree(baseMorteDTO.getExclusionDuree())
                .codeEnrolement(baseMorteDTO.getCodeCentreEtatCivil() + baseMorteDTO.getYearRegistryNum() + baseMorteDTO.getRegistryNum())
                .build();

        return baseMorteRepository.save(bm);
    }

    public BaseMorte updateArchive(String idBm, BaseMorteDTO baseMorteDTO)
    {
        BaseMorte update_bm = baseMorteRepository.findById(idBm).orElse(null);

        if (update_bm != null)
        {
            update_bm.setTableNum(baseMorteDTO.getTableNum());
            update_bm.setExYearBac(baseMorteDTO.getExYearBac());
            update_bm.setFirstname(baseMorteDTO.getFirstname());
            update_bm.setLastname(baseMorteDTO.getLastname());
            update_bm.setPlace_birth(baseMorteDTO.getPlace_birth());
            update_bm.setExclusionDuree(baseMorteDTO.getExclusionDuree());
            update_bm.setCodeCentreEtatCivil(baseMorteDTO.getCodeCentreEtatCivil());
            update_bm.setYearRegistryNum(baseMorteDTO.getYearRegistryNum());
            update_bm.setRegistryNum(baseMorteDTO.getRegistryNum());
            update_bm.setCodeEnrolement(baseMorteDTO.getCodeCentreEtatCivil() + baseMorteDTO.getYearRegistryNum() + baseMorteDTO.getRegistryNum());
            return baseMorteRepository.save(update_bm);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Prog with ID " + idBm + " is not found");
        }
    }

    public void deleteArchive(String idBm)
    {
        BaseMorte update_bm = baseMorteRepository.findById(idBm).orElse(null);
        if (update_bm != null)
        {
            baseMorteRepository.delete(update_bm);
        }
        else
        {
            // Handle the case where the user is not found
            throw new NotFoundException("Prog with ID " + idBm + " is not found");
        }
    }

}