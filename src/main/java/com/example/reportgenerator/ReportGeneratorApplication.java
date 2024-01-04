package com.example.reportgenerator;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootApplication
public class ReportGeneratorApplication {
    /*private static String content = """
            <?xml version="1.0" standalone="no" ?>
            <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
            <svg width="30px" height="34px" viewBox="0 0 30 34" style="background-color: #ffffffff" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" >
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -155 -159)" d="M 3156.58,3660.54 L 3144.58,3667.46 L 3144.58,3372.54 L 3156.58,3379.46 L 3156.58,3660.54 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -155 -159)" d="M 3208.42,3634.07 L 3196.42,3634.07 L 3196.42,3405.93 L 3208.42,3405.93 L 3208.42,3634.07 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -155 -159)" d="M 3400,3801.07 L 3400,3814.93 L 3144.58,3667.46 L 3156.58,3660.54 L 3400,3801.07 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -155 -159)" d="M 3643.42,3660.54 L 3655.42,3667.46 L 3400,3814.93 L 3400,3801.07 L 3643.42,3660.54 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -155 -159)" d="M 3594.58,3628.87 L 3600.58,3639.27 L 3403,3753.34 L 3397,3742.94 L 3594.58,3628.87 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -155 -159)" d="M 3643.42,3379.46 L 3655.42,3372.54 L 3655.42,3667.46 L 3643.42,3660.54 L 3643.42,3379.46 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -155 -159)" d="M 3400,3238.93 L 3400,3225.07 L 3655.42,3372.54 L 3643.42,3379.46 L 3400,3238.93 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -155 -159)" d="M 3397,3297.06 L 3403,3286.66 L 3600.58,3400.73 L 3594.58,3411.13 L 3397,3297.06 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -155 -159)" d="M 3156.58,3379.46 L 3144.58,3372.54 L 3400,3225.07 L 3400,3238.93 L 3156.58,3379.46 Z " />
            </svg>

            """;*/
    private static String content = """
            <?xml version="1.0" standalone="no" ?>
            <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
            <svg width="902px" height="338px" viewBox="0 0 902 338" style="background-color: #ffffffff" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" >
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 8589.53,4093.61 L 8577.53,4114.39 L 8577.53,3805.61 L 8589.53,3826.39 L 8589.53,4093.61 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 8820.94,3960 L 8844.94,3960 L 8577.53,4114.39 L 8589.53,4093.61 L 8820.94,3960 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 8589.53,3826.39 L 8577.53,3805.61 L 8844.94,3960 L 8820.94,3960 L 8589.53,3826.39 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 3156.58,3660.54 L 3144.58,3667.46 L 3144.58,3372.54 L 3156.58,3379.46 L 3156.58,3660.54 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 3208.42,3634.07 L 3196.42,3634.07 L 3196.42,3405.93 L 3208.42,3405.93 L 3208.42,3634.07 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 3400,3801.07 L 3400,3814.93 L 3144.58,3667.46 L 3156.58,3660.54 L 3400,3801.07 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 3643.42,3660.54 L 3655.42,3667.46 L 3400,3814.93 L 3400,3801.07 L 3643.42,3660.54 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 3594.58,3628.87 L 3600.58,3639.27 L 3403,3753.34 L 3397,3742.94 L 3594.58,3628.87 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 3643.42,3379.46 L 3655.42,3372.54 L 3655.42,3667.46 L 3643.42,3660.54 L 3643.42,3379.46 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 3400,3238.93 L 3400,3225.07 L 3655.42,3372.54 L 3643.42,3379.46 L 3400,3238.93 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 3397,3297.06 L 3403,3286.66 L 3600.58,3400.73 L 3594.58,3411.13 L 3397,3297.06 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 3156.58,3379.46 L 3144.58,3372.54 L 3400,3225.07 L 3400,3238.93 L 3156.58,3379.46 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 6914.61,3526 L 4893.33,3526 L 4893.33,3514 L 6914.61,3514 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7149.86,3520 C 7149.86,3520 6879.86,3588.5 6879.86,3588.5 C 6879.86,3588.5 6913.61,3549.97 6913.61,3520 C 6913.61,3489.59 6879.86,3450.5 6879.86,3450.5 C 6879.86,3450.5 7149.86,3520 7149.86,3520 C 7149.86,3520 7149.86,3520 7149.86,3520 " />
            <path stroke="#000000" stroke-width="1" fill="transparent" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7149.86,3520 C 7149.86,3520 6879.86,3588.5 6879.86,3588.5 C 6879.86,3588.5 6913.61,3549.97 6913.61,3520 C 6913.61,3489.59 6879.86,3450.5 6879.86,3450.5 C 6879.86,3450.5 7149.86,3520 7149.86,3520 C 7149.86,3520 7149.86,3520 7149.86,3520 " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 14284.8,3632.67 L 9800,3632.67 L 9800,3620.67 L 14284.8,3620.67 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 14520,3626.67 C 14520,3626.67 14250,3695.17 14250,3695.17 C 14250,3695.17 14283.8,3656.63 14283.8,3626.67 C 14283.8,3596.26 14250,3557.17 14250,3557.17 C 14250,3557.17 14520,3626.67 14520,3626.67 C 14520,3626.67 14520,3626.67 14520,3626.67 " />
            <path stroke="#000000" stroke-width="1" fill="transparent" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 14520,3626.67 C 14520,3626.67 14250,3695.17 14250,3695.17 C 14250,3695.17 14283.8,3656.63 14283.8,3626.67 C 14283.8,3596.26 14250,3557.17 14250,3557.17 C 14250,3557.17 14520,3626.67 14520,3626.67 C 14520,3626.67 14520,3626.67 14520,3626.67 " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 15437.1,3384.41 L 15431.3,3398.34 L 15277.8,3132.49 L 15302.6,3151.43 L 15437.1,3384.41 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 15712.8,3312.18 L 15712.8,3324.59 L 15431.3,3398.34 L 15437.1,3384.41 L 15712.8,3312.18 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 15977.4,3381.91 L 16002.1,3400.85 L 15712.8,3324.59 L 15712.8,3312.18 L 15977.4,3381.91 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 15842.9,3148.93 L 15848.7,3135 L 16002.1,3400.85 L 15977.4,3381.91 L 15842.9,3148.93 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 15567.1,3221.13 L 15567.2,3208.72 L 15848.7,3135 L 15842.9,3148.93 L 15567.1,3221.13 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 15302.6,3151.43 L 15277.8,3132.49 L 15567.2,3208.72 L 15567.1,3221.13 L 15302.6,3151.43 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 16397.1,4384.41 L 16391.3,4398.34 L 16237.8,4132.49 L 16262.6,4151.43 L 16397.1,4384.41 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 16672.8,4312.18 L 16672.8,4324.59 L 16391.3,4398.34 L 16397.1,4384.41 L 16672.8,4312.18 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 16937.4,4381.91 L 16962.1,4400.85 L 16672.8,4324.59 L 16672.8,4312.18 L 16937.4,4381.91 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 16802.9,4148.93 L 16808.7,4135 L 16962.1,4400.85 L 16937.4,4381.91 L 16802.9,4148.93 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 16527.1,4221.13 L 16527.2,4208.72 L 16808.7,4135 L 16802.9,4148.93 L 16527.1,4221.13 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 16262.6,4151.43 L 16237.8,4132.49 L 16527.2,4208.72 L 16527.1,4221.13 L 16262.6,4151.43 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 17783.8,3891.08 L 17778,3905.01 L 17624.5,3639.16 L 17649.3,3658.1 L 17783.8,3891.08 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 18059.5,3818.85 L 18059.5,3831.26 L 17778,3905.01 L 17783.8,3891.08 L 18059.5,3818.85 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 18324,3888.57 L 18348.8,3907.52 L 18059.5,3831.26 L 18059.5,3818.85 L 18324,3888.57 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 18189.5,3655.59 L 18195.3,3641.67 L 18348.8,3907.52 L 18324,3888.57 L 18189.5,3655.59 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 17913.8,3727.8 L 17913.8,3715.39 L 18195.3,3641.67 L 18189.5,3655.59 L 17913.8,3727.8 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 17649.3,3658.1 L 17624.5,3639.16 L 17913.8,3715.39 L 17913.8,3727.8 L 17649.3,3658.1 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 570.479,3664.41 L 564.665,3678.34 L 411.176,3412.49 L 435.968,3431.43 L 570.479,3664.41 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 846.182,3592.18 L 846.173,3604.59 L 564.665,3678.34 L 570.479,3664.41 L 846.182,3592.18 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 1110.69,3661.91 L 1135.48,3680.85 L 846.173,3604.59 L 846.182,3592.18 L 1110.69,3661.91 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 976.201,3428.93 L 982.016,3415 L 1135.48,3680.85 L 1110.69,3661.91 L 976.201,3428.93 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 700.478,3501.13 L 700.488,3488.72 L 982.016,3415 L 976.201,3428.93 L 700.478,3501.13 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 435.968,3431.43 L 411.176,3412.49 L 700.488,3488.72 L 700.478,3501.13 L 435.968,3431.43 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7101.13,6766.31 L 7089.13,6775.03 L 7089.13,6478.31 L 7101.13,6487.03 L 7101.13,6766.31 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7152.97,6733 L 7140.97,6733 L 7140.97,6520.33 L 7152.97,6520.33 L 7152.97,6733 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7366.75,6852.61 L 7371.33,6866.72 L 7089.13,6775.03 L 7101.13,6766.31 L 7366.75,6852.61 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7530.9,6626.67 L 7545.74,6626.67 L 7371.33,6866.72 L 7366.75,6852.61 L 7530.9,6626.67 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7469.39,6623.14 L 7479.1,6630.19 L 7354.09,6802.25 L 7344.38,6795.2 L 7469.39,6623.14 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7366.75,6400.72 L 7371.33,6386.62 L 7545.74,6626.67 L 7530.9,6626.67 L 7366.75,6400.72 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7101.13,6487.03 L 7089.13,6478.31 L 7371.33,6386.62 L 7366.75,6400.72 L 7101.13,6487.03 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7834.47,592.974 L 7822.47,601.692 L 7822.47,304.974 L 7834.47,313.693 L 7834.47,592.974 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7886.31,559.669 L 7874.31,559.669 L 7874.31,346.997 L 7886.31,346.997 L 7886.31,559.669 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 8100.08,679.277 L 8104.66,693.383 L 7822.47,601.692 L 7834.47,592.974 L 8100.08,679.277 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 8264.24,453.333 L 8279.07,453.333 L 8104.66,693.383 L 8100.08,679.277 L 8264.24,453.333 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 8202.72,449.807 L 8212.43,456.86 L 8087.42,628.915 L 8077.72,621.862 L 8202.72,449.807 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 8100.08,227.39 L 8104.66,213.283 L 8279.07,453.333 L 8264.24,453.333 L 8100.08,227.39 Z " />
            <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -18 -8)" d="M 7834.47,313.693 L 7822.47,304.974 L 8104.66,213.283 L 8100.08,227.39 L 7834.47,313.693 Z " />
            </svg>
                        
            """;

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException, TranscoderException {
        // SpringApplication.run(ReportGeneratorApplication.class, args);f
        float paddingX = 20f, paddingY = 20f;
        ReactionSchemeSVGHandler schemeSVGHandler = new ReactionSchemeSVGHandler(PDRectangle.A4, paddingX, paddingY);

        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDFTranscoder pdfTranscoder = new PDFTranscoder();
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(content.getBytes()));
        //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileOutputStream outputStream = new FileOutputStream("test3.pdf");
        TranscoderOutput output = new TranscoderOutput(outputStream);
        pdfTranscoder.transcode(input, output);

        //PDDocument transcodedSvgToPDF = Loader.loadPDF(outputStream.toByteArray());
        PDDocument transcodedSvgToPDF = Loader.loadPDF(new File("test3.pdf"));
        PDPage pageWithSVG = transcodedSvgToPDF.getPage(0);
        PDFormXObject object = new PDFormXObject(new PDStream(transcodedSvgToPDF, pageWithSVG.getContents()));
        //PDFormXObject object = new PDFormXObject(new PDStream(doc, pageWithSVG.getContents()));

        object.setResources(pageWithSVG.getResources());
        object.setBBox(pageWithSVG.getBBox());
        AffineTransform matrix = object.getMatrix().createAffineTransform();
        float scaleX = (page.getMediaBox().getWidth() - (paddingX * 4)) / pageWithSVG.getMediaBox().getWidth();
        float scaleY = (page.getMediaBox().getWidth() - paddingY) / pageWithSVG.getMediaBox().getWidth();
        if (scaleY > 1 || scaleX > 1) {
            scaleX = 1;
            scaleY = 1;
        }
        matrix.translate(paddingX * 2, page.getMediaBox().getHeight() - paddingY - (pageWithSVG.getBBox().getHeight() * scaleY));
        matrix.scale(scaleX, scaleY);
        object.setMatrix(matrix);
        object.setFormType(1);

        PDPageContentStream stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);
        stream.drawForm(object);
//        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20f);
//        stream.beginText();
//        stream.newLineAtOffset(50, 150);
//        stream.showText("test test test test test test");
//        stream.endText();
        stream.setLineWidth(10.0f);
        //stream.addRect(paddingX*2, page.getMediaBox().getHeight() - paddingY - (pageWithSVG.getBBox().getHeight() * scaleY), pageWithSVG.getMediaBox().getWidth() * scaleX, pageWithSVG.getMediaBox().getHeight() * scaleY);
        stream.close();
        doc.save("lastTest.pdf");


        schemeSVGHandler.setSVGSource(content);
        PDDocument document = schemeSVGHandler.createPDPageWithSVG();
        document.save("test.pdf");
        List<User> users = new ArrayList<>();
        users.add(new User(1, null, "noPass", 999,-1));
        users.add(new User(2, "Krasunchyk", "yesPass", 9999,-2));
        int size = 100;
        while (users.size() < size) {
            users.add(new User(new Random().nextInt(3, 999), "Daniel Bat'kovych", "dasdasdaPass", new Random().nextInt(1000, 10000000), new Random().nextInt( 1, 100)));
        }
        TableGenerator<User> tableGenerator = new TableGenerator<>(users, User.class, doc, (pageWithSVG.getMediaBox().getHeight() * scaleY) + paddingY);
        tableGenerator.createTable("tableTest.pdf", PDRectangle.A4, 12);
    }
}
