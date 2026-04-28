package com.officedubac.project.controllers;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FooterHandler extends PdfPageEventHelper {

    private final Font footerFont;
    private final String user;
    private PdfTemplate totalPagesTemplate;
    private BaseFont baseFont;

    public FooterHandler(Font font, String user) {
        this.footerFont = font;
        this.user = user;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        totalPagesTemplate = writer.getDirectContent().createTemplate(30, 16);
        try {
            baseFont = footerFont.getBaseFont();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();

        try {
            PdfPTable footer = new PdfPTable(3);
            footer.setWidths(new int[]{1, 1, 1});
            footer.setTotalWidth(document.right() - document.left());
            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(12);
            footer.getDefaultCell().setBorder(Rectangle.TOP);

            String dateTimeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            PdfPCell leftCell = new PdfPCell(new Phrase("Liste éditée le : " + dateTimeNow + " via portailBAC", footerFont));
            leftCell.setBorder(Rectangle.TOP);
            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(leftCell);

            PdfPCell centerCell = new PdfPCell(new Phrase("Par : " + user, footerFont));
            centerCell.setBorder(Rectangle.TOP);
            centerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footer.addCell(centerCell);

            // Cellule alignée à droite : "Page X sur [total]"
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.TOP);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            // Ajout de la cellule au tableau footer
            footer.addCell(rightCell);

            footer.writeSelectedRows(0, -1, document.left(), document.bottom() + 10, cb);

            // Position verticale
            float textBase = document.bottom() - 1;
            // Écrire "Page X sur "
            String pageText = "Page " + writer.getPageNumber() + " sur ";
            float pageTextWidth = baseFont.getWidthPoint(pageText, footerFont.getSize());
            cb.beginText();
            cb.setFontAndSize(baseFont, footerFont.getSize());
            cb.setTextMatrix(document.right() - pageTextWidth - 15, textBase); // -20 pour marger un peu à droite
            cb.showText(pageText);
            cb.endText();
            // Ajouter le total juste après
            cb.addTemplate(totalPagesTemplate, document.right() - 15, textBase);

        }
        catch (DocumentException ex)
        {
            throw new ExceptionConverter(ex);
        }
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        totalPagesTemplate.beginText();
        totalPagesTemplate.setFontAndSize(baseFont, footerFont.getSize());
        totalPagesTemplate.showText(String.valueOf(writer.getPageNumber() - 1));
        totalPagesTemplate.endText();
    }
}