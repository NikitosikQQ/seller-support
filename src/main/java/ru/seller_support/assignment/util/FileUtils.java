package ru.seller_support.assignment.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@UtilityClass
@Slf4j
public class FileUtils {

    public static byte[] createZip(byte[] excelBytes, String excelFileName,
                                   byte[] pdfBytes, String pdfName) {
        if (Objects.isNull(excelBytes) || Objects.isNull(pdfBytes)) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry excelEntry = new ZipEntry(excelFileName);
            zos.putNextEntry(excelEntry);
            zos.write(excelBytes);
            zos.closeEntry();

            ZipEntry pdfEntry = new ZipEntry(pdfName);
            zos.putNextEntry(pdfEntry);
            zos.write(pdfBytes);
            zos.closeEntry();

            zos.finish();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании архива с PDF и Excel файлами", e);
        }
        log.info("Успешно подготовлен zip архив");
        return baos.toByteArray();
    }
    
    public static byte[] mergePdfFiles(List<byte[]> pdfFiles) {
        if (Objects.isNull(pdfFiles) || pdfFiles.isEmpty()) {
            return null;
        }
        ByteArrayOutputStream mergedPdf = new ByteArrayOutputStream();

        try (PdfDocument pdfDoc = new PdfDocument(new PdfWriter(mergedPdf))) {
            PdfMerger merger = new PdfMerger(pdfDoc);

            for (byte[] pdfFile : pdfFiles) {
                try (PdfDocument sourcePdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfFile)))) {
                    merger.merge(sourcePdf, 1, sourcePdf.getNumberOfPages());
                } catch (IOException e) {
                    throw new RuntimeException("Ошибка при объединении pdf файлов этикеток");
                }
            }
        }
        log.info("Успешно подготовлен pdf-файл");
        return mergedPdf.toByteArray();
    }

}
