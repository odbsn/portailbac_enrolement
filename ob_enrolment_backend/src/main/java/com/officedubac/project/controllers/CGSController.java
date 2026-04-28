package com.officedubac.project.controllers;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.officedubac.project.dto.*;
import com.officedubac.project.models.*;
import com.officedubac.project.repository.CandidatRepository;
import com.officedubac.project.repository.ListeFinaleCndidatsCGSRepository;
import com.officedubac.project.services.CandidatService;
import com.officedubac.project.services.ConcoursGeneralService;
import com.officedubac.project.services.ParametrageService;
import com.officedubac.project.utils.IpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/enrollment-cgs")
@RequiredArgsConstructor
@Tag(name="Candidat Controller", description = "Endpoints responsables de la gestion des données CGS")
public class CGSController
{
    @Autowired
    private final ConcoursGeneralService concoursGeneralService;
    @Autowired
    private final ListeFinaleCndidatsCGSRepository listeFinaleCndidatsCGSRepository;
    @Autowired
    private IpUtils ipUtils;

    @GetMapping("/all-regles-centre")
    public List<RegleCentre> getAll() {
        return concoursGeneralService.findAll();
    }

    @GetMapping("/regles-centre/{id}")
    public ResponseEntity<RegleCentre> getById(@PathVariable String id) {
        return concoursGeneralService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create-regles-centre")
    public RegleCentre create(@RequestBody RegleCentre regle) {
        return concoursGeneralService.create(regle);
    }

    @PutMapping("/{id}")
    public RegleCentre update(@PathVariable String id, @RequestBody RegleCentre regle) {
        return concoursGeneralService.update(id, regle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        concoursGeneralService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/repartition-candidatsCGS-par-centre")
    public List<ListeFinaleCandidatsCGS> repCentre(@RequestParam long session) {
        return concoursGeneralService.repartitionParCentreCGS(session);
    }

    @GetMapping("/get-centreCompo-by-academia")
    public List<String> centresByAcademia(@RequestParam String academia, @RequestParam Long session) {
        return concoursGeneralService.getCentresByAcademia(academia, session);
    }

    @GetMapping("/get-niveaux-by-centre")
    public List<String> niveauxByAcademia(@RequestParam String centre, @RequestParam Long session) {
        return concoursGeneralService.getNiveauxByCentre(centre, session);
    }

    @GetMapping("/get-discipline-by-centre-niv-session")
    public List<String> disciplinesByAcademia(@RequestParam String centre, @RequestParam String niveau, @RequestParam Long session) {
        return concoursGeneralService.getDisciplineByCentreNiveauSession(centre, niveau, session);
    }


    @GetMapping(value="/get-all-repartition")
    public ResponseEntity<List<ListeFinaleCandidatsCGS>> fusionRep() throws Exception
    {
        return ResponseEntity.ok(this.concoursGeneralService.getAllData());
    }

    @GetMapping("/generate-concours-general")
    public void generateConcoursGeneral(
            @RequestParam(value = "discipline") String discipline,
            @RequestParam(value = "centre") String centre,
            @RequestParam(value = "session") Long session,
            @RequestParam(value = "level") String level,
            HttpServletResponse response) throws IOException, DocumentException {

        // ================= NORMALISATION =================
        // Si aucune discipline n'est fournie, on prend celle du document
        if (discipline == null || discipline.trim().isEmpty()) {
            discipline = "";
        }

        // Récupération des candidats depuis la base
        List<ListeFinaleCandidatsCGS> candidats = listeFinaleCndidatsCGSRepository.findByCentreCompositionAndSessionAndDisciplineAndLevelOrderByLastnameAsc(centre, session, discipline, level);

        if (candidats.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        // Configuration de la réponse PDF (comme avant)
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=concours_general_.pdf");

        // Document en orientation portrait (A4)
        Document document = new Document(PageSize.A4.rotate(), 36f, 36f, 36f, 36f);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        FooterEvent event = new FooterEvent(initializeFonts().verySmallFont);
        writer.setPageEvent(event);
        document.open();

        Image logo = Image.getInstance(
                new ClassPathResource("images/sn.png")
                        .getInputStream().readAllBytes());
        logo.scaleToFit(70f, 70f);

        // ================= POLICES =================
        Font helv10 = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL);
        Font helv12Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font helv14 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD);
        Font helv16Bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);

        // ================= GÉNÉRATION =================
        generateConcoursPage(document, discipline, candidats, logo, helv10, helv12Bold, helv14, helv16Bold);

        document.close();
    }

    /**
     * Construit la page avec l'en‑tête, le titre et le tableau des candidats.
     */
    private void generateConcoursPage(Document document, String discipline,
                                      List<ListeFinaleCandidatsCGS> candidats, Image logo,
                                      Font f10, Font f12Bold, Font f14, Font f16Bold) throws DocumentException
    {
        // --- 1. EN-TÊTE ---
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{0.55f, 4f, 1f});

        PdfPCell imageCell = new PdfPCell(logo);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(imageCell);

        Paragraph headerText = new Paragraph(
                "REPUBLIQUE DU SENEGAL\nUn Peuple - Un But - Une Foi\n" +
                        "Ministère de l'Enseignement supérieur, de la Recherche et de l'Innovation" +
                        "\nUniversité Cheikh Anta Diop\nOffice du Baccalauréat",
                f10
        );
        headerText.setLeading(14f, 0);
        headerText.setAlignment(Element.ALIGN_LEFT);
        PdfPCell textCell = new PdfPCell(headerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(textCell);

        Font f16Bold_ = new Font(f16Bold);
        f16Bold_.setStyle(Font.BOLD);

        // Cellule académie (par exemple "DAKAR")
        PdfPCell academieCell = new PdfPCell(new Phrase("CONCOURS GENERAL SENEGALAIS" , f16Bold_));
        academieCell.setBorder(Rectangle.NO_BORDER);
        academieCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        academieCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.addCell(academieCell);

        document.add(header);

        Font f10BoldItalic = new Font(f12Bold);
        f10BoldItalic.setStyle(Font.BOLDITALIC);

        Paragraph centreCompo = new Paragraph("Centre de composition : " + (candidats.isEmpty() ? "" : candidats.get(0).getCentreComposition()), f12Bold);
        centreCompo.setAlignment(Element.ALIGN_CENTER);
        centreCompo.setSpacingAfter(2f);
        document.add(centreCompo);

        Paragraph disciplinePar = new Paragraph("Discipline : " + discipline, f12Bold);
        disciplinePar.setAlignment(Element.ALIGN_CENTER);
        disciplinePar.setSpacingAfter(2f);
        document.add(disciplinePar);

        Paragraph niveau = new Paragraph("Niveau : " + (candidats.isEmpty() ? "" : candidats.get(0).getLevel()) + " / Effectif : " + candidats.size() + " candidats.", f12Bold);
        niveau.setAlignment(Element.ALIGN_CENTER);
        niveau.setSpacingAfter(10f);
        document.add(niveau);

        // --- 2. TITRE ---
        Paragraph titre = new Paragraph("Liste officielle des candidats au concours général " + candidats.get(0).getSession() + "\n" + candidats.get(0).getAcademia() + " (" + (candidats.isEmpty() ? "" : candidats.get(0).getLevel()) + ")", f16Bold);
        titre.setAlignment(Element.ALIGN_CENTER);
        titre.setSpacingAfter(20f);
        document.add(titre);

        // --- 3. TABLEAU DES CANDIDATS ---
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        // Largeurs relatives des colonnes (ajustez si nécessaire)
        float[] columnWidths = {0.5f, 2f, 2.5f, 1.5f, 0.8f, 1.75f, 0.8f, 3f, 3f, 1.75f};
        try {
            table.setWidths(columnWidths);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // En‑tête du tableau
        String[] headers = {
                "N°", "Discipline", "Prénom(s)", "Nom", "Sexe",
                "Date de naissance", "Série", "Etablissement d'origine",
                "Centre de composition", "Emargement"
        };
        for (String h : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(h, f12Bold));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(5f);
            table.addCell(headerCell);
        }

        // Parcours des candidats
        int numero = 1;
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd"); // format de la base
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        for (ListeFinaleCandidatsCGS c : candidats) {
            String dateNaissanceFormatee = "";
            if (c.getDate_birth() != null) {
                try {
                    Date date = inputFormat.parse(c.getDate_birth());
                    dateNaissanceFormatee = outputFormat.format(date);
                } catch (ParseException e) {
                    dateNaissanceFormatee = c.getDate_birth(); // fallback
                }
            }

            addRow(table,
                    String.valueOf(numero++),               // ou c.getOrdre()
                    discipline,                              // ou c.getDiscipline()
                    c.getFirstname(),
                    c.getLastname(),
                    c.getSexe(),
                    dateNaissanceFormatee,
                    c.getSerie(),
                    c.getEtablissementOrigine(),
                    c.getCentreComposition(),
                    "",                                      // émargement vide
                    f10);
        }

        document.add(table);
    }

    /**
     * Ajoute une ligne au tableau avec les bonnes alignements.
     */
    private void addRow(PdfPTable table, String num, String discipline, String prenom, String nom,
                        String sexe, String dateNaiss, String serie, String etablissement,
                        String centre, String emargement, Font font) {

        // N°
        PdfPCell cell = new PdfPCell(new Phrase(num, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);

        // Discipline
        cell = new PdfPCell(new Phrase(discipline, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);

        // Prénom(s)
        cell = new PdfPCell(new Phrase(prenom, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);

        // Nom
        cell = new PdfPCell(new Phrase(nom, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);

        // Sexe
        cell = new PdfPCell(new Phrase(sexe, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);

        // Date de naissance
        cell = new PdfPCell(new Phrase(dateNaiss, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);

        // Série
        cell = new PdfPCell(new Phrase(serie, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);

        // Etablissement d'origine
        cell = new PdfPCell(new Phrase(etablissement, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);

        // Centre de composition
        cell = new PdfPCell(new Phrase(centre, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);

        // Emargement (vide)
        cell = new PdfPCell(new Phrase(emargement, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        table.addCell(cell);
    }


    @Operation(summary="Service de listing des candidats selon l'établissement et l'édition du CGS")
    @GetMapping("/candidats-cgs/etablissement/{etablissementId}/{session}")
    public ResponseEntity<List<ConcoursGeneral>> getCandidatsParEtablissement(@PathVariable String etablissementId, @PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.concoursGeneralService.getCandidatsParEtablissement(etablissementId, session));
    }


    // Par abus de langue, j'ai mis ville, en réalité, c'est le centre d'examen de l'établissement du candidat
    @Operation(summary="Service de listing des villes au CGS")
    @GetMapping("/villes-cgs/{session}")
    public ResponseEntity<List<String>> getVillesOfCandidate_(@PathVariable Long session) throws Exception {
        return ResponseEntity.ok(this.concoursGeneralService.getVillesOfCandidate(session));
    }

    @GetMapping("/etab-not-receptionned-cgs/{session}")
    public ResponseEntity<List<EtabSummaryDTO>> getSummarizeEtabNotReceptionned(
            @PathVariable Long session) {

        List<EtabSummaryDTO> result = concoursGeneralService.getSummarizeEtabNotReceptionned(session);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/liste-cgs/{session}")
    public ResponseEntity<List<ListeFinaleCandidatsCGS>> getAllCdtCgs(
            @PathVariable Long session) {

        List<ListeFinaleCandidatsCGS> result = concoursGeneralService.getAlllCGS(session);
        return ResponseEntity.ok(result);
    }

    private static class FontConfiguration {
        Font verySmallFont;
    }

    private FontConfiguration initializeFonts() {
        FontConfiguration fonts = new FontConfiguration();
        fonts.verySmallFont = FontFactory.getFont(FontFactory.TIMES, 8);
        return fonts;
    }




}
