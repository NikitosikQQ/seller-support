package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.util.CommonUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {

    private static final String REPORT_NAME_PREFIX = "Заказы %s.xlsx";
    private static final String SHEET_NAME = "Заказы";
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
            Map.entry(2, post -> post.getProducts().getFirst().getArticle()),
            Map.entry(3, post -> post.getProducts().getFirst().getLength()),
            Map.entry(4, post -> post.getProducts().getFirst().getWidth()),
            Map.entry(5, post -> post.getProducts().getFirst().getQuantity()),
            Map.entry(6, PostingInfoModel::getMarketplace),
            Map.entry(7, post -> post.getProducts().getFirst().getAreaInMeters()),
            Map.entry(8, post -> CommonUtils.formatInstant(post.getInProcessAt())),
            Map.entry(9, post -> post.getProducts().getFirst().getTotalPrice()),
            Map.entry(10, post -> post.getProducts().getFirst().getPricePerSquareMeter()),
            Map.entry(11, post -> post.getProducts().getFirst().getColor()),
            Map.entry(12, post -> post.getProducts().getFirst().getComment())
    );

    public void createReportFile(List<PostingInfoModel> postings) {
        String fileName = prepareReportFileName();
        try (Workbook wb = initialWorkBook(); OutputStream outputStream = new FileOutputStream(fileName)) {
            fillSheet(wb, postings);
            setColumnWidths(wb.getSheet(SHEET_NAME));
            wb.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String prepareReportFileName() {
        Instant now = Instant.now();
        String date = CommonUtils.DATE_FORMATTER.format(now);
        return String.format(REPORT_NAME_PREFIX, date);
    }

    private Workbook initialWorkBook() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(SHEET_NAME);
        createHeaderRow(sheet);
        return wb;
    }

    private void createHeaderRow(Sheet sheet) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < HEADERS_NAMING_MAP.size(); i++) {
            row.createCell(i).setCellValue(HEADERS_NAMING_MAP.get(i));
        }
    }

    private void fillSheet(Workbook wb, List<PostingInfoModel> postings) {
        Sheet sheet = wb.getSheet(SHEET_NAME);
        Integer previousColorNumber = null;
        int rowIndex = 1;

        for (PostingInfoModel posting : postings) {

            Integer currentColorNumber = posting.getProducts().getFirst().getColorNumber();

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
    }

    private void setColumnWidths(Sheet sheet) {
        for (int i = 0; i < HEADERS_NAMING_MAP.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

}
