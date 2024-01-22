package com.en.reportgenerator.service;

import com.en.reportgenerator.domain.AllFieldsToStringReady;
import com.en.reportgenerator.domain.Compound;
import com.en.reportgenerator.domain.Experiment;
import com.en.reportgenerator.dto.ReportDTO;
import com.en.reportgenerator.util.PDFUtil;
import com.en.reportgenerator.util.TableGenerator;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class ReportService {

    private static final PDFont DEFAULT_DOC_FONT = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
    private static final float DEFAULT_HEADING_FONT_SIZE = 9;
    private static final float DEFAULT_PAGE_SIDE_MARGIN = 20f;
    private static final float DEFAULT_PAGE_VERTICAL_MARGIN = 30f;
    private static final float TAB_SPACE = 5f;
    private static final float DEFAULT_VERTICAL_PAGE_MARGIN = PDRectangle.A4.getHeight() / 25;

    private static final float DEFAULT_TABLE_FONT_SIZE = 8;


    public PDDocument generatePDF(ReportDTO reportData) {
        Experiment experiment = reportData.getExperiment();
        List<Compound> reactants = reportData.getReactants();
        List<Compound> reagents = reportData.getReagents();
        List<Compound> products = reportData.getProducts();

        String reactionSchemaSVG = experiment.svg();
        String reactionProcedure = experiment.comment();
        int experimentId = experiment.id();

        PDDocument document = new PDDocument();
        PDRectangle pageFormat = PDRectangle.A4;
        PDPage firstPage = new PDPage(pageFormat);
        document.addPage(firstPage);
        float lastLineYHeight = drawExperimentHeading(document, firstPage, experimentId, pageFormat);

        lastLineYHeight = drawReactionSchema(reactionSchemaSVG, document, lastLineYHeight, pageFormat);
        lastLineYHeight = drawTable(reactants, document, lastLineYHeight, "REACTANTS TABLE:", pageFormat);

        lastLineYHeight = drawTable(reagents, document, lastLineYHeight, "REAGENTS TABLE:", pageFormat);

        lastLineYHeight = drawTable(products, document, lastLineYHeight, "PRODUCTS TABLE:", pageFormat);

        lastLineYHeight = PDFUtil.printTextContent(document, lastLineYHeight, DEFAULT_DOC_FONT, DEFAULT_HEADING_FONT_SIZE, "PROCEDURE:", DEFAULT_PAGE_SIDE_MARGIN + TAB_SPACE, DEFAULT_VERTICAL_PAGE_MARGIN, true);

        String fontPath = "src/main/resources/static/Roboto-LightItalic.ttf";
        PDFont font = DEFAULT_DOC_FONT;
        try {
            font = PDType0Font.load(document, new File(fontPath));
        } catch (IOException e) {
            LogFactory.getLog(this.getClass()).error("problem with procedure font loading");
        }
        PDFUtil.printTextContent(document, lastLineYHeight, font, DEFAULT_TABLE_FONT_SIZE, reactionProcedure, DEFAULT_PAGE_SIDE_MARGIN, DEFAULT_VERTICAL_PAGE_MARGIN, false);
        return document;
    }

    private <T extends Record & AllFieldsToStringReady> float drawTable(List<T> tableRows, PDDocument document, float lastLineYHeight, String tableHeading, PDRectangle pageFormat) {
        if (tableRows.isEmpty()) {
            return lastLineYHeight;
        }
        Class<T> clazz = (Class<T>) tableRows.get(0).getClass();
        lastLineYHeight = PDFUtil.printTextContent(document, lastLineYHeight, DEFAULT_DOC_FONT, DEFAULT_HEADING_FONT_SIZE, tableHeading, DEFAULT_PAGE_SIDE_MARGIN + TAB_SPACE, DEFAULT_VERTICAL_PAGE_MARGIN, true);
        TableGenerator<T> tableGenerator = new TableGenerator<>(tableRows, clazz, document, pageFormat.getHeight() - lastLineYHeight);
        return tableGenerator.createTable(pageFormat, DEFAULT_TABLE_FONT_SIZE);
    }

    private float drawReactionSchema(String reactionSchemaSVG, PDDocument document, float lastLineYHeight, PDRectangle pageFormat) {
        if (Objects.isNull(reactionSchemaSVG) || reactionSchemaSVG.isEmpty()) {
            return lastLineYHeight;
        }
        float maxWidth = pageFormat.getWidth() - 4 * DEFAULT_PAGE_SIDE_MARGIN;
        float minHeight = -1;
        return PDFUtil.drawSVG(document, DEFAULT_PAGE_SIDE_MARGIN, reactionSchemaSVG, DEFAULT_PAGE_SIDE_MARGIN, DEFAULT_PAGE_VERTICAL_MARGIN, maxWidth, lastLineYHeight, minHeight);
    }

    private float drawExperimentHeading(PDDocument document, PDPage firstPage, int expId, PDRectangle pageFormat) {

        try {
            PDPageContentStream str = new PDPageContentStream(document, firstPage, PDPageContentStream.AppendMode.APPEND, true);
            str.setFont(DEFAULT_DOC_FONT, DEFAULT_HEADING_FONT_SIZE);
            str.beginText();
            str.newLineAtOffset(DEFAULT_PAGE_SIDE_MARGIN, pageFormat.getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN + TAB_SPACE);
            str.showText("Experiment id : " + expId);
            str.endText();
            str.close();
            return firstPage.getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN - DEFAULT_DOC_FONT.getBoundingBox().getHeight() / 1000 * DEFAULT_HEADING_FONT_SIZE;
        } catch (IOException e) {
            throw new RuntimeException("problem with printing experiment data");
        }
    }
}
