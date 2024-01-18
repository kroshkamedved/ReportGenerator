package com.example.reportgenerator.util;

import com.example.reportgenerator.domain.AllFieldsToStringReady;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PDFUtil {
    /**
     * @param doc                PDF document where to print text
     * @param newLineYHeight     vertical position on the last document page where paragraph starts
     * @param font
     * @param fontSize
     * @param content
     * @param sideMargin
     * @param verticalPageMargin
     * @return Y height of the last line in the document
     */
    public static float drawParagraph(PDDocument doc, float newLineYHeight, PDFont font,
                                      float fontSize, String content,
                                      float sideMargin, float verticalPageMargin, boolean heading) {
        String[] words = content.split("\\s+");
        PDPage lastPage = doc.getPage(doc.getNumberOfPages() - 1);
        float rowHeight = 0;
        try {
            PDPageContentStream contentStream = new PDPageContentStream(doc, lastPage, PDPageContentStream.AppendMode.APPEND, true);
            contentStream.setFont(font, fontSize);
            contentStream.beginText();
            newLineYHeight = newLineYHeight - verticalPageMargin;
            contentStream.newLineAtOffset(sideMargin, newLineYHeight);
            float lineMaxWidth = lastPage.getMediaBox().getWidth() - 2 * sideMargin;
            boolean firstWord = true;
            StringBuilder stringBuilder = new StringBuilder();

            float cellMargin = ((font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * fontSize) * 0.5f;
            rowHeight = (font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * fontSize + cellMargin;


            for (String word : words) {
                if (newLineYHeight < verticalPageMargin + rowHeight || heading && (newLineYHeight < verticalPageMargin + 2 * rowHeight)) {
                    PDPage newPage = new PDPage(lastPage.getMediaBox());
                    doc.addPage(newPage);
                    contentStream.close();
                    contentStream = new PDPageContentStream(doc, newPage, PDPageContentStream.AppendMode.APPEND, true);
                    newLineYHeight = newPage.getMediaBox().getHeight() - verticalPageMargin;
                    contentStream.setFont(font, fontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(sideMargin, newLineYHeight);
                }
                if (firstWord && (font.getStringWidth(word + "\s".repeat(4)) / 1000) * fontSize < lineMaxWidth) {
                    stringBuilder.append("\s".repeat(4)).append(word).append("\s");
                    firstWord = false;
                    continue;
                } else if ((font.getStringWidth(stringBuilder + " " + word) / 1000) * fontSize > lineMaxWidth) {
                    contentStream.newLineAtOffset(0, -rowHeight);
                    contentStream.showText(stringBuilder.toString());
                    newLineYHeight = newLineYHeight - rowHeight;
                    stringBuilder.setLength(0);
                }
                stringBuilder.append(word).append('\s');
            }
            contentStream.newLineAtOffset(0, -rowHeight);
            contentStream.showText(stringBuilder.toString());
            contentStream.endText();
            contentStream.close();
        } catch (IOException e) {
            LoggerFactory.getLogger(PDFUtil.class).error("error during paragraph printing");
        }
        return newLineYHeight;
    }

    public static <T extends Record & AllFieldsToStringReady> float drawSVGStructure(PDDocument doc, float startX, String content, float sideMargin, float verticalMargin, float maxWidth, float newLineYHeight, TableGenerator<T> tableGenerator, float minHeight) {
        PDPage lastPage = doc.getPage(doc.getNumberOfPages() - 1);
        PDFTranscoder pdfTranscoder = new PDFTranscoder();
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(content.getBytes()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);
        try {
            pdfTranscoder.transcode(input, output);
            PDDocument transcodedSvgToPDF = Loader.loadPDF(outputStream.toByteArray());
            PDPage pageWithSVG = transcodedSvgToPDF.getPage(0);
            PDFormXObject object = new PDFormXObject(new PDStream(transcodedSvgToPDF, pageWithSVG.getContents()));
            object.setResources(pageWithSVG.getResources());
            object.setBBox(pageWithSVG.getBBox());
            AffineTransform matrix = object.getMatrix().createAffineTransform();
            float svgWidth = pageWithSVG.getMediaBox().getWidth();
            float scaleX = maxWidth / (svgWidth);
            float scaleY = 1;
            if (scaleX > 1) {
                scaleX = 1;
            } else {
                scaleY = scaleX;
            }
            float currentRowHeight = pageWithSVG.getBBox().getHeight() * scaleY;
            float originalScaledHeight = currentRowHeight;
            currentRowHeight = Math.max(currentRowHeight, minHeight);

            newLineYHeight = newLineYHeight - currentRowHeight - (2 * verticalMargin);
            float shift = (maxWidth - (pageWithSVG.getMediaBox().getWidth() * scaleX)) / 2;
            matrix.translate(startX + shift + sideMargin, newLineYHeight + ((currentRowHeight - originalScaledHeight) / 2) + verticalMargin);
            matrix.scale(scaleX, scaleY);
            object.setMatrix(matrix);
            object.setFormType(1);

            PDPageContentStream stream = new PDPageContentStream(doc, lastPage, PDPageContentStream.AppendMode.APPEND, true);
            stream.drawForm(object);
            stream.setStrokingColor(Color.black);
            stream.setLineWidth(0.125f);
            stream.addRect(startX, newLineYHeight, maxWidth + sideMargin * 2, currentRowHeight + verticalMargin * 2);
            stream.stroke();
            stream.close();
        } catch (IOException | TranscoderException e) {
            LoggerFactory.getLogger(PDFUtil.class).error("error during paragraph printing");
        }
        return newLineYHeight;
    }
}
