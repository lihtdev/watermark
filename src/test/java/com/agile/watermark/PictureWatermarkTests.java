package com.agile.watermark;

import com.agile.watermark.model.*;
import com.agile.watermark.util.WatermarkUtils;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.*;

/**
 * 给图片添加水印的测试类
 *
 * @author lihaitao
 * @since 2020/7/7
 */
public class PictureWatermarkTests {

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
        watermarkStyle.setYStart(-80);
        watermarkStyle.setFormat(WatermarkStyle.Format.OBLIQUE);

        TextWatermark watermark = new TextWatermark();
        watermark.setText("禁止复制");
        watermark.setFontFamily("楷体");
        watermark.setColor("green");
        watermark.setStyle(watermarkStyle);

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/picture.png");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/picture-watermark.png")) {
            WatermarkUtils.setWatermarkForWord(inputStream, outputStream, watermark);
        }

    }

    /**
     * 添加图片水印
     */
    @Test
    public void testSetImageWatermark() throws IOException {
        try (InputStream imageStream = new FileInputStream(BASE_PATH + "/watermark.png");
             InputStream inputStream = new FileInputStream(BASE_PATH + "/picture.png");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/picture-watermark.png")) {

            ImageWatermark watermark = new ImageWatermark(imageStream);
            watermark.setWidth(300);
            watermark.setHeight(200);

            WatermarkUtils.setWatermarkForPicture(inputStream, outputStream, watermark);
        }
    }

    public static void main(String[] args) {
//        Color red = Color.decode("red");
//        System.out.println(red);
        Color gray = Color.decode("#d0d0d0");
        System.out.println(gray);
        Color black = Color.decode("#000000");
        System.out.println(black);
    }

}
