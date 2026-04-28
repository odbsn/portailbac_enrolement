package com.officedubac.project.module.candidatFinis;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.officedubac.project.module.candidatFinis.dto.CandidatFinisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListeCandidatsPdfService {

    private static final float MARGIN = 30;
    /**
     * Version simplifiée du PDF de légende
     */
    private byte[] generateLegendePdf(List<CandidatFinisResponse> candidats,
                                      String etablissementNom,
                                      String etablissementCode,
                                      String centreExamenNom,
                                      String session) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        // Titre
        document.add(new Paragraph("LÉGENDE DES CENTRES D'ÉCRIT")
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Informations
        document.add(new Paragraph("Session : " + session).setFontSize(10));
        document.add(new Paragraph("Établissement : " + etablissementNom + " (" + etablissementCode + ")").setFontSize(10));
        document.add(new Paragraph("Centre d'examen : " + centreExamenNom).setFontSize(10));
        document.add(new Paragraph("\n"));

        // Centres d'écrit
        Set<String> centresEcrit = new TreeSet<>();
        for (CandidatFinisResponse c : candidats) {
            if (c.getCentreEcrit() != null) {
                String code = c.getCentreEcrit().getCode();
                String name = c.getCentreEcrit().getName();
                if (code != null && !code.isEmpty() && name != null && !name.isEmpty()) {
                    centresEcrit.add(code + " : " + name);
                } else if (code != null && !code.isEmpty()) {
                    centresEcrit.add(code);
                } else if (name != null && !name.isEmpty()) {
                    centresEcrit.add(name);
                }
            }
        }

        document.add(new Paragraph("Liste des centres d'écrit :").setBold().setFontSize(11));
        for (String centre : centresEcrit) {
            document.add(new Paragraph("• " + centre).setFontSize(10).setMarginLeft(15));
        }

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Total candidats : " + candidats.size()).setFontSize(10));
        document.add(new Paragraph("Date génération : " + LocalDate.now()).setFontSize(8).setFontColor(ColorConstants.DARK_GRAY));

        document.close();
        return outputStream.toByteArray();
    }
    public byte[] generateZipBySerie(List<CandidatFinisResponse> candidats,
                                     String etablissementNom,
                                     String etablissementCode,
                                     String centreExamenNom,
                                     String session) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            log.info("📄 Génération de la légende des centres d'écrit...");
            byte[] legendePdf = generateLegendePdf(candidats, etablissementNom, etablissementCode, centreExamenNom, session);

            ZipEntry legendeEntry = new ZipEntry("legende.pdf");
            zos.putNextEntry(legendeEntry);
            zos.write(legendePdf);
            zos.closeEntry();
            log.info("✅ legende.pdf ajouté au ZIP");

            // Grouper les candidats par série
            Map<String, List<CandidatFinisResponse>> candidatsBySerie = new HashMap<>();

            for (CandidatFinisResponse candidat : candidats) {
                String serie = candidat.getSerie() != null ? candidat.getSerie() : "SANS_SERIE";
                candidatsBySerie.computeIfAbsent(serie, k -> new ArrayList<>()).add(candidat);
            }

            // Générer un PDF pour chaque série
            for (Map.Entry<String, List<CandidatFinisResponse>> entry : candidatsBySerie.entrySet()) {
                String serie = entry.getKey();
                List<CandidatFinisResponse> candidatsSerie = entry.getValue();

                // Format: S1_liste_candidats.pdf
                String fileName = String.format("%s_liste_candidats.pdf", serie);

                // Générer le PDF pour cette série
                byte[] pdfBytes = generate(
                        candidatsSerie,
                        etablissementNom,
                        etablissementCode,
                        centreExamenNom,
                        session + " - Série " + serie
                );

                // Ajouter au ZIP
                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);
                zos.write(pdfBytes);
                zos.closeEntry();

                log.info("✅ PDF généré pour la série {} : {} candidats", serie, candidatsSerie.size());
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération du ZIP", e);
            throw new RuntimeException("Erreur génération ZIP", e);
        }

        return baos.toByteArray();
    }
    public byte[] generate(List<CandidatFinisResponse> candidats,
                           String etablissementNom,
                           String etablissementCode,
                           String centreExamenNom,
                           String session) throws IOException {
try {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    PdfWriter writer = new PdfWriter(outputStream);
    PdfDocument pdfDoc = new PdfDocument(writer);
    pdfDoc.addEventHandler(
            PdfDocumentEvent.END_PAGE,
            new HeaderHandler(etablissementNom,etablissementCode, centreExamenNom)
    );
    PdfFormXObject placeholder = new PdfFormXObject(new Rectangle(0, 0, 30, 10));
    Document document = new Document(pdfDoc, PageSize.A4.rotate());
    FooterHandler footerHandler = new FooterHandler(
            placeholder,
            centreExamenNom,
            session
    );

    pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, footerHandler);

    document.setMargins(60, MARGIN, 40, MARGIN);

    PdfFont font = PdfFontFactory.createFont();

    // ================= TRI =================
    candidats = candidats.stream()
            .sorted(
                    Comparator.comparing(
                                    CandidatFinisResponse::getSerie,
                                    Comparator.nullsLast(String::compareTo)
                            )
                            .thenComparing(
                                    CandidatFinisResponse::getNom,
                                    Comparator.nullsLast(String::compareTo)
                            )
                            .thenComparing(
                                    CandidatFinisResponse::getPrenoms,
                                    Comparator.nullsLast(String::compareTo)
                            )
                            .thenComparing(c -> {
                                try {
                                    return Integer.parseInt(
                                            c.getNumeroTable() == null ? "0" : c.getNumeroTable()
                                    );
                                } catch (Exception e) {
                                    return 0;
                                }
                            })
            )
            .toList();

    // ================= HEADER =================
    Table headerTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
    headerTable.setWidth(UnitValue.createPercentValue(100));

    Cell right = new Cell().setBorder(Border.NO_BORDER);
    right.add(center("UNIVERSITE CHEIKH ANTA DIOP DE DAKAR", font, true));
    right.add(center("--------------", font, false));
    right.add(center("OFFICE DU BACCALAUREAT", font, true));

    Cell left = new Cell().setBorder(Border.NO_BORDER);
    left.add(center("REPUBLIQUE DU SENEGAL", font, true));
    left.add(center("Un Peuple - Un But - Une Foi", font, false));
    left.add(center("----------------------------", font, false));

    headerTable.addCell(left);
    headerTable.addCell(right);

    document.add(headerTable);
    document.add(new Paragraph("\n"));

    // ================= TITRES =================
    document.add(new Paragraph("BACCALAUREAT DE L'ENSEIGNEMENT SECONDAIRE")
            .setBold()
            .setTextAlignment(TextAlignment.CENTER));

    document.add(new Paragraph("ANNEE " + LocalDate.now().getYear())
            .setBold()
            .setTextAlignment(TextAlignment.CENTER));

    document.add(new Paragraph("SESSION " + session)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER));

    document.add(new Paragraph("\n"));

    Table headerTable1 = new Table(1);
    headerTable1.setWidth(UnitValue.createPercentValue(100));
    headerTable1.setHorizontalAlignment(HorizontalAlignment.CENTER);

    Cell headerCell = new Cell()
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(0)
            .setMargin(0);

// ================= CONTENU =================
    headerCell.add(new Paragraph("LISTE DES CANDIDATS")
            .setBold()
            .setFontSize(11)
            .setMargin(0)
            .setTextAlignment(TextAlignment.CENTER));

    headerCell.add(new Paragraph("Etablissement : " + etablissementNom+ " (" + etablissementCode + ")")
            .setFontSize(11)
            .setMargin(0)
            .setTextAlignment(TextAlignment.CENTER));

    headerCell.add(new Paragraph("Ville du centre d'examen : " + centreExamenNom)
            .setFontSize(11)
            .setMargin(0)
            .setTextAlignment(TextAlignment.CENTER));

// ================= AJOUT TABLE =================
    headerTable1.addCell(headerCell);

    document.add(headerTable1);

    // ================= TABLE =================

    float[] widths = {
            1.2f, 0.8f, 1f, 0.8f,
            2.5f, 2f, 2f,
            0.8f, 1.5f, 2f,
            1.5f, 0.8f, 2.5f
    };

    Table table = new Table(UnitValue.createPercentArray(widths));
    table.setWidth(UnitValue.createPercentValue(100));
    table.setFixedLayout(); // 🔥 IMPORTANT pour supprimer warning

    String[] headers = {
            "Crt. Ecrit", "Jury", "N° Table", "Série",
            "Matière(s) Optionnelles", "Prénom(s)", "Nom",
            "Sexe", "Date naiss.", "Lieu naiss.",
            "Nationalité", "EPS", "Matière(S) Facultatives"
    };

    for (String h : headers) {
        table.addHeaderCell(
                new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(8))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
        );
    }

    // ================= DATA =================
    for (CandidatFinisResponse c : candidats) {

        table.addCell(cell(val(c.getCentreEcrit() != null ? c.getCentreEcrit().getCode() : "")));
        table.addCell(cell(val(c.getJury())));
        table.addCell(cell(val(c.getNumeroTable())));
        table.addCell(cell(val(c.getSerie())));
        table.addCell(cell(getOptional(c)));
        table.addCell(cell(val(c.getPrenoms())));
        table.addCell(cell(val(c.getNom())));
        table.addCell(cell(val(c.getSexe())));
        table.addCell(cell(val(c.getDateNaissance())));
        table.addCell(cell(val(c.getLieuNaissance())));
        table.addCell(cell(val(c.getNationalite())));
        table.addCell(cell(convertEpsValue(c.getEps())));
        table.addCell(cell(getFacultatives(c)));
    }
    document.add(table);

    // ================= FOOTER =================
    document.add(new Paragraph("\n"));
    document.add(new Paragraph("Nombre de candidats : " + candidats.size())
            .setFontSize(9));

    document.add(new Paragraph("\n"));
    document.add(new Paragraph("Legende :")
            .setFontSize(9)
            .setBold());

    Set<String> centresEcrit = new TreeSet<>();
    for (CandidatFinisResponse c : candidats) {
        if (c.getCentreEcrit() != null) {
            String code = c.getCentreEcrit().getCode();
            String name = c.getCentreEcrit().getName();

            if (code != null && !code.isEmpty()) {
                if (name != null && !name.isEmpty()) {
                    centresEcrit.add(code + " : " + name);
                } else {
                    centresEcrit.add(code);
                }
            } else if (name != null && !name.isEmpty()) {
                centresEcrit.add(name);
            }
        }
    }
    if (!centresEcrit.isEmpty()) {
        for (String centre : centresEcrit) {
            Paragraph bulletPoint = new Paragraph("• " + centre)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setBold()
                    .setFontColor(ColorConstants.DARK_GRAY);
            document.add(bulletPoint);
        }
    } else {
        Paragraph bulletPoint = new Paragraph("• Aucun centre d'écrit")
                .setFontSize(8)
                .setMarginLeft(10)
                .setFontColor(ColorConstants.DARK_GRAY);
        document.add(bulletPoint);
    }

    // ================= PAGINATION =================
    Canvas canvas = new Canvas(placeholder, pdfDoc);
    canvas.showTextAligned(
            String.valueOf(pdfDoc.getNumberOfPages()),
            0, -3, // Ajustez la position Y pour aligner avec le texte
            TextAlignment.LEFT
    );
    canvas.close();

    document.close();
    return outputStream.toByteArray();
} catch (Exception e) {
    log.error("❌ Erreur génération PDF candidats", e);
    throw new RuntimeException("Erreur génération PDF", e);
}

    }


    // ================= HELPERS =================

    private Cell cell(String value) {
        return new Cell()
                .add(new Paragraph(value == null ? "" : value)
                        .setFontSize(9)
                        .setMultipliedLeading(0.9f))
                .setPadding(2)
                .setTextAlignment(TextAlignment.CENTER);
    }
    // ================= HELPER POUR LES CENTRES D'ÉCRIT =================
    private String getCentresEcritLegende(List<CandidatFinisResponse> candidats) {
        Set<String> centresEcrit = new TreeSet<>();

        for (CandidatFinisResponse c : candidats) {
            if (c.getCentreEcrit() != null) {
                String code = c.getCentreEcrit().getCode();
                String name = c.getCentreEcrit().getName();

                if (code != null && !code.isEmpty()) {
                    if (name != null && !name.isEmpty()) {
                        centresEcrit.add(code + " : " + name);  // Format: CODE : NOM
                    } else {
                        centresEcrit.add(code);
                    }
                } else if (name != null && !name.isEmpty()) {
                    centresEcrit.add(name);
                }
            }
        }

        if (centresEcrit.isEmpty()) {
            return "Aucun centre d'écrit";
        }

        return "Centre(s) d'écrit : " + String.join(", ", centresEcrit);
    }
    private String convertEpsValue(String eps) {
        if (eps == null) return "";
        if ("A".equalsIgnoreCase(eps)) return "Apte";
        if ("I".equalsIgnoreCase(eps)) return "Inapte";
        return eps; // Au cas où il y aurait d'autres valeurs
    }
    private Paragraph center(String text, PdfFont font, boolean bold) {
        Paragraph p = new Paragraph(text)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        if (bold) p.setBold();
        return p;
    }

    private String val(String v) {
        return v != null ? v : "";
    }

    private String getOptional(CandidatFinisResponse c) {
        return Stream.of(c.getMo1(), c.getMo2(), c.getMo3())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    private String getFacultatives(CandidatFinisResponse c) {
        return Stream.of(c.getEf1(), c.getEf2())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }
}