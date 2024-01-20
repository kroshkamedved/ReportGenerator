package com.example.reportgenerator.controller;

import com.example.reportgenerator.domain.Compound;
import com.example.reportgenerator.dto.ReportDTO;
import com.example.reportgenerator.util.PDFUtil;
import com.example.reportgenerator.util.TableGenerator;
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
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController()
@RequestMapping("/report")
public class ReportController {

    private static String content = """
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
                  

            """;

    @PostMapping(path = "/pdf", produces = "application/pdf", headers = {HttpHeaders.CONTENT_DISPOSITION + "=attachment; filename=report.pdf"})
    public ByteArrayResource generatePDF(@RequestBody ReportDTO reportData) throws IOException, InvocationTargetException, IllegalAccessException, TranscoderException {
        List<Compound> reactants = reportData.getReactants();
        List<Compound> reagents = reportData.getReagents();
        List<Compound> products = reportData.getProducts();
        if (!reagents.isEmpty()) {
            reactants.add(reagents.get(0));
            products.add(reagents.get(0));
        }
        reactants.add(products.get(0));
        products.add(reactants.get(0));
        PDDocument document = new PDDocument();
        PDPage firstPage = new PDPage(PDRectangle.A5);
        document.addPage(firstPage);
        float lastLineYHeight = 0f;

        PDPageContentStream str = new PDPageContentStream(document, firstPage, PDPageContentStream.AppendMode.APPEND, true);
        str.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 9);
        str.beginText();
        str.newLineAtOffset(TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, firstPage.getMediaBox().getHeight() - TableGenerator.DEFAULT_PAGE_VERTICAL_MARGIN + 5f);
        str.showText("Experiment id : " + reportData.getExperiment().id());
        str.endText();
        str.close();
        float y = firstPage.getMediaBox().getHeight();

        //
        if(reportData.getExperiment().svg() != null){

            PDFTranscoder pdfTranscoder = new PDFTranscoder();
            TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(reportData.getExperiment().svg().getBytes()));
            // TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(content.getBytes()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //FileOutputStream outputStream = new FileOutputStream("test3.pdf");
            TranscoderOutput output = new TranscoderOutput(outputStream);
            pdfTranscoder.transcode(input, output);
            PDDocument transcodedSvgToPDF = Loader.loadPDF(outputStream.toByteArray());
            //PDDocument transcodedSvgToPDF = Loader.loadPDF(new File("test3.pdf"));
            PDPage pageWithSVG = transcodedSvgToPDF.getPage(0);
            PDFormXObject object = new PDFormXObject(new PDStream(transcodedSvgToPDF, pageWithSVG.getContents()));
            //PDFormXObject object = new PDFormXObject(new PDStream(doc, pageWithSVG.getContents()));
            object.setResources(pageWithSVG.getResources());
            object.setBBox(pageWithSVG.getBBox());
            AffineTransform matrix = object.getMatrix().createAffineTransform();
            float scaleX = (firstPage.getMediaBox().getWidth() - (TableGenerator.DEFAULT_PAGE_SIDE_MARGIN * 4)) / pageWithSVG.getMediaBox().getWidth();
            float scaleY = (firstPage.getMediaBox().getWidth() - (20f * 4)) / pageWithSVG.getMediaBox().getWidth();
            if (scaleY > 1 || scaleX > 1) {
                scaleX = 1;
                scaleY = 1;
            }
            float x = firstPage.getMediaBox().getWidth() / 2 - (pageWithSVG.getMediaBox().getWidth() * scaleX) / 2;
             y = firstPage.getMediaBox().getHeight() - 2 * TableGenerator.DEFAULT_PAGE_VERTICAL_MARGIN - (pageWithSVG.getBBox().getHeight() * scaleY);


            matrix.translate(x, y);
            matrix.scale(scaleX, scaleY);
            object.setMatrix(matrix);
            object.setFormType(1);

            PDPageContentStream stream = new PDPageContentStream(document, firstPage, PDPageContentStream.AppendMode.APPEND, true);
            stream.drawForm(object);
//        stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20f);
//        stream.beginText();
//        stream.newLineAtOffset(50, 150);
//        stream.showText("test test test test test test");
//        stream.endText();
            stream.setLineWidth(0.5f);
            stream.addRect(TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, y - TableGenerator.DEFAULT_PAGE_VERTICAL_MARGIN, firstPage.getMediaBox().getWidth() - 2 * TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, pageWithSVG.getMediaBox().getHeight() * scaleY + 2 * TableGenerator.DEFAULT_PAGE_VERTICAL_MARGIN);
            // stream.addRect(TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, firstPage.getMediaBox().getHeight() - (2 * 20f + 0.5f * 20f) - (pageWithSVG.getBBox().getHeight() * scaleY), pageWithSVG.getMediaBox().getWidth() * scaleX + TableGenerator.DEFAULT_PAGE_SIDE_MARGIN * 2, (pageWithSVG.getMediaBox().getHeight() * scaleY) + 20f);
            stream.stroke();
            stream.close();
        }

        //

        //lastLineYHeight = PDFUtil.drawParagraph(document, y, new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12, "REACTANTS TABLE:", TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, TableGenerator.DEFAULT_PAGE_VERTICAL_MARGIN * 2);
        lastLineYHeight = PDFUtil.drawParagraph(document, y - TableGenerator.DEFAULT_PAGE_VERTICAL_MARGIN, new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12, "REACTANTS TABLE:", TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, 20f, true);
        TableGenerator<Compound> reactantsTable = new TableGenerator<>(reactants, Compound.class, document, firstPage.getMediaBox().getHeight() - lastLineYHeight);
        lastLineYHeight = reactantsTable.createTable(firstPage.getMediaBox(), 10);

        lastLineYHeight = PDFUtil.drawParagraph(document, lastLineYHeight, new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12, "REAGENTS TABLE:", TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, 20f, true);
        TableGenerator<Compound> reagentsTable = new TableGenerator<>(reagents, Compound.class, document, firstPage.getMediaBox().getHeight() - lastLineYHeight);
        lastLineYHeight = reagentsTable.createTable(firstPage.getMediaBox(), reactantsTable.getCurrentFontSize());

        // document.save("paintedAsItShouldBe.pdf");
        lastLineYHeight = PDFUtil.drawParagraph(document, lastLineYHeight, new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12, "PRODUCTS TABLE:", TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, 20f, true);

        TableGenerator<Compound> productsTable = new TableGenerator<>(products, Compound.class, document, firstPage.getMediaBox().getHeight() - lastLineYHeight);
        lastLineYHeight = productsTable.createTable(firstPage.getMediaBox(), reagentsTable.getCurrentFontSize());
        // document.save("paintedAsItShouldBeNextLine.pdf");

        lastLineYHeight = PDFUtil.drawParagraph(document, lastLineYHeight, new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 10, "PROCEDURE:", TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, 20f, true);
        String fontPath = "src/main/resources/static/Roboto-LightItalic.ttf";
        PDFont font = PDType0Font.load(document, new File(fontPath));
        lastLineYHeight = PDFUtil.drawParagraph(document, lastLineYHeight, font, 8, reportData.getExperiment().comment(), TableGenerator.DEFAULT_PAGE_SIDE_MARGIN, 20f, false);


        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        document.save(byteArray);
        document.close();
        return new ByteArrayResource(byteArray.toByteArray());
    }
}
