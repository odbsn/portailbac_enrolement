package com.officedubac.project.module.candidatFinis;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

public class HeaderHandler implements IEventHandler {

    private final String etablissementNom;
    private final String etablissementCode;
    private final String centreExamenNom;

    public HeaderHandler(String etablissementNom,String etablissementCode, String centreExamenNom) {
        this.etablissementNom = etablissementNom;
        this.etablissementCode = etablissementCode;
        this.centreExamenNom = centreExamenNom;
    }

    @Override
    public void handleEvent(Event event) {

        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();

        int pageNumber = pdfDoc.getPageNumber(page);

        // ================= EXCLURE PAGE 1 =================
        if (pageNumber == 1) {
            return;
        }

        Rectangle pageSize = page.getPageSize();
        float x = pageSize.getWidth() / 2;
        float y = pageSize.getTop() - 20;

        Canvas canvas = new Canvas(page, pageSize);

        // ================= TITRE =================
        canvas.showTextAligned(
                new Paragraph("LISTE DES CANDIDATS")
                        .setBold()
                        .setFontSize(11)
                        .setMarginBottom(11),
                x,
                y,
                TextAlignment.CENTER
        );

        // ================= ETABLISSEMENT =================
        canvas.showTextAligned(
                new Paragraph("Etablissement : " + etablissementNom+ " (" + etablissementCode + ")")
                        .setFontSize(11)
                        .setMarginBottom(11),
                x,
                y - 18,
                TextAlignment.CENTER
        );

        // ================= CENTRE =================
        canvas.showTextAligned(
                new Paragraph("Ville du centre d'examen : " + centreExamenNom)
                        .setFontSize(11)
                        .setMarginBottom(11),
                x,
                y - 36,
                TextAlignment.CENTER
        );

        canvas.close();
    }
}
