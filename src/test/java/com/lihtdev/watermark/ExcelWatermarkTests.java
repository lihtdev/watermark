package com.lihtdev.watermark;

import com.lihtdev.watermark.model.*;
import com.lihtdev.watermark.model.*;
import com.lihtdev.watermark.util.WatermarkUtils;
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

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/excel.xls");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/excel-watermark.xls")) {
            WatermarkUtils.setWatermarkForExcel(inputStream, outputStream, watermark);
        }
    }

    /**
     * 添加重复的文本水印
     */
    @Test
    public void testSetRepeatTextWatermark() throws IOException {
        RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
        watermarkStyle.setFormat(WatermarkStyle.Format.OBLIQUE);

        TextWatermark watermark = new TextWatermark();
        watermark.setText("禁止复制");
        watermark.setFontFamily("楷体");
        watermark.setColor("red");
        watermark.setStyle(watermarkStyle);

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/excel.xls");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/excel-watermark.xls")) {
            WatermarkUtils.setWatermarkForExcel(inputStream, outputStream, watermark);
        }

    }

    /**
     * 添加固定位置的图片水印
     */
    @Test
    public void testSetPositionImageWatermark() throws IOException {
        PositionWatermarkStyle watermarkStyle = new PositionWatermarkStyle();
        watermarkStyle.setPositions(new PositionWatermarkStyle.Position[]{
                PositionWatermarkStyle.Position.LEFT_BOTTOM, PositionWatermarkStyle.Position.LEFT_TOP,
                PositionWatermarkStyle.Position.RIGHT_BOTTOM, PositionWatermarkStyle.Position.RIGHT_TOP,
                PositionWatermarkStyle.Position.CENTER});
        watermarkStyle.setFormat(WatermarkStyle.Format.HORIZONTAL);

        try (InputStream imageStream = new FileInputStream(BASE_PATH + "/watermark.png");
             InputStream inputStream = new FileInputStream(BASE_PATH + "/excel.xls");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/excel-watermark.xls")) {

            ImageWatermark watermark = new ImageWatermark(imageStream);
            watermark.setWidth(230);
            watermark.setHeight(68);
            watermark.setStyle(watermarkStyle);

            WatermarkUtils.setWatermarkForExcel(inputStream, outputStream, watermark);
        }
    }

    /**
     * 添加重复的图片水印
     */
    @Test
    public void testSetRepeatImageWatermark() throws IOException {
        RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
        watermarkStyle.setOpacity(0.5f);
        watermarkStyle.setFormat(WatermarkStyle.Format.OBLIQUE);
        watermarkStyle.setXSpace(50);
        watermarkStyle.setYSpace(50);

        try (InputStream imageStream = new FileInputStream(BASE_PATH + "/watermark.png");
             InputStream inputStream = new FileInputStream(BASE_PATH + "/excel.xls");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/excel-watermark.xls")) {

            ImageWatermark watermark = new ImageWatermark(imageStream);
            watermark.setWidth(230);
            watermark.setHeight(68);
            watermark.setStyle(watermarkStyle);

            WatermarkUtils.setWatermarkForExcel(inputStream, outputStream, watermark);
        }
    }

}
