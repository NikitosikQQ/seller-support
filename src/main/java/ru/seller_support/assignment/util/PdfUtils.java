package ru.seller_support.assignment.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@UtilityClass
public class PdfUtils {

    public static byte[] mergePdfFiles(List<byte[]> pdfFiles) {
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

        return mergedPdf.toByteArray();
    }
}
