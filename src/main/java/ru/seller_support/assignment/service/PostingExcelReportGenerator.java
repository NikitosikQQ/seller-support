package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.adapter.postgres.entity.RoleEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.SummaryOfMaterialModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.SortingPostingByParam;
import ru.seller_support.assignment.util.CommonUtils;
import ru.seller_support.assignment.util.SecurityUtils;

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
    private static final String OTHER_MATERIALS_TITLE = "Остальное";
    private static final String POSTINGS_WITH_WRONG_ARTICLE_TITLE = "Некорректные артикулы";
    private static final String POSTINGS_WITH_WRONG_BOX_TITLE = "Некорректные грузоместа";

    private static final int COLUMN_NUMBER_OF_SUMMARY_TABLE = 13;

    private static final int SKIP_ROWS_BETWEEN_SHOPS_COUNT = 3;

    private static final Set<Integer> SHORT_KEYS = Set.of(0, 1, 2, 3, 4, 5, 6, 7);

    private static final Map<Integer, String> HEADERS_MAIN_FULL_NAMING_MAP = Map.ofEntries(
            Map.entry(0, "Номер отправления"),
            Map.entry(1, "Номер паллета"),
            Map.entry(2, "Артикул"),
            Map.entry(3, "Длина"),
            Map.entry(4, "Ширина"),
            Map.entry(5, "Количество"),
            Map.entry(6, "Маркетплейс"),
            Map.entry(7, "Комментарий"),
            Map.entry(8, "Принят в обработку"),
            Map.entry(9, "Сумма отправления"),
            Map.entry(10, "Метраж"),
            Map.entry(11, "Сумма за метр кв")
    );

    private static final Map<Integer, Function<PostingInfoModel, Object>> VALUES_CELLS_MAP = Map.ofEntries(
            Map.entry(0, PostingInfoModel::getPostingNumber),
            Map.entry(1, PostingInfoModel::getPalletNumber),
            Map.entry(2, post -> post.getProduct().getArticle()),
            Map.entry(3, post -> post.getProduct().getLength()),
            Map.entry(4, post -> post.getProduct().getWidth()),
            Map.entry(5, post -> post.getProduct().getQuantity()),
            Map.entry(6, PostingInfoModel::getMarketplace),
            Map.entry(7, post -> post.getProduct().getComment()),
            Map.entry(8, post -> CommonUtils.formatInstantToDateTimeString(post.getInProcessAt())),
            Map.entry(9, post -> post.getProduct().getTotalPrice()),
            Map.entry(10, post -> post.getProduct().getAreaInMeters()),
            Map.entry(11, post -> post.getProduct().getPricePerSquareMeter())
    );

    private final PostingPreparationService preparationService;

    public byte[] createNewPostingFile(List<PostingInfoModel> postings,
                                       List<PostingInfoModel> wrongPostings,
                                       List<PostingInfoModel> wrongBoxPostings,
                                       Map<MaterialEntity, List<ArticlePromoInfoEntity>> materialArticlesMap) {
        if (Objects.isNull(postings) || postings.isEmpty()) {
            return null;
        }
        Map<Marketplace, List<PostingInfoModel>> groupedPostings = postings.stream()
                .collect(Collectors.groupingBy(PostingInfoModel::getMarketplace));

        boolean isNotFullReport = checkUserRolesForFullReport();

        List<SummaryOfMaterialModel> summaryOfMaterials = new ArrayList<>();
        if (!isNotFullReport) {
            summaryOfMaterials = preparationService.calculateSummaryPerDay(postings);
        }

        int nextRowIndex = 1;
        try (Workbook wb = initialWorkBook(isNotFullReport); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            nextRowIndex = fillSheetLDCPRaspil(wb, groupedPostings.get(Marketplace.OZON), isNotFullReport);
            for (Map.Entry<Marketplace, List<PostingInfoModel>> entry : groupedPostings.entrySet()) {
                Marketplace marketplace = entry.getKey();
                List<PostingInfoModel> mutablePostings = entry.getValue();
                nextRowIndex = createRowTitle(wb, nextRowIndex, marketplace.getValue());
                for (MaterialEntity material : materialArticlesMap.keySet()) {
                    List<ArticlePromoInfoEntity> articles = materialArticlesMap.get(material);
                    nextRowIndex = fillSheetByMaterial(wb, mutablePostings, nextRowIndex, material, articles, isNotFullReport);
                }
                nextRowIndex = fillSheetRemaining(wb, mutablePostings, nextRowIndex, isNotFullReport);
            }
            fillSheetBySummary(wb, summaryOfMaterials, isNotFullReport);
            if (Objects.nonNull(wrongPostings) && !wrongPostings.isEmpty()) {
                nextRowIndex = fillSheetByWrongPostings(wb, wrongPostings, nextRowIndex);
            }
            if (Objects.nonNull(wrongBoxPostings) && !wrongBoxPostings.isEmpty()) {
                fillSheetByWrongBoxPostings(wb, wrongBoxPostings, nextRowIndex);
            }
            setColumnWidths(wb.getSheet(SHEET_NAME));
            wb.write(outputStream);
            log.info("Успешно подготовлен excel-файл для отчета отправлений");
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fillSheetBySummary(Workbook wb, List<SummaryOfMaterialModel> summaryOfMaterials, boolean isNotFullReport) {
        if (isNotFullReport) {
            return;
        }
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
            if (Objects.isNull(row)) {
                row = sheet.createRow(payloadInderRow);
            }
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

    private Workbook initialWorkBook(boolean isNotFullReport) {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(SHEET_NAME);
        sheet.setColumnWidth(0, 10000);
        createHeaderRow(sheet, isNotFullReport);
        return wb;
    }

    private void createHeaderRow(Sheet sheet, boolean isNotFullReport) {
        Row row = sheet.createRow(0);
        var headersMap = getHeaders(isNotFullReport);
        headersMap.forEach((i, header) -> row.createCell(i).setCellValue(header));
    }

    private int fillSheetLDCPRaspil(Workbook wb, List<PostingInfoModel> postings, boolean isNotFullReport) {
        int rowIndex = 1;
        List<PostingInfoModel> LDSPRaspilPostings = filterPostingsByShop(postings, LDSP_RASPIL_SHOP_NAME);
        if (LDSPRaspilPostings.isEmpty()) {
            return rowIndex;
        }

        rowIndex = createRowTitle(wb, rowIndex, LDSP_RASPIL_SHOP_TITLE_NAME);
        Sheet sheet = wb.getSheet(SHEET_NAME);

        rowIndex = fillRowsBySortingStrategy(sheet, LDSPRaspilPostings, rowIndex, SortingPostingByParam.COLOR_NUMBER, isNotFullReport);

        rowIndex = addEmptyRows(sheet, rowIndex);

        postings.removeIf(post -> post.getShopName().equalsIgnoreCase(LDSP_RASPIL_SHOP_NAME));

        return rowIndex;
    }

    private int fillSheetByMaterial(Workbook wb, List<PostingInfoModel> postings,
                                    int nextRowIndex,
                                    MaterialEntity material,
                                    List<ArticlePromoInfoEntity> articles,
                                    boolean isNotFullReport) {
        if (material.isNotSeparate()) {
            return nextRowIndex;
        }
        Sheet sheet = wb.getSheet(SHEET_NAME);

        Set<String> promoNames = preparationService.extractPromoNames(articles);
        List<PostingInfoModel> filteredPostings = preparationService.getFilteringPostingsByArticle(postings, articles);

        if (filteredPostings.isEmpty()) {
            return nextRowIndex;
        }

        int rowIndex = createRowTitle(wb, nextRowIndex, material.getSeparatorName());

        rowIndex = fillRowsBySortingStrategy(sheet, filteredPostings, rowIndex, material.getSortingPostingBy(), isNotFullReport);

        rowIndex = addEmptyRows(sheet, rowIndex);

        postings.removeIf(post -> promoNames.contains(post.getProduct().getPromoName()));

        return rowIndex;
    }

    private int fillSheetRemaining(Workbook wb, List<PostingInfoModel> postings, int nextRowIndex, boolean isNotFullReport) {
        if (postings.isEmpty()) {
            return nextRowIndex;
        }
        Sheet sheet = wb.getSheet(SHEET_NAME);
        createRowTitle(wb, nextRowIndex - 1, OTHER_MATERIALS_TITLE);
        int rowIndex = fillRowsBySortingStrategy(sheet, postings, nextRowIndex, SortingPostingByParam.COLOR_NUMBER, isNotFullReport);
        return rowIndex + 2;
    }

    private int fillSheetByWrongPostings(Workbook wb, List<PostingInfoModel> wrongPostings, int nextRowIndex) {
        Sheet sheet = wb.getSheet(SHEET_NAME);
        nextRowIndex = createRowTitle(wb, nextRowIndex, POSTINGS_WITH_WRONG_ARTICLE_TITLE);
        for (PostingInfoModel posting : wrongPostings) {
            Row row = sheet.createRow(nextRowIndex++);
            row.createCell(0).setCellValue(posting.getPostingNumber());
            row.createCell(1).setCellValue(posting.getPalletNumber());
            row.createCell(2).setCellValue(String.format("Артикул: %s; Название магазина: %s",
                    posting.getProduct().getArticle(), posting.getShopName()));
        }
        return nextRowIndex;
    }

    private void fillSheetByWrongBoxPostings(Workbook wb, List<PostingInfoModel> wrongBoxPostings, int nextRowIndex) {
        Sheet sheet = wb.getSheet(SHEET_NAME);
        nextRowIndex = createRowTitle(wb, nextRowIndex, POSTINGS_WITH_WRONG_BOX_TITLE);
        for (PostingInfoModel posting : wrongBoxPostings) {
            Row row = sheet.createRow(nextRowIndex++);
            row.createCell(0).setCellValue(posting.getPostingNumber());
            row.createCell(1).setCellValue(posting.getPalletNumber());
            row.createCell(2).setCellValue(String.format("Артикул: %s; Название магазина: %s",
                    posting.getProduct().getArticle(), posting.getShopName()));
        }
    }

    private List<PostingInfoModel> filterPostingsByShop(List<PostingInfoModel> postings, String shopName) {
        return postings.stream()
                .filter(post -> post.getShopName().equalsIgnoreCase(shopName))
                .collect(Collectors.toList());
    }

    private int fillRowsBySortingStrategy(Sheet sheet,
                                          List<PostingInfoModel> postings,
                                          int rowIndex,
                                          SortingPostingByParam sortingParam,
                                          boolean isNotFullReport) {
        Comparator<PostingInfoModel> comparator = getComparatorBySortingParam(sortingParam);

        return fillRowsBySorting(sheet, postings, rowIndex, comparator, isNotFullReport, post -> {
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
                                  boolean isNotFullReport,
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
            fillRow(row, posting, isNotFullReport);
            previousGroupValue = currentGroupValue;

            rowIndex++;
        }

        return rowIndex;
    }

    private int createRowTitle(Workbook wb, int titleIndexRow, String rowText) {
        Sheet sheet = wb.getSheet(SHEET_NAME);

        Row row = sheet.createRow(titleIndexRow);
        row.setHeightInPoints(25);

        Cell cell = row.createCell(2);
        cell.setCellValue(rowText);
        cell.setCellStyle(getTitleStyle(wb));
        return ++titleIndexRow;
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

    private void fillRow(Row row, PostingInfoModel posting, boolean isNotFullReport) {
        var valuesMap = getValuesCellsMap(isNotFullReport);
        valuesMap.forEach((j, function) -> {
            Object value = function.apply(posting);
            switch (value) {
                case Number number -> row.createCell(j).setCellValue(number.doubleValue());
                case String str -> row.createCell(j).setCellValue(str);
                case Marketplace marketplace -> row.createCell(j).setCellValue(marketplace.getValue());
                default -> row.createCell(j).setCellValue("");
            }
        });
    }

    private void setColumnWidths(Sheet sheet) {
        for (int i = 1; i < HEADERS_MAIN_FULL_NAMING_MAP.size(); i++) {
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

        CellStyle style = row.getRowNum() == 1 ? getTitleStyle(wb) : getBorderedStyle(wb);
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

    private Map<Integer, String> getHeaders(boolean isNotFull) {
        if (isNotFull) {
            return HEADERS_MAIN_FULL_NAMING_MAP.entrySet().stream()
                    .filter(entry -> SHORT_KEYS.contains(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return HEADERS_MAIN_FULL_NAMING_MAP;
    }

    private Map<Integer, Function<PostingInfoModel, Object>> getValuesCellsMap(boolean isNotFull) {
        if (isNotFull) {
            return VALUES_CELLS_MAP.entrySet().stream()
                    .filter(entry -> SHORT_KEYS.contains(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return VALUES_CELLS_MAP;
    }


    private boolean checkUserRolesForFullReport() {
        Set<String> roles = SecurityUtils.getRoles();
        return roles.stream().noneMatch(RoleEntity.ROLES_FOR_FULL_REPORTS::contains);
    }
}
