package com.example.reportgenerator;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@RequiredArgsConstructor
@Setter
public class TableGenerator<T extends Record & AllFieldsToStringReady> {
    private static final float DEFAULT_PAGE_VERTICAL_MARGIN = 30f;
    private static final float DEFAULT_PAGE_SIDE_MARGIN = 20f;
    private static final float DEFAULT_LINE_WIDTH = 2.0f;

    private final PDDocument document;
    private final List<T> tableRows;
    private final Class<T> clazz;
    private final SortedMap<String, Method> tableHeaders;
    private final Map<String, Float> columnWidthMap;
    private PDType1Font font;
    private float rowHeight;
    private PDPage pageWithSVG;
    private float calculatedSVGHeight;

    public TableGenerator(List<T> tableRows, Class<T> clazz, PDDocument document, float calculatedSVGHeight) {
        this.tableRows = tableRows;
        this.clazz = clazz;
        this.document = document;
        tableHeaders = new TreeMap<>();
        columnWidthMap = new HashMap<>();
        font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        this.pageWithSVG = document.getPage(0);
        this.calculatedSVGHeight = calculatedSVGHeight;
        initialize();
    }

    public void setCurrentFontSize(float currentFontSize) {
        this.currentFontSize = currentFontSize;
        this.cellMargin = ((font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * currentFontSize) * 0.5f;
        this.rowHeight = (font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * currentFontSize + cellMargin;

    }

    private float currentFontSize;
    private float tableWidthCoefficient = 1;
    public float cellMargin;

    public TableGenerator(List<T> tableRows, Class<T> clazz) {
        this.tableRows = tableRows;
        this.clazz = clazz;
        document = new PDDocument();
        tableHeaders = new TreeMap<>();
        columnWidthMap = new HashMap<>();
        font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        initialize();
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
            Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).findAny().ifPresent(method -> tableHeaders.put(field.getName(), method));
        }
    }

    public void createTable(String path, PDRectangle pdRectangle, float fontSize) throws IOException, InvocationTargetException, IllegalAccessException {
        setCurrentFontSize(fontSize);
        PDPage page;
        float startY;
        if (pageWithSVG != null) {
            pdRectangle = pageWithSVG.getMediaBox();
            page = pageWithSVG;
            startY = page.getMediaBox().getHeight() - calculatedSVGHeight + DEFAULT_PAGE_VERTICAL_MARGIN;
            pageWithSVG = null;
        } else {
            page = new PDPage(pdRectangle);
            startY = page.getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
            document.addPage(page);
        }
        PDPageContentStream stream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
        float startX = DEFAULT_PAGE_SIDE_MARGIN;
        float tableMaxWidth = pdRectangle.getWidth() - (DEFAULT_PAGE_SIDE_MARGIN * 2);
        float lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;

        adjustFont(tableMaxWidth, stream);
        int rowNumber = 1;
        stream = drawTableHeader(stream, cellMargin, startX, startY, rowHeight, lastRawPosition);
        stream = drawTableRaws(stream, startX, startY, rowHeight, rowNumber, lastRawPosition);



        /* TODO
            before starting to draw table we should calculate:
            1. Table column quantity.
                max width for every column with default font size and default paddings.
                    Summarize columns width to obtain table width. If obtained width wider the page width - adjust table font.
            2. Calculate table height.
               Draw table headers. Headers position centered. Content apply toUpperCase();
            3. Draw table content. lower case.To numerate each row.
                If cursor reached end of the page - create new PDPage, draw header on the new page and continue
                fulfill table.
         */

        stream.close();
        document.save(path);
        document.close();
    }

    private PDPageContentStream drawTableRaws(PDPageContentStream stream, float startX, float startY, float rowHeight, int rowNumber, float lastRawPosition) throws InvocationTargetException, IllegalAccessException, IOException {
        for (T t : tableRows) {
            startY = startY - rowHeight;
            if (lastRawPosition > startY) {
                stream.close();
                stream = addNextPage(stream);
                startY = document.getPage(0).getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
                startX = DEFAULT_PAGE_SIDE_MARGIN;
                lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;
                stream = drawTableHeader(stream, cellMargin, startX, startY, rowHeight, lastRawPosition);
                startY = startY - rowHeight;
            }
            drawRow(stream, cellMargin, startX, startY, t);
            rowNumber++;
        }
        return stream;
    }

    private void drawRow(PDPageContentStream stream, float cellMargin, float startX, float startY, T t) throws InvocationTargetException, IllegalAccessException, IOException {
        for (String string : tableHeaders.keySet()) {
            drawCell(stream, startX, startY, columnWidthMap.get(string), rowHeight, tableHeaders.get(string).invoke(t).toString());
            startX = startX + columnWidthMap.get(string) + 2 * cellMargin;
        }
    }

    private PDPageContentStream drawTableHeader(PDPageContentStream stream, float cellMargin, float startX, float startY, float rowHeight, float lastRawPosition) throws IOException {
        if (lastRawPosition > startY) {
            stream = addNextPage(stream);
            startY = document.getPage(0).getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
            startX = DEFAULT_PAGE_SIDE_MARGIN;
            lastRawPosition = DEFAULT_PAGE_VERTICAL_MARGIN;
            drawTableHeader(stream, cellMargin, startX, startY, rowHeight, lastRawPosition);
        }
        for (String string : tableHeaders.keySet()) {
            drawCell(stream, startX, startY, columnWidthMap.get(string), rowHeight, string);
            startX = startX + columnWidthMap.get(string) + 2 * cellMargin;
        }
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
            float maxColumnValue = findMaxColumnWidth(columnName);
            columnWidthMap.put(columnName, (maxColumnValue / 1000) * currentFontSize);
        }
        double totalWidth = calculateTotalWidth();
        if (totalWidth > tableMaxWidth) {
            setCurrentFontSize(--currentFontSize);
            adjustFont(tableMaxWidth, stream);
        }
        tableWidthCoefficient = (float) (tableMaxWidth / totalWidth);
    }

    private double calculateTotalWidth() {
        return columnWidthMap.values().stream()
                .mapToDouble(Float::doubleValue)
                .sum() + (cellMargin * 2 * columnWidthMap.size()) + (DEFAULT_LINE_WIDTH * (columnWidthMap.size() + 1) * 2);
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
                })
                .map(Object::toString)
                .map((value) -> {
                    try {
                        return font.getStringWidth(value);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .max(Comparator.comparingDouble(value -> value))
                .filter(result -> result > columnNameWidth)
                .orElse(columnNameWidth);
    }

    public void drawCell(PDPageContentStream contentStream, float x, float y, float width, float rowHeight, String content) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x * tableWidthCoefficient + cellMargin * tableWidthCoefficient + (width * tableWidthCoefficient / 2 - (font.getStringWidth(content) / 2 / 1000 * currentFontSize)), (y - rowHeight + cellMargin));
        contentStream.showText(content);
        contentStream.endText();
        contentStream.setLineWidth(DEFAULT_LINE_WIDTH);
        contentStream.addRect(x * tableWidthCoefficient, y - rowHeight, width * tableWidthCoefficient + 2 * cellMargin * tableWidthCoefficient, rowHeight);
        contentStream.stroke();
    }
}
