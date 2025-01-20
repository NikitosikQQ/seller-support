package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {

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
            Map.entry(9, "Сумма за метр кв"),
            Map.entry(10, "Номер цвета"),
            Map.entry(11, "Цвет"),
            Map.entry(12, "Комментарий")
    );

    public void createReportFile() {
        Workbook wb = initialWorkBook();
        try (OutputStream outputStream = new FileOutputStream("test.xlsx")) {
            wb.write(outputStream);
            log.info("успешно создан файл");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private Workbook initialWorkBook() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Отправления");
        createHeaderRow(sheet);
        return wb;
    }

    private void createHeaderRow(Sheet sheet) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < HEADERS_NAMING_MAP.size(); i++) {
            row.createCell(i).setCellValue(HEADERS_NAMING_MAP.get(i));
        }
    }

}
