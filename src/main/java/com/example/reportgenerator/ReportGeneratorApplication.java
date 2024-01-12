package com.example.reportgenerator;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.springframework.boot.SpringApplication;
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
    private static String content = """
            <?xml version="1.0" standalone="no" ?>
                  <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
                  <svg width="78px" height="43px" viewBox="0 0 78 43" style="background-color: #ffffffff" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" >
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 39.9792 39.8411)" style="font:normal normal normal 200px 'Arial'" >
                  N</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2308.4,1957.14 L 2296.4,1964.06 L 2296.4,1676.06 L 2302.4,1672.6 L 2308.4,1676.06 L 2308.4,1957.14 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2360.24,1930.67 L 2348.24,1930.67 L 2348.24,1702.53 L 2360.24,1702.53 L 2360.24,1930.67 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2453.24,2040.76 L 2447.24,2051.16 L 2296.4,1964.06 L 2308.4,1957.14 L 2453.24,2040.76 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2795.2,1957.14 L 2807.2,1964.06 L 2655.29,2051.78 L 2649.29,2041.38 L 2795.2,1957.14 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2746.36,1925.48 L 2752.36,1935.87 L 2629.37,2006.88 L 2623.37,1996.49 L 2746.36,1925.48 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2795.2,1676.06 L 2801.2,1672.6 L 2807.2,1676.06 L 2807.2,1964.06 L 2795.2,1957.14 L 2795.2,1676.06 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2551.8,1535.53 L 2551.8,1521.67 L 2801.2,1665.67 L 2801.2,1672.6 L 2795.2,1676.06 L 2551.8,1535.53 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2548.8,1593.65 L 2554.8,1583.26 L 2752.36,1697.33 L 2746.36,1707.72 L 2548.8,1593.65 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2308.4,1676.06 L 2302.4,1672.6 L 2302.4,1665.67 L 2551.8,1521.67 L 2551.8,1535.53 L 2308.4,1676.06 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2053,1535.53 L 2053,1521.67 L 2302.4,1665.67 L 2302.4,1672.6 L 2296.4,1676.06 L 2053,1535.53 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 1896.17,1626.01 L 1890.17,1615.61 L 2053,1521.67 L 2053,1535.53 L 1896.17,1626.01 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 1922.09,1670.94 L 1916.09,1660.54 L 2049.99,1583.29 L 2055.99,1593.68 L 1922.09,1670.94 Z " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 2.92884 19.3643)" style="font:normal normal normal 200px 'Arial'" >
                  O</text>
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 65.2117 11.023)" style="font:normal normal normal 199px 'Arial'" >
                  Br</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -69)" d="M 2949.79,1579.88 L 2955.79,1590.27 L 2807.2,1676.06 L 2801.2,1672.6 L 2801.2,1665.67 L 2949.79,1579.88 Z " />
                  </svg>
                  

            """;
    /*private static String content = """
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
                        
            """;*/
   /* private static String content = """
            <?xml version="1.0" standalone="no" ?>
                  <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
                  <svg width="600px" height="145px" viewBox="0 0 600 145" style="background-color: #ffffffff" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" >
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 526.659 79.5011)" style="font:normal normal normal 200px 'Arial'" >
                  N</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12042,2130.34 L 12030,2137.26 L 12030,1849.26 L 12036,1845.8 L 12042,1849.26 L 12042,2130.34 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12093.8,2103.87 L 12081.8,2103.87 L 12081.8,1875.73 L 12093.8,1875.73 L 12093.8,2103.87 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12186.8,2213.96 L 12180.8,2224.36 L 12030,2137.26 L 12042,2130.34 L 12186.8,2213.96 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12528.8,2130.34 L 12540.8,2137.26 L 12388.9,2224.98 L 12382.9,2214.58 L 12528.8,2130.34 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12480,2098.68 L 12486,2109.07 L 12363,2180.08 L 12357,2169.69 L 12480,2098.68 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12528.8,1849.26 L 12540.8,1842.34 L 12540.8,2137.26 L 12528.8,2130.34 L 12528.8,1849.26 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12285.4,1708.73 L 12285.4,1694.87 L 12540.8,1842.34 L 12528.8,1849.26 L 12285.4,1708.73 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12282.4,1766.85 L 12288.4,1756.46 L 12486,1870.53 L 12480,1880.92 L 12282.4,1766.85 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12042,1849.26 L 12036,1845.8 L 12036,1838.87 L 12285.4,1694.87 L 12285.4,1708.73 L 12042,1849.26 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 11786.6,1708.73 L 11786.6,1701.8 L 11792.6,1698.34 L 12036,1838.87 L 12036,1845.8 L 12030,1849.26 L 11786.6,1708.73 Z " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 502.276 36.3011)" style="font:normal normal normal 200px 'Arial'" >
                  F</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 11780.6,1517.36 L 11792.6,1517.36 L 11792.6,1698.34 L 11786.6,1701.8 L 11780.6,1698.33 L 11780.6,1517.36 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 11636.8,1795.13 L 11630.8,1784.73 L 11780.6,1698.33 L 11786.6,1701.8 L 11786.6,1708.73 L 11636.8,1795.13 Z " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 489.796 57.9011)" style="font:normal normal normal 200px 'Arial'" >
                  F</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12737.8,1721.69 L 12743.8,1732.08 L 12734.1,1739.41 L 12726.6,1726.36 L 12737.8,1721.69 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12683.3,1744.48 L 12696.8,1767.84 L 12687.2,1775.17 L 12672.1,1749.15 L 12683.3,1744.48 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12628.9,1767.27 L 12649.8,1803.6 L 12640.2,1810.93 L 12617.7,1771.94 L 12628.9,1767.27 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12574.4,1790.06 L 12602.9,1839.36 L 12593.2,1846.69 L 12563.2,1794.73 L 12574.4,1790.06 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 13061.8,1695.8 L 13072.2,1701.8 L 13070.2,1707.8 L 12773.8,1707.8 L 12794.6,1695.8 L 13061.8,1695.8 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12928.2,1464.4 L 12928.2,1452.4 L 12936.4,1454.6 L 13076.4,1697.09 L 13072.2,1701.8 L 13061.8,1695.8 L 12928.2,1464.4 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12794.6,1695.8 L 12773.8,1707.8 L 12920,1454.6 L 12928.2,1452.4 L 12928.2,1464.4 L 12794.6,1695.8 Z " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 578.469 57.9011)" style="font:normal normal normal 200px 'Arial'" >
                  NH</text>
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 592.913 59.8455)" style="font:normal normal normal 150px 'Arial'" >
                  2</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 13235,1761.18 L 13205,1813.14 L 13070.2,1707.8 L 13072.2,1701.8 L 13076.4,1697.09 L 13235,1761.18 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 10788.5,1872.8 L 9970.6,1872.8 L 9970.6,1860.8 L 10788.5,1860.8 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 11023.8,1866.8 C 11023.8,1866.8 10753.8,1935.36 10753.8,1935.36 C 10753.8,1935.36 10787.5,1896.8 10787.5,1866.8 C 10787.5,1836.36 10753.8,1797.24 10753.8,1797.24 C 10753.8,1797.24 11023.8,1866.8 11023.8,1866.8 C 11023.8,1866.8 11023.8,1866.8 11023.8,1866.8 " />
                  <path stroke="#000000" stroke-width="1" fill="transparent" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 11023.8,1866.8 C 11023.8,1866.8 10753.8,1935.36 10753.8,1935.36 C 10753.8,1935.36 10787.5,1896.8 10787.5,1866.8 C 10787.5,1836.36 10753.8,1797.24 10753.8,1797.24 C 10753.8,1797.24 11023.8,1866.8 11023.8,1866.8 C 11023.8,1866.8 11023.8,1866.8 11023.8,1866.8 " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 325.329 76.8411)" style="font:normal normal normal 200px 'Arial'" >
                  N</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8015.4,2077.14 L 8003.4,2084.06 L 8003.4,1796.06 L 8009.4,1792.6 L 8015.4,1796.06 L 8015.4,2077.14 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8067.24,2050.67 L 8055.24,2050.67 L 8055.24,1822.53 L 8067.24,1822.53 L 8067.24,2050.67 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8160.24,2160.76 L 8154.24,2171.16 L 8003.4,2084.06 L 8015.4,2077.14 L 8160.24,2160.76 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8502.2,2077.14 L 8514.2,2084.06 L 8362.29,2171.78 L 8356.29,2161.38 L 8502.2,2077.14 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8453.36,2045.48 L 8459.36,2055.87 L 8336.37,2126.88 L 8330.37,2116.49 L 8453.36,2045.48 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8502.2,1796.06 L 8514.2,1789.14 L 8514.2,2084.06 L 8502.2,2077.14 L 8502.2,1796.06 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8258.8,1655.53 L 8258.8,1641.67 L 8514.2,1789.14 L 8502.2,1796.06 L 8258.8,1655.53 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8255.8,1713.65 L 8261.8,1703.26 L 8459.36,1817.33 L 8453.36,1827.72 L 8255.8,1713.65 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8015.4,1796.06 L 8009.4,1792.6 L 8009.4,1785.67 L 8258.8,1641.67 L 8258.8,1655.53 L 8015.4,1796.06 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 7760,1655.53 L 7760,1648.6 L 7766,1645.14 L 8009.4,1785.67 L 8009.4,1792.6 L 8003.4,1796.06 L 7760,1655.53 Z " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 300.946 33.6411)" style="font:normal normal normal 200px 'Arial'" >
                  F</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 7754,1464.16 L 7766,1464.16 L 7766,1645.14 L 7760,1648.6 L 7754,1645.13 L 7754,1464.16 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 7610.24,1741.93 L 7604.25,1731.53 L 7754,1645.13 L 7760,1648.6 L 7760,1655.53 L 7610.24,1741.93 Z " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 288.466 55.2411)" style="font:normal normal normal 200px 'Arial'" >
                  F</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8711.15,1668.49 L 8717.15,1678.88 L 8707.53,1686.21 L 8699.99,1673.16 L 8711.15,1668.49 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8656.7,1691.28 L 8670.19,1714.64 L 8660.57,1721.97 L 8645.54,1695.95 L 8656.7,1691.28 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8602.26,1714.07 L 8623.23,1750.4 L 8613.61,1757.73 L 8591.1,1718.74 L 8602.26,1714.07 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8547.81,1736.86 L 8576.27,1786.16 L 8566.65,1793.49 L 8536.65,1741.53 L 8547.81,1736.86 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 9035.21,1642.6 L 9045.6,1648.6 L 9043.63,1654.6 L 8747.21,1654.6 L 8767.99,1642.6 L 9035.21,1642.6 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8901.6,1411.2 L 8901.6,1399.2 L 8909.8,1401.4 L 9049.81,1643.89 L 9045.6,1648.6 L 9035.21,1642.6 L 8901.6,1411.2 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8767.99,1642.6 L 8747.21,1654.6 L 8893.4,1401.4 L 8901.6,1399.2 L 8901.6,1411.2 L 8767.99,1642.6 Z " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 377.139 55.2411)" style="font:normal normal normal 200px 'Arial'" >
                  N</text>
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 377.139 64.0744)" style="font:normal normal normal 200px 'Arial'" >
                  H</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 9208.44,1707.98 L 9178.44,1759.94 L 9043.63,1654.6 L 9045.6,1648.6 L 9049.81,1643.89 L 9208.44,1707.98 Z " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 389.902 48.023)" style="font:normal normal normal 199px 'Arial'" >
                  Boc</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 9443.59,1699.88 L 9449.59,1710.27 L 9398.49,1739.78 L 9392.49,1729.38 L 9443.59,1699.88 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 6728.23,2023.97 L 5806.6,2945.6 L 5798.6,2937.6 L 6720.23,2015.97 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 6890.6,1853.6 C 6890.6,1853.6 6748.46,2093.29 6748.46,2093.29 C 6748.46,2093.29 6744.87,2042.01 6723.53,2020.67 C 6702.19,1999.33 6650.91,1995.74 6650.91,1995.74 C 6650.91,1995.74 6890.6,1853.6 6890.6,1853.6 C 6890.6,1853.6 6890.6,1853.6 6890.6,1853.6 " />
                  <path stroke="#000000" stroke-width="1" fill="transparent" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 6890.6,1853.6 C 6890.6,1853.6 6748.46,2093.29 6748.46,2093.29 C 6748.46,2093.29 6744.87,2042.01 6723.53,2020.67 C 6702.19,1999.33 6650.91,1995.74 6650.91,1995.74 C 6650.91,1995.74 6890.6,1853.6 6890.6,1853.6 C 6890.6,1853.6 6890.6,1853.6 6890.6,1853.6 " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 39.9792 70.8411)" style="font:normal normal normal 200px 'Arial'" >
                  N</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2308.4,1957.14 L 2296.4,1964.06 L 2296.4,1676.06 L 2302.4,1672.6 L 2308.4,1676.06 L 2308.4,1957.14 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2360.24,1930.67 L 2348.24,1930.67 L 2348.24,1702.53 L 2360.24,1702.53 L 2360.24,1930.67 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2453.24,2040.76 L 2447.24,2051.16 L 2296.4,1964.06 L 2308.4,1957.14 L 2453.24,2040.76 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2795.2,1957.14 L 2807.2,1964.06 L 2655.29,2051.78 L 2649.29,2041.38 L 2795.2,1957.14 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2746.36,1925.48 L 2752.36,1935.87 L 2629.37,2006.88 L 2623.37,1996.49 L 2746.36,1925.48 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2795.2,1676.06 L 2801.2,1672.6 L 2807.2,1676.06 L 2807.2,1964.06 L 2795.2,1957.14 L 2795.2,1676.06 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2551.8,1535.53 L 2551.8,1521.67 L 2801.2,1665.67 L 2801.2,1672.6 L 2795.2,1676.06 L 2551.8,1535.53 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2548.8,1593.65 L 2554.8,1583.26 L 2752.36,1697.33 L 2746.36,1707.72 L 2548.8,1593.65 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2308.4,1676.06 L 2302.4,1672.6 L 2302.4,1665.67 L 2551.8,1521.67 L 2551.8,1535.53 L 2308.4,1676.06 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2053,1535.53 L 2053,1521.67 L 2302.4,1665.67 L 2302.4,1672.6 L 2296.4,1676.06 L 2053,1535.53 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 1896.17,1626.01 L 1890.17,1615.61 L 2053,1521.67 L 2053,1535.53 L 1896.17,1626.01 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 1922.09,1670.94 L 1916.09,1660.54 L 2049.99,1583.29 L 2055.99,1593.68 L 1922.09,1670.94 Z " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 2.92884 50.3643)" style="font:normal normal normal 200px 'Arial'" >
                  O</text>
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 65.2117 42.023)" style="font:normal normal normal 199px 'Arial'" >
                  Br</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 2949.79,1579.88 L 2955.79,1590.27 L 2807.2,1676.06 L 2801.2,1672.6 L 2801.2,1665.67 L 2949.79,1579.88 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 13171.6,1304.94 L 13183.6,1311.86 L 12936.4,1454.6 L 12928.2,1452.4 L 12928.2,1445.47 L 13171.6,1304.94 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 13171.6,1023.86 L 13183.6,1016.94 L 13183.6,1311.86 L 13171.6,1304.94 L 13171.6,1023.86 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12928.2,883.328 L 12928.2,869.472 L 13183.6,1016.94 L 13171.6,1023.86 L 12928.2,883.328 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12684.8,1023.86 L 12672.8,1016.94 L 12928.2,869.472 L 12928.2,883.328 L 12684.8,1023.86 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12684.8,1304.94 L 12672.8,1311.86 L 12672.8,1016.94 L 12684.8,1023.86 L 12684.8,1304.94 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 12928.2,1445.47 L 12928.2,1452.4 L 12920,1454.6 L 12672.8,1311.86 L 12684.8,1304.94 L 12928.2,1445.47 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 9145,1251.74 L 9157,1258.66 L 8909.8,1401.4 L 8901.6,1399.2 L 8901.6,1392.27 L 9145,1251.74 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 9145,970.664 L 9157,963.736 L 9157,1258.66 L 9145,1251.74 L 9145,970.664 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8901.6,830.128 L 8901.6,816.272 L 9157,963.736 L 9145,970.664 L 8901.6,830.128 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8658.2,970.664 L 8646.2,963.736 L 8901.6,816.272 L 8901.6,830.128 L 8658.2,970.664 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8658.2,1251.74 L 8646.2,1258.66 L 8646.2,963.736 L 8658.2,970.664 L 8658.2,1251.74 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 8901.6,1392.27 L 8901.6,1399.2 L 8893.4,1401.4 L 8646.2,1258.66 L 8658.2,1251.74 L 8901.6,1392.27 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 3917.71,2885.42 L 3309.8,1832.6 L 3319.8,1826.6 L 3927.71,2879.42 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 4001,3018 C 4001,3018 3871.02,2885.62 3871.02,2885.62 C 3871.02,2885.62 3904.72,2891.84 3922.21,2881.55 C 3939.71,2871.27 3950.96,2838.62 3950.96,2838.62 C 3950.96,2838.62 4001,3018 4001,3018 C 4001,3018 4001,3018 4001,3018 " />
                  <path stroke="#000000" stroke-width="1" fill="transparent" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 4001,3018 C 4001,3018 3871.02,2885.62 3871.02,2885.62 C 3871.02,2885.62 3904.72,2891.84 3922.21,2881.55 C 3939.71,2871.27 3950.96,2838.62 3950.96,2838.62 C 3950.96,2838.62 4001,3018 4001,3018 C 4001,3018 4001,3018 4001,3018 " />
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 129.695 128.161)" style="font:normal normal normal 200px 'Arial'" >
                  Br</text>
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 184.866 142.561)" style="font:normal normal normal 200px 'Arial'" >
                  F</text>
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 197.336 120.961)" style="font:normal normal normal 200px 'Arial'" >
                  F</text>
                  <text x="0" y="0" stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 159.369 99.3611)" style="font:normal normal normal 200px 'Arial'" >
                  N</text>
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 4939.6,3257.93 L 4939.6,3244.07 L 5183,3103.54 L 5189,3107 L 5189,3113.93 L 4939.6,3257.93 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 4942.6,3196.34 L 4936.6,3185.95 L 5134.16,3071.88 L 5140.16,3082.27 L 4942.6,3196.34 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 5183,2822.46 L 5195,2815.54 L 5195,3103.54 L 5189,3107 L 5183,3103.54 L 5183,2822.46 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 4939.6,3244.07 L 4939.6,3257.93 L 4690.2,3113.93 L 4690.2,3107 L 4696.2,3103.54 L 4939.6,3244.07 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 4684.2,2815.54 L 4696.2,2822.46 L 4696.2,3103.54 L 4690.2,3107 L 4684.2,3103.53 L 4684.2,2815.54 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 4736.04,2848.93 L 4748.04,2848.93 L 4748.04,3077.07 L 4736.04,3077.07 L 4736.04,2848.93 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 5189,3113.93 L 5189,3107 L 5195,3103.54 L 5438.4,3244.07 L 5438.4,3251 L 5432.4,3254.46 L 5189,3113.93 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 4684.2,3103.53 L 4690.2,3107 L 4690.2,3113.93 L 4508.73,3218.62 L 4502.73,3208.23 L 4684.2,3103.53 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 5432.4,3254.46 L 5438.4,3251 L 5444.4,3254.46 L 5444.4,3435.56 L 5432.4,3435.56 L 5432.4,3254.46 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 5444.4,3254.46 L 5438.4,3251 L 5438.4,3244.07 L 5593.16,3154.72 L 5599.16,3165.11 L 5444.4,3254.46 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 5195,2815.54 L 5183,2822.46 L 5037.13,2738.24 L 5043.13,2727.85 L 5195,2815.54 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 5140.16,2843.73 L 5134.16,2854.12 L 5011.21,2783.13 L 5017.21,2772.74 L 5140.16,2843.73 Z " />
                  <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -84 -38)" d="M 4696.2,2822.46 L 4684.2,2815.54 L 4834.99,2728.47 L 4840.99,2738.86 L 4696.2,2822.46 Z " />
                  </svg>


            """;*/

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException, TranscoderException {
         SpringApplication.run(ReportGeneratorApplication.class, args);
        /*float paddingX = 20f, paddingY = 20f;
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
        float scaleY = (page.getMediaBox().getWidth() - (paddingY * 4)) / pageWithSVG.getMediaBox().getWidth();
        if (scaleY > 1 || scaleX > 1) {
            scaleX = 1;
            scaleY = 1;
        }
        matrix.translate(paddingX * 2, page.getMediaBox().getHeight() - 2 * paddingY - (pageWithSVG.getBBox().getHeight() * scaleY));
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
        stream.setLineWidth(0.5f);
        stream.addRect(paddingX, page.getMediaBox().getHeight() - (2 * paddingY + 0.5f * paddingY) - (pageWithSVG.getBBox().getHeight() * scaleY), pageWithSVG.getMediaBox().getWidth() * scaleX + paddingX * 2, (pageWithSVG.getMediaBox().getHeight() * scaleY) + paddingY);
        stream.stroke();
        stream.close();
        doc.save("lastTest.pdf");


        schemeSVGHandler.setSVGSource(content);
        PDDocument document = schemeSVGHandler.createPDPageWithSVG();
        document.save("test.pdf");
        List<User> users = new ArrayList<>();
        users.add(new User(1, null, "noPass", 999, -1));
        users.add(new User(2, "Krasunchyk", "yesPass", 9999, -2));
        int size = 100;
        while (users.size() < size) {
            users.add(new User(new Random().nextInt(3, 999), "Daniel Bat'kovych", "dasdasdaPass", new Random().nextInt(1000, 10000000), new Random().nextInt(1, 100)));
        }
        TableGenerator<User> tableGenerator = new TableGenerator<>(users, User.class, doc, (pageWithSVG.getMediaBox().getHeight() * scaleY) + 2 * paddingY);
        float lastLineHeight = tableGenerator.createTable(PDRectangle.A4, 11.5f);

        int lastPageIndex = doc.getNumberOfPages() - 1;
//        PDPageContentStream newStream = new PDPageContentStream(doc, doc.getPage(lastPageIndex), PDPageContentStream.AppendMode.APPEND, true);

//        newStream.beginText();
//        newStream.newLineAtOffset(TableGenerator.DEFAULT_PAGE_SIDE_MARGIN + 4, lastLineHeight - 30);
//        newStream.setFont(tableGenerator.getFont(), tableGenerator.getCurrentFontSize());
//        newStream.showText("Tabulation size in the context of formatting or text display typically refers to the number of spaces or characters that represent a single level of indentation when using tabs. It's often customizable to suit coding styles or text layout preferences.\n" +
//                "\n" +
//                "For instance, in Java, a common convention is to use a tab size of 4 spaces for indentation. If you're referring to setting a specific tabulation size for your PDF content creation using PDFBox, it's important to note that PDFBox does not directly handle tabulation or indentation in the same way as a text editor might.\n" +
//                "\n" +
//                "However, you can simulate indentation by using spaces or adjusting the starting X coordinate when drawing text. If you want to simulate tabulation with spaces, you can simply multiply the desired tabulation size by the number of spaces you want to use for each tab level. Here's an example:.");
//        newStream.endText();
//        newStream.addRect(TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, lastLineHeight - 38, 500, 22);
//        newStream.stroke();
//        newStream.close();
        String textContent = """
                Tabulation size in the context of formatting or text display typically refers to the number of spaces or characters that represent a single level of indentation when using tabs. It's often customizable to suit coding styles or text layout preferences.
                                
                For instance, in Java, a common convention is to use a tab size of 4 spaces for indentation. If you're referring to setting a specific tabulation size for your PDF content creation using PDFBox, it's important to note that PDFBox does not directly handle tabulation or indentation in the same way as a text editor might.
                                
                However, you can simulate indentation by using spaces or adjusting the starting X coordinate when drawing text. If you want to simulate tabulation with spaces, you can simply multiply the desired tabulation size by the number of spaces you want to use for each tab level. Here's an example:
                """;
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);

        lastLineHeight = PDFUtil.drawParagraph(doc, lastLineHeight - paddingY, font, 12, textContent, 20, 30);
        doc.save("testWithTwoTables.pdf");
        doc.close();*/
    }
}
