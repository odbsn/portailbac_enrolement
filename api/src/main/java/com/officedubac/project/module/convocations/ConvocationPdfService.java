package com.officedubac.project.module.convocations;

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.officedubac.project.module.candidatFinis.CandidatFinis;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisResponse;
import com.officedubac.project.module.epreuve.EpreuveReactiveService;
import com.officedubac.project.module.epreuve.dto.EpreuveResponse;
import com.officedubac.project.module.jour.Jour;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Service
@RequiredArgsConstructor
public class ConvocationPdfService {
    private final EpreuveReactiveService epreuveService;

    // ========== AJOUT CACHE ==========
    private Jour jourEPSCache;
    private final Map<String, List<EpreuveResponse>> epreuvesCache = new ConcurrentHashMap<>();

    private Cell createLabel(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(new DeviceGray(0.92f))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.5f))
                .setBorderRadius(new BorderRadius(8))
                .setPadding(2)
                .setMarginBottom(0);
    }
    private Paragraph createValue(String text, PdfFont font) {
        return new Paragraph(text)
                .setFont(font)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(0)
                .setMarginBottom(1);
    }
    private String value(String v) {
        return v == null ? "-" : v;
    }
    public byte[] generateConvocation(CandidatFinis c, List<EpreuveResponse> epreuves, Jour jourEPS) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(8, 15, 8, 15);

        PdfFont normalFont = PdfFontFactory.createFont("Helvetica");
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont lucidaFont = null;
        try {
            java.io.File fontFile = new java.io.File("src/main/resources/fonts/LucidaUnicodeCalligraphy.ttf");
            if (fontFile.exists()) {
                lucidaFont = PdfFontFactory.createFont(fontFile.getAbsolutePath());
                System.out.println("✅ Police chargée depuis: " + fontFile.getAbsolutePath());
            } else {
                System.err.println("Fichier non trouvé: " + fontFile.getAbsolutePath());
                throw new IOException("Fichier non trouvé");
            }
        } catch (Exception e) {
            System.err.println("Police non trouvée, utilisation de l'italique");
            lucidaFont = normalFont;
        }
        document.setFont(normalFont);

        // ================= HEADER =================
        Table header = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setMarginBottom(2);

        header.addCell(createNoBorderCell(
                "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT", 9, false));

        header.addCell(createNoBorderCell(
                "REPUBLIQUE DU SENEGAL\nUn Peuple - Un But - Une Foi", 9, false)
                .setTextAlignment(TextAlignment.RIGHT));

        document.add(header);

        // ================= HEADER GRID =================
        Table headerGrid = new Table(UnitValue.createPercentArray(new float[]{24, 36, 20}))
                .useAllAvailableWidth()
                .setMarginTop(0)
                .setMarginBottom(0);

        Cell cellLeft = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

// Paragraphe principal
        Paragraph convocation = new Paragraph("CONVOCATION")
                .setFont(boldFont)
                .setFontSize(12)
                .setUnderline()
                .setItalic()
                .setTextAlignment(TextAlignment.LEFT);
        cellLeft.add(convocation);

// Petit texte mention
        Paragraph mentionImpression = new Paragraph("À imprimer impérativement")
                .setFont(normalFont)
                .setFontSize(9)
                .setFontColor(ColorConstants.RED)
                .setTextAlignment(TextAlignment.LEFT);
        cellLeft.add(mentionImpression);

        headerGrid.addCell(cellLeft);
        Cell cellCenter = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        cellCenter.add(new Paragraph("BACCALAUREAT DE L'ENSEIGNEMENT SECONDAIRE")
                .setFont(boldFont)
                .setFontSize(10));

        cellCenter.add(new Paragraph("SESSION NORMALE"+" "+(LocalDate.now().getYear()))
                .setFont(boldFont)
                .setFontSize(10));

        String serieText = "Série : " + (c.getSerie() != null ? c.getSerie() : "-");
        cellCenter.add(
                new Paragraph(serieText)
                        .setFont(boldFont)
                        .setFontSize(9)
                        .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                        .setBorderRadius(new BorderRadius(6))
                        .setPadding(1)
                        .setMarginTop(1)
        );

        headerGrid.addCell(cellCenter);
        Cell cellRight = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0)
                .setVerticalAlignment(VerticalAlignment.TOP);

// Table pour aligner horizontalement
        Table qrTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(160);

// Premier QR Code avec titre
        Paragraph title1 = new Paragraph("Résultats Bac")
                .setFont(boldFont)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
//                .setMarginBottom(2)
                ;

        String url1 = "https://portailbac.ucad.sn/search";
        BarcodeQRCode qrCode1 = new BarcodeQRCode(url1);
        Image qrImage1 = new Image(qrCode1.createFormXObject(ColorConstants.BLACK, pdf))
                .scaleToFit(70, 70);

        Div qrDiv1 = new Div()
                .setTextAlignment(TextAlignment.CENTER);
        qrDiv1.add(qrImage1);
        qrDiv1.add(title1);

        Cell qrCell1 = new Cell().add(qrDiv1).setBorder(Border.NO_BORDER);
        qrTable.addCell(qrCell1);

// Deuxième QR Code avec titre
        String reglementText = "RÈGLEMENT INTÉRIEUR DU CENTRE D'EXAMEN\n\n" +
                "1. Fraude et tentative de fraude\n" +
                "Toute fraude ou tentative de fraude entraînera des sanctions disciplinaires sévères, notamment :\n" +
                "- L'exclusion immédiate de la session d'examen,\n" +
                "- La traduction devant la Commission de Discipline de l'Université,\n" +
                "- Et, le cas échéant, des poursuites pénales.\n\n" +
                "2. Comportements délictueux\n" +
                "Tout comportement délictueux ou perturbateur, dans l'enceinte ou aux abords du centre d'examen, sera pareillement sanctionné. Tout candidat perturbant le bon déroulement de l'épreuve sera immédiatement exclu.\n\n" +
                "3. Pièces obligatoires\n" +
                "Le candidat doit être en mesure de présenter, à toute réquisition : sa pièce d'identité ou carte d'identité scolaire, sa convocation à l'examen.\n\n" +
                "4. Placement et durée minimale de présence\n" +
                "Le candidat doit obligatoirement s'installer à la place qui lui est assignée, et y rester pendant toute la durée de l'épreuve. Il n'est autorisé à quitter la salle qu'après au moins une (01) heure à compter du début de l'épreuve.\n\n" +
                "5. Cas de malaise ou incident médical\n" +
                "En cas de malaise grave, le candidat peut demander à sortir, à condition :\n" +
                "- D'avoir remis sa copie et ses brouillons,\n" +
                "- D'être accompagné d'un surveillant.\n" +
                "L'incident sera consigné auprès de la police du candidat, laquelle ne pourra lui être restituée qu'après décision du Président du jury.\n\n" +
                "6. Présentation de la copie\n" +
                "Le candidat doit commencer sa rédaction sur la première page de la feuille. Il est tenu d'utiliser uniquement le matériel fourni : feuilles de copie officielles, intercalaires, brouillons mis à disposition par le centre.\n\n" +
                "7. Objets interdits\n" +
                "Il est formellement interdit de conserver, au sol ou à portée de main, tout manuscrit, imprimé, livre, ouvrage ou document, ainsi que tout objet communicant connecté. Conformément à la circulaire n° 0032 du 07 janvier 2012, l'usage et la possession de téléphones portables, ainsi que de tout autre terminal de communication électronique, sont strictement interdits dans l'enceinte du centre d'examen.\n\n" +
                "8. Matériel autorisé\n" +
                "L'usage du dictionnaire n'est autorisé que pour les épreuves de Grec, Latin et Arabe classique. Pour les épreuves de Mathématiques et de Sciences Physiques, l'utilisation de règles, cercles à calcul et tables numériques (logarithmes, statistiques, financières) est permise, à condition que ces instruments ne comportent aucune annotation ou indication susceptible d'aider le candidat.\n\n" +
                "9. Communication interdite\n" +
                "Toute forme de communication ou tentative de communication, entre candidats ou avec toute personne extérieure à la salle, est strictement interdite. Les complices seront sanctionnés au même titre que les fraudeurs.\n\n" +
                "10. Remise des copies et relevés de notes\n" +
                "À la fin de l'épreuve, chaque candidat doit remettre une copie, même blanche avec en-tête dûment rempli, avant de signer la liste de présence. À l'issue du premier groupe, les candidats sont invités à se rendre au secrétariat du jury pour retirer leurs relevés de notes.\n\n" +
                "a) Les candidats admissibles, autorisés à subir les épreuves du second groupe au vu des notes obtenues, font connaître, dans la demi-journée qui suit la proclamation des résultats, les trois (03) disciplines sur lesquelles ils désirent composer au second groupe ;\n" +
                "b) À la fin de l'examen, les relevés de notes non retirés resteront disponibles auprès du Chef de centre.";
        Paragraph title2 = new Paragraph("Règlement Intérieur")
                .setFont(boldFont)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
//                .setMarginBottom(2)
                ;

        String url2 = "https://portailbac.ucad.sn/reglement";
        BarcodeQRCode qrCode2 = new BarcodeQRCode(url2);
        Image qrImage2 = new Image(qrCode2.createFormXObject(ColorConstants.BLUE, pdf))
                .scaleToFit(70, 70);

        Div qrDiv2 = new Div()
                .setTextAlignment(TextAlignment.CENTER);

        qrDiv2.add(qrImage2);
        qrDiv2.add(title2);

        Cell qrCell2 = new Cell().add(qrDiv2).setBorder(Border.NO_BORDER);
        qrTable.addCell(qrCell2);

        cellRight.add(qrTable);
        headerGrid.addCell(cellRight);
        document.add(headerGrid);

        // ================= GRID 3 COLONNES =================
        Table grid = new Table(UnitValue.createPercentArray(new float[]{30, 45, 25}))
                .useAllAvailableWidth()
                .setMarginBottom(0)
                .setMarginTop(0);

        // ===== LEFT COLUMN =====
        Cell leftColumn = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingRight(3)
                .setMarginRight(5);

        leftColumn.add(createLabel("Prénom(s)", normalFont));
        leftColumn.add(createValue(value(c.getPrenoms()), boldFont));

        leftColumn.add(createLabel("Nom", normalFont));
        leftColumn.add(createValue(value(c.getNom()), boldFont));

        leftColumn.add(createLabel("Date de Naissance", normalFont));
        leftColumn.add(createValue(formatDate(c.getDateNaissance()), boldFont));

        leftColumn.add(createLabel("Lieu de Naissance", normalFont));
        leftColumn.add(createValue(value(c.getLieuNaissance()), boldFont));

        String etabName = c.getEtablissement() != null ? c.getEtablissement().getName() : "-";
        leftColumn.add(createLabel("Etablissement fréquenté", normalFont));
        leftColumn.add(createValue(etabName, boldFont));

        leftColumn.add(createLabel("Candidat", normalFont));
        leftColumn.add(createValue(value(c.getTypeCandidat()), boldFont));

        Table sexeNationaliteTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setMarginTop(0);

        Cell sexeCell = new Cell().setBorder(Border.NO_BORDER);
        sexeCell.add(createLabel("Sexe", normalFont));
        sexeCell.add(createValue(value(c.getSexe()), boldFont));

        Cell natCell = new Cell().setBorder(Border.NO_BORDER);
        natCell.add(createLabel("Nationalité", normalFont));
        natCell.add(createValue(value(c.getNationalite()), boldFont));

        sexeNationaliteTable.addCell(sexeCell);
        sexeNationaliteTable.addCell(natCell);
        leftColumn.add(sexeNationaliteTable);
        grid.addCell(leftColumn);

        // ===== CENTER COLUMN =====
        Cell centerColumn = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingRight(5)
                .setMarginRight(5);

        Table miniTable = new Table(UnitValue.createPercentArray(new float[]{20, 20, 35, 10, 15}))
                .useAllAvailableWidth();

        String[] miniHeaders = {"N° Jury", "N° table", "Centre d'écrit", "N° Bât.", "N° Salle"};
        for (String h : miniHeaders) {
            miniTable.addCell(createHeaderCell(h, 8, boldFont));
        }

        String centreEcrit = c.getCentreEcritParticulier() != null
                ? c.getCentreEcritParticulier()
                : (c.getCentreEcrit() != null ? c.getCentreEcrit().getName() : "-");

        miniTable.addCell(createDataCell(c.getJury() != null ? c.getJury() : "-", 8));
        miniTable.addCell(createDataCell(c.getNumeroTable() != null ? c.getNumeroTable() : "-", 8));
        miniTable.addCell(createDataCell(centreEcrit, 8));
        miniTable.addCell(createDataCell("-", 8));
        miniTable.addCell(createDataCell("-", 8));

        centerColumn.add(miniTable);
        centerColumn.add(new Paragraph("\n").setMarginTop(0).setMarginBottom(0));

        Table optionnellesEpsRow = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .useAllAvailableWidth()
                .setMarginTop(0)
                .setMarginBottom(0);

        Cell optionnellesCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0);

        boolean hasOptionnelles = hasMatieresOptionnelles(c);

        if (hasOptionnelles) {
            Paragraph optLabel = new Paragraph("Matière(s) Optionnelle(s):")
                    .setUnderline()
                    .setFontSize(10)
                    .setFont(boldFont);
            optionnellesCell.add(optLabel);

            for (String opt : getMatieresOptionnelles(c)) {
                optionnellesCell.add(new Paragraph(opt).setFontSize(9).setMarginLeft(10));
            }
        } else {
            Paragraph facLabel = new Paragraph("Matière(s) facultative(s):")
                    .setUnderline()
                    .setFontSize(10)
                    .setFont(boldFont);
            optionnellesCell.add(facLabel);

            List<String> facultatives = getMatieresFacultatives(c);
            if (facultatives.isEmpty()) {
                optionnellesCell.add(new Paragraph("Aucune matière facultative choisie").setFontSize(9).setMarginLeft(10));
            } else {
                for (String fac : facultatives) {
                    optionnellesCell.add(new Paragraph(fac).setFontSize(9).setMarginLeft(10));
                }
            }
        }

        Cell epsCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0)
                .setTextAlignment(TextAlignment.RIGHT);

        String epsValue = c.getEps() != null ? c.getEps() : "-";
        if ("A".equals(c.getEps())) epsValue = "Apte";
        if ("I".equals(c.getEps())) epsValue = "Inapte";

        Paragraph epsParagraph = new Paragraph("E.P.S : ")
                .setFont(boldFont)
                .setUnderline()
                .setFontSize(10);
        Paragraph epsParagraphValue = new Paragraph(epsValue)
                .setFont(boldFont)
                .setFontSize(10)
                .setMarginRight(10);
        epsCell.add(epsParagraph);
        epsCell.add(epsParagraphValue);

        optionnellesEpsRow.addCell(optionnellesCell);
        optionnellesEpsRow.addCell(epsCell);
        centerColumn.add(optionnellesEpsRow);

        if (hasOptionnelles) {
            centerColumn.add(new Paragraph("\n").setMarginTop(0).setMarginBottom(0));
            Paragraph facLabel = new Paragraph("Matière(s) facultative(s):")
                    .setFont(boldFont)
                    .setUnderline()
                    .setFontSize(10);
            centerColumn.add(facLabel);

            List<String> facultatives = getMatieresFacultatives(c);
            if (facultatives.isEmpty()) {
                centerColumn.add(new Paragraph("Aucune matière facultative choisie").setFontSize(9).setMarginLeft(10));
            } else {
                for (String fac : facultatives) {
                    centerColumn.add(new Paragraph(fac).setFontSize(9).setMarginLeft(10));
                }
            }
        }

        centerColumn.add(new Paragraph("\n").setMarginTop(0).setMarginBottom(0));

        Paragraph note = new Paragraph("N.B : Toute information vous paraissant erronée doit être signalée au plus tard le 30 Mai" +" "+(LocalDate.now().getYear()))
                .setFontSize(8)
                .setFont(boldFont);
        centerColumn.add(note);
        grid.addCell(centerColumn);

        // ===== RIGHT COLUMN =====
        Cell rightColumn = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(2);

        rightColumn.add(new Paragraph("TRES IMPORTANT")
                .setFont(boldFont)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        Paragraph importantText = new Paragraph()
                .setFontSize(8)
                .setMultipliedLeading(1.2f)
                .setTextAlignment(TextAlignment.JUSTIFIED);

        importantText.add("Pendant toute la durée de la session, vous devez :\n");
        importantText.add("- Être en salle, muni de cette convocation et de votre pièce d'identité le matin à 7h15 et l'après-midi à 14h15. Aucun retardataire ne sera admis en salle.\n");
        importantText.add("- Retirer auprès du Président de jury votre relevé de notes, qui est indispensable pour le choix des épreuves du 2ème groupe. Ce choix doit se faire dans la demi journée qui suit la proclamation des résultats.\n");
        importantText.add("- Retirer votre diplôme à l'Inspection d'Académie de votre région ou à l'Office du Baccalauréat à partir d'une date qui sera communiquée après l'examen.");

        rightColumn.add(importantText);
        grid.addCell(rightColumn);
        document.add(grid);

        // ================= RAPPEL TELEPHONE =================
        Paragraph rappel = new Paragraph("Rappel: Le téléphone portable et autres appareils assimilés sont formellement interdits dans les centres d'examen. Tout contrevenant sera exclu de l'ensemble de l'examen et traduit devant le Conseil de Discipline.")
                .setFont(boldFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.JUSTIFIED);
        document.add(rappel);

        // ================= SECTION EPS =================
        Table epsSection = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth()
                .setMarginTop(1)
                .setMarginBottom(1);

        Cell epsLeft = new Cell()
                .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1))
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPadding(2);

        epsLeft.add(new Paragraph("Epreuves d'Education Physique et Sportive")
                .setFont(boldFont)
                .setFontSize(10));

        String jourEPSDate = (jourEPS != null && jourEPS.getName() != null)
                ? (jourEPS.getName())
                : "date à définir";
        epsLeft.add(new Paragraph("A partir du : " + jourEPSDate)
                .setMarginLeft(8)
                .setFontSize(10));

        String centreEPS = c.getCentreActEPS() != null ? c.getCentreActEPS().getName() : "-";
        epsLeft.add(new Paragraph("Centre : " + centreEPS)
                .setMarginLeft(8)
                .setFontSize(8));

        Cell epsRight = new Cell()
                .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1))
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPadding(2);

        epsRight.add(new Paragraph("Veuillez prendre attache avec ledit centre pour les détails et précisions concernant le déroulement de ces épreuves d'EPS")
                .setFontSize(8)
                .setFont(lucidaFont)
                .setTextAlignment(TextAlignment.LEFT));

        epsSection.addCell(epsLeft);
        epsSection.addCell(epsRight);
        document.add(epsSection);

        // ================= INFO 2ND GROUPE =================
        Paragraph secondGroupe = new Paragraph()
                .setFontSize(8)
                .setFont(boldFont)
                .setTextAlignment(TextAlignment.JUSTIFIED);
        secondGroupe.add("En cas d'admissibilité au Second (2nd) groupe d'épreuves, vous aurez à choisir trois (03) matières pour la recomposition dont ");
        secondGroupe.add("deux (02) dominantes et une (01) non-dominante");
        secondGroupe.add(". Se référer à la colonne dénommée ");
        secondGroupe.add("\"Matières autorisées au 2nd groupe d'épreuves\"");
        secondGroupe.add(" du planning");
        document.add(secondGroupe);

        // ================= TITRE PLANNING =================
        Paragraph planningTitle = new Paragraph();
        planningTitle.add(new Text("Planning de déroulement de l'examen").setFont(boldFont).setFontSize(11).setUnderline());
        planningTitle.add(new Text(" : Série " + (c.getSerie() != null ? c.getSerie() : "-")).setFont(boldFont).setFontSize(11));
        planningTitle.setMarginTop(0);
        document.add(planningTitle);
        // ================= TABLEAU DES EPREUVES =================
        Table epreuvesTable = new Table(UnitValue.createPercentArray(new float[]{25, 14, 10, 8, 6, 10, 27}))
                .useAllAvailableWidth();

        String[] tableHeaders = {"Matière de l'épreuve", "Date", "Heure", "Durée", "Coef.", "Nature", "2nd groupe"};
        for (String h : tableHeaders) {
            epreuvesTable.addCell(createHeaderCell(h, 8, boldFont));
        }

        int rowIndex = 0;
        for (EpreuveResponse e : epreuves) {
            boolean isEvenRow = (rowIndex % 2 == 0);

            // ✅ MATIÈRE EN GRAS SI DOMINANTE
            // ✅ MATIÈRE EN GRAS SI DOMINANTE + GESTION LV1/LV2
            String matiere = e.getMatiere() != null ? e.getMatiere().getName() : "-";
            String type = e.getType() != null ? e.getType() : "";

// Gestion spéciale pour LV1 et LV2
            if ("LV1".equals(matiere) && "Écrit".equals(type)) {
                matiere = "LV1 - Ecrit";
            } else if ("LV2".equals(matiere) && "Écrit".equals(type)) {
                matiere = "LV2 - Ecrit";
            } else if ("LV1".equals(matiere) && ("Oral/TP".equals(type) || "Oral".equals(type))) {
                matiere = "LV1 - Oral";
            }
            Paragraph matiereParagraph;
            if (Boolean.TRUE.equals(e.getEstDominant())) {
                matiere = "★ " + matiere;
                matiereParagraph = new Paragraph(matiere).setFont(boldFont).setFontSize(9); // GRAS
            } else {
                matiereParagraph = new Paragraph(matiere).setFont(normalFont).setFontSize(9);
            }
            Cell matiereCell = new Cell().add(matiereParagraph)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 0.3f))
                    .setPadding(2)
                    .setTextAlignment(TextAlignment.CENTER);
            if (isEvenRow) {
                matiereCell.setBackgroundColor(new DeviceGray(0.9f));
            } else {
                matiereCell.setBackgroundColor(ColorConstants.WHITE);
            }
            epreuvesTable.addCell(matiereCell);

            // Date
            String dateStr = e.getJourDebut() != null ? e.getJourDebut().getName() : "-";
            epreuvesTable.addCell(createDataCell(dateStr, 8, isEvenRow));

            // Heure
            String heureStr = e.getHeureDebut() != null ? e.getHeureDebut().getHeure() : "-";
            epreuvesTable.addCell(createDataCell(heureStr, 8, isEvenRow));

            // Durée
            String dureeStr = e.getDuree() != null ? e.getDuree() : "-";
            epreuvesTable.addCell(createDataCell(dureeStr, 8, isEvenRow));

            // Coefficient
            String coefStr = e.getCoefficient() != null ? String.valueOf(e.getCoefficient()) : "-";
            epreuvesTable.addCell(createDataCell(coefStr, 8, isEvenRow));

            // ✅ NATURE EN GRAS
            String natureStr = e.getType() != null ? e.getType() : "-";
            Paragraph natureParagraph = new Paragraph(natureStr).setFont(boldFont).setFontSize(9); // TOUJOURS GRAS
            Cell natureCell = new Cell().add(natureParagraph)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 0.3f))
                    .setPadding(2)
                    .setTextAlignment(TextAlignment.CENTER);
            if (isEvenRow) {
                natureCell.setBackgroundColor(new DeviceGray(0.9f));
            } else {
                natureCell.setBackgroundColor(ColorConstants.WHITE);
            }
            epreuvesTable.addCell(natureCell);
            // 2nd groupe
            String secondGroupStr = "=== NON ===";
            if (e.getType() != null && (e.getType().equals("Écrit") || e.getType().equals("Ecrit"))) {
                String autorisation = Boolean.TRUE.equals(e.getAutorisation()) ? "OUI" : "NON";
                String dominante = Boolean.TRUE.equals(e.getEstDominant()) ? "DOMINANTE" : "Non-dominante";
                secondGroupStr = autorisation + " / " + dominante;
            }
            epreuvesTable.addCell(createDataCell(secondGroupStr, 8, isEvenRow));

            rowIndex++;
        }
        document.add(epreuvesTable);

        // ================= FOOTER =================
        String ville = (c.getEtablissement() != null && c.getEtablissement().getVille() != null)
                ? c.getEtablissement().getVille().getName()
                : "Dakar";

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Paragraph footer = new Paragraph(ville + ", le " + currentDate)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(8);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }
    private Cell createHeaderCell(String text, int fontSize, PdfFont boldFont) {
        return new Cell()
                .add(new Paragraph(text).setFont(boldFont).setFontSize(fontSize))
                .setBackgroundColor(new DeviceGray(0.9f))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.3f))
                .setPadding(2)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createDataCell(String text, int fontSize) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "-").setFontSize(fontSize))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.3f))
                .setPadding(2)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createDataCell(String text, int fontSize, boolean isEvenRow) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "-").setFontSize(fontSize))
                .setBorder(new SolidBorder(ColorConstants.BLACK, 0.3f))
                .setPadding(2)
                .setTextAlignment(TextAlignment.CENTER);

        if (isEvenRow) {
            cell.setBackgroundColor(new DeviceGray(0.9f));
        } else {
            cell.setBackgroundColor(ColorConstants.WHITE);
        }
        return cell;
    }

    private Cell createNoBorderCell(String text, int fontSize, boolean bold) {
        Paragraph p = new Paragraph(text).setFontSize(fontSize);
        if (bold) p.setBold();
        return new Cell()
                .add(p)
                .setBorder(Border.NO_BORDER)
                .setPadding(1);
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "-";
        try {
            LocalDate date = LocalDate.parse(dateString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH);
            return date.format(formatter);
        } catch (Exception e) {
            return dateString;
        }
    }

    private boolean hasMatieresOptionnelles(CandidatFinis c) {
        return (c.getMo1() != null && !c.getMo1().isEmpty()) ||
                (c.getMo2() != null && !c.getMo2().isEmpty()) ||
                (c.getMo3() != null && !c.getMo3().isEmpty()) ;
    }

    private List<String> getMatieresOptionnelles(CandidatFinis c) {
        List<String> options = new java.util.ArrayList<>();
        if (c.getMo1() != null && !c.getMo1().isEmpty()) options.add("- LV1 : " + c.getMo1());
        if (c.getMo2() != null && !c.getMo2().isEmpty()) options.add("- LV2 : " + c.getMo2());
        if (c.getMo3() != null && !c.getMo3().isEmpty()) options.add("- Sciences de la Nature(P.C ou SVT) : " + c.getMo3());
        return options;
    }

    private List<String> getMatieresFacultatives(CandidatFinis c) {
        List<String> facultatives = new java.util.ArrayList<>();
        if (c.getCentreMatFac1() != null && !c.getCentreMatFac1().isEmpty()) {
            String lib = c.getLibMatFac1() != null ? c.getLibMatFac1() : "Matière 1";
            facultatives.add("- " + lib + " : " + c.getCentreMatFac1());
        }
        if (c.getCentreMatFac2() != null && !c.getCentreMatFac2().isEmpty()) {
            String lib = c.getLibMatFac2() != null ? c.getLibMatFac2() : "Matière 2";
            facultatives.add("- " + lib + " : " + c.getCentreMatFac2());
        }
        return facultatives;
    }

    // ========== MÉTHODE OPTIMISÉE AVEC CACHE ==========
    public Mono<byte[]> generateConvocation(String numeroTable) {
        return epreuveService.findByNumeroTable(numeroTable)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Candidat introuvable pour table: " + numeroTable)
                ))
                .flatMap(candidat -> {
                    String serieCode = candidat.getSerie();

                    // Récupération avec cache pour le jour EPS
                    Mono<Jour> jourMono = (jourEPSCache != null)
                            ? Mono.just(jourEPSCache)
                            : epreuveService.getJourEPS().doOnNext(j -> jourEPSCache = j);

                    // Récupération avec cache pour les épreuves
                    Mono<List<EpreuveResponse>> epreuvesMono;
                    if (epreuvesCache.containsKey(serieCode)) {
                        epreuvesMono = Mono.just(epreuvesCache.get(serieCode));
                    } else {
                        epreuvesMono = epreuveService.getBySerie(serieCode)
                                .doOnNext(ep -> epreuvesCache.put(serieCode, ep));
                    }

                    return Mono.zip(epreuvesMono, jourMono)
                            .flatMap(tuple -> {
                                List<EpreuveResponse> epreuves = tuple.getT1();
                                Jour jourEPS = tuple.getT2();
                                return Mono.fromCallable(() -> generateConvocation(candidat, epreuves, jourEPS))
                                        .subscribeOn(Schedulers.boundedElastic());
                            });
                });
    }
    private void generateConvocationContent(Document document, CandidatFinis c, List<EpreuveResponse> epreuves, Jour jourEPS,
                                            PdfDocument pdf, PdfFont normalFont, PdfFont boldFont, PdfFont lucidaFont) {
        // ================= HEADER =================
        Table header = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setMarginBottom(2);

        header.addCell(createNoBorderCell(
                "UNIVERSITE CHEIKH ANTA DIOP DE DAKAR\nOFFICE DU BACCALAUREAT", 9, false));

        header.addCell(createNoBorderCell(
                "REPUBLIQUE DU SENEGAL\nUn Peuple - Un But - Une Foi", 9, false)
                .setTextAlignment(TextAlignment.RIGHT));

        document.add(header);

        // ================= HEADER GRID =================
        Table headerGrid = new Table(UnitValue.createPercentArray(new float[]{24, 36, 20}))
                .useAllAvailableWidth()
                .setMarginTop(0)
                .setMarginBottom(0);

        Cell cellLeft = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

// Paragraphe principal
        Paragraph convocation = new Paragraph("CONVOCATION")
                .setFont(boldFont)
                .setFontSize(12)
                .setUnderline()
                .setItalic()
                .setTextAlignment(TextAlignment.LEFT);
        cellLeft.add(convocation);

// Petit texte mention
        Paragraph mentionImpression = new Paragraph("À imprimer impérativement")
                .setFont(normalFont)
                .setFontSize(9)
                .setFontColor(ColorConstants.RED)
                .setTextAlignment(TextAlignment.LEFT);
        cellLeft.add(mentionImpression);

        headerGrid.addCell(cellLeft);

        Cell cellCenter = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        cellCenter.add(new Paragraph("BACCALAUREAT DE L'ENSEIGNEMENT SECONDAIRE")
                .setFont(boldFont)
                .setFontSize(10));

        cellCenter.add(new Paragraph("SESSION NORMALE" + " " + (LocalDate.now().getYear()))
                .setFont(boldFont)
                .setFontSize(10));

        String serieText = "Série : " + (c.getSerie() != null ? c.getSerie() : "-");
        cellCenter.add(
                new Paragraph(serieText)
                        .setFont(boldFont)
                        .setFontSize(9)
                        .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                        .setBorderRadius(new BorderRadius(6))
                        .setPadding(1)
                        .setMarginTop(1)
        );

        headerGrid.addCell(cellCenter);

        Cell cellRight = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0)
                .setVerticalAlignment(VerticalAlignment.TOP);

        Table qrTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(160);

        Paragraph title1 = new Paragraph("Résultats Bac")
                .setFont(boldFont)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER);

        String url1 = "https://portailbac.ucad.sn/search";
        BarcodeQRCode qrCode1 = new BarcodeQRCode(url1);
        Image qrImage1 = new Image(qrCode1.createFormXObject(ColorConstants.BLACK, pdf))
                .scaleToFit(70, 70);

        Div qrDiv1 = new Div().setTextAlignment(TextAlignment.CENTER);
        qrDiv1.add(qrImage1);
        qrDiv1.add(title1);

        Cell qrCell1 = new Cell().add(qrDiv1).setBorder(Border.NO_BORDER);
        qrTable.addCell(qrCell1);

        Paragraph title2 = new Paragraph("Règlement Intérieur")
                .setFont(boldFont)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER);

        String url2 = "https://portailbac.ucad.sn/reglement";
        BarcodeQRCode qrCode2 = new BarcodeQRCode(url2);
        Image qrImage2 = new Image(qrCode2.createFormXObject(ColorConstants.BLUE, pdf))
                .scaleToFit(70, 70);

        Div qrDiv2 = new Div().setTextAlignment(TextAlignment.CENTER);
        qrDiv2.add(qrImage2);
        qrDiv2.add(title2);

        Cell qrCell2 = new Cell().add(qrDiv2).setBorder(Border.NO_BORDER);
        qrTable.addCell(qrCell2);

        cellRight.add(qrTable);
        headerGrid.addCell(cellRight);
        document.add(headerGrid);

        // ================= GRID 3 COLONNES =================
        Table grid = new Table(UnitValue.createPercentArray(new float[]{30, 45, 25}))
                .useAllAvailableWidth()
                .setMarginBottom(0)
                .setMarginTop(0);

        // LEFT COLUMN
        Cell leftColumn = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingRight(3)
                .setMarginRight(5);

        leftColumn.add(createLabel("Prénom(s)", normalFont));
        leftColumn.add(createValue(value(c.getPrenoms()), boldFont));

        leftColumn.add(createLabel("Nom", normalFont));
        leftColumn.add(createValue(value(c.getNom()), boldFont));

        leftColumn.add(createLabel("Date de Naissance", normalFont));
        leftColumn.add(createValue(formatDate(c.getDateNaissance()), boldFont));

        leftColumn.add(createLabel("Lieu de Naissance", normalFont));
        leftColumn.add(createValue(value(c.getLieuNaissance()), boldFont));

        String etabName = c.getEtablissement() != null ? c.getEtablissement().getName() : "-";
        leftColumn.add(createLabel("Etablissement fréquenté", normalFont));
        leftColumn.add(createValue(etabName, boldFont));

        leftColumn.add(createLabel("Candidat", normalFont));
        leftColumn.add(createValue(value(c.getTypeCandidat()), boldFont));

        Table sexeNationaliteTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setMarginTop(0);

        Cell sexeCell = new Cell().setBorder(Border.NO_BORDER);
        sexeCell.add(createLabel("Sexe", normalFont));
        sexeCell.add(createValue(value(c.getSexe()), boldFont));

        Cell natCell = new Cell().setBorder(Border.NO_BORDER);
        natCell.add(createLabel("Nationalité", normalFont));
        natCell.add(createValue(value(c.getNationalite()), boldFont));

        sexeNationaliteTable.addCell(sexeCell);
        sexeNationaliteTable.addCell(natCell);
        leftColumn.add(sexeNationaliteTable);
        grid.addCell(leftColumn);

        // CENTER COLUMN
        Cell centerColumn = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingRight(5)
                .setMarginRight(5);

        Table miniTable = new Table(UnitValue.createPercentArray(new float[]{20, 20, 35, 10, 15}))
                .useAllAvailableWidth();

        String[] miniHeaders = {"N° Jury", "N° table", "Centre d'écrit", "N° Bât.", "N° Salle"};
        for (String h : miniHeaders) {
            miniTable.addCell(createHeaderCell(h, 8, boldFont));
        }

        String centreEcrit = c.getCentreEcritParticulier() != null
                ? c.getCentreEcritParticulier()
                : (c.getCentreEcrit() != null ? c.getCentreEcrit().getName() : "-");

        miniTable.addCell(createDataCell(c.getJury() != null ? c.getJury() : "-", 8));
        miniTable.addCell(createDataCell(c.getNumeroTable() != null ? c.getNumeroTable() : "-", 8));
        miniTable.addCell(createDataCell(centreEcrit, 8));
        miniTable.addCell(createDataCell("-", 8));
        miniTable.addCell(createDataCell("-", 8));

        centerColumn.add(miniTable);
        centerColumn.add(new Paragraph("\n").setMarginTop(0).setMarginBottom(0));

        Table optionnellesEpsRow = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .useAllAvailableWidth()
                .setMarginTop(0)
                .setMarginBottom(0);

        Cell optionnellesCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0);

        boolean hasOptionnelles = hasMatieresOptionnelles(c);

        if (hasOptionnelles) {
            Paragraph optLabel = new Paragraph("Matière(s) Optionnelle(s):")
                    .setUnderline()
                    .setFontSize(10)
                    .setFont(boldFont);
            optionnellesCell.add(optLabel);

            for (String opt : getMatieresOptionnelles(c)) {
                optionnellesCell.add(new Paragraph(opt).setFontSize(9).setMarginLeft(10));
            }
        } else {
            Paragraph facLabel = new Paragraph("Matière(s) facultative(s):")
                    .setUnderline()
                    .setFontSize(10)
                    .setFont(boldFont);
            optionnellesCell.add(facLabel);

            List<String> facultatives = getMatieresFacultatives(c);
            if (facultatives.isEmpty()) {
                optionnellesCell.add(new Paragraph("Aucune matière facultative choisie").setFontSize(9).setMarginLeft(10));
            } else {
                for (String fac : facultatives) {
                    optionnellesCell.add(new Paragraph(fac).setFontSize(9).setMarginLeft(10));
                }
            }
        }

        Cell epsCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0)
                .setTextAlignment(TextAlignment.RIGHT);

        String epsValue = c.getEps() != null ? c.getEps() : "-";
        if ("A".equals(c.getEps())) epsValue = "Apte";
        if ("I".equals(c.getEps())) epsValue = "Inapte";

        Paragraph epsParagraph = new Paragraph("E.P.S : ")
                .setFont(boldFont)
                .setUnderline()
                .setFontSize(10);
        Paragraph epsParagraphValue = new Paragraph(epsValue)
                .setFont(boldFont)
                .setFontSize(10)
                .setMarginRight(10);
        epsCell.add(epsParagraph);
        epsCell.add(epsParagraphValue);

        optionnellesEpsRow.addCell(optionnellesCell);
        optionnellesEpsRow.addCell(epsCell);
        centerColumn.add(optionnellesEpsRow);

        if (hasOptionnelles) {
            centerColumn.add(new Paragraph("\n").setMarginTop(0).setMarginBottom(0));
            Paragraph facLabel = new Paragraph("Matière(s) facultative(s):")
                    .setFont(boldFont)
                    .setUnderline()
                    .setFontSize(10);
            centerColumn.add(facLabel);

            List<String> facultatives = getMatieresFacultatives(c);
            if (facultatives.isEmpty()) {
                centerColumn.add(new Paragraph("Aucune matière facultative choisie").setFontSize(9).setMarginLeft(10));
            } else {
                for (String fac : facultatives) {
                    centerColumn.add(new Paragraph(fac).setFontSize(9).setMarginLeft(10));
                }
            }
        }

        centerColumn.add(new Paragraph("\n").setMarginTop(0).setMarginBottom(0));

        Paragraph note = new Paragraph("N.B : Toute information vous paraissant erronée doit être signalée au plus tard le 30 Mai" + " " + (LocalDate.now().getYear()))
                .setFontSize(8)
                .setFont(boldFont);
        centerColumn.add(note);
        grid.addCell(centerColumn);

        // RIGHT COLUMN
        Cell rightColumn = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(2);

        rightColumn.add(new Paragraph("TRES IMPORTANT")
                .setFont(boldFont)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        Paragraph importantText = new Paragraph()
                .setFontSize(8)
                .setMultipliedLeading(1.2f)
                .setTextAlignment(TextAlignment.JUSTIFIED);

        importantText.add("Pendant toute la durée de la session, vous devez :\n");
        importantText.add("- Être en salle, muni de cette convocation et de votre pièce d'identité le matin à 7h15 et l'après-midi à 14h15. Aucun retardataire ne sera admis en salle.\n");
        importantText.add("- Retirer auprès du Président de jury votre relevé de notes, qui est indispensable pour le choix des épreuves du 2ème groupe. Ce choix doit se faire dans la demi journée qui suit la proclamation des résultats.\n");
        importantText.add("- Retirer votre diplôme à l'Inspection d'Académie de votre région ou à l'Office du Baccalauréat à partir d'une date qui sera communiquée après l'examen.");

        rightColumn.add(importantText);
        grid.addCell(rightColumn);
        document.add(grid);

        // RAPPEL TELEPHONE
        Paragraph rappel = new Paragraph("Rappel: Le téléphone portable et autres appareils assimilés sont formellement interdits dans les centres d'examen. Tout contrevenant sera exclu de l'ensemble de l'examen et traduit devant le Conseil de Discipline.")
                .setFont(boldFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.JUSTIFIED);
        document.add(rappel);

        // SECTION EPS
        Table epsSection = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth()
                .setMarginTop(1)
                .setMarginBottom(1);

        Cell epsLeft = new Cell()
                .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1))
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPadding(2);

        epsLeft.add(new Paragraph("Epreuves d'Education Physique et Sportive")
                .setFont(boldFont)
                .setFontSize(10));

        String jourEPSDate = (jourEPS != null && jourEPS.getName() != null)
                ? (jourEPS.getName())
                : "date à définir";
        epsLeft.add(new Paragraph("A partir du : " + jourEPSDate)
                .setMarginLeft(8)
                .setFontSize(10));

        String centreEPS = c.getCentreActEPS() != null ? c.getCentreActEPS().getName() : "-";
        epsLeft.add(new Paragraph("Centre : " + centreEPS)
                .setMarginLeft(8)
                .setFontSize(8));

        Cell epsRight = new Cell()
                .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1))
                .setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1))
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setPadding(1);

        epsRight.add(new Paragraph("Veuillez prendre attache avec ledit centre pour les détails et précisions concernant le déroulement de ces épreuves d'EPS")
                .setFontSize(8)
                .setFont(lucidaFont)
                .setTextAlignment(TextAlignment.LEFT));

        epsSection.addCell(epsLeft);
        epsSection.addCell(epsRight);
        document.add(epsSection);

        // INFO 2ND GROUPE
        Paragraph secondGroupe = new Paragraph()
                .setFontSize(8)
                .setFont(boldFont)
                .setTextAlignment(TextAlignment.JUSTIFIED);
        secondGroupe.add("En cas d'admissibilité au Second (2nd) groupe d'épreuves, vous aurez à choisir trois (03) matières pour la recomposition dont ");
        secondGroupe.add("deux (02) dominantes et une (01) non-dominante");
        secondGroupe.add(". Se référer à la colonne dénommée ");
        secondGroupe.add("\"Matières autorisées au 2nd groupe d'épreuves\"");
        secondGroupe.add(" du planning");
        document.add(secondGroupe);

        // TITRE PLANNING
        Paragraph planningTitle = new Paragraph();
        planningTitle.add(new Text("Planning de déroulement de l'examen").setFont(boldFont).setFontSize(11).setUnderline());
        planningTitle.add(new Text(" : Série " + (c.getSerie() != null ? c.getSerie() : "-")).setFont(boldFont).setFontSize(11));
        planningTitle.setMarginTop(0);
        document.add(planningTitle);

        // TABLEAU DES EPREUVES
        Table epreuvesTable = new Table(UnitValue.createPercentArray(new float[]{25, 14, 10, 8, 6, 10, 27}))
                .useAllAvailableWidth();

        String[] tableHeaders = {"Matière de l'épreuve", "Date", "Heure", "Durée", "Coef.", "Nature", "2nd groupe"};
        for (String h : tableHeaders) {
            epreuvesTable.addCell(createHeaderCell(h, 8, boldFont));
        }
        int rowIndex = 0;
        for (EpreuveResponse e : epreuves) {
            boolean isEvenRow = (rowIndex % 2 == 0);

            String matiere = e.getMatiere() != null ? e.getMatiere().getName() : "-";
            String type = e.getType() != null ? e.getType() : "";

            if ("LV1".equals(matiere) && "Écrit".equals(type)) {
                matiere = "LV1 - Ecrit";
            } else if ("LV2".equals(matiere) && "Écrit".equals(type)) {
                matiere = "LV2 - Ecrit";
            } else if ("LV1".equals(matiere) && ("Oral/TP".equals(type) || "Oral".equals(type))) {
                matiere = "LV1 - Oral";
            }
            Paragraph matiereParagraph;
            if (Boolean.TRUE.equals(e.getEstDominant())) {
                matiere = "★ " + matiere;
                matiereParagraph = new Paragraph(matiere).setFont(boldFont).setFontSize(9); // GRAS
            } else {
                matiereParagraph = new Paragraph(matiere).setFont(normalFont).setFontSize(9);
            }
            Cell matiereCell = new Cell().add(matiereParagraph)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 0.3f))
                    .setPadding(2)
                    .setTextAlignment(TextAlignment.CENTER);
            if (isEvenRow) {
                matiereCell.setBackgroundColor(new DeviceGray(0.9f));
            } else {
                matiereCell.setBackgroundColor(ColorConstants.WHITE);
            }
            epreuvesTable.addCell(matiereCell);

            // Date
            String dateStr = e.getJourDebut() != null ? e.getJourDebut().getName() : "-";
            epreuvesTable.addCell(createDataCell(dateStr, 8, isEvenRow));

            // Heure
            String heureStr = e.getHeureDebut() != null ? e.getHeureDebut().getHeure() : "-";
            epreuvesTable.addCell(createDataCell(heureStr, 8, isEvenRow));

            // Durée
            String dureeStr = e.getDuree() != null ? e.getDuree() : "-";
            epreuvesTable.addCell(createDataCell(dureeStr, 8, isEvenRow));

            // Coefficient
            String coefStr = e.getCoefficient() != null ? String.valueOf(e.getCoefficient()) : "-";
            epreuvesTable.addCell(createDataCell(coefStr, 8, isEvenRow));
            // ✅ NATURE EN GRAS
            String natureStr = e.getType() != null ? e.getType() : "-";
            Paragraph natureParagraph = new Paragraph(natureStr).setFont(boldFont).setFontSize(9); // TOUJOURS GRAS
            Cell natureCell = new Cell().add(natureParagraph)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 0.3f))
                    .setPadding(2)
                    .setTextAlignment(TextAlignment.CENTER);
            if (isEvenRow) {
                natureCell.setBackgroundColor(new DeviceGray(0.9f));
            } else {
                natureCell.setBackgroundColor(ColorConstants.WHITE);
            }
            epreuvesTable.addCell(natureCell);
            // 2nd groupe
            String secondGroupStr = "=== NON ===";
            if (e.getType() != null && (e.getType().equals("Écrit") || e.getType().equals("Ecrit"))) {
                String autorisation = Boolean.TRUE.equals(e.getAutorisation()) ? "OUI" : "NON";
                String dominante = Boolean.TRUE.equals(e.getEstDominant()) ? "DOMINANTE" : "Non-dominante";
                secondGroupStr = autorisation + " / " + dominante;
            }
            epreuvesTable.addCell(createDataCell(secondGroupStr, 8, isEvenRow));

            rowIndex++;
        }

        document.add(epreuvesTable);

        // FOOTER
        String ville = (c.getEtablissement() != null && c.getEtablissement().getVille() != null)
                ? c.getEtablissement().getVille().getName()
                : "Dakar";

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        Paragraph footer = new Paragraph(ville + ", le " + currentDate)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(8);
        document.add(footer);
    }

    // ========== NOUVELLES MÉTHODES POUR LES SÉRIES ==========

    /**
     * Génère un PDF unique contenant toutes les convocations d'une série
     * Chaque convocation est sur une page séparée
     */
    public byte[] generateConvocationsPdfBySerie(List<CandidatFinisResponse> candidats, String serieCode) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(8, 15, 8, 15);

        PdfFont normalFont = PdfFontFactory.createFont("Helvetica");
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont lucidaFont = loadLucidaFont(normalFont);

        List<EpreuveResponse> epreuves = getEpreuvesBySerieFromCache(serieCode);
        Jour jourEPS = getJourEPSFromCache();

        int count = 0;
        int total = candidats.size();

        log.info("🚀 Génération PDF pour série {} - {} candidats", serieCode, total);

        for (CandidatFinisResponse candidat : candidats) {
            CandidatFinis candidatEntity = convertToEntity(candidat);

            generateConvocationContent(document, candidatEntity, epreuves, jourEPS, pdf,
                    normalFont, boldFont, lucidaFont);

            count++;

            if (count < total) {
                pdf.addNewPage();
            }

            if (count % 10 == 0) {
                log.info("📄 Progression: {}/{} convocations générées", count, total);
            }
        }

        document.close();
        log.info("✅ PDF généré pour série {}: {} pages", serieCode, total);
        return out.toByteArray();
    }

    // ========== MÉTHODES DE CACHE ET CONVERSION ==========

    private PdfFont loadLucidaFont(PdfFont fallbackFont) {
        try {
            java.io.File fontFile = new java.io.File("src/main/resources/fonts/LucidaUnicodeCalligraphy.ttf");
            if (fontFile.exists()) {
                return PdfFontFactory.createFont(fontFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Police Lucida non trouvée");
        }
        return fallbackFont;
    }

    private List<EpreuveResponse> getEpreuvesBySerieFromCache(String serieCode) {
        if (epreuvesCache.containsKey(serieCode)) {
            return epreuvesCache.get(serieCode);
        }
        List<EpreuveResponse> epreuves = epreuveService.getBySerie(serieCode).block();
        epreuvesCache.put(serieCode, epreuves);
        return epreuves;
    }

    private Jour getJourEPSFromCache() {
        if (jourEPSCache != null) {
            return jourEPSCache;
        }
        jourEPSCache = epreuveService.getJourEPS().block();
        return jourEPSCache;
    }

    private CandidatFinis convertToEntity(CandidatFinisResponse response) {
        CandidatFinis entity = new CandidatFinis();
        entity.setId(response.getId());
        entity.setNumeroTable(response.getNumeroTable());
        entity.setNom(response.getNom());
        entity.setPrenoms(response.getPrenoms());
        entity.setSerie(response.getSerie());
        entity.setDateNaissance(response.getDateNaissance());
        entity.setLieuNaissance(response.getLieuNaissance());
        entity.setNationalite(response.getNationalite());
        entity.setJury(response.getJury());
        entity.setSexe(response.getSexe());
        entity.setTypeCandidat(response.getTypeCandidat());
        entity.setEps(response.getEps());
        entity.setEtablissement(response.getEtablissement());
        entity.setCentreEcrit(response.getCentreEcrit());
        entity.setCentreActEPS(response.getCentreActEPS());
        entity.setMo1(response.getMo1());
        entity.setMo2(response.getMo2());
        entity.setMo3(response.getMo3());
        entity.setEf1(response.getEf1());
        entity.setEf2(response.getEf2());
        entity.setCentreMatFac1(response.getCentreMatFac1());
        entity.setLibMatFac1(response.getLibMatFac1());
        entity.setCentreMatFac2(response.getCentreMatFac2());
        entity.setLibMatFac2(response.getLibMatFac2());
        return entity;
    }
    /**
     * Génère un ZIP contenant un PDF par série
     * Chaque PDF a une page par candidat (convocation)
     *
     * @param candidatsParSerie Map des candidats groupés par série
     * @return ZIP contenant tous les PDFs
     */
    public byte[] generateConvocationsZipBySeries(Map<String, List<CandidatFinisResponse>> candidatsParSerie) throws IOException {
        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(zipOut);

        int totalSeries = candidatsParSerie.size();
        int currentSerie = 0;

        log.info("🚀 Génération ZIP pour {} séries", totalSeries);

        for (Map.Entry<String, List<CandidatFinisResponse>> serieEntry : candidatsParSerie.entrySet()) {
            String serieCode = serieEntry.getKey();
            List<CandidatFinisResponse> candidats = serieEntry.getValue();
            currentSerie++;

            log.info("📄 Traitement série {}/{}: {} - {} candidats", currentSerie, totalSeries, serieCode, candidats.size());

            // ⚠️ IMPORTANT: Créer un NOUVEAU document pour chaque série
            byte[] pdfBytes = generatePdfForSerie(serieCode, candidats);

            // Ajouter le PDF au ZIP
            String filename = "convocations_serie_" + sanitizeFileName(serieCode) + ".pdf";
            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filename);
            zos.putNextEntry(zipEntry);
            zos.write(pdfBytes);
            zos.closeEntry();

            log.info("✅ Ajouté au ZIP: {} ({} pages)", filename, candidats.size());
        }

        zos.close();
        log.info("📦 ZIP généré avec succès: {} séries", totalSeries);
        return zipOut.toByteArray();
    }

    /**
     * Génère un PDF pour une série spécifique
     * Crée un nouveau document PDF indépendant
     */
    private byte[] generatePdfForSerie(String serieCode, List<CandidatFinisResponse> candidats) throws IOException {
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(pdfOut);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(8, 15, 8, 15);

        // Charger les fonts pour ce document
        PdfFont normalFont = PdfFontFactory.createFont("Helvetica");
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont lucidaFont = loadLucidaFont(normalFont);

        // Récupérer les épreuves pour cette série (une seule fois)
        List<EpreuveResponse> epreuves = getEpreuvesBySerieFromCache(serieCode);

        // Récupérer le jour EPS (une seule fois)
        Jour jourEPS = getJourEPSFromCache();

        int totalCandidats = candidats.size();
        int candidatCount = 0;

        for (CandidatFinisResponse candidat : candidats) {

            if (candidatCount > 0) {
                document.add(new AreaBreak());
            }

            CandidatFinis candidatEntity = convertToEntity(candidat);

            generateConvocationContent(document, candidatEntity, epreuves, jourEPS, pdf,
                    normalFont, boldFont, lucidaFont);

            candidatCount++;
        }
        document.close();
        return pdfOut.toByteArray();
    }
    /**
     * Nettoie un nom de fichier
     */
    private String sanitizeFileName(String name) {
        if (name == null) return "inconnu";
        return name.replaceAll("[^a-zA-Z0-9À-ÿ_-]", "_");
    }
}