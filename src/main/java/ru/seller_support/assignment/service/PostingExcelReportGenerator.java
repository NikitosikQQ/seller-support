package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.SummaryOfMaterialModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.SortingPostingByParam;
import ru.seller_support.assignment.util.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostingExcelReportGenerator {

    private static final String SHEET_NAME = "Отправления";

    private static final String LDSP_RASPIL_SHOP_NAME = "LDSPRaspil";

    private static final String LDSP_RASPIL_SHOP_TITLE_NAME = "ЛДСП-РАСПИЛ";

    private static final int COLUMN_NUMBER_OF_SUMMARY_TABLE = 13;

    private static final int SKIP_ROWS_BETWEEN_SHOPS_COUNT = 3;
    private static final int START_ROW_INDEX = 2;


    private static final Map<Integer, String> HEADERS_MAIN_NAMING_MAP = Map.ofEntries(
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
            Map.entry(11, "Комментарий")
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
            Map.entry(11, post -> post.getProduct().getComment())
    );

    private final PostingPreparationService preparationService;

    public byte[] createNewPostingFile(List<PostingInfoModel> postings,
                                       Map<MaterialEntity, List<ArticlePromoInfoEntity>> materialArticlesMap) {
        if (Objects.isNull(postings) || postings.isEmpty()) {
            return null;
        }
        List<SummaryOfMaterialModel> summaryOfMaterials = preparationService.calculateSummaryPerDay(postings);

        List<PostingInfoModel> mutablePostings = new ArrayList<>(postings);
        int nextRowIndex;
        try (Workbook wb = initialWorkBook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            nextRowIndex = fillSheetLDCPRaspil(wb, mutablePostings);
            for (MaterialEntity material : materialArticlesMap.keySet()) {
                List<ArticlePromoInfoEntity> articles = materialArticlesMap.get(material);
                nextRowIndex = fillSheetByMaterial(wb, mutablePostings, nextRowIndex, material, articles);
            }
            fillSheetRemaining(wb, mutablePostings, nextRowIndex);
            fillSheetBySummary(wb, summaryOfMaterials);
            setColumnWidths(wb.getSheet(SHEET_NAME));
            wb.write(outputStream);
            log.info("Успешно подготовлен excel-файл для отчета отправлений");
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fillSheetBySummary(Workbook wb, List<SummaryOfMaterialModel> summaryOfMaterials) {
        Sheet sheet = wb.getSheet(SHEET_NAME);
        int headerIndexRow = 1;
        Row row = sheet.getRow(headerIndexRow);
        fillHeaderSummary(wb, row, CommonUtils.formatInstantToDateString(Instant.now()), COLUMN_NUMBER_OF_SUMMARY_TABLE);
        row = sheet.getRow(++headerIndexRow);
        fillHeaderSummary(wb, row, "Материал", COLUMN_NUMBER_OF_SUMMARY_TABLE);
        fillHeaderSummary(wb, row, "М2", COLUMN_NUMBER_OF_SUMMARY_TABLE + 1);
        fillHeaderSummary(wb, row, "Общая сумма продаж", COLUMN_NUMBER_OF_SUMMARY_TABLE + 2);
        fillHeaderSummary(wb, row, "Средняя цена за М2", COLUMN_NUMBER_OF_SUMMARY_TABLE + 3);

        int payloadInderRow = 3;
        for (SummaryOfMaterialModel summary : summaryOfMaterials) {
            int columnNumber = COLUMN_NUMBER_OF_SUMMARY_TABLE;
            row = sheet.getRow(payloadInderRow);
            Cell cell = row.createCell(columnNumber);
            cell.setCellValue(summary.getMaterialName());
            cell.setCellStyle(getBorderedStyle(wb));
            sheet.setColumnWidth(columnNumber, 7000);

            Cell cellOfM2 = row.createCell(++columnNumber);
            cellOfM2.setCellValue(summary.getTotalAreaInMeterPerDay().doubleValue());
            cellOfM2.setCellStyle(getBorderedStyle(wb));
            sheet.setColumnWidth(columnNumber, 7000);

            Cell cellOfTotalPricePerMaterial = row.createCell(++columnNumber);
            cellOfTotalPricePerMaterial.setCellValue(summary.getTotalPricePerDay().doubleValue());
            cellOfTotalPricePerMaterial.setCellStyle(getBorderedStyle(wb));
            sheet.setColumnWidth(columnNumber, 7000);

            Cell cellOfAveragePricePerMaterial = row.createCell(++columnNumber);
            cellOfAveragePricePerMaterial.setCellValue(summary.getAveragePricePerSquareMeter().doubleValue());
            cellOfAveragePricePerMaterial.setCellStyle(getBorderedStyle(wb));
            sheet.setColumnWidth(columnNumber, 7000);

            payloadInderRow++;
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
        for (int i = 0; i < HEADERS_MAIN_NAMING_MAP.size(); i++) {
            row.createCell(i).setCellValue(HEADERS_MAIN_NAMING_MAP.get(i));
        }
    }

    private int fillSheetLDCPRaspil(Workbook wb, List<PostingInfoModel> postings) {
        createRowTitle(wb, 1, LDSP_RASPIL_SHOP_TITLE_NAME);
        Sheet sheet = wb.getSheet(SHEET_NAME);
        int rowIndex = START_ROW_INDEX;

        List<PostingInfoModel> LDSPRaspilPostings = filterPostingsByShop(postings, LDSP_RASPIL_SHOP_NAME);
        if (LDSPRaspilPostings.isEmpty()) {
            return rowIndex;
        }

        rowIndex = fillRowsBySortingStrategy(sheet, LDSPRaspilPostings, rowIndex, SortingPostingByParam.COLOR_NUMBER);

        rowIndex = addEmptyRows(sheet, rowIndex);

        postings.removeIf(post -> post.getShopName().equalsIgnoreCase(LDSP_RASPIL_SHOP_NAME));

        return rowIndex;
    }

    private int fillSheetByMaterial(Workbook wb, List<PostingInfoModel> postings,
                                    int nextRowIndex,
                                    MaterialEntity material,
                                    List<ArticlePromoInfoEntity> articles) {
        if (material.isNotSeparate()) {
            return nextRowIndex;
        }

        int indexRowTitle = (nextRowIndex != 1) ? nextRowIndex - 1 : nextRowIndex;
        createRowTitle(wb, indexRowTitle, material.getSeparatorName());

        Sheet sheet = wb.getSheet(SHEET_NAME);
        int rowIndex = nextRowIndex;

        Set<String> promoNames = preparationService.extractPromoNames(articles);
        List<PostingInfoModel> filteredPostings = preparationService.getFilteringPostingsByArticle(postings, articles);

        if (filteredPostings.isEmpty()) {
            return rowIndex;
        }

        rowIndex = fillRowsBySortingStrategy(sheet, filteredPostings, rowIndex, material.getSortingPostingBy());

        rowIndex = addEmptyRows(sheet, rowIndex);

        postings.removeIf(post -> promoNames.contains(post.getProduct().getPromoName()));

        return rowIndex;
    }

    private void fillSheetRemaining(Workbook wb, List<PostingInfoModel> postings, int nextRowIndex) {
        Sheet sheet = wb.getSheet(SHEET_NAME);
        createRowTitle(wb, nextRowIndex - 1, Marketplace.OZON.name());
        fillRowsBySortingStrategy(sheet, postings, nextRowIndex, SortingPostingByParam.COLOR_NUMBER);
    }

    private List<PostingInfoModel> filterPostingsByShop(List<PostingInfoModel> postings, String shopName) {
        return postings.stream()
                .filter(post -> post.getShopName().equalsIgnoreCase(shopName))
                .collect(Collectors.toList());
    }

    private int fillRowsBySortingStrategy(Sheet sheet,
                                          List<PostingInfoModel> postings,
                                          int rowIndex,
                                          SortingPostingByParam sortingParam) {
        Comparator<PostingInfoModel> comparator = getComparatorBySortingParam(sortingParam);

        return fillRowsBySorting(sheet, postings, rowIndex, comparator, post -> {
            if (SortingPostingByParam.COLOR_NAME.equals(sortingParam)) {
                return post.getProduct().getColor();
            } else if (SortingPostingByParam.PROMO_NAME.equals(sortingParam)) {
                return post.getProduct().getPromoName();
            } else {
                return post.getProduct().getColorNumber();
            }
        });
    }

    private Comparator<PostingInfoModel> getComparatorBySortingParam(SortingPostingByParam sortingParam) {
        if (SortingPostingByParam.COLOR_NAME.equals(sortingParam)) {
            return Comparator.comparing(post -> post.getProduct().getColor());
        } else if (SortingPostingByParam.PROMO_NAME.equals(sortingParam)) {
            return Comparator.comparing(post -> post.getProduct().getPromoName());
        } else {
            return Comparator.comparing(post -> post.getProduct().getColorNumber());
        }
    }

    private int fillRowsBySorting(Sheet sheet,
                                  List<PostingInfoModel> postings,
                                  int rowIndex,
                                  Comparator<PostingInfoModel> comparator,
                                  Function<PostingInfoModel, Object> groupingFunction) {
        List<PostingInfoModel> sortedPostings = postings.stream()
                .sorted(comparator)
                .toList();

        Object previousGroupValue = null;

        for (PostingInfoModel posting : sortedPostings) {
            Object currentGroupValue = groupingFunction.apply(posting);

            if (Objects.nonNull(previousGroupValue) && !previousGroupValue.equals(currentGroupValue)) {
                rowIndex++;
                sheet.createRow(rowIndex);
            }

            Row row = sheet.createRow(rowIndex);
            fillRow(row, posting);
            previousGroupValue = currentGroupValue;

            rowIndex++;
        }

        return rowIndex;
    }

    private void createRowTitle(Workbook wb, int titleIndexRow, String rowText) {
        Sheet sheet = wb.getSheet(SHEET_NAME);

        Row row = sheet.createRow(titleIndexRow);
        row.setHeightInPoints(25);

        Cell cell = row.createCell(2);
        cell.setCellValue(rowText);
        cell.setCellStyle(getTitleStyle(wb));
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

    private void fillRow(Row row, PostingInfoModel posting) {
        for (int j = 0; j < VALUES_CELLS_MAP.size(); j++) {
            Object value = VALUES_CELLS_MAP.get(j).apply(posting);
            switch (value) {
                case Number number -> row.createCell(j).setCellValue(number.doubleValue());
                case String str -> row.createCell(j).setCellValue(str);
                case Marketplace marketplace -> row.createCell(j).setCellValue(marketplace.toString());
                default -> row.createCell(j).setCellValue("");
            }
        }
    }

    private void setColumnWidths(Sheet sheet) {
        for (int i = 1; i < HEADERS_MAIN_NAMING_MAP.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private int addEmptyRows(Sheet sheet, int rowIndex) {
        for (int i = 0; i < SKIP_ROWS_BETWEEN_SHOPS_COUNT; i++) {
            sheet.createRow(++rowIndex);
        }
        return rowIndex;
    }

    private void fillHeaderSummary(Workbook wb, Row row, String value, int index) {
        Cell cell = row.createCell(index);
        if (row.getRowNum() == 1) {
            Sheet sheet = wb.getSheet(SHEET_NAME);
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 13, 16));
        }
        cell.setCellValue(value);

        CellStyle style =  row.getRowNum() == 1 ? getTitleStyle(wb) : getBorderedStyle(wb);
        cell.setCellStyle(style);
    }

    private CellStyle getBorderedStyle(Workbook wb) {
        CellStyle borderedStyle = wb.createCellStyle();
        borderedStyle.setBorderTop(BorderStyle.THIN);
        borderedStyle.setBorderBottom(BorderStyle.THIN);
        borderedStyle.setBorderLeft(BorderStyle.THIN);
        borderedStyle.setBorderRight(BorderStyle.THIN);
        return borderedStyle;
    }
}
