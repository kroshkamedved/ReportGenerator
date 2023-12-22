package com.example.reportgenerator;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootApplication
public class ReportGeneratorApplication {
    private static String content = """
            <?xml version="1.0" standalone="no" ?>
                                                           <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
                                                           <svg width="776" height="252" viewBox="0 0 776 252" style="background-color: #ffffffff" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" >
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 2280.31,2607.78 L 2268.31,2613.56 L 2268.31,2319.78 L 2280.31,2325.56 L 2280.31,2607.78 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 2500.96,2783.74 L 2498,2796.72 L 2268.31,2613.56 L 2280.31,2607.78 L 2500.96,2783.74 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 2776.11,2720.94 L 2784.41,2731.35 L 2498,2796.72 L 2500.96,2783.74 L 2776.11,2720.94 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 2898.56,2466.67 L 2911.88,2466.67 L 2784.41,2731.35 L 2776.11,2720.94 L 2898.56,2466.67 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 2776.11,2212.39 L 2784.41,2201.98 L 2911.88,2466.67 L 2898.56,2466.67 L 2776.11,2212.39 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 2500.96,2149.59 L 2498,2136.61 L 2784.41,2201.98 L 2776.11,2212.39 L 2500.96,2149.59 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 2280.31,2325.56 L 2268.31,2319.78 L 2498,2136.61 L 2500.96,2149.59 L 2280.31,2325.56 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 11259.7,2419.33 L 3666.67,2419.33 L 3666.67,2407.33 L 11259.7,2407.33 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 11495,2413.33 C 11495,2413.33 11225,2481.83 11225,2481.83 C 11225,2481.83 11258.7,2443.3 11258.7,2413.33 C 11258.7,2382.93 11225,2343.83 11225,2343.83 C 11225,2343.83 11495,2413.33 11495,2413.33 C 11495,2413.33 11495,2413.33 11495,2413.33 " />
                                                           <path stroke="#000000" stroke-width="1" fill="transparent" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 11495,2413.33 C 11495,2413.33 11225,2481.83 11225,2481.83 C 11225,2481.83 11258.7,2443.3 11258.7,2413.33 C 11258.7,2382.93 11225,2343.83 11225,2343.83 C 11225,2343.83 11495,2413.33 11495,2413.33 C 11495,2413.33 11495,2413.33 11495,2413.33 " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 12738.4,2688.18 L 12726.4,2693.15 L 12726.4,2400.18 L 12738.4,2405.15 L 12738.4,2688.18 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 12938.5,2888.31 L 12933.5,2900.31 L 12726.4,2693.15 L 12738.4,2688.18 L 12938.5,2888.31 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 13221.5,2888.31 L 13226.5,2900.31 L 12933.5,2900.31 L 12938.5,2888.31 L 13221.5,2888.31 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 13421.6,2688.18 L 13433.6,2693.15 L 13226.5,2900.31 L 13221.5,2888.31 L 13421.6,2688.18 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 13421.6,2405.15 L 13433.6,2400.18 L 13433.6,2693.15 L 13421.6,2688.18 L 13421.6,2405.15 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 13221.5,2205.02 L 13226.5,2193.02 L 13433.6,2400.18 L 13421.6,2405.15 L 13221.5,2205.02 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 12938.5,2205.02 L 12933.5,2193.02 L 13226.5,2193.02 L 13221.5,2205.02 L 12938.5,2205.02 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 12738.4,2405.15 L 12726.4,2400.18 L 12933.5,2193.02 L 12938.5,2205.02 L 12738.4,2405.15 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7769.92,1393.87 L 7757.92,1400.8 L 7757.92,1105.87 L 7769.92,1112.8 L 7769.92,1393.87 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7821.76,1367.4 L 7809.76,1367.4 L 7809.76,1139.26 L 7821.76,1139.26 L 7821.76,1367.4 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 8013.33,1534.41 L 8013.33,1548.26 L 7757.92,1400.8 L 7769.92,1393.87 L 8013.33,1534.41 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 8256.75,1393.87 L 8268.75,1400.8 L 8013.33,1548.26 L 8013.33,1534.41 L 8256.75,1393.87 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 8207.91,1362.21 L 8213.91,1372.6 L 8016.33,1486.67 L 8010.33,1476.28 L 8207.91,1362.21 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 8256.75,1112.8 L 8268.75,1105.87 L 8268.75,1400.8 L 8256.75,1393.87 L 8256.75,1112.8 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 8013.33,972.262 L 8013.33,958.405 L 8268.75,1105.87 L 8256.75,1112.8 L 8013.33,972.262 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 8010.33,1030.39 L 8016.33,1020 L 8213.91,1134.07 L 8207.91,1144.46 L 8010.33,1030.39 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7769.92,1112.8 L 7757.92,1105.87 L 8013.33,958.405 L 8013.33,972.262 L 7769.92,1112.8 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7463.25,4847.2 L 7451.25,4854.13 L 7451.25,4559.2 L 7463.25,4566.13 L 7463.25,4847.2 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7515.09,4820.74 L 7503.09,4820.74 L 7503.09,4592.6 L 7515.09,4592.6 L 7515.09,4820.74 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7706.67,4987.74 L 7706.67,5001.59 L 7451.25,4854.13 L 7463.25,4847.2 L 7706.67,4987.74 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7950.08,4847.2 L 7962.08,4854.13 L 7706.67,5001.59 L 7706.67,4987.74 L 7950.08,4847.2 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7901.24,4815.54 L 7907.24,4825.93 L 7709.67,4940 L 7703.67,4929.61 L 7901.24,4815.54 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7950.08,4566.13 L 7962.08,4559.2 L 7962.08,4854.13 L 7950.08,4847.2 L 7950.08,4566.13 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7706.67,4425.59 L 7706.67,4411.74 L 7962.08,4559.2 L 7950.08,4566.13 L 7706.67,4425.59 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7703.67,4483.72 L 7709.67,4473.33 L 7907.24,4587.4 L 7901.24,4597.79 L 7703.67,4483.72 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 7463.25,4566.13 L 7451.25,4559.2 L 7706.67,4411.74 L 7706.67,4425.59 L 7463.25,4566.13 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 17183.3,2740.54 L 17171.3,2747.46 L 17171.3,2452.54 L 17183.3,2459.46 L 17183.3,2740.54 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 17235.1,2714.07 L 17223.1,2714.07 L 17223.1,2485.93 L 17235.1,2485.93 L 17235.1,2714.07 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 17426.7,2881.07 L 17426.7,2894.93 L 17171.3,2747.46 L 17183.3,2740.54 L 17426.7,2881.07 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 17670.1,2740.54 L 17682.1,2747.46 L 17426.7,2894.93 L 17426.7,2881.07 L 17670.1,2740.54 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 17621.2,2708.87 L 17627.2,2719.27 L 17429.7,2833.34 L 17423.7,2822.94 L 17621.2,2708.87 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 17670.1,2459.46 L 17682.1,2452.54 L 17682.1,2747.46 L 17670.1,2740.54 L 17670.1,2459.46 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 17426.7,2318.93 L 17426.7,2305.07 L 17682.1,2452.54 L 17670.1,2459.46 L 17426.7,2318.93 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 17423.7,2377.06 L 17429.7,2366.66 L 17627.2,2480.73 L 17621.2,2491.13 L 17423.7,2377.06 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 17183.3,2459.46 L 17171.3,2452.54 L 17426.7,2305.07 L 17426.7,2318.93 L 17183.3,2459.46 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 3529.92,513.869 L 3517.92,520.797 L 3517.92,225.869 L 3529.92,232.797 L 3529.92,513.869 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 3581.76,487.403 L 3569.76,487.403 L 3569.76,259.263 L 3581.76,259.263 L 3581.76,487.403 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 3773.33,654.405 L 3773.33,668.261 L 3517.92,520.797 L 3529.92,513.869 L 3773.33,654.405 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 4016.75,513.869 L 4028.75,520.797 L 3773.33,668.261 L 3773.33,654.405 L 4016.75,513.869 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 3967.91,482.207 L 3973.91,492.6 L 3776.33,606.67 L 3770.33,596.277 L 3967.91,482.207 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 4016.75,232.797 L 4028.75,225.869 L 4028.75,520.797 L 4016.75,513.869 L 4016.75,232.797 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 3773.33,92.2616 L 3773.33,78.4054 L 4028.75,225.869 L 4016.75,232.797 L 3773.33,92.2616 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 3770.33,150.389 L 3776.33,139.997 L 3973.91,254.067 L 3967.91,264.459 L 3770.33,150.389 Z " />
                                                           <path stroke="transparent" fill="#000000" transform="matrix(0.05 0 0 0.05 -111 -1)" d="M 3529.92,232.797 L 3517.92,225.869 L 3773.33,78.4054 L 3773.33,92.2616 L 3529.92,232.797 Z " />
                                                           </svg>
                                                           
                                                     
                                                     
            """;

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException, TranscoderException {
        // SpringApplication.run(ReportGeneratorApplication.class, args);
        ReactionSchemeSVGHandler schemeSVGHandler = new ReactionSchemeSVGHandler(PDRectangle.A4);


        schemeSVGHandler.setSVGSource(content);
        PDDocument document = schemeSVGHandler.createPDPageWithSVG();
        document.save("test.pdf");
        List<User> users = new ArrayList<>();
        users.add(new User(1, "Dovbeny", "noPass", 999));
        users.add(new User(2, "Krasunchyk", "yesPass", 9999));
        int size = 99;
        while (users.size() < size) {
            users.add(new User(new Random().nextInt(3, 999), "Daniel Bat'kovych", "dasdasdaPass", new Random().nextInt(1000, 10000000)));
        }
        TableGenerator<User> tableGenerator = new TableGenerator<>(users, User.class, document, schemeSVGHandler.calculateSVGHeight());
        tableGenerator.createTable("tableTest.pdf", PDRectangle.A4, 12);
    }
}
