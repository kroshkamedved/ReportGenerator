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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    private static final String REGEX_FOR_MOL_NAMES = "\\[|\\]|\\)|\\s|\\((?=[a-zA-Z])|-|";//TODO CORRECT REGULAR EXPRESSION
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
    private float headerRowHeight;
    private final Map<T, Float> multiLineFieldsHeight = new HashMap<>();
    private final Map<T, Set<String>> multiRowColumns = new HashMap<>();
    private float currentFontHeight;


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
        this.currentFontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * currentFontSize;
        this.cellMargin = currentFontHeight * 0.5f;
        this.headerRowHeight = (font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * currentFontSize + cellMargin;
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
                Optional<Float> maxCurrentColumnSVGWidth = calculateSVGWidth(field, "width");
                maxCurrentColumnSVGWidth.ifPresent(aFloat -> svgFieldsWidthMap.put(methodName, aFloat));
            }
            if (svgPresent && svgFieldsWidthMap.isEmpty()) svgPresent = false;
            Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).findAny().ifPresent(method -> tableHeaders.put(field.getName(), method));
        }
    }

    //TODO check this logic
    private Optional<Float> calculateSVGWidth(Field field, String attribute) {
        field.setAccessible(true);
        return tableRows.stream()
                .map(t -> {
                    try {
                        return (String) field.get(t);
                    } catch (IllegalAccessException ilg) {
                        throw new RuntimeException("SVG width calculation problem");
                    }
                }).filter(Objects::nonNull)
                .map(svg -> getSizeAttributeValueFromSVG(svg, attribute))
                .max(Float::compare)
                .map(a -> Float.min(a, maxSvgColumnWidth));
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
        float tableMaxWidth = Math.round(pdRectangle.getWidth() - (DEFAULT_PAGE_SIDE_MARGIN * 2));
        float lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;

        adjustFont(tableMaxWidth, stream);
        int rowNumber = 1;
        stream = drawTableHeader(stream, startX, headerRowHeight, lastRawPosition);
        if (pageChanged) {
            this.newLineYHeight = page.getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
        }
        stream = drawTableRaws(stream, startX, rowHeight, rowNumber, lastRawPosition);
        pageChanged = false;
        stream.close();
        return newLineYHeight;
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
        for (String columnName : tableHeaders.keySet()) {
            Object content = tableHeaders.get(columnName).invoke(t);
            if (content == null) content = "";
            if (svgPresent && svgFieldsWidthMap.containsKey(columnName)) {
                float multiRowMaxColumnHeight = 0f;
                if (multiLineFieldsHeight.containsKey(t)) {
                    multiRowMaxColumnHeight = multiLineFieldsHeight.get(t);
                }
                nextRowYHeight = PDFUtil.drawSVGStructure(document, startX, content.toString(), cellMargin, cellMargin, svgFieldsWidthMap.get(columnName), newLineYHeight, this, multiRowMaxColumnHeight);
                startX = startX + svgFieldsWidthMap.get(columnName) + cellMargin * 2;
            } else {
                //TODO debug and prevent add column separation + calculate width before row draw
                if (multiRowColumns.containsKey(t) && multiRowColumns.get(t).contains(columnName)) {
                    //TODO DEBUG strangePostMan results  if(nextRowYHeight == newLineYHeight) nextRowYHeight = multiLineFieldsHeight.get(t);
                    startX = drawMultiRowCell(stream, startX, columnName, newLineYHeight - nextRowYHeight, content.toString(), tableWidthCoefficient, REGEX_FOR_MOL_NAMES, t);
                } else {
                    startX = drawCell(stream, startX, columnName, newLineYHeight - nextRowYHeight, content.toString(), tableWidthCoefficient);
                }
            }
        }
        if (svgPresent) {
            newLineYHeight = nextRowYHeight;
            //TODO compare svg height with multiline rows
        }
    }

    private float drawMultiRowCell(PDPageContentStream stream, float startX, String columnName, float rowHeight, String content, float tableWidthCoefficient, String regex, T t) throws IOException {
        float currentColumnMaxWidth = columnWidthMap.get(columnName);
        List<String> tmpList = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        StringBuilder sb = new StringBuilder();
        int start = 0;
        int end = 0;
        while (matcher.find()) {
            end = matcher.end();
            String currentSubstring = content.substring(start, end);
            try {
                if (calcStringWidth(sb + currentSubstring + (2 * cellMargin)) < currentColumnMaxWidth) {
                    sb.append(currentSubstring);
                    start = end;
                } else {
                    tmpList.add(sb.toString());
                    sb.setLength(0);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!sb.isEmpty()) tmpList.add(sb.toString());
        //  splitRegexNotMatchedContentResidue(tmpList, end, content, currentColumnMaxWidth);
        String[] words = new String[tmpList.size()];
        words = tmpList.toArray(words);
        drawRectangle(columnName, stream, startX, newLineYHeight, rowHeight, tableWidthCoefficient);
        // float currentContentHeight = multiLineFieldsHeight.get(t);
        // float verticalCenterPoint = calcCenteredTextVerticalPosition(newLineYHeight, currentContentHeight, cellMargin, content);
        float contentHeight = multiLineFieldsHeight.get(t);
        float verticalPadding = (rowHeight - contentHeight) / 2;
        float currentStartPoint = newLineYHeight - rowHeight + verticalPadding + contentHeight;
        for (var line : words) {
            drawMultiRowContent(stream, startX, columnName, currentStartPoint - headerRowHeight + cellMargin, line, tableWidthCoefficient); // TODO hard calculations almost hardcoding
            currentStartPoint = currentStartPoint - headerRowHeight;
        }
        return startX + (columnWidthMap.get(columnName) + 2 * cellMargin) * tableWidthCoefficient;
    }

    private int calcResidueRows(int end, String content, float currentColumnMaxWidth) throws IOException {
        List<String> substrings = new ArrayList<>();
        splitRegexNotMatchedContentResidue(substrings, end, content, currentColumnMaxWidth);
        return substrings.size();
    }

    private void splitRegexNotMatchedContentResidue(List<String> tmpList, int end, String content, float currentColumnMaxWidth) throws IOException {
        String residue = content.substring(end);
        float residueTextWidth = calcStringWidth(residue);
        if (residueTextWidth > 0) {
            double rowsNumber = Math.ceil(residueTextWidth / currentColumnMaxWidth - (2 * cellMargin));
            int rowSymbolsQuantity = (int) Math.floor(residue.length() / rowsNumber);
            end = 0;
            for (int i = 0; i < rowsNumber; i++) {
                if (i == rowsNumber - 1) {
                    tmpList.add(residue.substring(end));
                } else {
                    tmpList.add(residue.substring(0, rowSymbolsQuantity));
                    end = end + rowSymbolsQuantity;
                }
            }
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
        this.newLineYHeight = newLineYHeight - rowHeight;
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
        float totalWidth = Math.round(calculateTotalWidth());
        while (totalWidth > tableMaxWidth) {
            if (currentFontSize == DEFAULT_MIN_FONT_SIZE) {
                adjustLargestRows(tableMaxWidth);
                totalWidth = Math.round(calculateTotalWidth());
                break;
            }
            setCurrentFontSize(--currentFontSize);
            adjustFont(tableMaxWidth, stream);
            totalWidth = Math.round(calculateTotalWidth());
        }
        float totalRowSVGWidth = Math.round(svgFieldsWidthMap.values().stream().reduce(0.0f, Float::sum));
        tableWidthCoefficient = (tableMaxWidth - totalRowSVGWidth) / (totalWidth - totalRowSVGWidth);
    }

    private void adjustLargestRows(float tableMaxWidth) {
        float totalWidth = Math.round(calculateTotalWidth());
        float marginsTotalLength = tableHeaders.size() * (2 * cellMargin);
        float acceptableColumnSize = (tableMaxWidth - marginsTotalLength) / tableHeaders.size();
        Set<String> columns = new HashSet<>();
        while (totalWidth > tableMaxWidth) {
            Optional<Map.Entry<String, Float>> widestColumn = columnWidthMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue());
            for (var row : tableRows) {
                try {
                    String rowWidestColumnValue = tableHeaders.get(widestColumn.get().getKey()).invoke(row).toString();
                    int rowNumber = calcRowNumber(rowWidestColumnValue, acceptableColumnSize);
                    float currentColumnMinHeight = rowNumber * (currentFontHeight + cellMargin);
                    if (rowNumber > 1) {
                        columnWidthMap.put(widestColumn.get().getKey(), acceptableColumnSize);
                        columns.add(widestColumn.get().getKey());
                        if (multiLineFieldsHeight.containsKey(row)) {
                            currentColumnMinHeight = currentColumnMinHeight > multiLineFieldsHeight.get(row) ? currentColumnMinHeight : multiLineFieldsHeight.get(row);
                        }
                        multiRowColumns.put(row, columns);
                        multiLineFieldsHeight.put(row, currentColumnMinHeight);
                        totalWidth = Math.round(calculateTotalWidth());
                    }
                } catch (InvocationTargetException | IllegalAccessException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private int calcRowNumber(String rowWidestColumnValue, float averageColumnWidth) throws IOException {
        Pattern pattern = Pattern.compile(REGEX_FOR_MOL_NAMES);
        Matcher matcher = pattern.matcher(rowWidestColumnValue);
        StringBuilder sb = new StringBuilder();
        if (calcStringWidth(rowWidestColumnValue) > averageColumnWidth) {
            int rowQuantity = 0;
            int start = 0;
            int end = 0;
            while (matcher.find()) {
                end = matcher.end();
                String currentSubstring = rowWidestColumnValue.substring(start, end);
                try {
                    if (calcStringWidth(sb + currentSubstring + (2 * cellMargin)) < averageColumnWidth) {
                        sb.append(currentSubstring);
                        start = end;
                    } else {
                        rowQuantity++;
                        sb.setLength(0);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!sb.isEmpty()) ++rowQuantity;
            //  if (rowQuantity != 0) rowQuantity = rowQuantity + calcResidueRows(end, rowWidestColumnValue, averageColumnWidth);
            return rowQuantity == 0 ? 1 : rowQuantity;
        } else return 1;
    }

    private float calculateTotalWidth() {
        return columnWidthMap.values().stream()
                .reduce(0.0f, Float::sum) + (cellMargin * 2 * columnWidthMap.size());
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
    public float drawCell(PDPageContentStream contentStream, float x, String columnName, float rowHeight, String content, float tableWidthCoefficient) throws IOException {
        drawRectangle(columnName, contentStream, x, newLineYHeight, rowHeight, tableWidthCoefficient);
        return drawContent(contentStream, x, columnName, rowHeight, content, tableWidthCoefficient);
    }

    private float drawContent(PDPageContentStream contentStream, float startX, String columnName, float rowHeight, String content, float tableWidthCoefficient) throws IOException {
        float width = columnWidthMap.get(columnName);
        float contentDependentHorizontalShift = calcCenteredTextStartingPosition(width, content);
        if (svgFieldsWidthMap.containsKey(columnName)) {
            contentDependentHorizontalShift = contentDependentHorizontalShift / tableWidthCoefficient;
            tableWidthCoefficient = 1;
        }
        contentStream.setFont(font, currentFontSize);
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.black);
        contentStream.newLineAtOffset(startX + contentDependentHorizontalShift, calcCenteredTextVerticalPosition(newLineYHeight, rowHeight, cellMargin, content));
        contentStream.showText(content);
        contentStream.endText();
        return startX + (columnWidthMap.get(columnName) + 2 * cellMargin) * tableWidthCoefficient;
    }

    private void drawMultiRowContent(PDPageContentStream contentStream, float startX, String columnName, float startY, String content, float tableWidthCoefficient) throws IOException {
        float width = columnWidthMap.get(columnName);
        float contentDependentHorizontalShift = calcCenteredTextStartingPosition(width, content);
        if (svgFieldsWidthMap.containsKey(columnName)) {
            contentDependentHorizontalShift = contentDependentHorizontalShift / tableWidthCoefficient;
        }
        contentStream.setFont(font, currentFontSize);
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.black);
        contentStream.newLineAtOffset(startX + contentDependentHorizontalShift, startY);
        contentStream.showText(content);
        contentStream.endText();
    }

    private void drawRectangle(String columnName, PDPageContentStream contentStream, float startX, float newLineYHeight, float rowHeight, float tableWidthCoefficient) throws IOException {
        if (svgFieldsWidthMap.containsKey(columnName)) tableWidthCoefficient = 1;
        float width = columnWidthMap.get(columnName);
        contentStream.addRect(startX, newLineYHeight - rowHeight, width * tableWidthCoefficient + 2 * cellMargin * tableWidthCoefficient, rowHeight);

        contentStream.setStrokingColor(Color.black);
        contentStream.setLineWidth(DEFAULT_LINE_WIDTH);
        contentStream.stroke();
        if (drawingHeaders) {
            contentStream.addRect(startX + DEFAULT_LINE_WIDTH, newLineYHeight - rowHeight + DEFAULT_LINE_WIDTH, width * tableWidthCoefficient + 2 * cellMargin * tableWidthCoefficient - 2 * DEFAULT_LINE_WIDTH, rowHeight - 2 * DEFAULT_LINE_WIDTH);
            contentStream.setNonStrokingColor(Color.lightGray);
            contentStream.fill();
        }
    }

    private float calcCenteredTextVerticalPosition(float newLineYHeight, float rowHeight, float cellMargin, String content) {
        float defaultRowHeight = headerRowHeight - (2 * cellMargin);
        return newLineYHeight - rowHeight + (rowHeight / 2) - (defaultRowHeight / 2);
    }

    private float calcCenteredTextStartingPosition(float width, String content) throws IOException {
        float adjustedCellMargin = cellMargin * tableWidthCoefficient;
        float adjustedCurrentColumnCenterPosition = (width * tableWidthCoefficient) / 2;
        float currentContentHalfWidth = calcStringWidth(content) / 2;
        return (adjustedCellMargin) + (adjustedCurrentColumnCenterPosition - currentContentHalfWidth);
    }

    private float calcStringWidth(String content) throws IOException {
        return font.getStringWidth(content) / 1000 * currentFontSize;
    }

    private static float getSizeAttributeValueFromSVG(String source, String attribute) throws RuntimeException {
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


