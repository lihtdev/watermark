package com.agile.watermark;

import com.agile.watermark.model.*;
import com.agile.watermark.util.WatermarkUtils;
import org.junit.jupiter.api.Test;

import java.io.*;

/**
 * @author lihaitao
 * @since 2020/7/6
 */
public class ExcelWatermarkTests {

    private static final String BASE_PATH = "c:/temp/watermark/";

    /**
     * 添加固定位置的文本水印
     */
    @Test
    public void testSetPositionTextWatermark() throws IOException {

        PositionWatermarkStyle watermarkStyle = new PositionWatermarkStyle();
        watermarkStyle.setPositions(new PositionWatermarkStyle.Position[]{
                PositionWatermarkStyle.Position.LEFT_BOTTOM, PositionWatermarkStyle.Position.LEFT_TOP,
                PositionWatermarkStyle.Position.RIGHT_BOTTOM, PositionWatermarkStyle.Position.RIGHT_TOP,
                PositionWatermarkStyle.Position.CENTER});
        watermarkStyle.setFormat(WatermarkStyle.Format.HORIZONTAL);

        TextWatermark watermark = new TextWatermark();
        watermark.setText("禁止复制");
        watermark.setFontFamily("楷体");
        watermark.setColor("red");
        watermark.setStyle(watermarkStyle);

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/excel.docx");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/excel-watermark.docx")) {
            WatermarkUtils.setWatermarkForExcel(inputStream, outputStream, watermark);
        }
    }

    /**
     * 添加重复的文本水印
     */
    @Test
    public void testSetRepeatTextWatermark() throws IOException {
        RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
        watermarkStyle.setYStart(-80);
        watermarkStyle.setFormat(WatermarkStyle.Format.OBLIQUE);

        TextWatermark watermark = new TextWatermark();
        watermark.setText("禁止复制");
        watermark.setFontFamily("楷体");
        watermark.setColor("green");
        watermark.setStyle(watermarkStyle);

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/excel.docx");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/excel-watermark.docx")) {
            WatermarkUtils.setWatermarkForExcel(inputStream, outputStream, watermark);
        }

    }

    /**
     * 添加图片水印
     */
    @Test
    public void testSetImageWatermark() throws IOException {
        try (InputStream imageStream = new FileInputStream(BASE_PATH + "/watermark.png");
             InputStream inputStream = new FileInputStream(BASE_PATH + "/excel.docx");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/excel-watermark.docx")) {

            ImageWatermark watermark = new ImageWatermark(imageStream);
            watermark.setWidth(300);
            watermark.setHeight(200);

            WatermarkUtils.setWatermarkForExcel(inputStream, outputStream, watermark);
        }
    }

}
