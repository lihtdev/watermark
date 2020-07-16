package com.agile.watermark;

import com.agile.watermark.enums.ChineseFont;
import com.agile.watermark.model.*;
import com.agile.watermark.util.WatermarkUtils;
import org.junit.jupiter.api.Test;

import java.io.*;

/**
 * 给图片添加水印的测试类
 *
 * @author lihaitao
 * @since 2020/7/7
 */
public class PictureWatermarkTests {

    private static final String BASE_PATH = "/Users/lihaitao/temp/watermark/";

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
        watermark.setText("禁止watermark复制");
        watermark.setFontFamily("楷体");
        watermark.setFontSize(100);
        watermark.setColor(TextWatermark.Color.RED);
        watermark.setStyle(watermarkStyle);

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/picture.png");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/picture-watermark.png")) {
            WatermarkUtils.setWatermarkForPicture(inputStream, outputStream, watermark);
        }
    }

    /**
     * 添加重复的文本水印
     */
    @Test
    public void testSetRepeatTextWatermark() throws IOException {
        RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
        watermarkStyle.setFormat(WatermarkStyle.Format.OBLIQUE);
        watermarkStyle.setOpacity(0.8f);

        TextWatermark watermark = new TextWatermark();
        watermark.setText("禁止复制");
        watermark.setFontFamily(ChineseFont.ALIBABA_PU_HUI_TI);
        watermark.setFontSize(50);
        watermark.setColor(TextWatermark.Color.GREEN);
        watermark.setStyle(watermarkStyle);

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/picture.png");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/picture-watermark.png")) {
            WatermarkUtils.setWatermarkForPicture(inputStream, outputStream, watermark);
        }

    }

    /**
     * 添加固定位置的图片水印
     */
    @Test
    public void testSetPositionImageWatermark() throws IOException {
        try (InputStream imageStream = new FileInputStream(BASE_PATH + "/watermark.png");
             InputStream inputStream = new FileInputStream(BASE_PATH + "/picture.png");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/picture-watermark.png")) {

            PositionWatermarkStyle watermarkStyle = new PositionWatermarkStyle();
            watermarkStyle.setPositions(new PositionWatermarkStyle.Position[]{
                    PositionWatermarkStyle.Position.LEFT_BOTTOM, PositionWatermarkStyle.Position.LEFT_TOP,
                    PositionWatermarkStyle.Position.RIGHT_BOTTOM, PositionWatermarkStyle.Position.RIGHT_TOP,
                    PositionWatermarkStyle.Position.CENTER});
            watermarkStyle.setFormat(WatermarkStyle.Format.HORIZONTAL);

            ImageWatermark watermark = new ImageWatermark(imageStream);
            watermark.setWidth(430);
            watermark.setHeight(138);
            watermark.setStyle(watermarkStyle);

            WatermarkUtils.setWatermarkForPicture(inputStream, outputStream, watermark);
        }
    }

    /**
     * 添加重复的图片水印
     */
    @Test
    public void testSetRepeatImageWatermark() throws IOException {
        try (InputStream imageStream = new FileInputStream(BASE_PATH + "/watermark.jpg");
             InputStream inputStream = new FileInputStream(BASE_PATH + "/picture.jpg");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/picture-watermark.jpg")) {

            RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
            watermarkStyle.setFormat(WatermarkStyle.Format.OBLIQUE);
            watermarkStyle.setOpacity(0.5f);

            ImageWatermark watermark = new ImageWatermark(imageStream);
            watermark.setWidth(200);
            watermark.setHeight(100);
            watermark.setStyle(watermarkStyle);

            WatermarkUtils.setWatermarkForPicture(inputStream, outputStream, watermark);
        }
    }

    @Test
    public void testSetQRCodeWatermark() throws IOException {
        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/picture.png");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/picture-watermark.png")) {

            PositionWatermarkStyle watermarkStyle = new PositionWatermarkStyle();
            watermarkStyle.setFormat(WatermarkStyle.Format.HORIZONTAL);

            QRCodeWatermark watermark = new QRCodeWatermark("我是中国人");
            watermark.setStyle(watermarkStyle);

            WatermarkUtils.setWatermarkForPicture(inputStream, outputStream, watermark);
        }
    }

}
