package ru.seller_support.assignment.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@UtilityClass
@Slf4j
public class FileUtils {

    public static byte[] createZip(byte[] excelBytes, String excelFileName,
                                   byte[] pdfBytes, String pdfName,
                                   Map<String, byte[]> chpuTemplates) {
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

            chpuTemplates.forEach((key, value) -> {
                try {
                    ZipEntry templateEntry = new ZipEntry(key);
                    zos.putNextEntry(templateEntry);
                    zos.write(value);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException("Ошибка при сохранении в архив шаблонов раскроя", e);
                }
            });

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

    public static byte[] convertSVGtoPDF(byte[] svgData) {
        try (ByteArrayInputStream svgInputStream = new ByteArrayInputStream(svgData);
             ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {

            Transcoder transcoder = new PDFTranscoder();

            TranscoderInput input = new TranscoderInput(svgInputStream);

            TranscoderOutput output = new TranscoderOutput(pdfOutputStream);

            transcoder.transcode(input, output);

            return pdfOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при конвертации SVG В PDF файлов этикеток");
        }
    }

}
