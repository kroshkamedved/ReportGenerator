package com.en.reportgenerator.util;

import com.en.reportgenerator.domain.AllFieldsToStringReady;
import com.en.reportgenerator.domain.SVGColumn;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
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
    private static final String REGEX_FOR_MOL_NAMES = "\\[|\\]|\\)|\\s|\\((?=[a-zA-Z])|-|";
    private static float DEFAULT_MIN_FONT_SIZE = 6.0f;
    private static final int DEFAULT_A4_PIXEL_WIDTH = 842;

    private final PDDocument document;
    private PDPage pageWithSVG;
    private PDFont font;
    private final List<T> tableRows;
    private final Class<T> clazz;
    private Map<String, Method> tableHeaders;
    private Map<String, Float> columnWidthMap;
    private Map<String, Float> svgFieldsWidthMap;
    private final Map<T, Float> multiLineFieldsHeight = new HashMap<>();
    private final Map<T, Set<String>> multiRowColumns = new HashMap<>();
    private final Map<T, Map<String, Float>> specificRowColumnContentHeight = new HashMap<>();
    private float rowHeight;
    private float firstPageAdditionalTopPadding;
    private boolean pageChanged;
    private float currentFontSize;
    private float tableWidthCoefficient = 1;
    private float cellMargin;
    private float currentYTopValue;
    private boolean drawingHeaders;
    private boolean svgPresent;
    private final float maxSvgColumnWidth;
    private float headerRowHeight;
    private float currentFontHeight;


    public TableGenerator(List<T> tableRows, Class<T> clazz, PDDocument document, float firstPageAdditionalTopPadding, PDFont font) {
        this.tableRows = tableRows;
        this.clazz = clazz;
        this.document = document;
        tableHeaders = new TreeMap<>();
        columnWidthMap = new HashMap<>();
        this.font = font;
        this.pageWithSVG = document.getPage(document.getNumberOfPages() - 1);
        this.firstPageAdditionalTopPadding = firstPageAdditionalTopPadding;
        this.maxSvgColumnWidth = pageWithSVG.getMediaBox().getWidth() * 0.15f;
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
        prepareMultiRowColumnsMap(tableRows);
    }

    public void setCurrentFontSize(float currentFontSize) {
        this.currentFontSize = currentFontSize;
        this.currentFontHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * currentFontSize;
        this.cellMargin = currentFontHeight * 0.5f;
        this.headerRowHeight = (font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * currentFontSize + cellMargin;
    }

    private void prepareMultiRowColumnsMap(List<T> tableRows) {
        for (var row : tableRows) {
            multiRowColumns.put(row, new HashSet<>());
            specificRowColumnContentHeight.put(row, new HashMap<>());
        }
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
            Optional<String> notEmptyColumn = tableRows.stream()
                    .map(obj -> {
                        try {
                            field.setAccessible(true);
                            return field.get(obj);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(str -> !str.isEmpty())
                    .findAny();
            if (notEmptyColumn.isPresent()) {
                Arrays.stream(methods)
                        .filter(method -> method.getName().equals(methodName))
                        .findAny()
                        .ifPresent(method -> tableHeaders.put(field.getName(), method));
            }
        }
    }

    private Optional<Float> calculateSVGWidth(Field field, String width) {
        field.setAccessible(true);
        float fieldNameWidth = 0f;
        try {
            fieldNameWidth = calcStringWidth(field.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        float nameWidth = fieldNameWidth;
        return tableRows.stream()
                .map(t -> {
                    try {
                        return (String) field.get(t);
                    } catch (IllegalAccessException ilg) {
                        throw new RuntimeException("SVG width calculation problem");
                    }
                }).filter(Objects::nonNull)
                .map(svg -> getSizeAttributeValueFromSVG(svg, width))
                .max(Float::compare)
                .map(a -> Float.min(a, maxSvgColumnWidth))
                .map(a -> Float.max(a, nameWidth));
    }

    /**
     * Draw table on the page.
     *
     * @param pdRectangle page size
     * @param fontSize    preferred font size(not guaranteed)
     * @return Y height of the last line in the document
     */
    public float createTable(PDRectangle pdRectangle, float fontSize) {
        setCurrentFontSize(fontSize);
        initialize();
        PDPage page;
        if (pageWithSVG != null) {
            pdRectangle = pageWithSVG.getMediaBox();
            page = pageWithSVG;
            this.currentYTopValue = page.getMediaBox().getHeight() - firstPageAdditionalTopPadding - DEFAULT_PAGE_VERTICAL_MARGIN;
            pageWithSVG = null;
        } else {
            page = new PDPage(pdRectangle);
            this.currentYTopValue = page.getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
            document.addPage(page);
        }
        try {
            PDPageContentStream stream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
            float startX = DEFAULT_PAGE_SIDE_MARGIN;
            float tableMaxWidth = Math.round(pdRectangle.getWidth() - (DEFAULT_PAGE_SIDE_MARGIN * 2));
            float lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;

            adjustFont(tableMaxWidth, stream);
            if (currentYTopValue < DEFAULT_PAGE_VERTICAL_MARGIN + headerRowHeight + PDRectangle.A4.getHeight() / 9)
                currentYTopValue = -1;
            stream = drawTableHeader(stream, startX, headerRowHeight, lastRawPosition);
            stream = drawTableRows(stream, startX, lastRawPosition);
            pageChanged = false;
            stream.close();
            return currentYTopValue;
        } catch (IOException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("problem during table generation");
        }
    }

    private PDPageContentStream drawTableRows(PDPageContentStream stream, float startX, float lastRawPosition) throws InvocationTargetException, IllegalAccessException, IOException {
        for (T t : tableRows) {
            float currentRowMaxHeight = headerRowHeight;
            if (specificRowColumnContentHeight.containsKey(t)) {
                currentRowMaxHeight = specificRowColumnContentHeight.get(t).values().stream()
                        .max(Float::compareTo)
                        .orElse(headerRowHeight);
            }
            if (lastRawPosition > currentYTopValue - currentRowMaxHeight) {
                stream.close();
                stream = addNextPage(stream);
                this.currentYTopValue = document.getPage(0).getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
                startX = DEFAULT_PAGE_SIDE_MARGIN;
                lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;
                stream = drawTableHeader(stream, startX, headerRowHeight, lastRawPosition);
                this.currentYTopValue = currentYTopValue - rowHeight;
            }
            float yPositionBeforeRowDrawn = currentYTopValue;
            drawRow(stream, cellMargin, startX, t);
            if (yPositionBeforeRowDrawn == currentYTopValue) currentYTopValue = currentYTopValue - headerRowHeight;
        }
        return stream;
    }

    private void drawRow(PDPageContentStream stream, float cellMargin, float startX, T t) throws InvocationTargetException, IllegalAccessException, IOException {
        float multiRowMaxCellHeight = 0f;
        float nextRowYHeight = currentYTopValue - headerRowHeight;
        for (String columnName : tableHeaders.keySet()) {
            Object content = tableHeaders.get(columnName).invoke(t);
            if (content == null) {
                content = "";
            }
            if (svgPresent && svgFieldsWidthMap.containsKey(columnName)) {

                if (multiLineFieldsHeight.containsKey(t)) {
                    multiRowMaxCellHeight = multiLineFieldsHeight.get(t);
                }
                nextRowYHeight = PDFUtil.drawSVG(document, startX, content.toString(), cellMargin, cellMargin, columnWidthMap.get(columnName), currentYTopValue, multiRowMaxCellHeight);
                startX = startX + columnWidthMap.get(columnName) + cellMargin * 2;
            } else {
                if (multiLineFieldsHeight.containsKey(t) && !svgPresent) {
                    multiRowMaxCellHeight = multiLineFieldsHeight.get(t);
                    float rowHeight = Float.max(multiRowMaxCellHeight, headerRowHeight);
                    nextRowYHeight = currentYTopValue - rowHeight;
                }
                if (multiRowColumns.get(t).contains(columnName)) {
                    startX = drawMultiRowCell(stream, startX, columnName, currentYTopValue - nextRowYHeight, content.toString(), tableWidthCoefficient, REGEX_FOR_MOL_NAMES, t);
                } else {
                    startX = drawCell(stream, startX, columnName, currentYTopValue - nextRowYHeight, content.toString(), tableWidthCoefficient);
                }
            }
        }
        if (svgPresent || nextRowYHeight != headerRowHeight) {
            currentYTopValue = nextRowYHeight;
        }
    }

    private float drawMultiRowCell(PDPageContentStream stream, float startX, String columnName, float rowHeight, String content, float tableWidthCoefficient, String regex, T t) throws IOException {
        float currentColumnMaxWidth = columnWidthMap.get(columnName) * tableWidthCoefficient;
        List<String> tmpList = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        StringBuilder sb = new StringBuilder();
        int start = 0;
        int end;
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
        String[] words = new String[tmpList.size()];
        words = tmpList.toArray(words);
        drawRectangle(columnName, stream, startX, currentYTopValue, rowHeight, tableWidthCoefficient);
        float contentHeight = multiLineFieldsHeight.get(t);
        if (specificRowColumnContentHeight.containsKey(t) && specificRowColumnContentHeight.get(t).containsKey(columnName)) {
            contentHeight = specificRowColumnContentHeight.get(t).get(columnName);
        }
        float verticalPadding = (rowHeight - contentHeight) / 2;
        float currentStartPoint = currentYTopValue - rowHeight + verticalPadding + contentHeight;
        for (var line : words) {
            drawMultiRowContent(stream, startX, columnName, currentStartPoint - headerRowHeight + cellMargin, line, tableWidthCoefficient); // TODO hard calculations almost hardcoding
            currentStartPoint = currentStartPoint - headerRowHeight;
        }
        return startX + (columnWidthMap.get(columnName) + 2 * cellMargin) * tableWidthCoefficient;
    }

    private PDPageContentStream drawTableHeader(PDPageContentStream stream, float startX, float rowHeight, float lastRawPosition) throws IOException {
        drawingHeaders = true;
        Optional<T> first = tableRows.stream().findFirst();
        float minNextRowHeight = headerRowHeight;
        if (first.isPresent() && specificRowColumnContentHeight.containsKey(first.get())) {
            minNextRowHeight = minNextRowHeight + specificRowColumnContentHeight.get(first.get()).values().stream().max(Float::compareTo).orElse(0f);
        }
        if (lastRawPosition > currentYTopValue - minNextRowHeight) {
            stream = addNextPage(stream);
            pageChanged = true;
            this.currentYTopValue = document.getPage(0).getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
            startX = DEFAULT_PAGE_SIDE_MARGIN;
            lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;
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
        this.currentYTopValue = currentYTopValue - rowHeight;
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
                float currentColumnMaxWidthValue = Float.max(svgFieldsWidthMap.get(columnName), calcStringWidth(columnName));
                columnWidthMap.put(columnName, currentColumnMaxWidthValue);
            } else {
                float maxColumnValue = findMaxColumnWidth(columnName);
                columnWidthMap.put(columnName, (maxColumnValue / 1000) * currentFontSize);
            }
        }
        float totalWidth = calculateTotalWidth();
        while (totalWidth > tableMaxWidth) {
            if (currentFontSize == DEFAULT_MIN_FONT_SIZE) {
                adjustLargestRows(tableMaxWidth);
                totalWidth = Math.round(calculateTotalWidth());
                break;
            }
            setCurrentFontSize(--currentFontSize);
            adjustFont(tableMaxWidth, stream);
            totalWidth = (calculateTotalWidth());
        }
        float totalRowSVGWidth = (svgFieldsWidthMap.values().stream().reduce(0.0f, (f1, f2) -> f1 + (2 * cellMargin) + f2));
        tableWidthCoefficient = (tableMaxWidth - totalRowSVGWidth) / (totalWidth - totalRowSVGWidth);
    }

    private void adjustLargestRows(float tableMaxWidth) {
        float totalWidth = Math.round(calculateTotalWidth());
        float marginsTotalLength = tableHeaders.size() * (2 * cellMargin);
        float totalRowSVGWidth = (svgFieldsWidthMap.values().stream().reduce(0.0f, (f1, f2) -> f1 + (2 * cellMargin) + f2));
        do {
            Optional<Map.Entry<String, Float>> widestColumn = columnWidthMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue());
            String columnName = widestColumn.get().getKey();
            for (var row : tableRows) {
                float acceptableColumnSize = tableMaxWidth - marginsTotalLength - columnWidthMap.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(columnName))
                        .map(Map.Entry::getValue)
                        .reduce(0f, Float::sum);
                if (acceptableColumnSize < 0 || acceptableColumnSize > tableMaxWidth / 3) {
                    acceptableColumnSize = DEFAULT_SVG_COLUMN_HEIGHT;
                }
                try {
                    String rowWidestColumnValue = tableHeaders.get(columnName).invoke(row).toString();
                    columnWidthMap.put(columnName, acceptableColumnSize);
                    totalWidth = Math.round(calculateTotalWidth());
                    float coefficient = (tableMaxWidth - totalRowSVGWidth) / (totalWidth - totalRowSVGWidth);
                    tableWidthCoefficient = coefficient > 1 ? coefficient : 1;
                    int rowNumber = calcRowNumber(rowWidestColumnValue, acceptableColumnSize * tableWidthCoefficient);
                    float currentColumnMinHeight = rowNumber * (currentFontHeight + cellMargin);
                    specificRowColumnContentHeight.get(row).put(columnName, currentColumnMinHeight);
                    if (rowNumber > 1) {
                        multiRowColumns.get(row).add(columnName);
                        if (multiLineFieldsHeight.containsKey(row)) {
                            currentColumnMinHeight = currentColumnMinHeight > multiLineFieldsHeight.get(row) ? currentColumnMinHeight : multiLineFieldsHeight.get(row);
                        }
                        multiLineFieldsHeight.put(row, currentColumnMinHeight);
                    }
                } catch (InvocationTargetException | IllegalAccessException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } while (totalWidth > tableMaxWidth);
    }

    private int calcRowNumber(String rowWidestColumnValue, float averageColumnWidth) throws IOException {
        Pattern pattern = Pattern.compile(REGEX_FOR_MOL_NAMES);
        Matcher matcher = pattern.matcher(rowWidestColumnValue);
        StringBuilder sb = new StringBuilder();
        if (calcStringWidth(rowWidestColumnValue) > averageColumnWidth) {
            int rowQuantity = 0;
            int start = 0;
            int end;
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
        drawRectangle(columnName, contentStream, x, currentYTopValue, rowHeight, tableWidthCoefficient);
        return drawContent(contentStream, x, columnName, rowHeight, content, tableWidthCoefficient);
    }

    private float drawContent(PDPageContentStream contentStream, float startX, String columnName, float rowHeight, String content, float tableWidthCoefficient) throws IOException {
        float width = columnWidthMap.get(columnName);
        if (svgFieldsWidthMap.containsKey(columnName)) tableWidthCoefficient = 1;
        float contentDependentHorizontalShift = calcCenteredTextStartingPosition(width, content, tableWidthCoefficient);
        if (svgFieldsWidthMap.containsKey(columnName)) {
            contentDependentHorizontalShift = contentDependentHorizontalShift / tableWidthCoefficient;
            tableWidthCoefficient = 1;
        }
        contentStream.setFont(font, currentFontSize);
        contentStream.beginText();
        contentStream.setNonStrokingColor(Color.black);
        contentStream.newLineAtOffset(startX + contentDependentHorizontalShift, calcCenteredTextVerticalPosition(currentYTopValue, rowHeight, cellMargin, content));
        contentStream.showText(content);
        contentStream.endText();
        return startX + (columnWidthMap.get(columnName) + 2 * cellMargin) * tableWidthCoefficient;
    }

    private void drawMultiRowContent(PDPageContentStream contentStream, float startX, String columnName, float startY, String content, float tableWidthCoefficient) throws IOException {
        float width = columnWidthMap.get(columnName);
        float contentDependentHorizontalShift = calcCenteredTextStartingPosition(width, content, tableWidthCoefficient);
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

    private float calcCenteredTextStartingPosition(float width, String content, float tableWidthCoefficient) throws IOException {
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


