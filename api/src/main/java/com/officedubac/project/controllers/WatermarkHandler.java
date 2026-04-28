package com.officedubac.project.controllers;

import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPageEventHelper;


import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class WatermarkHandler extends PdfPageEventHelper {

    private final Font watermarkFont;
    private final String watermarkText;

    public WatermarkHandler(String watermarkText) {
        this.watermarkText = watermarkText;
        this.watermarkFont = new Font(Font.HELVETICA, 60, Font.BOLD, new Color(255, 100, 100, 70)); // rouge clair transparent
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