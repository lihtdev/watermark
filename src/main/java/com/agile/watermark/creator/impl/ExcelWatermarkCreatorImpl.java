package com.agile.watermark.creator.impl;

import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.model.Watermark;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;

/**
 * 给 Excel 文件添加水印
 *
 * @author lihaitao
 * @since 2020/7/5
 */
public class ExcelWatermarkCreatorImpl implements WatermarkCreator {

    private InputStream inputStream;

    private InputStream imageStream;

    private HSSFWorkbook workbook;

    @Override
    public void create(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {

    }

    public static void excel2003(String src, String target, String text)
            throws IOException, EncryptedDocumentException, InvalidFormatException {
        HSSFWorkbook wb = null;
        OutputStream out = null;
        try {
            InputStream input = new FileInputStream(src);

            wb = (HSSFWorkbook) WorkbookFactory.create(input);
            HSSFSheet sheet = null;

            int sheetNumbers = wb.getNumberOfSheets();

            // sheet
            for (int i = 0; i < sheetNumbers; i++) {
                sheet = wb.getSheetAt(i);
                // sheet.createDrawingPatriarch();

                HSSFPatriarch dp = sheet.createDrawingPatriarch();
                HSSFClientAnchor anchor = new HSSFClientAnchor(0, 255, 550, 0, (short) 0, 1, (short) 6, 5);

                // HSSFComment comment = dp.createComment(anchor);
                HSSFTextbox txtbox = dp.createTextbox(anchor);

                HSSFRichTextString rtxt = new HSSFRichTextString(text);
                HSSFFont draftFont = (HSSFFont) wb.createFont();
                // 水印颜色
                draftFont.setColor((short) 55);
//                draftFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                // 字体大小
                draftFont.setFontHeightInPoints((short) 30);
                draftFont.setFontName("Verdana");
                rtxt.applyFont(draftFont);
                txtbox.setString(rtxt);
                // 倾斜度
                txtbox.setRotationDegree((short) 315);
                txtbox.setLineWidth(600);
                txtbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
                txtbox.setNoFill(true);
            }

            out = new FileOutputStream(target);
            wb.write(out);
        } finally {
            if (wb != null) {
                wb.close();
            }
            if (out != null) {
                out.close();
            }
        }

    }

    public static void excel2007(String src, String target, String text)
            throws IOException, EncryptedDocumentException, InvalidFormatException {
        XSSFWorkbook wb = null;
        OutputStream out = null;
        try {
            InputStream input = new FileInputStream(src);
            wb = (XSSFWorkbook) WorkbookFactory.create(input);

            XSSFSheet sheet = null;
            int sheetNumbers = wb.getNumberOfSheets();
            for (int i = 0; i < sheetNumbers; i++) {
                sheet = wb.getSheetAt(i);
                XSSFDrawing dp = sheet.createDrawingPatriarch();
                XSSFClientAnchor anchor = new XSSFClientAnchor(0, 550, 550, 0, (short) 0, 1, (short) 6, 5);
                XSSFTextBox txtbox = dp.createTextbox(anchor);
                XSSFRichTextString rtxt = new XSSFRichTextString(text);
                XSSFFont draftFont = (XSSFFont) wb.createFont();
                draftFont.setColor((short) 55);
//                draftFont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
                draftFont.setFontHeightInPoints((short) 30);
                draftFont.setFontName("Verdana");
                rtxt.applyFont(draftFont);
                txtbox.setText(rtxt);
                // 倾斜度
                txtbox.setLineWidth(600);
                txtbox.setLineStyle(HSSFShape.LINESTYLE_NONE);
                txtbox.setNoFill(true);
            }

            out = new FileOutputStream(target);
            wb.write(out);
        } finally {
            if (wb != null) {
                wb.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        if (imageStream != null) {
            imageStream.close();
        }
        // outputStream 不需要关闭，因添加水印后要返回给调用者
    }
}
