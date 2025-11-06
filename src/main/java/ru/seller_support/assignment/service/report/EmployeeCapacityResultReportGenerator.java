package ru.seller_support.assignment.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.domain.EmployeeCapacityResult;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

import static ru.seller_support.assignment.util.CommonUtils.EMPTY_STRING;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeCapacityResultReportGenerator {

    private static final String SHEET_NAME = "Выполненнные работы";
    private static final String REPORT_HEADER_FORMAT = "Отчет о выполненных работах с %s по %s";
    private static final String DELTA_PLUS_FORMAT = "(+%s)";
    private static final String DELTA_MINUS_FORMAT = "(-%s)";

    private static final Set<Integer> DELTA_COLUMNS = Set.of(3, 5);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Map<Integer, String> HEADERS_MAIN_FULL_NAMING_MAP = Map.ofEntries(
            Map.entry(0, "Логин работника"),
            Map.entry(1, "Рабочее место"),
            Map.entry(2, "Объем обработанного метража, м^2"),
            Map.entry(3, "Разница с прошлым периодом, м^2"),
            Map.entry(4, "Заработано, руб"),
            Map.entry(5, "Разница с прошлым периодом, руб")
    );

    private static final Map<Integer, Function<EmployeeCapacityResult, Object>> VALUES_CELLS_MAP = Map.ofEntries(
            Map.entry(0, EmployeeCapacityResult::getUsername),
            Map.entry(1, EmployeeCapacityResult::getWorkplace),
            Map.entry(2, EmployeeCapacityResult::getTotalCapacity),
            Map.entry(3, EmployeeCapacityResult::getDeltaCapacity),
            Map.entry(4, EmployeeCapacityResult::getTotalEarnedAmount),
            Map.entry(5, EmployeeCapacityResult::getDeltaEarnedAmount)
    );

    public byte[] generateReport(List<EmployeeCapacityResult> data, LocalDate minProcessedAt, LocalDate maxProcessedAt) {
        if (Objects.isNull(data)) {
            return null;
        }

        List<EmployeeCapacityResult> mutableDatas = new ArrayList<>(data);
        try (Workbook wb = initialWorkBook(minProcessedAt, maxProcessedAt); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int nextRowIndex = 3;
            for (EmployeeCapacityResult mutableData : mutableDatas) {
                nextRowIndex = fillSheetByData(wb, mutableData, nextRowIndex);
            }
            setColumnWidths(wb.getSheet(SHEET_NAME));
            wb.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int fillSheetByData(Workbook wb, EmployeeCapacityResult mutableData, int nextRowIndex) {
        Sheet sheet = wb.getSheet(SHEET_NAME);
        Row row = sheet.createRow(nextRowIndex);
        fillRow(row, mutableData, wb);
        return nextRowIndex + 1;
    }

    private void fillRow(Row row, EmployeeCapacityResult result, Workbook wb) {
        VALUES_CELLS_MAP.forEach((j, function) -> {
            Object value = function.apply(result);
            if (value == null) {
                value = EMPTY_STRING;
            }
            if (DELTA_COLUMNS.contains(j) && !value.equals(EMPTY_STRING)) {
                value = formatDelta((BigDecimal) value);
            }
            switch (value) {
                case Number number -> {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(getBorderedStyle(wb));
                    cell.setCellValue(number.doubleValue());
                }
                case String str -> {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(getBorderedStyle(wb));
                    cell.setCellValue(str);
                }
                case Workplace workplace -> {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(getBorderedStyle(wb));
                    cell.setCellValue(workplace.getValue());
                }
                default -> {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(getBorderedStyle(wb));
                    cell.setCellValue(EMPTY_STRING);
                }
            }
        });
    }

    private Workbook initialWorkBook(LocalDate minProcessedAt, LocalDate maxProcessedAt) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(SHEET_NAME);
        sheet.setColumnWidth(0, 10000);
        createHeaderRow(wb, sheet, minProcessedAt, maxProcessedAt);
        return wb;
    }

    private void createHeaderRow(Workbook wb, Sheet sheet, LocalDate minProcessedAt, LocalDate maxProcessedAt) {
        Row firstRow = sheet.createRow(0);
        String minProcessAtFormat = DATE_FORMATTER.format(minProcessedAt);
        String maxProcessAtFormat = DATE_FORMATTER.format(maxProcessedAt);

        Cell headerSell = firstRow.createCell(0);
        headerSell.setCellValue(String.format(REPORT_HEADER_FORMAT, minProcessAtFormat, maxProcessAtFormat));

        CellStyle titleStyle = getTitleStyle(wb);
        headerSell.setCellStyle(titleStyle);
        firstRow.setHeightInPoints(25);
        sheet.addMergedRegion(new CellRangeAddress(firstRow.getRowNum(), firstRow.getRowNum(), 0, 3));

        Row columnsRow = sheet.createRow(2);
        HEADERS_MAIN_FULL_NAMING_MAP.forEach((i, header) -> {
           Cell cell = columnsRow.createCell(i);
           cell.setCellStyle(getBorderedStyle(wb));
           cell.setCellValue(header);
        });
    }

    private CellStyle getTitleStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 24);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle getBorderedStyle(Workbook wb) {
        CellStyle borderedStyle = wb.createCellStyle();
        borderedStyle.setBorderTop(BorderStyle.THIN);
        borderedStyle.setBorderBottom(BorderStyle.THIN);
        borderedStyle.setBorderLeft(BorderStyle.THIN);
        borderedStyle.setBorderRight(BorderStyle.THIN);
        return borderedStyle;
    }

    private String formatDelta(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "(0)";
        }

        BigDecimal rounded = value.setScale(2, RoundingMode.HALF_UP);

        if (rounded.compareTo(BigDecimal.ZERO) > 0) {
            return String.format(DELTA_PLUS_FORMAT, rounded);
        } else if (rounded.compareTo(BigDecimal.ZERO) < 0) {
            return String.format(DELTA_MINUS_FORMAT, rounded.abs());
        }
        return "(0)";
    }

    private void setColumnWidths(Sheet sheet) {
        for (int i = 1; i < HEADERS_MAIN_FULL_NAMING_MAP.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
