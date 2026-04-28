package com.officedubac.project.controllers;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;

public class WatermarkHandler2 extends PdfPageEventHelper {

    private final Font watermarkFont;
    private final String watermarkText;

    public WatermarkHandler2(String watermarkText) {
        this.watermarkText = watermarkText;
        this.watermarkFont = new Font(Font.HELVETICA, 60, Font.BOLD, new Color(100, 255, 100, 70)); // vert clair transparent
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte canvas = writer.getDirectContentUnder(); // "sous" le contenu
        Phrase watermark = new Phrase(watermarkText, watermarkFont);

        ColumnText.showTextAligned(
                canvas,
                Element.ALIGN_CENTER,
                watermark,
                (document.getPageSize().getWidth()) / 2,
                (document.getPageSize().getHeight()) / 2,
                45 // rotation du texte
        );
    }
}