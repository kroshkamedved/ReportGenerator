package com.example.reportgenerator;

import lombok.Getter;
import lombok.Setter;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class ReactionSchemeSVGHandler {
    private static final float POINT_TO_MM_COEFFICIENT = 0.352777778f;
    private static final int DEFAULT_A4_PIXEL_WIDTH = 842;
    private static final float POINT_TO_PX_COEFFICIENT = DEFAULT_A4_PIXEL_WIDTH / PDRectangle.A4.getWidth();


    private PDRectangle pageFormat;
    private String SVGSource;
    private final SVGDocumentFactory factory;
    private SVGDocument svgDocument;
    private float originalWidth;
    private float originalHeight;

    public ReactionSchemeSVGHandler(PDRectangle pageFormat) {
        this.pageFormat = pageFormat;
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
        this.originalWidth = Float.parseFloat(documentElement.getAttribute("width"));
        this.originalHeight = Float.parseFloat(documentElement.getAttribute("height"));
        documentElement.setAttribute("width", pageFormat.getWidth() * POINT_TO_MM_COEFFICIENT + "mm");
        documentElement.setAttribute("height", pageFormat.getHeight() * POINT_TO_MM_COEFFICIENT + "mm");
        documentElement.setAttribute("preserveAspectRatio", "xMidYMin meet");
        this.SVGSource = DOMUtilities.getXML(svgDocument);
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

    public float calculateSVGHeight() {
        if (originalWidth > 0) {
            return (pageFormat.getWidth() / originalWidth * originalHeight) * POINT_TO_PX_COEFFICIENT;
        } else throw new RuntimeException("PROBLEM WITH SVG ALLOCATION");
    }
}
