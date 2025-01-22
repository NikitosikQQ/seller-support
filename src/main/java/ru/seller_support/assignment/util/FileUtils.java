package ru.seller_support.assignment.util;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@UtilityClass
public class FileUtils {

    public static byte[] createZip(byte[] excelBytes, String excelFileName,
                                   byte[] pdfBytes, String pdfName) {
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
        return baos.toByteArray();
    }
}
