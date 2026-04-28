package com.officedubac.project.module.candidatFinis;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FooterHandler implements IEventHandler {

    private final String centreNom;
    private final String session;
    private final PdfFormXObject placeholder;

    public FooterHandler(PdfFormXObject placeholder,
                         String centreNom,
                         String session) {
        this.placeholder = placeholder;
        this.centreNom = centreNom;
        this.session = session;
    }

    @Override
    public void handleEvent(Event event) {

        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateEdition = LocalDate.now().format(formatter);

        int pageNumber = pdfDoc.getPageNumber(page);

        Rectangle pageSize = page.getPageSize();
        float y = pageSize.getBottom() + 10;

        Canvas canvas = new Canvas(page, pageSize);
        // ================= GAUCHE =================
        canvas.showTextAligned(
                new Paragraph("Edition du : " + dateEdition)
                        .setFontSize(8),
                pageSize.getLeft() + 40,
                y,
                TextAlignment.LEFT
        );
        // ================= CENTRE =================
        canvas.showTextAligned(
                new Paragraph(
                        "BACCALAUREAT DE L'ENSEIGNEMENT SECONDAIRE\n" +
                                "ANNEE " + LocalDate.now().getYear() + " - SESSION " + session
                )
                        .setFontSize(9)
                        .setBold(),
                pageSize.getWidth() / 2,
                y,
                TextAlignment.CENTER
        );
        Paragraph footerRight = new Paragraph("Page " + pageNumber + " / ")
                .setFontSize(10);
        Image image = new Image(placeholder);
        image.setWidth(20); // Largeur fixe pour éviter les déformations
        image.setHeight(10); // Hauteur fixe
        footerRight.add(image);

        canvas.showTextAligned(
                footerRight,
                pageSize.getRight() - 40,
                y,
                TextAlignment.RIGHT
        );

        canvas.close();
    }
}