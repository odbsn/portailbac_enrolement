package com.officedubac.project.controllers;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.officedubac.project.models.*;
import com.officedubac.project.repository.*;
import com.officedubac.project.services.CandidatService;
import com.officedubac.project.services.ConcoursGeneralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/pdf")
@RequiredArgsConstructor
@Tag(name="PDF Controller", description = "Endpoints responsables de la gestion des PDF")
public class PdfController
{
    @Autowired
    private final CandidatService candidatService;

    @Autowired
    private final ConcoursGeneralService concoursGeneralService;

    @Autowired
    private final EtablissementRepository etablissementRepository;

    @Autowired
    private final CandidatRepository candidatRepository;

    private final ConcoursGeneralRepository concoursGeneralRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final CompteDroitInscriptionRepository compteDroitInscriptionRepository;

    @Autowired
    private final MatiereRepository matiereRepository;


    @Operation(summary="Service de génération d'une liste de candidats en PDF")
    @GetMapping("/generate")
    public void generatePdf(HttpServletResponse response,
                            @RequestParam String etablissementId,
                            @RequestParam Long session,
                            @RequestParam String user,
                            @RequestParam(required = false) String sortBy,
                            @RequestParam String serie,
                            @RequestParam(required = false) String optionI,
                            @RequestParam(required = false) Long start,
                            @RequestParam(required = false) Long end,
                            @RequestParam(required = false) String cExam
                            ) throws IOException, DocumentException {

        Etablissement etb = etablissementRepository.findById(etablissementId).orElse(null);
        assert etb != null;
        System.out.println("A ce niveau" + etablissementId + " " + session + " " + user + " " + sortBy + " " + serie + " " + optionI + " " + start + " " + end);
        CompteDroitsInscription cdI = compteDroitInscriptionRepository.findByEtablissementNameAndSession(etb.getName(), session);
        System.out.println("A ce niveau" + cdI.getSession() + " " + cdI.getEtablissement().getName());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");

        // Enregistrement des polices
        FontFactory.register("fonts/gadugi-normal.ttf", "GADUGI");
        FontFactory.register("fonts/gadugi-gras.ttf", "GADUGI-GRAS");

        Font normal = FontFactory.getFont("GADUGI", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        Font gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        Font big_gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
        Font bold = gras;

        Document document = new Document(PageSize.A4.rotate(), 20f, 20f, 30f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(normal, user));
        if ("oneCdt".equals(optionI) || "rangeCdt".equals(optionI))
        {
            writer.setPageEvent(new WatermarkHandler("RAJOUT"));
        }
        else
        {
            writer.setPageEvent(new WatermarkHandler("LISTE PROVISOIRE"));
        }

        document.open();

        // Header avec logo
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{0.5f, 3f, 2f});

        //Cellule avec logo
        Image logo = Image.getInstance(
                new ClassPathResource("images/logo-UCAD_.png").getInputStream().readAllBytes()
        );
        logo.scaleToFit(70, 70);
        PdfPCell imageCell = new PdfPCell(logo);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(imageCell);

    // Cellule avec texte
        Paragraph headerText = new Paragraph(
                "REPUBLIQUE DU SENEGAL\nUn Peuple – Un But – Une Foi\n" +
                        "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT",
                gras
        );
        headerText.setLeading(20f, 0);
        headerText.setAlignment(Element.ALIGN_LEFT);
        PdfPCell textCell = new PdfPCell(headerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(textCell);

        // Créer le petit tableau 3x3
        PdfPTable smallTable = new PdfPTable(3);

// Définir la largeur des colonnes (ajuster si nécessaire)
        smallTable.setWidths(new float[] { 0.5f, 0.5f, 0.5f }); // Largeurs égales pour chaque colonne

// Aligner le texte au centre pour chaque cellule
        PdfPCell cell1 = new PdfPCell(new Phrase("FAEB 1"));
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase("FAEB 2"));
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell2);

        PdfPCell cell3 = new PdfPCell(new Phrase("FAEB 3"));
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell3);

// Ligne 2 : Ajouter les données avec un alignement correct
        PdfPCell cell4 = new PdfPCell(new Phrase(String.valueOf(cdI.getCount_5000())));
        cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell4);

        PdfPCell cell5 = new PdfPCell(new Phrase(String.valueOf(cdI.getCount_1000_EF())));
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell5);

        PdfPCell cell6 = new PdfPCell(new Phrase(String.valueOf(cdI.getCount_1000_OB())));
        cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell6);

// Insérer le petit tableau dans une cellule (celle-ci sera sans bordure)
        PdfPCell tableCell = new PdfPCell(smallTable);
        tableCell.setBorder(Rectangle.NO_BORDER);
        tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // Centrage vertical
        tableCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Centrage horizontal

// Ajouter la cellule du tableau dans le tableau principal (headerTable)
        headerTable.addCell(tableCell);

        // Ajouter le headerTable au document
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        Paragraph sessionInfo = new Paragraph(
                "BACCALAUREAT DE L’ENSEIGNEMENT SECONDAIRE\nANNEE " + session + "\nSession NORMALE " + session, big_gras);
        sessionInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(sessionInfo);

        String ligneCentreExamen;

        if (cExam == null || cExam.trim().isEmpty()) {
            ligneCentreExamen = "Centre d'examen : " + etb.getCentreExamen().getName() + "\n";
        }
        else
        {
            ligneCentreExamen = "Centre d'examen : " + cExam + "\n";
        }

        if (etb != null)
        {
            Paragraph etablissement = new Paragraph(
                    "\n~~Liste des candidats~~\n" +
                            "Etablissement de provenance : " + etb.getCode() + " / " + etb.getName() + "\n" +
                            "Type de candidats présentés : " + etb.getTypeCandidat().getName().toUpperCase() + "\n" +
                            ligneCentreExamen +
                    "(Veuillez ne pas considérer cette liste comme officielle)", big_gras);
            etablissement.setAlignment(Element.ALIGN_CENTER);
            document.add(etablissement);
        }

        List<Candidat> result = null;

        if ("all".equals(serie))
        {
            result = candidatService.getAllCandidatsForPdf(etablissementId, session, sortBy);
        }
        else
        {
            if ("allCdt".equals(optionI))
            {
                result = candidatService.getFilteredCandidatsForPdf(etablissementId, session, sortBy, serie);
            }
            if ("oneCdt".equals(optionI))
            {
                result = candidatService.getFilteredOneCandidatsForPdf(etablissementId, session, serie, String.valueOf(start));
            }
            if ("rangeCdt".equals(optionI))
            {
                result = candidatService.getFilteredCandidatsByBoundsPdf(etablissementId, session, sortBy, serie, start, end);
            }
        }

        assert result != null;
        if (!cExam.trim().isEmpty())
        {
            result = result.stream()
                    .filter(c -> cExam.equals(c.getCentreExamen().getName()))
                    .sorted(Comparator.comparing(c -> c.getSerie().getCode()))
                    .collect(Collectors.toList());
        }
        else
        {
            result = result.stream()
                    .sorted(Comparator.comparing(c -> c.getSerie().getCode()))
                    .collect(Collectors.toList());
        }

        // Données de séries
        String[] headers = {
                "Code\nC.E.C", "Nom du Centre\nd’Etat Civil (C.E.C)", "Ann.\nDécl.", "N° Acte\nNais.", "N° de\nDos.",
                "Prénom (s)", "Nom", "Sexe", "Date Nais.", "Lieu Nais.", "Nationalité", "Matière (s)\nOptionn. (s)", "EPS", "Matièr. (s)\nFacult. (s)", "Nb.\nfois", "Signat."
        };

        String currentSerie = null;
        PdfPTable table = null;
        // marge minimale avant le footer (en points : 1 cm ≈ 28.3 points)
        final float MIN_BOTTOM_MARGIN = 60f; // environ 2 cm

        int total = 0;
        int garcons = 0;
        int filles = 0;

        for (int i = 0; i < result.size(); i++) {
            Candidat c = result.get(i);
            String candidateSerie = c.getSerie() != null ? c.getSerie().getName() : "";

            boolean isNewSerie = currentSerie == null || !candidateSerie.equals(currentSerie);

            if (isNewSerie)
            {
                if (table != null) {
                    // 🔹 Vérifie si assez d'espace pour le tableau + marge
                    float spaceLeft = document.getPageSize().getHeight()
                            - document.topMargin()
                            - writer.getVerticalPosition(true);

                    System.out.println("document.topMargin() " + document.topMargin());
                    System.out.println("document.getPageSize().getHeight() " + document.getPageSize().getHeight());
                    System.out.println("writer.getVerticalPosition(true) " + writer.getVerticalPosition(true));
                    System.out.println("spaceLeft " + spaceLeft);
                    System.out.println("MIN_BOTTOM_MARGIN " + MIN_BOTTOM_MARGIN);

                    if (spaceLeft < MIN_BOTTOM_MARGIN) {
                        document.newPage();
                    }

                    document.add(table);
                    Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                    effectif.setSpacingBefore(3f);
                    document.add(effectif);
                    //document.newPage();
                }

                currentSerie = candidateSerie;
                total = garcons = filles = 0;

                Paragraph serieTitle = new Paragraph(currentSerie, big_gras);
                serieTitle.setSpacingBefore(5f);
                serieTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(serieTitle);

                table = new PdfPTable(16);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{
                        1.5f, 4f, 1.5f, 2f, 1.9f, 4.45f, 4f, 1.4f,
                        3f, 3f, 3f, 4f, 1.5f, 2.8f, 1.2f, 2f
                });

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, bold));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBackgroundColor(new Color(253, 245, 230));
                    table.addCell(cell);
                }
            }

            total++;
            if ("M".equalsIgnoreCase(c.getGender().name())) {
                garcons++;
            } else if ("F".equalsIgnoreCase(c.getGender().name())) {
                filles++;
            }

            String[] candidateData = this.mapCandidatToPdfData(c);
            for (String value : candidateData) {
                PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            if (i == result.size() - 1 && table != null)
            {
                document.add(table);
                Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                effectif.setSpacingBefore(5f);
                document.add(effectif);
            }
        }

        document.close();
    }

    @Operation(summary="Service de génération d'une liste officielle de candidats en PDF")
    @GetMapping("/generate-officielle-liste")
    public void generateOLPdf(HttpServletResponse response,
                            @RequestParam String etablissementId,
                            @RequestParam Long session,
                            @RequestParam String user,
                            @RequestParam String serie) throws IOException, DocumentException {

        Etablissement etb = etablissementRepository.findById(etablissementId).orElse(null);
        assert etb != null;
        System.out.println("A ce niveau" + session + " " + etb.getCode());
        CompteDroitsInscription cdI = compteDroitInscriptionRepository.findByEtablissementNameAndSession(etb.getName(), session);
        System.out.println("A ce niveau" + cdI.getSession() + " " + cdI.getEtablissement().getName());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");

        // Enregistrement des polices
        FontFactory.register("fonts/gadugi-normal.ttf", "GADUGI");
        FontFactory.register("fonts/gadugi-gras.ttf", "GADUGI-GRAS");

        Font normal = FontFactory.getFont("GADUGI", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        Font gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        Font big_gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
        Font bold = gras;

        Document document = new Document(PageSize.A4.rotate(), 20f, 20f, 30f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(normal, user));
        writer.setPageEvent(new WatermarkHandler2("LISTE DEFINITIVE"));
        document.open();

        // Header avec logo
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{0.5f, 5f});

        //Cellule avec logo
        Image logo = Image.getInstance(
                new ClassPathResource("images/logo-UCAD_.png").getInputStream().readAllBytes()
        );
        logo.scaleToFit(70, 70);
        PdfPCell imageCell = new PdfPCell(logo);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(imageCell);

        // Cellule avec texte
        Paragraph headerText = new Paragraph(
                "REPUBLIQUE DU SENEGAL\nUn Peuple – Un But – Une Foi\n" +
                        "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT",
                gras
        );
        headerText.setLeading(20f, 0);
        headerText.setAlignment(Element.ALIGN_LEFT);
        PdfPCell textCell = new PdfPCell(headerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(textCell);

        /**
        // Créer le petit tableau 3x3
        PdfPTable smallTable = new PdfPTable(3);

// Définir la largeur des colonnes (ajuster si nécessaire)
        smallTable.setWidths(new float[] { 0.5f, 0.5f, 0.5f }); // Largeurs égales pour chaque colonne

        // Aligner le texte au centre pour chaque cellule

        PdfPCell cell1 = new PdfPCell(new Phrase("FAEB 1"));
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase("FAEB 2"));
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell2);

        PdfPCell cell3 = new PdfPCell(new Phrase("FAEB 3"));
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell3);

        // Ligne 2 : Ajouter les données avec un alignement correct
        PdfPCell cell4 = new PdfPCell(new Phrase(String.valueOf(cdI.getCount_5000())));
        cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell4.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell4);

        PdfPCell cell5 = new PdfPCell(new Phrase(String.valueOf(cdI.getCount_1000_EF())));
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell5);

        PdfPCell cell6 = new PdfPCell(new Phrase(String.valueOf(cdI.getCount_1000_OB())));
        cell6.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        smallTable.addCell(cell6);

// Insérer le petit tableau dans une cellule (celle-ci sera sans bordure)
        PdfPCell tableCell = new PdfPCell(smallTable);
        tableCell.setBorder(Rectangle.NO_BORDER);
        tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // Centrage vertical
        tableCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Centrage horizontal



// Ajouter la cellule du tableau dans le tableau principal (headerTable)
        headerTable.addCell(tableCell);
         **/

        // Ajouter le headerTable au document
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        Paragraph sessionInfo = new Paragraph(
                "BACCALAUREAT DE L’ENSEIGNEMENT SECONDAIRE\nANNEE " + session + "\nSession NORMALE " + session, big_gras);
        sessionInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(sessionInfo);

        if (etb != null) {
            Paragraph etablissement = new Paragraph(
                    "\n~~LISTE OFFICIELLE DES CANDIDATS~~\n" +
                            "Etablissement de provenance : " + etb.getCode() + " / " + etb.getName() + "\n" +
                            "Centre d’examen : " + etb.getCentreExamen().getName() + "\n", big_gras);
            etablissement.setAlignment(Element.ALIGN_CENTER);
            document.add(etablissement);
        }

        List<Candidat> result;

        if ("all".equals(serie))
        {
            result = candidatService.getAllCandidatsForPdfOL(etablissementId, session);
        }
        else
        {
            result = candidatService.getFilteredCandidatsForPdfOL(etablissementId, session, serie);
        }

        result = result.stream()
                .sorted(Comparator.comparing(c -> c.getSerie().getCode()))
                .collect(Collectors.toList());

        // Données de séries
        String[] headers = {
                "Code\nC.E.C", "Nom du Centre\nd’Etat Civil (C.E.C)", "Ann.\nDécl.", "N° Acte\nNais.",
                "Prénom (s)", "Nom", "Sexe", "Date Nais.", "Lieu Nais.", "Nationalité", "Matière (s)\nOptionn. (s) / Spécialité", "EPS", "Matièr. (s)\nFacult. (s)", "Nb.\nfois", "Avis OB"
        };

        String currentSerie = null;
        PdfPTable table = null;
        // marge minimale avant le footer (en points : 1 cm ≈ 28.3 points)
        final float MIN_BOTTOM_MARGIN = 60f; // environ 2 cm

        int total = 0;
        int garcons = 0;
        int filles = 0;

        for (int i = 0; i < result.size(); i++) {
            Candidat c = result.get(i);
            String candidateSerie = c.getSerie() != null ? c.getSerie().getName() : "";

            boolean isNewSerie = currentSerie == null || !candidateSerie.equals(currentSerie);

            if (isNewSerie)
            {
                if (table != null) {
                    // 🔹 Vérifie si assez d'espace pour le tableau + marge
                    float spaceLeft = document.getPageSize().getHeight()
                            - document.topMargin()
                            - writer.getVerticalPosition(true);

                    System.out.println("document.topMargin() " + document.topMargin());
                    System.out.println("document.getPageSize().getHeight() " + document.getPageSize().getHeight());
                    System.out.println("writer.getVerticalPosition(true) " + writer.getVerticalPosition(true));
                    System.out.println("spaceLeft " + spaceLeft);
                    System.out.println("MIN_BOTTOM_MARGIN " + MIN_BOTTOM_MARGIN);

                    if (spaceLeft < MIN_BOTTOM_MARGIN) {
                        document.newPage();
                    }

                    document.add(table);
                    Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                    effectif.setSpacingBefore(3f);
                    document.add(effectif);
                    //document.newPage();
                }

                currentSerie = candidateSerie;
                total = garcons = filles = 0;

                Paragraph serieTitle = new Paragraph(currentSerie, big_gras);
                serieTitle.setSpacingBefore(5f);
                serieTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(serieTitle);

                table = new PdfPTable(15);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{
                        1.5f, 4f, 1.5f, 2f, 4.45f, 4f, 1.4f,
                        3f, 3f, 3f, 4f, 1.5f, 2.8f, 1.2f, 2f
                });

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, bold));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBackgroundColor(new Color(253, 245, 230));
                    table.addCell(cell);
                }
            }

            total++;
            if ("M".equalsIgnoreCase(c.getGender().name())) {
                garcons++;
            } else if ("F".equalsIgnoreCase(c.getGender().name())) {
                filles++;
            }

            String[] candidateData = this.mapCandidatToPdfDataOL(c);
            for (String value : candidateData) {
                PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            if (i == result.size() - 1 && table != null)
            {
                document.add(table);
                Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                effectif.setSpacingBefore(5f);
                document.add(effectif);
            }
        }

        document.close();
        // Maintenant que tout est OK :
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");
    }

    @Operation(summary = "Service de génération d'une liste de candidats en PDF du CGS")
    @GetMapping("/generate-cgs")
    public void generatePdfCGS(HttpServletResponse response,
                               @RequestParam String etablissementId,
                               @RequestParam Long session,
                               @RequestParam String user,
                               @RequestParam String specialite,
                               @RequestParam String level) throws IOException, DocumentException {

        // Récupération établissement et compte droits inscription
        Etablissement etb = etablissementRepository.findById(etablissementId)
                .orElseThrow(() -> new RuntimeException("Établissement introuvable"));

        CompteDroitsInscription cdI = compteDroitInscriptionRepository
                .findByEtablissementNameAndSession(etb.getName(), session);

        // Configuration réponse PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");

        // Polices
        FontFactory.register("fonts/gadugi-normal.ttf", "GADUGI");
        FontFactory.register("fonts/gadugi-gras.ttf", "GADUGI-GRAS");
        Font normal = FontFactory.getFont("GADUGI", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        Font gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        Font bigGras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);

        Document document = new Document(PageSize.A4.rotate(), 20f, 20f, 30f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(normal, user));
        document.open();

        // --- Header principal ---
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{0.5f, 3f, 2f});

        // Logo
        Image logo = Image.getInstance(new ClassPathResource("images/logo-UCAD_.png").getInputStream().readAllBytes());
        logo.scaleToFit(70, 70);
        PdfPCell imageCell = new PdfPCell(logo);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(imageCell);

        // Texte header
        Paragraph headerText = new Paragraph(
                "REPUBLIQUE DU SENEGAL\nUn Peuple – Un But – Une Foi\n" +
                        "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT",
                gras
        );

        headerText.setLeading(20f, 0);
        headerText.setAlignment(Element.ALIGN_LEFT);
        PdfPCell textCell = new PdfPCell(headerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(textCell);

        // Créer le petit tableau 3x3
        PdfPTable smallTable = new PdfPTable(3);

        // Définir la largeur des colonnes (ajuster si nécessaire)
        smallTable.setWidths(new float[] { 0.5f, 0.5f, 0.5f }); // Largeurs égales pour chaque colonne

        // Insérer le petit tableau dans une cellule (celle-ci sera sans bordure)
        PdfPCell tableCell = new PdfPCell(smallTable);
        tableCell.setBorder(Rectangle.NO_BORDER);
        tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // Centrage vertical
        tableCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Centrage horizontal

        // Ajouter la cellule du tableau dans le tableau principal (headerTable)
        headerTable.addCell(tableCell);

        // Ajouter le headerTable au document
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        // --- Titre session ---
        Paragraph sessionInfo = new Paragraph(
                "CONCOURS GENERAL SENEGALAIS \nEDITION " + session + "\nDISCIPLINE " + specialite,
                bigGras
        );
        sessionInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(sessionInfo);

        // --- Titre établissement ---
        Paragraph etablissement = new Paragraph(
                "\n~~LISTE NOMINATIVE DES CANDIDATS~~\n" +
                        "Etablissement de provenance : " + etb.getCode() + " / " + etb.getName() + "\n" +
                        "CLASSE : " + level,
                bigGras
        );
        etablissement.setAlignment(Element.ALIGN_CENTER);
        document.add(etablissement);

        // --- Tableau unique pour tous les candidats ---
        List<ConcoursGeneral> result = candidatService.getFilteredCandidatsForPdfCGS(etablissementId, session, specialite, level);

        String[] headers = {
                "Ordre\nmérite", "Prénom(s)", "Nom", "Sexe", "Date de naissance",
                "Série", "Classe Année\n" + (session - 2), "Classe Année\n" + (session - 1), "Note élève (/20)", "Note classe (/20)",
                "Prénom(s)\ndu professeur", "NOM\ndu professeur"
        };

        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);
        table.setSpacingAfter(10f);
        table.setWidths(new float[]{2f,4f,4f,2f,3f,2f,2.5f,2.5f,2.5f,2.5f,4f,4f});

        // En-têtes
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, bigGras));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new Color(253, 245, 230));
            table.addCell(cell);
        }

        // Lignes candidats
        for (int i = 0; i < result.size(); i++) {
            ConcoursGeneral c = result.get(i);
            String[] candidateData = mapCandidatToPdfDataCGS(c, i);
            for (String value : candidateData) {
                PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }
        }

        document.add(table);
        document.close();
    }


    @Operation(summary="Service de génération des rejets en PDF")
    @GetMapping("/generate-rejets")
    public void generatePdfRejet(HttpServletResponse response,
                            @RequestParam String etablissementId,
                            @RequestParam Long session,
                            @RequestParam String user) throws IOException, DocumentException {

        Etablissement etb = etablissementRepository.findById(etablissementId).orElse(null);
        assert etb != null;
        System.out.println("A ce niveau" + session + " " + etb.getCode());
        CompteDroitsInscription cdI = compteDroitInscriptionRepository.findByEtablissementNameAndSession(etb.getName(), session);
        System.out.println("A ce niveau" + cdI.getSession() + " " + cdI.getEtablissement().getName());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");

        // Enregistrement des polices
        FontFactory.register("fonts/gadugi-normal.ttf", "GADUGI");
        FontFactory.register("fonts/gadugi-gras.ttf", "GADUGI-GRAS");

        Font normal = FontFactory.getFont("GADUGI", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        Font gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        Font big_gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
        Font bold = gras;

        Document document = new Document(PageSize.A4.rotate(), 20f, 20f, 30f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(normal, user));
        document.open();

        // Header avec logo
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{0.5f, 3f, 2f});

// 1️⃣ Cellule avec logo
        Image logo = Image.getInstance(
                new ClassPathResource("images/logo-UCAD_.png").getInputStream().readAllBytes()
        );
        logo.scaleToFit(70, 70);
        PdfPCell imageCell = new PdfPCell(logo);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(imageCell);

// 2️⃣ Cellule avec texte
        Paragraph headerText = new Paragraph(
                "REPUBLIQUE DU SENEGAL\nUn Peuple – Un But – Une Foi\n" +
                        "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT",
                gras
        );
        headerText.setLeading(20f, 0);
        headerText.setAlignment(Element.ALIGN_LEFT);
        PdfPCell textCell = new PdfPCell(headerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(textCell);

        // Créer le petit tableau 3x3
        PdfPTable smallTable = new PdfPTable(3);

        // Définir la largeur des colonnes (ajuster si nécessaire)
        smallTable.setWidths(new float[] { 0.5f, 0.5f, 0.5f }); // Largeurs égales pour chaque colonne

    // Insérer le petit tableau dans une cellule (celle-ci sera sans bordure)
        PdfPCell tableCell = new PdfPCell(smallTable);
        tableCell.setBorder(Rectangle.NO_BORDER);
        tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // Centrage vertical
        tableCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Centrage horizontal

    // Ajouter la cellule du tableau dans le tableau principal (headerTable)
        headerTable.addCell(tableCell);

        // Ajouter le headerTable au document
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        Paragraph sessionInfo = new Paragraph(
                "BACCALAUREAT DE L’ENSEIGNEMENT SECONDAIRE\nANNEE " + session + "\nSession NORMALE " + session, big_gras);
        sessionInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(sessionInfo);

        if (etb != null) {
            Paragraph etablissement = new Paragraph(
                    "\n~~CAS DE REJETS DES DOSSIERS~~\n" +
                            "Etablissement de provenance : " + etb.getCode() + " / " + etb.getName() + "\n" +
                            "Centre d’examen : " + etb.getCentreExamen().getName(), big_gras);
            etablissement.setAlignment(Element.ALIGN_CENTER);
            document.add(etablissement);
        }

        List<Candidat> candidatsAvecRejets = candidatService.getFilteredCandidatsForPdfReject(etablissementId, session);

        List<Candidat> result = candidatsAvecRejets.stream()
                .filter(c -> c.getRejets() != null && !c.getRejets().isEmpty())
                .collect(Collectors.toList());

        // Données de séries
        String[] headers = {
                "N° de\nDos.", "Prénom (s)", "Nom", "Date Nais.", "Lieu Nais.", "Motif (s) de Rejet (s)", "Opérateur OB"
        };

        String currentSerie = null;
        PdfPTable table = null;
        // marge minimale avant le footer (en points : 1 cm ≈ 28.3 points)
        final float MIN_BOTTOM_MARGIN = 60f; // environ 2 cm

        int total = 0;
        int garcons = 0;
        int filles = 0;

        for (int i = 0; i < result.size(); i++) {
            Candidat c = result.get(i);
            String candidateSerie = c.getSerie() != null ? c.getSerie().getName() : "";

            boolean isNewSerie = currentSerie == null || !candidateSerie.equals(currentSerie);

            if (isNewSerie)
            {
                if (table != null) {
                    // 🔹 Vérifie si assez d'espace pour le tableau + marge
                    float spaceLeft = document.getPageSize().getHeight()
                            - document.topMargin()
                            - writer.getVerticalPosition(true);

                    System.out.println("document.topMargin() " + document.topMargin());
                    System.out.println("document.getPageSize().getHeight() " + document.getPageSize().getHeight());
                    System.out.println("writer.getVerticalPosition(true) " + writer.getVerticalPosition(true));
                    System.out.println("spaceLeft " + spaceLeft);
                    System.out.println("MIN_BOTTOM_MARGIN " + MIN_BOTTOM_MARGIN);

                    if (spaceLeft < MIN_BOTTOM_MARGIN) {
                        document.newPage();
                    }

                    document.add(table);
                    Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                    effectif.setSpacingBefore(3f);
                    document.add(effectif);
                    //document.newPage();
                }

                currentSerie = candidateSerie;
                total = garcons = filles = 0;

                Paragraph serieTitle = new Paragraph(currentSerie, big_gras);
                serieTitle.setSpacingBefore(5f);
                serieTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(serieTitle);

                table = new PdfPTable(7);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{
                        1.2f, 4.45f, 4f, 3f, 3f, 7f, 3.3f
                });

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, bold));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBackgroundColor(new Color(253, 245, 230));
                    table.addCell(cell);
                }
            }

            total++;
            if ("M".equalsIgnoreCase(c.getGender().name())) {
                garcons++;
            } else if ("F".equalsIgnoreCase(c.getGender().name())) {
                filles++;
            }

            String[] candidateData = this.mapCandidatToPdfData2(c);
            for (String value : candidateData) {
                PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            if (i == result.size() - 1 && table != null)
            {
                document.add(table);
                Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                effectif.setSpacingBefore(5f);
                document.add(effectif);
            }
        }

        document.close();
    }



    @Operation(summary="Service de génération des rejets en PDF")
    @GetMapping("/generate-rejets-cgs")
    public void generatePdfRejet_(HttpServletResponse response,
                                 @RequestParam String etablissementId,
                                 @RequestParam Long session,
                                 @RequestParam String user) throws IOException, DocumentException {

        Etablissement etb = etablissementRepository.findById(etablissementId).orElse(null);
        assert etb != null;
        System.out.println("A ce niveau" + session + " " + etb.getCode());
        // CompteDroitsInscription cdI = compteDroitInscriptionRepository.findByEtablissementNameAndSession(etb.getName(), session);
        // System.out.println("A ce niveau" + cdI.getSession() + " " + cdI.getEtablissement().getName());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");

        // Enregistrement des polices
        FontFactory.register("fonts/gadugi-normal.ttf", "GADUGI");
        FontFactory.register("fonts/gadugi-gras.ttf", "GADUGI-GRAS");

        Font normal = FontFactory.getFont("GADUGI", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        Font gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        Font big_gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
        Font bold = gras;

        Document document = new Document(PageSize.A4.rotate(), 20f, 20f, 30f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(normal, user));
        document.open();

        // Header avec logo
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{0.5f, 3f, 2f});

        // 1️⃣ Cellule avec logo
        Image logo = Image.getInstance(
                new ClassPathResource("images/logo-UCAD_.png").getInputStream().readAllBytes()
        );
        logo.scaleToFit(70, 70);
        PdfPCell imageCell = new PdfPCell(logo);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(imageCell);

        // 2️⃣ Cellule avec texte
        Paragraph headerText = new Paragraph(
                "REPUBLIQUE DU SENEGAL\nUn Peuple – Un But – Une Foi\n" +
                        "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT",
                gras
        );
        headerText.setLeading(20f, 0);
        headerText.setAlignment(Element.ALIGN_LEFT);
        PdfPCell textCell = new PdfPCell(headerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(textCell);

        // Créer le petit tableau 3x3
        PdfPTable smallTable = new PdfPTable(3);

        // Définir la largeur des colonnes (ajuster si nécessaire)
        smallTable.setWidths(new float[] { 0.5f, 0.5f, 0.5f }); // Largeurs égales pour chaque colonne

        // Insérer le petit tableau dans une cellule (celle-ci sera sans bordure)
        PdfPCell tableCell = new PdfPCell(smallTable);
        tableCell.setBorder(Rectangle.NO_BORDER);
        tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // Centrage vertical
        tableCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Centrage horizontal

        // Ajouter la cellule du tableau dans le tableau principal (headerTable)
        headerTable.addCell(tableCell);

        // Ajouter le headerTable au document
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        Paragraph sessionInfo = new Paragraph(
                "CONCOURS GENERAL SENEGALAIS\nANNEE " + session, big_gras);
        sessionInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(sessionInfo);

        if (etb != null) {
            Paragraph etablissement = new Paragraph(
                    "\n~~CAS DE REJETS DES DOSSIERS~~\n" +
                            "Etablissement de provenance : " + etb.getCode() + " / " + etb.getName() + "\n" +
                            "Centre d’examen : " +
                            (etb.getCentreExamen() != null ? etb.getCentreExamen().getName() : ""),
                    big_gras);
            etablissement.setAlignment(Element.ALIGN_CENTER);
            document.add(etablissement);
        }

        List<ConcoursGeneral> candidatsAvecRejets = concoursGeneralService.getFilteredCandidatsForPdfReject(etablissementId, session);

        List<ConcoursGeneral> result = candidatsAvecRejets.stream()
                .filter(c -> c.getRejets() != null && !c.getRejets().isEmpty())
                .toList();

        // Données de séries
        String[] headers = {
                "Classe", "Prénom (s)", "Nom", "Date Nais.", "Lieu Nais.", "Motif (s) de Rejet (s)", "Opérateur OB"
        };

        String currentSerie = null;
        PdfPTable table = null;
        // marge minimale avant le footer (en points : 1 cm ≈ 28.3 points)
        final float MIN_BOTTOM_MARGIN = 60f; // environ 2 cm

        int total = 0;
        int garcons = 0;
        int filles = 0;

        for (int i = 0; i < result.size(); i++) {
            ConcoursGeneral c = result.get(i);
            String candidateSerie = c.getSerie() != null ? c.getSerie().getName() : "";

            boolean isNewSerie = !candidateSerie.equals(currentSerie);

            if (isNewSerie)
            {
                if (table != null) {
                    // 🔹 Vérifie si assez d'espace pour le tableau + marge
                    float spaceLeft = document.getPageSize().getHeight()
                            - document.topMargin()
                            - writer.getVerticalPosition(true);

                    System.out.println("document.topMargin() " + document.topMargin());
                    System.out.println("document.getPageSize().getHeight() " + document.getPageSize().getHeight());
                    System.out.println("writer.getVerticalPosition(true) " + writer.getVerticalPosition(true));
                    System.out.println("spaceLeft " + spaceLeft);
                    System.out.println("MIN_BOTTOM_MARGIN " + MIN_BOTTOM_MARGIN);

                    if (spaceLeft < MIN_BOTTOM_MARGIN) {
                        document.newPage();
                    }

                    document.add(table);
                    Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                    effectif.setSpacingBefore(3f);
                    document.add(effectif);
                    //document.newPage();
                }

                currentSerie = candidateSerie;
                total = garcons = filles = 0;

                Paragraph serieTitle = new Paragraph(currentSerie, big_gras);
                serieTitle.setSpacingBefore(5f);
                serieTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(serieTitle);

                table = new PdfPTable(7);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{
                        2f, 4.45f, 4f, 3f, 3f, 7f, 3.3f
                });

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, bold));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBackgroundColor(new Color(253, 245, 230));
                    table.addCell(cell);
                }
            }

            total++;
            if ("M".equalsIgnoreCase(c.getGender().name())) {
                garcons++;
            } else if ("F".equalsIgnoreCase(c.getGender().name())) {
                filles++;
            }

            String[] candidateData = this.mapCandidatToPdfData2CGS(c);
            for (String value : candidateData) {
                PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            if (i == result.size() - 1 && table != null)
            {
                document.add(table);
                Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                effectif.setSpacingBefore(5f);
                document.add(effectif);
            }
        }

        document.close();
    }


    @Operation(summary="Service de génération du PV de reception en PDF")
    @GetMapping("/generate-pv")
    public void generatePVReception(HttpServletResponse response,
                                    @RequestParam Long session,
                                    @RequestParam String user,
                                    @RequestParam String codeEtab) throws IOException, DocumentException {

        List<Candidat> cdt = candidatRepository.findByOperatorAndSessionSorted(user, session, codeEtab);
        User us = userRepository.findByLogin(user).orElse(null);
        assert cdt != null;
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");

        // Enregistrement des polices
        FontFactory.register("fonts/gadugi-normal.ttf", "GADUGI");
        FontFactory.register("fonts/gadugi-gras.ttf", "GADUGI-GRAS");

        Font normal = FontFactory.getFont("GADUGI", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        Font gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        Font big_gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
        Font bold = gras;

        Document document = new Document(PageSize.A4.rotate(), 20f, 20f, 30f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(normal, user));
        document.open();

        // Header avec logo
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{0.5f, 3f, 2f});

// 1️⃣ Cellule avec logo
        Image logo = Image.getInstance(
                new ClassPathResource("images/logo-UCAD_.png").getInputStream().readAllBytes()
        );
        logo.scaleToFit(70, 70);
        PdfPCell imageCell = new PdfPCell(logo);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(imageCell);

// 2️⃣ Cellule avec texte
        Paragraph headerText = new Paragraph(
                "REPUBLIQUE DU SENEGAL\nUn Peuple – Un But – Une Foi\n" +
                        "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT",
                gras
        );
        headerText.setLeading(20f, 0);
        headerText.setAlignment(Element.ALIGN_LEFT);
        PdfPCell textCell = new PdfPCell(headerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(textCell);

        // Créer le petit tableau 3x3
        PdfPTable smallTable = new PdfPTable(3);

        // Définir la largeur des colonnes (ajuster si nécessaire)
        smallTable.setWidths(new float[] { 0.5f, 0.5f, 0.5f }); // Largeurs égales pour chaque colonne

        // Insérer le petit tableau dans une cellule (celle-ci sera sans bordure)
        PdfPCell tableCell = new PdfPCell(smallTable);
        tableCell.setBorder(Rectangle.NO_BORDER);
        tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // Centrage vertical
        tableCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Centrage horizontal

        // Ajouter la cellule du tableau dans le tableau principal (headerTable)
        headerTable.addCell(tableCell);

        // Ajouter le headerTable au document
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        Paragraph sessionInfo = new Paragraph(
                "BACCALAUREAT DE L’ENSEIGNEMENT SECONDAIRE\nANNEE " + session, big_gras);
        sessionInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(sessionInfo);

        if (cdt != null) {
            Etablissement etab = etablissementRepository.findByCode(codeEtab);
            assert us != null;
            Paragraph etablissement = new Paragraph(
                    "\n~~PV DE RECEPTION DES DOSSIERS DE CANDIDATURE~~" +
                            "\nEtablissement : " + etab.getName() +
                            "\nRéceptionniste : " + us.getFirstname() + " " + us.getLastname() + " / Clé : " + us.getId(), big_gras);
            etablissement.setAlignment(Element.ALIGN_CENTER);
            document.add(etablissement);
        }

        // Données de séries
        String[] headers = {
                "N° de\nDos.", "Prénom (s)", "Nom", "Date Nais.", "Lieu Nais.", "Sexe", "Série", "Etab.", "Décision", "Motif (s) de Rejet (s)", "Horodatage"
        };

        String currentSerie = null;
        // marge minimale avant le footer (en points : 1 cm ≈ 28.3 points)
        final float MIN_BOTTOM_MARGIN = 60f; // environ 2 cm

        // 📌 1. Créer une seule fois le tableau
        PdfPTable table = new PdfPTable(11);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);
        table.setSpacingAfter(10f);
        table.setWidths(new float[]{1.2f, 4.45f, 3.45f, 2.45f, 3f, 1.2f, 1.2f, 1.3f, 1.67f, 6.85f, 3.45f});

        // 📌 2. Ajouter les headers une seule fois
        for (String header : headers)
        {
            PdfPCell cell = new PdfPCell(new Phrase(header, bold));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new Color(253, 245, 230));
            table.addCell(cell);
        }

        // 📌 3. Ajouter toutes les lignes dans le même tableau
        for (Candidat c : cdt)
        {

            String[] candidateData = this.mapCandidatToPdfData4(c);

            for (String value : candidateData) {
                PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }
        }

        // 📌 4. Ajouter le tableau UNE SEULE FOIS
        document.add(table);

        document.close();
    }

    @Operation(summary="Service de génération du PV de reception en PDF")
    @GetMapping("/generate-pv-cgs")
    public void generatePVReception_(HttpServletResponse response,
                                    @RequestParam Long session,
                                    @RequestParam String user,
                                    @RequestParam String codeEtab) throws IOException, DocumentException {

        List<ConcoursGeneral> cdt = concoursGeneralRepository.findByEtablissementCodeAndSession(codeEtab, session);
        User us = userRepository.findByLogin(user).orElse(null);
        assert cdt != null;
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");

        // Enregistrement des polices
        FontFactory.register("fonts/gadugi-normal.ttf", "GADUGI");
        FontFactory.register("fonts/gadugi-gras.ttf", "GADUGI-GRAS");

        Font normal = FontFactory.getFont("GADUGI", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        Font gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        Font big_gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
        Font bold = gras;

        Document document = new Document(PageSize.A4.rotate(), 20f, 20f, 30f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(normal, user));
        document.open();

        // Header avec logo
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{0.5f, 3f, 2f});

// 1️⃣ Cellule avec logo
        Image logo = Image.getInstance(
                new ClassPathResource("images/logo-UCAD_.png").getInputStream().readAllBytes()
        );
        logo.scaleToFit(70, 70);
        PdfPCell imageCell = new PdfPCell(logo);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(imageCell);

// 2️⃣ Cellule avec texte
        Paragraph headerText = new Paragraph(
                "REPUBLIQUE DU SENEGAL\nUn Peuple – Un But – Une Foi\n" +
                        "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT",
                gras
        );
        headerText.setLeading(20f, 0);
        headerText.setAlignment(Element.ALIGN_LEFT);
        PdfPCell textCell = new PdfPCell(headerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(textCell);

        // Créer le petit tableau 3x3
        PdfPTable smallTable = new PdfPTable(3);

        // Définir la largeur des colonnes (ajuster si nécessaire)
        smallTable.setWidths(new float[] { 0.5f, 0.5f, 0.5f }); // Largeurs égales pour chaque colonne

        // Insérer le petit tableau dans une cellule (celle-ci sera sans bordure)
        PdfPCell tableCell = new PdfPCell(smallTable);
        tableCell.setBorder(Rectangle.NO_BORDER);
        tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // Centrage vertical
        tableCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Centrage horizontal

        // Ajouter la cellule du tableau dans le tableau principal (headerTable)
        headerTable.addCell(tableCell);

        // Ajouter le headerTable au document
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        Paragraph sessionInfo = new Paragraph(
                "CONCOURS GENERAL SENEGALAIS\nANNEE " + session, big_gras);
        sessionInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(sessionInfo);

        if (cdt != null) {
            Etablissement etab = etablissementRepository.findByCode(codeEtab);
            assert us != null;
            Paragraph etablissement = new Paragraph(
                    "\n~~PV DE RECEPTION DES DOSSIERS DE CANDIDATURE~~" +
                            "\nEtablissement : " + etab.getName() +
                            "\nRapport généré par : " + us.getFirstname() + " " + us.getLastname() + " / Clé : " + us.getId(), big_gras);
            etablissement.setAlignment(Element.ALIGN_CENTER);
            document.add(etablissement);
        }

        // Données de séries
        String[] headers = {
                "Prénom (s)", "Nom", "Date Nais.", "Lieu Nais.", "Sexe", "Série", "Etab.", "Décision", "Motif (s) de Rejet (s)", "Details"
        };

        String currentSerie = null;
        // marge minimale avant le footer (en points : 1 cm ≈ 28.3 points)
        final float MIN_BOTTOM_MARGIN = 60f; // environ 2 cm

        // 📌 1. Créer une seule fois le tableau
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);
        table.setSpacingAfter(10f);
        table.setWidths(new float[]{4.45f, 3.45f, 2.45f, 3f, 1.2f, 1.2f, 1.3f, 1.67f, 6.85f, 4.65f});

        // 📌 2. Ajouter les headers une seule fois
        for (String header : headers)
        {
            PdfPCell cell = new PdfPCell(new Phrase(header, bold));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new Color(253, 245, 230));
            table.addCell(cell);
        }

        // 📌 3. Ajouter toutes les lignes dans le même tableau
        for (ConcoursGeneral c : cdt)
        {

            String[] candidateData = this.mapCandidatToPdfData4_(c);

            for (String value : candidateData)
            {
                PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }
        }

        // 📌 4. Ajouter le tableau UNE SEULE FOIS
        document.add(table);

        document.close();
    }



    @Operation(summary="Service de génération des listes de contact en PDF")
    @GetMapping("/generate-liste-des-contacts")
    public void generateListedesContacts(HttpServletResponse response,
                                 @RequestParam String etablissementId,
                                 @RequestParam Long session,
                                 @RequestParam String user) throws IOException, DocumentException {

        Etablissement etb = etablissementRepository.findById(etablissementId).orElse(null);
        assert etb != null;
        // System.out.println("A ce niveau" + session + " " + etb.getCode());
        CompteDroitsInscription cdI = compteDroitInscriptionRepository.findByEtablissementNameAndSession(etb.getName(), session);
        // System.out.println("A ce niveau" + cdI.getSession() + " " + cdI.getEtablissement().getName());
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");

        // Enregistrement des polices
        FontFactory.register("fonts/gadugi-normal.ttf", "GADUGI");
        FontFactory.register("fonts/gadugi-gras.ttf", "GADUGI-GRAS");

        Font normal = FontFactory.getFont("GADUGI", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
        Font gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
        Font big_gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
        Font bold = gras;

        Document document = new Document(PageSize.A4.rotate(), 20f, 20f, 30f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(normal, user));
        document.open();

        // Header avec logo
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{0.5f, 3f, 2f});

// 1️⃣ Cellule avec logo
        Image logo = Image.getInstance(
                new ClassPathResource("images/logo-UCAD_.png").getInputStream().readAllBytes()
        );
        logo.scaleToFit(70, 70);
        PdfPCell imageCell = new PdfPCell(logo);
        imageCell.setBorder(Rectangle.NO_BORDER);
        imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(imageCell);

// 2️⃣ Cellule avec texte
        Paragraph headerText = new Paragraph(
                "REPUBLIQUE DU SENEGAL\nUn Peuple – Un But – Une Foi\n" +
                        "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT",
                gras
        );
        headerText.setLeading(20f, 0);
        headerText.setAlignment(Element.ALIGN_LEFT);
        PdfPCell textCell = new PdfPCell(headerText);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(textCell);

        // Créer le petit tableau 3x3
        PdfPTable smallTable = new PdfPTable(3);

        // Définir la largeur des colonnes (ajuster si nécessaire)
        smallTable.setWidths(new float[] { 0.5f, 0.5f, 0.5f }); // Largeurs égales pour chaque colonne

        // Insérer le petit tableau dans une cellule (celle-ci sera sans bordure)
        PdfPCell tableCell = new PdfPCell(smallTable);
        tableCell.setBorder(Rectangle.NO_BORDER);
        tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);  // Centrage vertical
        tableCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Centrage horizontal

        // Ajouter la cellule du tableau dans le tableau principal (headerTable)
        headerTable.addCell(tableCell);

        // Ajouter le headerTable au document
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        Paragraph sessionInfo = new Paragraph(
                "BACCALAUREAT DE L’ENSEIGNEMENT SECONDAIRE\nANNEE " + session + "\nSession NORMALE " + session, big_gras);
        sessionInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(sessionInfo);

        if (etb != null) {
            Paragraph etablissement = new Paragraph(
                    "\n~~LISTE DES CONTACTS~~\n" +
                            "Etablissement de provenance : " + etb.getCode() + " / " + etb.getName() + "\n" +
                            "Centre d’examen : " + etb.getCentreExamen().getName() + "\n" +
                            "(Liste valable uniquement pour la vérification par l’établissement)", big_gras);
            etablissement.setAlignment(Element.ALIGN_CENTER);
            document.add(etablissement);
        }

        List<Candidat> result = candidatService.getAllCandidatsForPdf(etablissementId, session, "lastname");

        result = result.stream()
                .sorted(Comparator.comparing(c -> c.getSerie().getCode()))
                .collect(Collectors.toList());

        // Données de séries
        String[] headers = {
                "N° de\nDos.", "Prénom (s)", "Nom", "Date Nais.", "Lieu Nais.", "Téléphone", "Adresse Email"
        };

        String currentSerie = null;
        PdfPTable table = null;
        // marge minimale avant le footer (en points : 1 cm ≈ 28.3 points)
        final float MIN_BOTTOM_MARGIN = 60f; // environ 2 cm

        int total = 0;
        int garcons = 0;
        int filles = 0;

        for (int i = 0; i < result.size(); i++) {
            Candidat c = result.get(i);
            String candidateSerie = c.getSerie() != null ? c.getSerie().getName() : "";

            boolean isNewSerie = currentSerie == null || !candidateSerie.equals(currentSerie);

            if (isNewSerie)
            {
                if (table != null) {
                    // 🔹 Vérifie si assez d'espace pour le tableau + marge
                    float spaceLeft = document.getPageSize().getHeight()
                            - document.topMargin()
                            - writer.getVerticalPosition(true);

                    System.out.println("document.topMargin() " + document.topMargin());
                    System.out.println("document.getPageSize().getHeight() " + document.getPageSize().getHeight());
                    System.out.println("writer.getVerticalPosition(true) " + writer.getVerticalPosition(true));
                    System.out.println("spaceLeft " + spaceLeft);
                    System.out.println("MIN_BOTTOM_MARGIN " + MIN_BOTTOM_MARGIN);

                    if (spaceLeft < MIN_BOTTOM_MARGIN) {
                        document.newPage();
                    }

                    document.add(table);
                    Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                    effectif.setSpacingBefore(3f);
                    document.add(effectif);
                    //document.newPage();
                }

                currentSerie = candidateSerie;
                total = garcons = filles = 0;

                Paragraph serieTitle = new Paragraph(currentSerie, big_gras);
                serieTitle.setSpacingBefore(5f);
                serieTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(serieTitle);

                table = new PdfPTable(7);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(10f);
                table.setWidths(new float[]{
                        1.2f, 4.45f, 4f, 3f, 3f, 4f, 7f
                });

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, bold));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBackgroundColor(new Color(253, 245, 230));
                    table.addCell(cell);
                }
            }

            total++;
            if ("M".equalsIgnoreCase(c.getGender().name())) {
                garcons++;
            } else if ("F".equalsIgnoreCase(c.getGender().name())) {
                filles++;
            }

            String[] candidateData = this.mapCandidatToPdfData3(c);
            for (String value : candidateData) {
                PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }

            if (i == result.size() - 1 && table != null)
            {
                document.add(table);
                Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                effectif.setSpacingBefore(5f);
                document.add(effectif);
            }
        }

        document.close();
    }


    @Operation(summary="Service de génération d'une liste de candidats avec leurs sujets en PDF")
    @GetMapping("/generate-cdts-with-sujets")
    public void generateSujetWithCdtPdf(HttpServletResponse response,
                              @RequestParam String etablissementId,
                              @RequestParam Long session,
                              @RequestParam String user) throws IOException
    {

        try
        {
            Etablissement etb = etablissementRepository.findById(etablissementId).orElse(null);
            assert etb != null;
            //System.out.println("A ce niveau" + session + " " + etb.getCode());
            CompteDroitsInscription cdI = compteDroitInscriptionRepository.findByEtablissementNameAndSession(etb.getName(), session);
            //System.out.println("A ce niveau" + cdI.getSession() + " " + cdI.getEtablissement().getName());
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=bac_liste_candidats.pdf");

            // Enregistrement des polices
            FontFactory.register("fonts/gadugi-normal.ttf", "GADUGI");
            FontFactory.register("fonts/gadugi-gras.ttf", "GADUGI-GRAS");

            Font normal = FontFactory.getFont("GADUGI", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 9);
            Font gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10);
            Font big_gras = FontFactory.getFont("GADUGI-GRAS", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
            Font bold = gras;

            Document document = new Document(PageSize.A4.rotate(), 20f, 20f, 30f, 40f);
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            writer.setPageEvent(new FooterHandler(normal, user));
            writer.setPageEvent(new WatermarkHandler2("VERIFICATION"));
            document.open();

            // Header avec logo
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{0.5f, 5f});

            //Cellule avec logo
            Image logo = Image.getInstance(
                    new ClassPathResource("images/logo-UCAD_.png").getInputStream().readAllBytes()
            );
            logo.scaleToFit(70, 70);
            PdfPCell imageCell = new PdfPCell(logo);
            imageCell.setBorder(Rectangle.NO_BORDER);
            imageCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(imageCell);

            // Cellule avec texte
            Paragraph headerText = new Paragraph(
                    "REPUBLIQUE DU SENEGAL\nUn Peuple – Un But – Une Foi\n" +
                            "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT",
                    gras
            );
            headerText.setLeading(20f, 0);
            headerText.setAlignment(Element.ALIGN_LEFT);
            PdfPCell textCell = new PdfPCell(headerText);
            textCell.setBorder(Rectangle.NO_BORDER);
            textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(textCell);

            // Ajouter le headerTable au document
            document.add(headerTable);
            document.add(Chunk.NEWLINE);

            Paragraph sessionInfo = new Paragraph(
                    "BACCALAUREAT DE L’ENSEIGNEMENT SECONDAIRE\nANNEE " + session + "\nSession NORMALE " + session, big_gras);
            sessionInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(sessionInfo);

            if (etb != null) {
                Paragraph etablissement = new Paragraph(
                        "\n~~LISTE DES PROJETS DE SOUTENANCE AVEC LES CANDIDATS~~\n" +
                                "Etablissement de provenance : " + etb.getCode() + " / " + etb.getName() + "\n" +
                                "Centre d’examen : " + etb.getCentreExamen().getName() + "\n", big_gras);
                etablissement.setAlignment(Element.ALIGN_CENTER);
                document.add(etablissement);
            }

            List<Candidat> result = candidatService.getAllCandidatsForPdfSujet(etablissementId, session);

            // Données de séries
            String[] headers = {
                    "Code\nC.E.C", "Nom du Centre\nd’Etat Civil (C.E.C)", "Ann.\nDécl.", "N° Acte\nNais.",
                    "Prénom (s)", "Nom", "Sexe", "Date Nais.", "Lieu Nais.", "Nationalité", "Matière (s)\nOptionn. (s) / Spécialité", "EPS", "Matièr. (s)\nFacult. (s)", "Nb.\nfois", "Série"
            };

            String currentSubject = null;
            PdfPTable table = null;
            // marge minimale avant le footer (en points : 1 cm ≈ 28.3 points)
            final float MIN_BOTTOM_MARGIN = 60f; // environ 2 cm

            int total = 0;
            int garcons = 0;
            int filles = 0;

            for (int i = 0; i < result.size(); i++) {
                Candidat c = result.get(i);
                String candidateSubject = c.getSubject() != null ? c.getSubject() : "";

                boolean isNewSubject = !candidateSubject.equals(currentSubject);

                if (isNewSubject)
                {
                    if (table != null) {
                        // 🔹 Vérifie si assez d'espace pour le tableau + marge
                        float spaceLeft = document.getPageSize().getHeight()
                                - document.topMargin()
                                - writer.getVerticalPosition(true);

                        System.out.println("document.topMargin() " + document.topMargin());
                        System.out.println("document.getPageSize().getHeight() " + document.getPageSize().getHeight());
                        System.out.println("writer.getVerticalPosition(true) " + writer.getVerticalPosition(true));
                        System.out.println("spaceLeft " + spaceLeft);
                        System.out.println("MIN_BOTTOM_MARGIN " + MIN_BOTTOM_MARGIN);

                        if (spaceLeft < MIN_BOTTOM_MARGIN) {
                            document.newPage();
                        }

                        document.add(table);
                        Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                        effectif.setSpacingBefore(3f);
                        document.add(effectif);
                        //document.newPage();
                    }

                    currentSubject = candidateSubject;
                    total = garcons = filles = 0;

                    Paragraph serieTitle = new Paragraph("Sujet : " + currentSubject, big_gras);
                    serieTitle.setSpacingBefore(5f);
                    serieTitle.setAlignment(Element.ALIGN_CENTER);
                    document.add(serieTitle);

                    table = new PdfPTable(15);
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(5f);
                    table.setSpacingAfter(10f);
                    table.setWidths(new float[]{
                            1.5f, 4f, 1.5f, 2f, 4.45f, 4f, 1.4f,
                            3f, 3f, 3f, 4f, 1.5f, 2.8f, 1.2f, 1.5f
                    });

                    for (String header : headers) {
                        PdfPCell cell = new PdfPCell(new Phrase(header, bold));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setBackgroundColor(new Color(253, 245, 230));
                        table.addCell(cell);
                    }
                }

                total++;
                if ("M".equalsIgnoreCase(c.getGender().name())) {
                    garcons++;
                } else if ("F".equalsIgnoreCase(c.getGender().name())) {
                    filles++;
                }

                String[] candidateData = this.mapCandidatToPdfDataSujet(c);
                for (String value : candidateData) {
                    PdfPCell cell = new PdfPCell(new Phrase(value, normal));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);
                }

                if (i == result.size() - 1 && table != null)
                {
                    document.add(table);
                    Paragraph effectif = new Paragraph("Effectif(s) : " + total + " | Garçon(s) : " + garcons + " | Fille(s) : " + filles, normal);
                    effectif.setSpacingBefore(5f);
                    document.add(effectif);
                }
            }

            document.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }



    public static String formatMatiereAbregee(String matiere) {

        if (matiere == null || matiere.trim().isEmpty()) return "";

        // Mots à ignorer
        Set<String> motsVides = Set.of("de", "la", "le", "et", "du", "des", "au", "aux", "d", "l", "à", "en", "sur", "avec", "sans");

        // Supprimer accents et mettre en minuscules
        String cleaned = removeAccents(matiere.toLowerCase());

        // Séparer les mots en enlevant les caractères spéciaux
        String[] mots = cleaned.replaceAll("[^a-zA-Z ]", " ").trim().split("\\s+");

        if (mots.length == 1) {
            // Un seul mot : prendre les 3 premières lettres
            return mots[0].substring(0, Math.min(3, mots[0].length())).toUpperCase();
        }

        // Plusieurs mots : prendre première lettre de chaque mot significatif
        StringBuilder sb = new StringBuilder();
        for (String mot : mots) {
            if (!motsVides.contains(mot) && !mot.isBlank()) {
                sb.append(mot.charAt(0));
            }
        }

        return sb.toString().toUpperCase();
    }

    // Méthode utilitaire pour enlever les accents
    public static String removeAccents(String input) {
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    public String[] mapCandidatToPdfData(Candidat c) {
        String code_cec = String.valueOf(c.getCentreEtatCivil().getCode());
        String cec = c.getCentreEtatCivil().getName();
        String an_dec = String.valueOf(c.getYear_registry_num());
        String r_num = String.valueOf(c.getRegistry_num());
        String dos_num = String.valueOf(c.getDosNumber());
        String firstname = c.getFirstname();
        String lastname = c.getLastname();
        String sexe = c.getGender().name();
        String date_naiss = c.getDate_birth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(c.getDate_birth()))
                : "";
        String place_birth = String.valueOf(c.getPlace_birth());
        String nat = String.valueOf(c.getNationality().getName());
        //String serie = String.valueOf(c.getSerie().getCode());

        // Matières optionnelles
        String mat1 = formatMatiereAbregee(c.getMatiere1() != null ? c.getMatiere1().getName() : "");
        String mat2 = formatMatiereAbregee(c.getMatiere2() != null ? c.getMatiere2().getName() : "");
        String mat3 = formatMatiereAbregee(c.getMatiere3() != null ? c.getMatiere3().getName() : "");

        String matieres = String.join(" - ",
                Stream.of(mat1, mat2, mat3).filter(s -> s != null && !s.isEmpty()).toArray(String[]::new)
        );

        String eps = c.getEps() != null ? c.getEps() : "";

        String epF1 = formatMatiereAbregee(c.getEprFacListA() != ListeA.Aucun ? c.getEprFacListA().name() : "");
        String epF2 = formatMatiereAbregee(c.getEprFacListB() != null ? c.getEprFacListB().getName() : "");

        String epr_fac = String.join(" - ",
                Stream.of(epF1, epF2).filter(s -> s != null && !s.isEmpty()).toArray(String[]::new)
        );

        String nb_fois = String.valueOf(c.getBac_do_count());

        return new String[]{
                code_cec,
                cec,
                an_dec,
                r_num,
                dos_num,
                firstname,
                lastname,
                sexe,
                date_naiss,
                place_birth,
                nat,
                matieres,
                eps,
                epr_fac,
                nb_fois,
                ""

        };
    }

    public String[] mapCandidatToPdfDataOL(Candidat c) {
        String code_cec = String.valueOf(c.getCentreEtatCivil().getCode());
        String cec = c.getCentreEtatCivil().getName();
        String an_dec = String.valueOf(c.getYear_registry_num());
        String r_num = String.valueOf(c.getRegistry_num());
        String firstname = c.getFirstname();
        String lastname = c.getLastname();
        String sexe = c.getGender().name();
        String date_naiss = c.getDate_birth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(c.getDate_birth()))
                : "";
        String place_birth = String.valueOf(c.getPlace_birth());
        String nat = String.valueOf(c.getNationality().getName());
        //String serie = String.valueOf(c.getSerie().getCode());

        // Matières optionnelles
        String mat1 = formatMatiereAbregee(c.getMatiere1() != null ? c.getMatiere1().getName() : "");
        String mat2 = formatMatiereAbregee(c.getMatiere2() != null ? c.getMatiere2().getName() : "");
        String mat3 = formatMatiereAbregee(c.getMatiere3() != null ? c.getMatiere3().getName() : "");

        String matieres = String.join(" - ",
                Stream.of(mat1, mat2, mat3).filter(s -> !s.isEmpty()).toArray(String[]::new)
        );

        String eps = c.getEps() != null ? c.getEps() : "";

        String epF1 = formatMatiereAbregee(c.getEprFacListA() != ListeA.Aucun ? c.getEprFacListA().name() : "");
        String epF2 = formatMatiereAbregee(c.getEprFacListB() != null ? c.getEprFacListB().getName() : "");

        String epr_fac = String.join(" - ",
                Stream.of(epF1, epF2).filter(s -> !s.isEmpty()).toArray(String[]::new)
        );

        String nb_fois = String.valueOf(c.getBac_do_count());

        return new String[]{
                code_cec,
                cec,
                an_dec,
                r_num,
                firstname,
                lastname,
                sexe,
                date_naiss,
                place_birth,
                nat,
                matieres,
                eps,
                epr_fac,
                nb_fois,
                "Accepté"
        };
    }

    public String[] mapCandidatToPdfDataSujet(Candidat c) {
        String code_cec = String.valueOf(c.getCentreEtatCivil().getCode());
        String cec = c.getCentreEtatCivil().getName();
        String an_dec = String.valueOf(c.getYear_registry_num());
        String r_num = String.valueOf(c.getRegistry_num());
        String firstname = c.getFirstname();
        String lastname = c.getLastname();
        String sexe = c.getGender().name();
        String date_naiss = c.getDate_birth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(c.getDate_birth()))
                : "";
        String place_birth = String.valueOf(c.getPlace_birth());
        String nat = String.valueOf(c.getNationality().getName());
        //String serie = String.valueOf(c.getSerie().getCode());

        // Matières optionnelles
        String mat1 = formatMatiereAbregee(c.getMatiere1() != null ? c.getMatiere1().getName() : "");
        String mat2 = formatMatiereAbregee(c.getMatiere2() != null ? c.getMatiere2().getName() : "");
        String mat3 = formatMatiereAbregee(c.getMatiere3() != null ? c.getMatiere3().getName() : "");

        String matieres = String.join(" - ",
                Stream.of(mat1, mat2, mat3).filter(s -> s != null && !s.isEmpty()).toArray(String[]::new)
        );

        String eps = c.getEps() != null ? c.getEps() : "";

        String epF1 = formatMatiereAbregee(c.getEprFacListA() != ListeA.Aucun ? c.getEprFacListA().name() : "");
        String epF2 = formatMatiereAbregee(c.getEprFacListB() != null ? c.getEprFacListB().getName() : "");

        String epr_fac = String.join(" - ",
                Stream.of(epF1, epF2).filter(s -> s != null && !s.isEmpty()).toArray(String[]::new)
        );

        String nb_fois = String.valueOf(c.getBac_do_count());

        String serie = String.valueOf(c.getSerie().getCode());

        return new String[]{
                code_cec,
                cec,
                an_dec,
                r_num,
                firstname,
                lastname,
                sexe,
                date_naiss,
                place_birth,
                nat,
                matieres,
                eps,
                epr_fac,
                nb_fois,
                serie

        };
    }

    public String[] mapCandidatToPdfDataCGS(ConcoursGeneral c, int index) {
        String merite = String.valueOf(index + 1);;
        String firstname = c.getFirstname();
        String lastname = c.getLastname();
        String sexe = c.getGender().name();
        String date_naiss = c.getDate_birth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(c.getDate_birth()))
                : "";
        String serie = c.getSerie().getCode();
        String classe_0 = c.getClasse_0();
        String classe_1 = c.getClasse_1();
        String note_0 = String.valueOf(c.getNote_student_disc());
        String note_1 = String.valueOf(c.getNote_classe_disc());

        String firstname_prof = String.valueOf(c.getFirstname_prof());
        String lastname_prof = String.valueOf(c.getLastname_prof());



        return new String[]{
                merite,
                firstname,
                lastname,
                sexe,
                date_naiss,
                serie,
                classe_0,
                classe_1,
                note_0,
                note_1,
                firstname_prof,
                lastname_prof,

        };
    }

    public String[] mapCandidatToPdfData2(Candidat c) {
        String dos_num = String.valueOf(c.getDosNumber());
        String firstname = c.getFirstname();
        String lastname = c.getLastname();
        String date_naiss = c.getDate_birth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(c.getDate_birth()))
                : "";
        String place_birth = String.valueOf(c.getPlace_birth());

        // récupérer la liste des rejets du candidat
        List<Rejet> rejetsList = c.getRejets(); // peut être List<Rejet> ou null

        // inclure l'observation également
                String rejetsAvecObservation = "";
                if (rejetsList != null && !rejetsList.isEmpty()) {
                    rejetsAvecObservation = rejetsList.stream()
                            .map(r -> "• " + r.getName() + " (" + r.getObservation() + ")")
                            .collect(Collectors.joining("\n"));
                }

        String date_ops = c.getDateOperation() != null
                ? c.getDateOperation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                : "";

        Optional<User> ops = userRepository.findByLogin(c.getOperator());

        String operation = "Opération effectuée le : " + date_ops + " par : " + ops.get().getFirstname() + " " + ops.get().getLastname();
        // construire la ligne pour ton PDF ou tableau
                return new String[]{
                        dos_num,
                        firstname,
                        lastname,
                        date_naiss,
                        place_birth,
                        rejetsAvecObservation,
                        operation// ou rejetsAvecObservation
                };

        }

    public String[] mapCandidatToPdfData2CGS(ConcoursGeneral c) {
        String dos_num = c.getLevel();
        String firstname = c.getFirstname();
        String lastname = c.getLastname();
        String date_naiss = c.getDate_birth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(c.getDate_birth()))
                : "";
        String place_birth = String.valueOf(c.getPlace_birth());

        // récupérer la liste des rejets du candidat
        List<Rejet> rejetsList = c.getRejets(); // peut être List<Rejet> ou null

        // inclure l'observation également
        String rejetsAvecObservation = "";
        if (rejetsList != null && !rejetsList.isEmpty()) {
            rejetsAvecObservation = rejetsList.stream()
                    .map(r -> "• " + r.getName() + " (" + r.getObservation() + ")")
                    .collect(Collectors.joining("\n"));
        }

        String date_ops = c.getDateOperation() != null
                ? c.getDateOperation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                : "";

        Optional<User> ops = userRepository.findByLogin(c.getOperator());

        String operation = "Opération effectuée le : " + date_ops + " par : " + ops.get().getFirstname() + " " + ops.get().getLastname();
        // construire la ligne pour ton PDF ou tableau
        return new String[]{
                dos_num,
                firstname,
                lastname,
                date_naiss,
                place_birth,
                rejetsAvecObservation,
                operation// ou rejetsAvecObservation
        };

    }

    public String[] mapCandidatToPdfData4(Candidat c) {
        String dos_num = String.valueOf(c.getDosNumber());
        String firstname = c.getFirstname();
        String lastname = c.getLastname();
        String date_naiss = c.getDate_birth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(c.getDate_birth()))
                : "";
        String place_birth = String.valueOf(c.getPlace_birth());
        String gender = c.getGender().name();
        String serie = c.getSerie().getCode();
        String etab = String.valueOf(c.getEtablissement().getCode());

        // récupérer la liste des rejets du candidat
        List<Rejet> rejetsList = c.getRejets(); // peut être List<Rejet> ou null

        // inclure l'observation également
        String rejetsAvecObservation = "NEANT";
        if (rejetsList != null && !rejetsList.isEmpty()) {
            rejetsAvecObservation = rejetsList.stream()
                    .map(r -> "• " + r.getName() + " (" + r.getObservation() + ")")
                    .collect(Collectors.joining("\n"));
        }

        String date_ops = c.getDateOperation() != null
                ? c.getDateOperation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                : "";

        String decision = switch (c.getDecision()) {
            case 0 -> "En Attente";
            case 1 -> "Validé";
            case 2 -> "Rejeté";
            default -> "";
        };

        String operation = "Opération effectuée le : " + date_ops;
        // construire la ligne pour ton PDF ou tableau
        return new String[]{
                dos_num,
                firstname,
                lastname,
                date_naiss,
                place_birth,
                gender,
                serie,
                etab,
                decision,
                rejetsAvecObservation,
                operation// ou rejetsAvecObservation
        };

    }

    public String[] mapCandidatToPdfData4_(ConcoursGeneral c) {
        // String dos_num = c.getId();
        String firstname = c.getFirstname();
        String lastname = c.getLastname();
        String date_naiss = c.getDate_birth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(c.getDate_birth()))
                : "";
        String place_birth = String.valueOf(c.getPlace_birth());
        String gender = c.getGender().name();
        String serie = c.getSerie().getCode();
        String etab = String.valueOf(c.getEtablissement().getCode());

        // récupérer la liste des rejets du candidat
        List<Rejet> rejetsList = c.getRejets(); // peut être List<Rejet> ou null

        // inclure l'observation également
        String rejetsAvecObservation = "NEANT";
        if (rejetsList != null && !rejetsList.isEmpty()) {
            rejetsAvecObservation = rejetsList.stream()
                    .map(r -> "• " + r.getName() + " (" + r.getObservation() + ")")
                    .collect(Collectors.joining("\n"));
        }

        String date_ops = c.getDateOperation() != null
                ? c.getDateOperation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                : "";

        String decision = switch (c.getDecision()) {
            case 0 -> "En Attente";
            case 1 -> "Validé";
            case 2 -> "Rejeté";
            default -> "";
        };

        String operation = "Opération effectuée le : " + date_ops + "\npar " + c.getOperator();
        // construire la ligne pour ton PDF ou tableau
        return new String[]{
                //dos_num,
                firstname,
                lastname,
                date_naiss,
                place_birth,
                gender,
                serie,
                etab,
                decision,
                rejetsAvecObservation,
                operation// ou rejetsAvecObservation
        };

    }

    public String[] mapCandidatToPdfData3(Candidat c) {
        String dos_num = String.valueOf(c.getDosNumber());
        String firstname = c.getFirstname();
        String lastname = c.getLastname();
        String date_naiss = c.getDate_birth() != null
                ? new SimpleDateFormat("dd/MM/yyyy").format(java.sql.Date.valueOf(c.getDate_birth()))
                : "";
        String place_birth = String.valueOf(c.getPlace_birth());

        String phone = c.getPhone1();

        String email = c.getEmail();

       // construire la ligne pour ton PDF ou tableau
        return new String[]{
                dos_num,
                firstname,
                lastname,
                date_naiss,
                place_birth,
                phone,
                email
        };

    }

}
