package com.officedubac.project.module.candidatFinis;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidatExportService {

    /**
     * Génère un fichier Excel des candidats avec les colonnes demandées
     */
    public ByteArrayInputStream generateCandidatsExcel(List<CandidatFinisResponse> candidats) {
        log.info("📊 Génération Excel pour {} candidats", candidats.size());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Convertir les DTOs
            List<CandidatExcelSimpleDto> exportData = convertToExcelDto(candidats);

            // Générer l'Excel
            ExcelWriter excelWriter = EasyExcel.write(outputStream, CandidatExcelSimpleDto.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("Candidats").build();
            excelWriter.write(exportData, writeSheet);
            excelWriter.finish();

            log.info("✅ Excel généré avec succès");
            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération de l'Excel", e);
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }

    /**
     * Convertit les réponses en DTO d'export Excel
     */
    private List<CandidatExcelSimpleDto> convertToExcelDto(List<CandidatFinisResponse> candidats) {
        return candidats.stream()
                .map(this::toExcelDto)
                .collect(Collectors.toList());
    }

    /**
     * Convertit un candidat en DTO d'export Excel
     */
    private CandidatExcelSimpleDto toExcelDto(CandidatFinisResponse c) {
        CandidatExcelSimpleDto dto = new CandidatExcelSimpleDto();

        // Crt. Ecrit
        dto.setCentreEcrit(getCentreEcrit(c));

        // Jury
        dto.setJury(c.getJury());

        // N° Table
        dto.setNumeroTable(c.getNumeroTable());

        // Série
        dto.setSerie(c.getSerie());

        // Matière(s) Optionnelles
        dto.setMatieresOptionnelles(getMatieresOptionnelles(c));

        // Prénom(s)
        dto.setPrenoms(c.getPrenoms());

        // Nom
        dto.setNom(c.getNom());

        // Sexe
        dto.setSexe(c.getSexe());

        // Date naiss.
        dto.setDateNaissance(c.getDateNaissance());

        // Lieu naiss.
        dto.setLieuNaissance(c.getLieuNaissance());

        // Nationalité
        dto.setNationalite(c.getNationalite());

        // EPS
        dto.setEps(c.getEps());

        // Matière(s) Facultatives
        dto.setMatieresFacultatives(getMatieresFacultatives(c));

        return dto;
    }

    /**
     * Récupère le centre d'écrit (particulier ou normal)
     */
    private String getCentreEcrit(CandidatFinisResponse c) {
        if (c.getCentreEcritParticulier() != null && !c.getCentreEcritParticulier().isEmpty()) {
            return c.getCentreEcritParticulier();
        }
        if (c.getCentreEcrit() != null) {
            return c.getCentreEcrit().getCode();
        }
        return "";
    }

    /**
     * Récupère les matières optionnelles (MO1, MO2, MO3, EF1, EF2)
     */
    private String getMatieresOptionnelles(CandidatFinisResponse c) {
        List<String> matieres = new java.util.ArrayList<>();

        if (c.getMo1() != null && !c.getMo1().isEmpty())
            matieres.add(c.getMo1());
        if (c.getMo2() != null && !c.getMo2().isEmpty())
            matieres.add(c.getMo2());
        if (c.getMo3() != null && !c.getMo3().isEmpty())
            matieres.add(c.getMo3());
        return String.join(", ", matieres);
    }

    /**
     * Récupère les matières facultatives
     */
    private String getMatieresFacultatives(CandidatFinisResponse c) {
        List<String> facultatives = new java.util.ArrayList<>();
        if (c.getNbMatFacult() != null && c.getNbMatFacult() > 0) {
            if (c.getEf1() != null && !c.getEf1().isEmpty()) {
                facultatives.add(c.getEf1());
            }
            if (c.getEf2() != null && !c.getEf2().isEmpty()) {
                facultatives.add(c.getEf2());
            }
        }
        return String.join(", ", facultatives);
    }
}