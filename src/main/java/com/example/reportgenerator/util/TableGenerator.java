package com.example.reportgenerator.util;

import com.example.reportgenerator.domain.AllFieldsToStringReady;
import com.example.reportgenerator.domain.SVGColumn;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Setter
@Getter
public class TableGenerator<T extends Record & AllFieldsToStringReady> {
    public static final float DEFAULT_PAGE_VERTICAL_MARGIN = 30f;
    public static final float DEFAULT_PAGE_SIDE_MARGIN = 20f;
    public static final float DEFAULT_LINE_WIDTH = 0.125f;
    private static final Float DEFAULT_SVG_COLUMN_HEIGHT = 150f;
    private static float DEFAULT_MIN_FONT_SIZE = 6.0f;
    private static final int DEFAULT_A4_PIXEL_WIDTH = 842;

    private final PDDocument document;
    private final List<T> tableRows;
    private final Class<T> clazz;
    private Map<String, Method> tableHeaders;
    private Map<String, Float> columnWidthMap;
    private PDType1Font font;
    private float rowHeight;
    private PDPage pageWithSVG;
    private float firstPageAdditionalTopPadding;
    private boolean pageChanged;
    private float currentFontSize;
    private float tableWidthCoefficient = 1;
    private float cellMargin;
    private float newLineYHeight;
    private boolean drawingHeaders;
    private boolean svgPresent;
    private Map<String, Float> svgFieldsWidthMap;
    private final float maxSvgColumnWidth;


    public TableGenerator(List<T> tableRows, Class<T> clazz, PDDocument document, float firstPageAdditionalTopPadding) {
        this.tableRows = tableRows;
        this.clazz = clazz;
        this.document = document;
        tableHeaders = new TreeMap<>();
        columnWidthMap = new HashMap<>();
        font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        this.pageWithSVG = document.getPage(document.getNumberOfPages() - 1);
        this.firstPageAdditionalTopPadding = firstPageAdditionalTopPadding;
        this.maxSvgColumnWidth = pageWithSVG.getMediaBox().getWidth() * 0.15f;
        initialize();
    }

    public void setCurrentFontSize(float currentFontSize) {
        this.currentFontSize = currentFontSize;
        this.cellMargin = ((font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * currentFontSize) * 0.5f;
        this.rowHeight = (font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * currentFontSize + cellMargin;
    }


    /**
     * Fulfill the tableHeaders map.
     * Contains field name as the key, which simultaneously corresponds to future table column name,
     * and the corresponding field getter method as the value.
     */
    private void initialize() {
        Method[] methods = clazz.getMethods();
        Field[] fields = clazz.getDeclaredFields();
        fulfillTableHeadersMap(fields, methods);
    }

    private void fulfillTableHeadersMap(Field[] fields, Method[] methods) {
        for (Field field : fields) {
            String methodName = field.getName();
            if (Arrays.stream(field.getAnnotations())
                    .anyMatch(a -> a.annotationType() == SVGColumn.class)) {
                if (!svgPresent) {
                    svgPresent = true;
                    svgFieldsWidthMap = new HashMap<>();
                }
                float maxCurrentColumnSVGWidth = calculateSVGWidth(field, "width");
                svgFieldsWidthMap.put(methodName, maxCurrentColumnSVGWidth);
            }
            Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).findAny().ifPresent(method -> tableHeaders.put(field.getName(), method));
        }
    }

    //TODO check this logic
    private float calculateSVGWidth(Field field, String attribute) {
        field.setAccessible(true);
        return tableRows.stream()
                .map(t -> {
                    try {
                        return (String) field.get(t);
                    } catch (IllegalAccessException ilg) {
                        throw new RuntimeException("SVG width calculation problem");
                    }
                })
                .map(svg -> getSizeAttributeValue(svg, attribute))
                .max(Float::compare)
                .map(a -> Float.min(a, maxSvgColumnWidth))
                .orElse(maxSvgColumnWidth);

    }

    /**
     * Draw table on the page.
     *
     * @param pdRectangle page size
     * @param fontSize    preferred font size(not guaranteed)
     * @return Y height of the last line in the document
     */
    public float createTable(PDRectangle pdRectangle, float fontSize) throws IOException, InvocationTargetException, IllegalAccessException {
        setCurrentFontSize(fontSize);
        PDPage page;
        if (pageWithSVG != null) {
            pdRectangle = pageWithSVG.getMediaBox();
            page = pageWithSVG;
            this.newLineYHeight = page.getMediaBox().getHeight() - firstPageAdditionalTopPadding - DEFAULT_PAGE_VERTICAL_MARGIN;
            pageWithSVG = null;
        } else {
            page = new PDPage(pdRectangle);
            this.newLineYHeight = page.getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
            document.addPage(page);
        }
        PDPageContentStream stream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
        float startX = DEFAULT_PAGE_SIDE_MARGIN;
        float tableMaxWidth = pdRectangle.getWidth() - (DEFAULT_PAGE_SIDE_MARGIN * 2);
        float lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;

        adjustFont(tableMaxWidth, stream);
        int rowNumber = 1;
        stream = drawTableHeader(stream, startX, rowHeight, lastRawPosition);
        if (pageChanged) this.newLineYHeight = page.getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
        stream = drawTableRaws(stream, startX, rowHeight, rowNumber, lastRawPosition);
        pageChanged = false;



        /* TODO
            Algorithm:
            before starting to draw table we should calculate:
            1. Table column quantity.
                max width for every column with default font size and default paddings.
                    Summarize columns width to obtain table width. If obtained width wider than page width - adjust table font.
            2. Calculate table height.
               Draw table headers. Headers position centered. Content apply toUpperCase();
            3. Draw table content. lower case.To numerate each row.
                If cursor reached end of the page - create new PDPage, draw header on the new page and continue
                fulfill table.
         */

        stream.close();
        return newLineYHeight - rowHeight;
    }

    private PDPageContentStream drawTableRaws(PDPageContentStream stream, float startX, float rowHeight, int rowNumber, float lastRawPosition) throws InvocationTargetException, IllegalAccessException, IOException {
        for (T t : tableRows) {
            this.newLineYHeight = newLineYHeight - rowHeight;
            if (lastRawPosition > newLineYHeight) {
                stream.close();
                stream = addNextPage(stream);
                this.newLineYHeight = document.getPage(0).getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
                startX = DEFAULT_PAGE_SIDE_MARGIN;
                lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;
                stream = drawTableHeader(stream, startX, rowHeight, lastRawPosition);
                this.newLineYHeight = newLineYHeight - rowHeight;
            }
            drawRow(stream, cellMargin, startX, t);
            rowNumber++;
        }
        return stream;
    }

    private void drawRow(PDPageContentStream stream, float cellMargin, float startX, T t) throws InvocationTargetException, IllegalAccessException, IOException {
        float nextRowYHeight = newLineYHeight;
        for (String string : tableHeaders.keySet()) {
            Object content = tableHeaders.get(string).invoke(t);
            if (content == null) content = "";
            if (svgPresent && svgFieldsWidthMap.containsKey(string)) {
                nextRowYHeight = PDFUtil.drawSVGStructure(document, startX, content.toString(), cellMargin, cellMargin, svgFieldsWidthMap.get(string), newLineYHeight, this);
                startX = startX + svgFieldsWidthMap.get(string);
            } else {
                startX = drawCell(stream, startX, string, rowHeight, content.toString(), tableWidthCoefficient);
            }
        }
        if (svgPresent) {
            newLineYHeight = nextRowYHeight;
        }
    }

    private PDPageContentStream drawTableHeader(PDPageContentStream stream, float startX, float rowHeight, float lastRawPosition) throws IOException {
        drawingHeaders = true;
        if (lastRawPosition > newLineYHeight) {
            stream = addNextPage(stream);
            pageChanged = true;
            this.newLineYHeight = document.getPage(0).getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
            startX = DEFAULT_PAGE_SIDE_MARGIN;
            lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;
            drawTableHeader(stream, startX, rowHeight, lastRawPosition);
        }
        if (svgPresent) {
            Comparator<String> comparator = Comparator.comparing(entry -> svgFieldsWidthMap.containsKey(entry) ? 0 : 1);
            tableHeaders = tableHeaders.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey(comparator))
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            )
                    );
        }

        for (var string : tableHeaders.keySet()) {
            startX = drawCell(stream, startX, string, rowHeight, string, tableWidthCoefficient);
        }
        drawingHeaders = false;
        return stream;
    }

    private PDPageContentStream addNextPage(PDPageContentStream stream) throws IOException {
        stream.close();
        PDPage newPage = new PDPage(document.getPage(0).getMediaBox());
        document.addPage(newPage);
        stream = new PDPageContentStream(document, newPage, PDPageContentStream.AppendMode.APPEND, true);
        stream.setFont(font, currentFontSize);
        return stream;
    }

    /**
     * Evaluate the acceptance of current font settings and adjust if it needed in order to allocate table on the current page
     *
     * @param tableMaxWidth - max theoretical width of the table
     * @param stream        - current document page, where the table will arise
     */
    private void adjustFont(float tableMaxWidth, PDPageContentStream stream) throws IOException, IllegalArgumentException {
        stream.setFont(font, currentFontSize);
        for (String columnName : tableHeaders.keySet()) {
            if (svgPresent && svgFieldsWidthMap.containsKey(columnName)) {
                columnWidthMap.put(columnName, svgFieldsWidthMap.get(columnName));
            } else {
                float maxColumnValue = findMaxColumnWidth(columnName);
                columnWidthMap.put(columnName, (maxColumnValue / 1000) * currentFontSize);
            }
        }
        double totalWidth = calculateTotalWidth();
        while (totalWidth > tableMaxWidth) {
            setCurrentFontSize(--currentFontSize);
            adjustFont(tableMaxWidth, stream);
            totalWidth = calculateTotalWidth();
        }
        tableWidthCoefficient = (float) (tableMaxWidth / totalWidth);
    }

    private double calculateTotalWidth() {
        return columnWidthMap.values().stream()
                .mapToDouble(Float::doubleValue)
                .sum() + (cellMargin * 2 * columnWidthMap.size());
    }

    private float findMaxColumnWidth(String columnName) throws IOException {
        float columnNameWidth = font.getStringWidth(columnName);
        return tableRows.stream()
                .map(row -> {
                    try {
                        return tableHeaders.get(columnName).invoke(row);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(Objects::nonNull)
                .map(Object::toString)
                .map((value) -> {
                    try {
                        return font.getStringWidth(value);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .max(Comparator.comparingDouble(value -> value))
                .filter(result -> result.compareTo(columnNameWidth) > 0)
                .orElse(columnNameWidth);
    }

    /**
     *
     */
    public float drawCell(PDPageContentStream contentStream, float x, String column, float rowHeight, String content, float tableWidthCoefficient) throws IOException {
        if (svgFieldsWidthMap.containsKey(column)) tableWidthCoefficient = 1;
        float width = columnWidthMap.get(column);
        contentStream.addRect(x, newLineYHeight - rowHeight, width * tableWidthCoefficient + 2 * cellMargin * tableWidthCoefficient, rowHeight);

        contentStream.setStrokingColor(Color.black);
        contentStream.setLineWidth(DEFAULT_LINE_WIDTH);
        contentStream.stroke();
        if (drawingHeaders) {
            contentStream.addRect(x + DEFAULT_LINE_WIDTH, newLineYHeight - rowHeight + DEFAULT_LINE_WIDTH, width * tableWidthCoefficient + 2 * cellMargin * tableWidthCoefficient - 2 * DEFAULT_LINE_WIDTH, rowHeight - 2 * DEFAULT_LINE_WIDTH);
            contentStream.setNonStrokingColor(Color.lightGray);
            contentStream.fill();
        }

        contentStream.setFont(font, currentFontSize);
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.black);
        contentStream.newLineAtOffset(x + calcCenteredTextStartingPosition(width, content), (newLineYHeight - rowHeight + cellMargin));
        contentStream.showText(content);
        contentStream.endText();
        return x + (columnWidthMap.get(column) + 2 * cellMargin) * tableWidthCoefficient;
    }

    private float calcCenteredTextStartingPosition(float width, String content) throws IOException {
        float adjustedCellMargin = cellMargin * tableWidthCoefficient;
        float adjustedCurrentColumnCenterPosition = (width * tableWidthCoefficient) / 2;
        float currentContentHalfWidth = (font.getStringWidth(content) / 2 / 1000 * currentFontSize);
        return (adjustedCellMargin) + (adjustedCurrentColumnCenterPosition - currentContentHalfWidth);
    }

    private static float getSizeAttributeValue(String source, String attribute) throws RuntimeException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        try {
            SVGDocument svgDocument = factory.createSVGDocument(null, new StringReader(source));
            Element element = svgDocument.getDocumentElement();
            String value = element.getAttribute(attribute);
            Pattern pattern = Pattern.compile("\\b\\d+(\\.\\d+)?");
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                return Float.parseFloat(matcher.group()) / (DEFAULT_A4_PIXEL_WIDTH / PDRectangle.A4.getWidth());
            } else {
                throw new RuntimeException("PROBLEM WITH SVG ALLOCATION");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


