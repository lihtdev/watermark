package com.agile.watermark.creator.impl;

import cn.hutool.core.img.GraphicsUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.IoUtil;
import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.exception.WatermarkException;
import com.agile.watermark.model.*;
import com.agile.watermark.util.ColorUtils;
import com.agile.watermark.util.FontUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 给 Excel 文件添加水印
 * <p>
 * TODO Excel不支持固定位置水印
 * TODO
 *
 * @author lihaitao
 * @since 2020/7/5
 */
public class ExcelWatermarkCreator implements WatermarkCreator {

    /**
     *
     */
    private InputStream inputStream;

    private OutputStream outputStream;

    private InputStream imageStream;

    /**
     * 工作簿
     */
    private Workbook workbook;

    @Override
    public void create(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        setTextWatermarkForExcel2003((TextWatermark) watermark);
    }

    public void setTextWatermarkForExcel2003(TextWatermark textWatermark) throws IOException {
        HSSFWorkbook workbook = (HSSFWorkbook) WorkbookFactory.create(inputStream);
        this.workbook = workbook;

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
            HSSFPatriarch dp = sheet.createDrawingPatriarch();
            HSSFClientAnchor anchor = new HSSFClientAnchor(0, 255, 550, 0, (short) 0, 1, (short) 6, 5);
            HSSFTextbox textBox = dp.createTextbox(anchor);

            HSSFRichTextString richTextString = new HSSFRichTextString(textWatermark.getText());
            HSSFFont draftFont = workbook.createFont();
            draftFont.setColor(Font.COLOR_NORMAL);
            draftFont.setBold(true);
            draftFont.setFontHeightInPoints((short) textWatermark.getFontSize());
            draftFont.setFontName(textWatermark.getFontFamily());
            richTextString.applyFont(draftFont);

            textBox.setString(richTextString);
            textBox.setRotationDegree((short) textWatermark.getStyle().getFormat().getRotation());
            textBox.setLineWidth(600);
            textBox.setLineStyle(HSSFShape.LINESTYLE_NONE);
            textBox.setNoFill(true);
        }

        workbook.write(outputStream);
    }

    public void excel2007(TextWatermark textWatermark) throws IOException, EncryptedDocumentException {
        XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(inputStream);
        this.workbook = workbook;

        int sheetNumbers = workbook.getNumberOfSheets();
        for (int i = 0; i < sheetNumbers; i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            XSSFDrawing dp = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = new XSSFClientAnchor(0, 550, 550, 0, (short) 0, 1, (short) 6, 5);
            XSSFTextBox textBox = dp.createTextbox(anchor);
            XSSFRichTextString richTextString = new XSSFRichTextString(textWatermark.getText());
            XSSFFont draftFont = workbook.createFont();
            draftFont.setColor(Font.COLOR_NORMAL);
            draftFont.setBold(true);
            draftFont.setFontHeightInPoints((short) textWatermark.getFontSize());
            draftFont.setFontName(textWatermark.getFontFamily());
            richTextString.applyFont(draftFont);
            textBox.setText(richTextString);
            textBox.setLineWidth(600);
            textBox.setLineStyle(HSSFShape.LINESTYLE_NONE);
            textBox.setNoFill(true);
        }
        workbook.write(outputStream);
    }

    private void setImageWatermarkForExcel2003(ImageWatermark imageWatermark) throws IOException {
        HSSFWorkbook workbook = (HSSFWorkbook) WorkbookFactory.create(inputStream);
        this.workbook = workbook;
        this.imageStream = imageWatermark.getImageStream();
        byte[] imageBytes = IoUtil.readBytes(imageStream, true);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            WatermarkStyle watermarkStyle = imageWatermark.getStyle();
            if (watermarkStyle instanceof PositionWatermarkStyle) {
                throw new WatermarkException("Excel文件不支持固定位置水印");
            } else if (watermarkStyle instanceof RepeatWatermarkStyle) {
                RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) watermarkStyle;
                for (int row = 0; row < repeatWatermarkStyle.getRows(); row++) {
                    for (int col = 0; col < repeatWatermarkStyle.getCols(); col++) {
                        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 550, 550, 0, (short) 0, 1, (short) 6, 5);
                        int index = workbook.addPicture(imageBytes, getPictureType(imageWatermark));
                        HSSFPicture picture = patriarch.createPicture(anchor, index);
                        picture.setRotationDegree((short) imageWatermark.getStyle().getFormat().getRotation());
                        picture.resize();
                    }
                }
            }
        }
    }

    private void createImageWatermark(HSSFPatriarch patriarch, ImageWatermark imageWatermark) {

    }

    private static void setImageWatermarkStyle(BufferedImage bufferedImage, WatermarkStyle watermarkStyle) {
        Graphics2D graphics = bufferedImage.createGraphics();
        // 设置对线段的锯齿状边缘处理
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, watermarkStyle.getOpacity()));
        graphics.dispose();
        Color argbColor = new Color(255, 255, 255, 0);
        GraphicsUtil.createGraphics(bufferedImage, argbColor);
    }

    private static BufferedImage createTextImage(TextWatermark textWatermark) {
        java.awt.Font font = FontUtils.getFont(textWatermark.getFontFamily(), java.awt.Font.BOLD, textWatermark.getFontSize());
        int alpha = Math.round(255 * textWatermark.getStyle().getOpacity());
        Color color = ColorUtils.toArgbColor(textWatermark.getAwtColor(), alpha);
        return ImgUtil.createImage(textWatermark.getText(), font, null, color, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * 获取图片类型代码
     *
     * @param imageWatermark 图片水印
     * @author lihaitao
     * @since 2020-07-05
     */
    private int getPictureType(ImageWatermark imageWatermark) {
        switch (imageWatermark.getType()) {
            case EMF:
                return Workbook.PICTURE_TYPE_EMF;
            case WMF:
                return Workbook.PICTURE_TYPE_WMF;
            case PICT:
                return Workbook.PICTURE_TYPE_PICT;
            case JPEG:
                return Workbook.PICTURE_TYPE_JPEG;
            case DIB:
                return Workbook.PICTURE_TYPE_DIB;
            default:
                // 默认为 png 格式
                return Workbook.PICTURE_TYPE_PNG;
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
