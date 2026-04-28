package com.officedubac.project.models;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FooterEvent extends PdfPageEventHelper {

    private Font font;

    public FooterEvent(Font font) {
        this.font = font;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {

        PdfPTable footer = new PdfPTable(1);

        // Largeur dynamique (important pour paysage)
        float pageWidth = document.right() - document.left();
        footer.setTotalWidth(pageWidth);
        footer.setLockedWidth(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dateHeure = LocalDateTime.now().format(formatter);

        PdfPCell cell = new PdfPCell(
                new Phrase(
                        "Liste générée le : " + dateHeure +
                                " | Division de la Scolarité | © PortailBAC - Office du Baccalauréat, tous droits réservés.",
                        font
                )
        );

        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingTop(5f);

        footer.addCell(cell);

        // Position bien collée en bas
        footer.writeSelectedRows(
                0, -1,
                document.left(),
                document.bottom() - 10,
                writer.getDirectContent()
        );
    }
}
