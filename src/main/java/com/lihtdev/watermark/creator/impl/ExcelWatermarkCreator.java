package com.lihtdev.watermark.creator.impl;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.IoUtil;
import com.lihtdev.watermark.creator.WatermarkCreator;
import com.lihtdev.watermark.exception.WatermarkException;
import com.lihtdev.watermark.model.*;
import com.lihtdev.watermark.util.ImageUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 给 Excel 文件添加水印
 * <p>
 * TODO 该类存在以下问题：水印位置尚未调整好
 *
 * @author lihaitao
 * @since 2020/7/5
 */
public class ExcelWatermarkCreator implements WatermarkCreator {

    /**
     * 原文件输入流
     */
    private InputStream inputStream;

    /**
     * 图片水印输入流
     */
    private InputStream imageStream;

    /**
     * 工作簿
     */
    private Workbook workbook;

    @Override
    public void create(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        this.inputStream = inputStream;
        this.workbook = WorkbookFactory.create(inputStream);

        if (watermark instanceof TextWatermark) {
            setTextWatermark((TextWatermark) watermark);
        } else if (watermark instanceof ImageWatermark) {
            ImageWatermark imageWatermark = (ImageWatermark) watermark;
            BufferedImage bufferedImage = ImageUtils.createImage(imageWatermark);
            imageWatermark.setImageStream(ImgUtil.toStream(bufferedImage, imageWatermark.getType().name()));
            setImageWatermark(imageWatermark);
        }

        this.workbook.write(outputStream);
    }

    public void setTextWatermark(TextWatermark textWatermark) throws IOException {
        BufferedImage bufferedImage = ImageUtils.createImage(textWatermark);
        ImageWatermark imageWatermark = new ImageWatermark(null);
        imageWatermark.setStyle(textWatermark.getStyle());
        imageWatermark.setWidth(bufferedImage.getWidth());
        imageWatermark.setHeight(bufferedImage.getHeight());
        imageWatermark.setType(ImageWatermark.Type.PNG);
        imageWatermark.setImageStream(ImgUtil.toStream(bufferedImage, imageWatermark.getType().name()));
        setImageWatermark(imageWatermark);
    }

    public void setImageWatermark(ImageWatermark imageWatermark) {
        if (this.workbook instanceof HSSFWorkbook) {
            setWatermarkForExcel2003(imageWatermark);
        } else if (this.workbook instanceof XSSFWorkbook) {
            setWatermarkForExcel2007(imageWatermark);
        }
    }

    private void setWatermarkForExcel2003(ImageWatermark imageWatermark) {
        this.imageStream = imageWatermark.getImageStream();
        byte[] imageBytes = IoUtil.readBytes(imageStream, true);
        HSSFWorkbook hssfWorkbook = (HSSFWorkbook) this.workbook;

        for (int i = 0; i < hssfWorkbook.getNumberOfSheets(); i++) {
            HSSFSheet sheet = hssfWorkbook.getSheetAt(i);
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();

            int defaultColumnWidth = sheet.getDefaultColumnWidth();
            int defaultRowHeight = sheet.getDefaultRowHeight();
            int watermarkInCols = imageWatermark.getWidth() / defaultColumnWidth;
            int watermarkInRows = imageWatermark.getHeight() / defaultRowHeight;

            WatermarkStyle watermarkStyle = imageWatermark.getStyle();
            if (watermarkStyle instanceof PositionWatermarkStyle) {
                throw new WatermarkException("Excel文件不支持固定位置水印");
            } else if (watermarkStyle instanceof RepeatWatermarkStyle) {
                RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) watermarkStyle;
                for (int row = 0; row < repeatWatermarkStyle.getRows(); row++) {
                    for (int col = 0; col < repeatWatermarkStyle.getCols(); col++) {
                        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0,
                                (short) col, row, (short) (col + watermarkInCols), row + watermarkInRows);
                        int index = hssfWorkbook.addPicture(imageBytes, getPictureType(imageWatermark));
                        HSSFPicture picture = patriarch.createPicture(anchor, index);
                        picture.setRotationDegree((short) imageWatermark.getStyle().getFormat().getRotation());
                        picture.resize();
                    }
                }
            }
        }
    }

    private void setWatermarkForExcel2007(ImageWatermark imageWatermark) {
        this.imageStream = imageWatermark.getImageStream();
        byte[] imageBytes = IoUtil.readBytes(imageStream, true);
        XSSFWorkbook xssfWorkbook = (XSSFWorkbook) this.workbook;

        for (int i = 0; i < xssfWorkbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = xssfWorkbook.getSheetAt(i);
            XSSFDrawing drawing = sheet.createDrawingPatriarch();

            int defaultColumnWidth = sheet.getDefaultColumnWidth();
            int defaultRowHeight = sheet.getDefaultRowHeight();
            int watermarkInCols = imageWatermark.getWidth() / defaultColumnWidth;
            int watermarkInRows = imageWatermark.getHeight() / defaultRowHeight;

            WatermarkStyle watermarkStyle = imageWatermark.getStyle();
            if (watermarkStyle instanceof PositionWatermarkStyle) {
                throw new WatermarkException("Excel文件不支持固定位置水印");
            } else if (watermarkStyle instanceof RepeatWatermarkStyle) {
                RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) watermarkStyle;
                for (int row = 0; row < repeatWatermarkStyle.getRows(); row++) {
                    for (int col = 0; col < repeatWatermarkStyle.getCols(); col++) {
                        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0,
                                col, row, col + watermarkInCols, row + watermarkInRows);
                        int index = xssfWorkbook.addPicture(imageBytes, getPictureType(imageWatermark));
                        XSSFPicture picture = drawing.createPicture(anchor, index);
                        picture.resize();
                    }
                }
            }
        }
    }

    private void createImageWatermark(HSSFPatriarch patriarch, ImageWatermark imageWatermark) {

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
