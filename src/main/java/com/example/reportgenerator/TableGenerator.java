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
    private static final float DEFAULT_TABLE_HEADER_FONT_SIZE = 14f;
    private static final float DEFAULT_PAGE_VERTICAL_MARGIN = 30f;
    private static final float DEFAULT_PAGE_SIDE_MARGIN = 20f;
    private static final float DEFAULT_LINE_WIDTH = 2.0f;
    private static final float DEFAULT_TABLE_CONTENT_FONT_SIZE = 12f;

    private final PDDocument document;
    private final List<T> tableRows;
    private final Class<T> clazz;
    private final SortedMap<String, Method> tableHeaders;
    private final Map<String, Float> columnWidthMap;
    private PDType1Font font;
    private float currentFontSize;

    public TableGenerator(List<T> tableRows, Class<T> clazz) {
        this.tableRows = tableRows;
        this.clazz = clazz;
        document = new PDDocument();
        tableHeaders = new TreeMap<>();
        columnWidthMap = new HashMap<>();
        font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        currentFontSize = DEFAULT_TABLE_HEADER_FONT_SIZE;
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
        for (Field field : fields) {
            // for simple class - String methodName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            String methodName = field.getName();
            Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).findAny().ifPresent(method -> tableHeaders.put(field.getName(), method));

        }
    }

    public void createTable(String path, PDRectangle pdRectangle, float fontSize) throws IOException, InvocationTargetException, IllegalAccessException {
        this.currentFontSize = fontSize;
        PDPage page = new PDPage(pdRectangle);
        document.addPage(page);

        PDPageContentStream stream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);


        float startY = page.getMediaBox().getHeight() - DEFAULT_PAGE_VERTICAL_MARGIN;
        float startX = DEFAULT_PAGE_SIDE_MARGIN;
        float tableMaxWidth = page.getMediaBox().getWidth() - (DEFAULT_PAGE_SIDE_MARGIN * 2);

        adjustFont(tableMaxWidth, stream);
        int rowNumber = 1;

        float cellMargin = 5f;
        float rowHeight = (font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000) * fontSize + cellMargin;
        drowTableHeader(stream, cellMargin, startX, startY, rowHeight);
        for (T t : tableRows) {
            drawRow(stream, cellMargin, startX, startY, rowHeight, t, rowNumber);
            rowNumber++;
        }



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

        // drawCell(stream, cellMargin, rowHeight, DEFAULT_PAGE_VERTICAL_MARGIN, startY, width, rowHeight, fields[0].getName());
        stream.close();
        document.save(path);
        document.close();
    }

    private void drawRow(PDPageContentStream stream, float cellMargin, float startX, float startY, float rowHeight, T t, int rowNumber) throws InvocationTargetException, IllegalAccessException, IOException {
        startY = startY - (rowHeight * rowNumber);
        for (String string : tableHeaders.keySet()) {
            drawCell(stream, cellMargin, startX, startY, columnWidthMap.get(string), rowHeight, tableHeaders.get(string).invoke(t).toString());
            startX = startX + columnWidthMap.get(string) + 2 * cellMargin;
        }
    }

    private void drowTableHeader(PDPageContentStream stream, float cellMargin, float startX, float startY, float rowHeight) throws IOException {
        for (String string : tableHeaders.keySet()) {
            drawCell(stream, cellMargin, startX, startY, columnWidthMap.get(string), rowHeight, string);
            startX = startX + columnWidthMap.get(string) + 2 * cellMargin;
        }
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
        double totalWidth = columnWidthMap.values().stream().mapToDouble(Float::doubleValue).sum();
        if (totalWidth > tableMaxWidth) {
            setCurrentFontSize(--currentFontSize);
            adjustFont(tableMaxWidth, stream);
        }
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
                .filter(result -> result > columnNameWidth).orElse(columnNameWidth);

    }

    public void drawCell(PDPageContentStream contentStream, float cellMargin, float x, float y, float width, float rowHeight, String content) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(x + cellMargin + (width / 2 - (font.getStringWidth(content) / 2 / 1000 * currentFontSize)), (y - rowHeight + cellMargin));
        contentStream.showText(content);
        contentStream.endText();
        contentStream.setLineWidth(DEFAULT_LINE_WIDTH);
        contentStream.addRect(x, y - rowHeight, width + 2 * cellMargin, rowHeight);
        contentStream.stroke();
    }
}
