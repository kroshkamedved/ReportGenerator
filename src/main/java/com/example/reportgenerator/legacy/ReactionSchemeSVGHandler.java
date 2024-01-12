package com.example.reportgenerator.legacy;

import lombok.Getter;
import lombok.Setter;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class ReactionSchemeSVGHandler {
    private static final float POINT_TO_MM_COEFFICIENT = 0.352777778f;
    private static final int DEFAULT_A4_PIXEL_WIDTH = 842;
    private static final float POINT_TO_PX_COEFFICIENT = DEFAULT_A4_PIXEL_WIDTH / PDRectangle.A4.getWidth();
    private final float paddingXInPoints;
    private final float paddingYInPoints;
    private final float schemaMaxHeight;


    private PDRectangle pageFormat;
    private String SVGSource;
    private final SVGDocumentFactory factory;
    private SVGDocument svgDocument;
    private float originalWidth;
    private float originalHeight;
    private float schemaMaxWidth;

    public ReactionSchemeSVGHandler(PDRectangle pageFormat, float paddingXInPoints, float paddingYInPoints) {
        this.paddingXInPoints = paddingXInPoints;
        this.paddingYInPoints = paddingYInPoints;
        this.pageFormat = pageFormat;
        schemaMaxWidth = pageFormat.getWidth() - (2 * paddingXInPoints);
        schemaMaxHeight = pageFormat.getHeight() - (2 * paddingXInPoints);
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        this.factory = new SAXSVGDocumentFactory(parser);
    }

    public void setSVGSource(String SVGSource) throws IOException {
        svgDocument = factory.createSVGDocument(null, new StringReader(SVGSource));
        this.SVGSource = SVGSource;
        adjustSVG();
    }

    private void adjustSVG() {
        Element documentElement = svgDocument.getDocumentElement();
        this.originalWidth = getSizeAttributeValue(documentElement.getAttribute("width"));
        this.originalHeight = getSizeAttributeValue(documentElement.getAttribute("height"));
        if (originalHeight > (pageFormat.getHeight() - 2 * paddingYInPoints) || originalWidth > (pageFormat.getWidth() - 2 * paddingXInPoints)) {
            System.out.println("empty space");
        }
        documentElement.setAttribute("preserveAspectRatio", "xMidYMin meet");
        documentElement.setAttribute("height", pageFormat.getHeight() * POINT_TO_MM_COEFFICIENT + "mm");
        documentElement.setAttribute("width", pageFormat.getWidth() * POINT_TO_MM_COEFFICIENT + "mm");
        this.SVGSource = DOMUtilities.getXML(svgDocument);
    }

    private float getSizeAttributeValue(String value) {
        Pattern pattern = Pattern.compile("\\b\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return Float.parseFloat(matcher.group()) / POINT_TO_PX_COEFFICIENT;
        } else {
            throw new RuntimeException("PROBLEM WITH SVG ALLOCATION");
        }
    }

    public PDDocument createPDPageWithSVG() throws TranscoderException, IOException {
        PDFTranscoder transcoder = new PDFTranscoder();
        TranscoderInput transcoderInput = new TranscoderInput(new ByteArrayInputStream(SVGSource.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, pageFormat.getWidth());
        // transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, pageFormat.getHeight());
        transcoder.transcode(transcoderInput, new TranscoderOutput(outputStream));
        return Loader.loadPDF(outputStream.toByteArray());
    }
}
