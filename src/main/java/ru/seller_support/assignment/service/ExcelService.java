package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.util.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {

    private static final String SHEET_NAME = "Заказы";
    private static final String LDSP_RASPIL_NAME = "LDSPRaspil";
    private static final Set<String> LDSP_25MM_PROMO_NAMES = Set.of("3А", "4А");
    private static final String LDSP_RASPIL_TITLE_NAME = "ЛДСП-РАСПИЛ";
    private static final String LDSP_25MM_TITLE_NAME = "ЛДСП 25мм";
    private static final int SKIP_ROWS_BETWEEN_SHOPS_COUNT = 3;
    private static final int START_ROW_INDEX = 2;

    private static final Map<Integer, String> HEADERS_NAMING_MAP = Map.ofEntries(
            Map.entry(0, "Номер отправления"),
            Map.entry(1, "Номер паллета"),
            Map.entry(2, "Артикул"),
            Map.entry(3, "Длина"),
            Map.entry(4, "Ширина"),
            Map.entry(5, "Количество"),
            Map.entry(6, "Маркетплейс"),
            Map.entry(7, "Метраж"),
            Map.entry(8, "Принят в обработку"),
            Map.entry(9, "Сумма отправления"),
            Map.entry(10, "Сумма за метр кв"),
            Map.entry(11, "Цвет"),
            Map.entry(12, "Комментарий")
    );
    private static final Map<Integer, Function<PostingInfoModel, Object>> VALUES_CELLS_MAP = Map.ofEntries(
            Map.entry(0, PostingInfoModel::getPostingNumber),
            Map.entry(1, PostingInfoModel::getPalletNumber),
            Map.entry(2, post -> post.getProduct().getArticle()),
            Map.entry(3, post -> post.getProduct().getLength()),
            Map.entry(4, post -> post.getProduct().getWidth()),
            Map.entry(5, post -> post.getProduct().getQuantity()),
            Map.entry(6, PostingInfoModel::getMarketplace),
            Map.entry(7, post -> post.getProduct().getAreaInMeters()),
            Map.entry(8, post -> CommonUtils.formatInstant(post.getInProcessAt())),
            Map.entry(9, post -> post.getProduct().getTotalPrice()),
            Map.entry(10, post -> post.getProduct().getPricePerSquareMeter()),
            Map.entry(11, post -> post.getProduct().getColor()),
            Map.entry(12, post -> post.getProduct().getComment())
    );

    public byte[] createReportFile(List<PostingInfoModel> postings) {
        if (Objects.isNull(postings) || postings.isEmpty()) {
            return null;
        }
        int nextRowIndex;
        try (Workbook wb = initialWorkBook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            nextRowIndex = fillSheetLDCPRaspil(wb, postings);
            nextRowIndex = fillSheetLDCP25mm(wb, postings, nextRowIndex);
            fillSheetOzon(wb, postings, nextRowIndex);
            setColumnWidths(wb.getSheet(SHEET_NAME));
            wb.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Workbook initialWorkBook() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(SHEET_NAME);
        sheet.setColumnWidth(0, 10000);
        createHeaderRow(sheet);
        return wb;
    }

    private void createHeaderRow(Sheet sheet) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < HEADERS_NAMING_MAP.size(); i++) {
            row.createCell(i).setCellValue(HEADERS_NAMING_MAP.get(i));
        }
    }

    private int fillSheetLDCPRaspil(Workbook wb, List<PostingInfoModel> postings) {
        createRowTitle(wb, 1, LDSP_RASPIL_TITLE_NAME);
        Sheet sheet = wb.getSheet(SHEET_NAME);
        int rowIndex = START_ROW_INDEX;
        List<PostingInfoModel> LDSPRaspilPostings = postings.stream()
                .filter(post -> post.getShopName().equalsIgnoreCase(LDSP_RASPIL_NAME))
                .toList();

        if (LDSPRaspilPostings.isEmpty()) {
            return rowIndex;
        }

        rowIndex = fillRow(sheet, LDSPRaspilPostings, rowIndex);
        for (int i = 0; i < SKIP_ROWS_BETWEEN_SHOPS_COUNT; i++) {
            sheet.createRow(++rowIndex);
        }

        return rowIndex;
    }

    private int fillSheetLDCP25mm(Workbook wb, List<PostingInfoModel> postings, int nextRowIndex) {
        createRowTitle(wb, nextRowIndex - 1, LDSP_25MM_TITLE_NAME);
        Sheet sheet = wb.getSheet(SHEET_NAME);
        int rowIndex = nextRowIndex;
        List<PostingInfoModel> LDSP25mmPostings = postings.stream()
                .filter(post -> LDSP_25MM_PROMO_NAMES.contains(post.getProduct().getPromoName()))
                .toList();

        if (LDSP25mmPostings.isEmpty()) {
            return rowIndex;
        }

        rowIndex = fillRow(sheet, LDSP25mmPostings, rowIndex);
        for (int i = 0; i < SKIP_ROWS_BETWEEN_SHOPS_COUNT; i++) {
            sheet.createRow(++rowIndex);
        }

        return rowIndex;
    }

    private void fillSheetOzon(Workbook wb, List<PostingInfoModel> postings, int nextRowIndex) {
        Sheet sheet = wb.getSheet(SHEET_NAME);
        createRowTitle(wb, nextRowIndex - 1, Marketplace.OZON.name());
        List<PostingInfoModel> postingOzon = postings.stream()
                .filter(post -> !post.getShopName().equalsIgnoreCase(LDSP_RASPIL_NAME)
                        && post.getMarketplace() == Marketplace.OZON
                        && !LDSP_25MM_PROMO_NAMES.contains(post.getProduct().getPromoName()))
                .toList();

        fillRow(sheet, postingOzon, nextRowIndex);
    }

    private void setColumnWidths(Sheet sheet) {
        for (int i = 1; i < HEADERS_NAMING_MAP.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private int fillRow(Sheet sheet, List<PostingInfoModel> postings, int rowIndex) {
        Integer previousColorNumber = null;
        for (PostingInfoModel posting : postings) {

            Integer currentColorNumber = posting.getProduct().getColorNumber();

            if (Objects.nonNull(previousColorNumber) && !previousColorNumber.equals(currentColorNumber)) {
                rowIndex++;
                sheet.createRow(rowIndex);
            }

            Row row = sheet.createRow(rowIndex);

            for (int j = 0; j < VALUES_CELLS_MAP.size(); j++) {
                Object value = VALUES_CELLS_MAP.get(j).apply(posting);
                switch (value) {
                    case Number number -> row.createCell(j).setCellValue(number.doubleValue());
                    case String str -> row.createCell(j).setCellValue(str);
                    case Marketplace marketplace -> row.createCell(j).setCellValue(marketplace.toString());
                    default -> row.createCell(j).setCellValue("");
                }
            }

            previousColorNumber = currentColorNumber;
            rowIndex++;
        }
        return rowIndex;
    }

    private void createRowTitle(Workbook wb, int titleIndexRow, String rowText) {
        Sheet sheet = wb.getSheet(SHEET_NAME);
        // Создаем шрифт
        Font font = wb.createFont();
        font.setBold(true); // Жирный текст
        font.setFontHeightInPoints((short) 24); // Размер шрифта

        // Создаем стиль ячейки
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER); // Горизонтальное выравнивание
        style.setVerticalAlignment(VerticalAlignment.CENTER); // Вертикальное выравнивание
        style.setWrapText(true); // Перенос текста

        // Создаем строку
        Row row = sheet.createRow(titleIndexRow);
        row.setHeightInPoints(25); // Устанавливаем высоту строки

        // Создаем ячейку
        Cell cell = row.createCell(0);
        cell.setCellValue(rowText); // Устанавливаем текст
        cell.setCellStyle(style);
    }

}
