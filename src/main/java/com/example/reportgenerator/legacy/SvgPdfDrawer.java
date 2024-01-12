package com.example.reportgenerator.legacy;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.svg.PDFTranscoder;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.net.MalformedURLException;

public class SvgPdfDrawer {
    private SVGGraphics2D svgGraphics2D;
    private BridgeContext ctx;
    private GVTBuilder builder;

    Float documentWidth = 100f;
    Float documentHeight = 200f;
    Float coef = 1f;
    private SAXSVGDocumentFactory factory;

    public SvgPdfDrawer(float width, float height) {
        initialize();
        coef = coef == 0 ? 1 : coef;
        documentWidth = width / coef;
        documentHeight = height / coef;

    }

    private void initialize() {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNamespaceURI = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNamespaceURI, "svg", null);

        SVGGeneratorContext sVGGeneratorContext = SVGGeneratorContext.createDefault(document);
        svgGraphics2D = new SVGGraphics2D(sVGGeneratorContext, false);

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        factory = new SAXSVGDocumentFactory(parser);

        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);
        ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);

        // System.out.println(userAgent.getLanguages());
        // System.out.println(userAgent.getDefaultFontFamily());
        // System.out.println(userAgent.getPixelUnitToMillimeter());
        coef = userAgent.getPixelUnitToMillimeter();
        builder = new GVTBuilder();
    }

    public void addSvgImage(String uri, InputStream is, float xPosition, float yPosition)
            throws MalformedURLException, IOException {
        // SVGDocument svgDocument = factory.createSVGDocument(svgFile.toURI()
        // .toURL().toString());
        Document svgDocument = factory.createSVGDocument(uri, is);
        // String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        // DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        // Document svgDocument = impl.createDocument(svgNS, "svg", null);

        GraphicsNode mapGraphics = builder.build(ctx, svgDocument);

        AffineTransform transformer = new AffineTransform();
        transformer.translate(xPosition, yPosition);

        mapGraphics.setTransform(transformer);
        mapGraphics.paint(svgGraphics2D);
    }

    public void addTextLine(String text, Font font, float xPosition, float yPosition) {
        svgGraphics2D.setFont(font);
        svgGraphics2D.drawString(text, xPosition, yPosition);
    }

    public ByteArrayOutputStream getPDFStream() throws IOException, TranscoderException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer out = new OutputStreamWriter(outputStream, "UTF-8");
        svgGraphics2D.stream(out, true);
        String svg = new String(outputStream.toByteArray(), "UTF-8");
        return transcode(svg);
    }

    private ByteArrayOutputStream transcode(String svg) throws TranscoderException {
        TranscoderInput input = new TranscoderInput(new StringReader(svg));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TranscoderOutput transOutput = new TranscoderOutput(output);
        PDFTranscoder transcoder = new PDFTranscoder();
        // transcoder.setL
        // Logger l = Logger.getLogger(PDFTranscoder.class);
        Log log = LogFactory.getLog(PDFTranscoder.class);
        ;
        transcoder.setLogger(log);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, documentWidth);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, documentHeight);
        // System.out.println("WH "+documentWidth + "\t"+documentHeight);
        // System.out.println(svg);
        transcoder.transcode(input, transOutput);

        return output;
    }
}
