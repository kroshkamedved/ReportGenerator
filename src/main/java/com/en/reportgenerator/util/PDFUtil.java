package com.en.reportgenerator.util;

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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PDFUtil {
    private static final float DEFAULT_BORDER_LINE_WIDTH = 0.125f;

    /**
     * @param doc                The PDF document where the text will be printed
     * @param currentYTopValue   The vertical position on the last page of the document where the paragraph starts
     * @param font               The font to be used for printing the text
     * @param fontSize           The size of the font to be used for printing the text
     * @param content            The content of the paragraph to be printed
     * @param sideMargin         The margin to be left on the sides of the page
     * @param verticalPageMargin The margin to be left at the top and bottom of the page
     * @param heading            A boolean value indicating whether the content is a single-line heading or a multi-line paragraph
     * @return The height (in the Y-direction) of the last line of the paragraph in the document
     */
    public static float printTextContent(PDDocument doc, float currentYTopValue, PDFont font,
                                         float fontSize, String content,
                                         float sideMargin, float verticalPageMargin, boolean heading) {
        String[] words = content.split("\\s+");
        PDPage lastPage = doc.getPage(doc.getNumberOfPages() - 1);
        float rowHeight = 0;
        PDPageContentStream contentStream = null;
        try {
            contentStream = new PDPageContentStream(doc, lastPage, PDPageContentStream.AppendMode.APPEND, true);
            contentStream.setFont(font, fontSize);
            contentStream.beginText();
            currentYTopValue = currentYTopValue - verticalPageMargin / 2;
            contentStream.newLineAtOffset(sideMargin, currentYTopValue);
            float lineMaxWidth = lastPage.getMediaBox().getWidth() - 2 * sideMargin;
            boolean firstWord = true;
            StringBuilder stringBuilder = new StringBuilder();

            float cellMargin = ((font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * fontSize) * 0.5f;
            rowHeight = (font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * fontSize + cellMargin;


            for (String word : words) {
                if (currentYTopValue < verticalPageMargin + rowHeight || heading && (currentYTopValue < verticalPageMargin + 2 * rowHeight)) {
                    PDPage newPage = new PDPage(lastPage.getMediaBox());
                    doc.addPage(newPage);
                    contentStream.endText();
                    contentStream.close();
                    contentStream = new PDPageContentStream(doc, newPage, PDPageContentStream.AppendMode.APPEND, true);
                    currentYTopValue = newPage.getMediaBox().getHeight() - verticalPageMargin;
                    contentStream.setFont(font, fontSize);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(sideMargin, currentYTopValue);
                }
                if (firstWord && (font.getStringWidth(word + "\s".repeat(4)) / 1000) * fontSize < lineMaxWidth) {
                    stringBuilder.append("\s".repeat(4)).append(word).append("\s");
                    firstWord = false;
                    continue;
                } else if ((font.getStringWidth(stringBuilder + " " + word) / 1000) * fontSize > lineMaxWidth) {
                    contentStream.newLineAtOffset(0, -rowHeight);
                    contentStream.showText(stringBuilder.toString());
                    currentYTopValue = currentYTopValue - rowHeight;
                    stringBuilder.setLength(0);
                }
                stringBuilder.append(word).append('\s');
            }
            contentStream.newLineAtOffset(0, -rowHeight);
            contentStream.showText(stringBuilder.toString());
            contentStream.endText();
            contentStream.close();
        } catch (IOException e) {
            throw new RuntimeException("error during paragraph printing");
        } finally {
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return currentYTopValue;
    }

    /**
     * @param doc              The PDF document where the SVG will be printed
     * @param startX           The horizontal position on the last page of the document where the SVG starts, counting from the left side
     * @param content          A string that contains the SVG
     * @param sidePadding      The padding to be left on the sides of the SVG
     * @param verticalPadding  The padding to be left at the top and bottom of the SVG
     * @param maxWidth         The maximum acceptable width of the resulting SVG
     * @param currentYTopValue The vertical position on the last page of the document where the SVG starts
     * @param minHeight        The minimum height of the resulting SVG
     * @return The height (in the Y-direction) from the bottom of the page to SVG beginning
     */
    public static float drawSVG(PDDocument doc, float startX, String content,
                                float sidePadding, float verticalPadding, float maxWidth,
                                float currentYTopValue, float minHeight) {
        int lastPageIndex = doc.getNumberOfPages() - 1;
        PDPage lastPage = doc.getPage(lastPageIndex);
        PDFTranscoder pdfTranscoder = new PDFTranscoder();
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(content.getBytes()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);
        PDPageContentStream stream = null;
        PDDocument transcodedSvgToPDF = null;
        float currentRowHeight = minHeight;
        try {
            if (!content.isEmpty()) {
                pdfTranscoder.transcode(input, output);
                transcodedSvgToPDF = Loader.loadPDF(outputStream.toByteArray());
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
                currentRowHeight = pageWithSVG.getBBox().getHeight() * scaleY;
                float originalScaledHeight = currentRowHeight;
                currentRowHeight = Math.max(currentRowHeight, minHeight);

                currentYTopValue = currentYTopValue - currentRowHeight - (2 * verticalPadding);
                float shift = (maxWidth - (pageWithSVG.getMediaBox().getWidth() * scaleX)) / 2;
                matrix.translate(startX + shift + sidePadding, currentYTopValue + ((currentRowHeight - originalScaledHeight) / 2) + verticalPadding);
                matrix.scale(scaleX, scaleY);
                object.setMatrix(matrix);
                object.setFormType(1);

                stream = new PDPageContentStream(doc, lastPage, PDPageContentStream.AppendMode.APPEND, true);
                stream.drawForm(object);
            } else {
                stream = new PDPageContentStream(doc, lastPage, PDPageContentStream.AppendMode.APPEND, true);
                currentYTopValue = currentYTopValue - currentRowHeight - (2 * verticalPadding);
            }
            stream.setStrokingColor(Color.black);
            stream.setLineWidth(DEFAULT_BORDER_LINE_WIDTH);
            stream.addRect(startX, currentYTopValue, maxWidth + sidePadding * 2, currentRowHeight + verticalPadding * 2);
            stream.stroke();
            outputStream.flush();
            outputStream.close();
            stream.close();
        } catch (IOException | TranscoderException e) {
            throw new RuntimeException("error during svg printing");
        } finally {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
        return currentYTopValue;
    }
}
