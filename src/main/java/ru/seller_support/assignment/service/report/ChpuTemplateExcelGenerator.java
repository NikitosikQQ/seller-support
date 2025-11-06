package ru.seller_support.assignment.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.ArticlePromoInfoEntity;
import ru.seller_support.assignment.adapter.postgres.entity.MaterialEntity;
import ru.seller_support.assignment.domain.PostingInfoModel;
import ru.seller_support.assignment.domain.ProductModel;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.service.OrderParamsCalculatorService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static ru.seller_support.assignment.util.CommonUtils.EMPTY_STRING;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChpuTemplateExcelGenerator {

    private static final int MAX_COLUMNS = 12;

    private static final String LDSP_RASPIL_SHOP_NAME = "LDSPRaspil";

    private static final String EXCEL_NAME_PATTERN = "Шаблон раскроя %s, толщина %s, номер цвета %s.xlsx";
    private static final String SHEET_NAME = "Шаблон раскроя";
    private static final String A_COLUMN_VALUE = "Да";
    private static final String B_COLUMN_VALUE = "Площадной";

    private final OrderParamsCalculatorService preparationService;

    public Map<String, byte[]> createChpuTemplates(List<PostingInfoModel> postings) {
        if (Objects.isNull(postings) || postings.isEmpty()) {
            return null;
        }
        Map<String, byte[]> templates = new TreeMap<>();
        Map<MaterialEntity, List<ArticlePromoInfoEntity>> groupedArticlesByMaterial = preparationService.getChpuMaterialArticlesMap();
        if (Objects.isNull(groupedArticlesByMaterial) || groupedArticlesByMaterial.isEmpty()) {
            return templates;
        }
        groupedArticlesByMaterial.forEach((material, articles) -> createTemplate(templates, material, articles, postings));
        return templates;
    }

    private void createTemplate(Map<String, byte[]> templates,
                                MaterialEntity material,
                                List<ArticlePromoInfoEntity> articles,
                                List<PostingInfoModel> postings) {
        // 1. Группировка по толщине
        Map<Integer, List<PostingInfoModel>> groupingPostingsByThickness = preparationService.groupByThicknessAndArticles(postings, articles);

        groupingPostingsByThickness.forEach((thickness, ordersByThickness) -> {
            // 2. Группировка по номеру цвета
            Map<Integer, List<PostingInfoModel>> groupingByColorNumber = ordersByThickness.stream()
                    .collect(Collectors.groupingBy(p -> p.getProduct().getColorNumber()));

            groupingByColorNumber.forEach((colorNumber, ordersByColor) -> {
                try (Workbook wb = initialWorkBook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    fillSheetByMaterial(wb, ordersByColor, material);
                    setColumnWidths(wb.getSheet(SHEET_NAME));
                    wb.write(outputStream);

                    log.info("Успешно подготовлен шаблон: материал '{}', толщина {}мм, цвет №{}", material.getName(), thickness, colorNumber);

                    String fileName = String.format(EXCEL_NAME_PATTERN, material.getName(), thickness, colorNumber);
                    templates.put(fileName, outputStream.toByteArray());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private Workbook initialWorkBook() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(SHEET_NAME);
        sheet.setColumnWidth(0, 10000);
        return wb;
    }

    private void fillSheetByMaterial(Workbook wb,
                                     List<PostingInfoModel> postings,
                                     MaterialEntity material) {
        Sheet sheet = wb.getSheet(SHEET_NAME);
        if (postings.isEmpty()) {
            return;
        }

        fillRows(sheet, postings, material);
    }

    private void setColumnWidths(Sheet sheet) {
        for (int i = 0; i < MAX_COLUMNS; i++) {
            if (i <= 4) {
                sheet.setColumnWidth(i, 5000);
                continue;
            }
            if (i == 5 || i == 6) {
                sheet.setColumnWidth(i, 5000);
                continue;
            }
            sheet.autoSizeColumn(i);
        }
    }

    private void fillRows(Sheet sheet, List<PostingInfoModel> postings, MaterialEntity material) {
        int rowIndex = 0;
        for (var posting : postings) {
            Row row = sheet.createRow(rowIndex);
            rowIndex++;
            fillRow(row, posting, material);
        }
    }

    private void fillRow(Row row, PostingInfoModel posting, MaterialEntity material) {
        ProductModel product = posting.getProduct();
        boolean lengthGreaterThanWidth = product.getLength() > product.getWidth();
        row.createCell(0).setCellValue(A_COLUMN_VALUE);
        row.createCell(1).setCellValue(B_COLUMN_VALUE);
        row.createCell(2).setCellValue(material.getChpuMaterialName());

        var cell3 = row.createCell(3);
        cell3.setCellValue(Integer.parseInt(material.getChpuArticleNumber()));

        row.createCell(4).setCellValue(product.getThickness());
        row.createCell(5).setCellValue(EMPTY_STRING);
        row.createCell(6).setCellValue(EMPTY_STRING);
        row.createCell(7).setCellValue(lengthGreaterThanWidth ? product.getLength() : product.getWidth());
        row.createCell(8).setCellValue(lengthGreaterThanWidth ? product.getWidth() : product.getLength());
        row.createCell(9).setCellValue(lengthGreaterThanWidth ? product.getLength() : product.getWidth());
        row.createCell(10).setCellValue(lengthGreaterThanWidth ? product.getWidth() : product.getLength());
        row.createCell(11).setCellValue(product.getQuantity());

        var cell12 = row.createCell(12);

        if (posting.getMarketplace() == Marketplace.WILDBERRIES) {
            long postingNumber = Long.parseLong(posting.getPostingNumber());
            cell12.setCellValue(postingNumber);
        } else {
            cell12.setCellValue(posting.getPostingNumber());
        }
    }
}
