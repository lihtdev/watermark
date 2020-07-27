package com.agile.watermark;

import com.agile.watermark.model.*;
import com.agile.watermark.util.WatermarkUtils;
import org.junit.jupiter.api.Test;

import java.io.*;

/**
 * 给 PDF 添加水印的测试类
 *
 * @author lihaitao
 * @since 2020/7/9
 */
public class PdfWatermarkTests {

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

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/pdf.pdf");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/position-text-watermark.pdf")) {
            WatermarkUtils.setWatermarkForPdf(inputStream, outputStream, watermark);
        }
    }

    /**
     * 添加重复的文本水印
     */
    @Test
    public void testSetRepeatTextWatermark() throws IOException {
        RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
        watermarkStyle.setFormat(WatermarkStyle.Format.OBLIQUE);
        watermarkStyle.setXSpace(50);
        watermarkStyle.setYSpace(50);

        TextWatermark watermark = new TextWatermark();
        watermark.setText("禁止复制");
        watermark.setFontFamily("楷体");
        watermark.setColor("orange");
        watermark.setStyle(watermarkStyle);

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/pdf.pdf");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/repeat-text-watermark.pdf")) {
            WatermarkUtils.setWatermarkForPdf(inputStream, outputStream, watermark);
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
             InputStream inputStream = new FileInputStream(BASE_PATH + "/pdf.pdf");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/position-image-watermark.pdf")) {

            ImageWatermark watermark = new ImageWatermark(imageStream);
            watermark.setWidth(230);
            watermark.setHeight(68);
            watermark.setStyle(watermarkStyle);

            WatermarkUtils.setWatermarkForPdf(inputStream, outputStream, watermark);
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
             InputStream inputStream = new FileInputStream(BASE_PATH + "/pdf.pdf");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/repeat-image-watermark.pdf")) {

            ImageWatermark watermark = new ImageWatermark(imageStream);
            watermark.setWidth(230);
            watermark.setHeight(68);
            watermark.setStyle(watermarkStyle);

            WatermarkUtils.setWatermarkForPdf(inputStream, outputStream, watermark);
        }
    }

    /**
     * 添加固定位置的二维码水印
     */
    @Test
    public void testSetPositionQRCodeWatermark() throws IOException {
        PositionWatermarkStyle watermarkStyle = new PositionWatermarkStyle();
        watermarkStyle.setFormat(WatermarkStyle.Format.HORIZONTAL);

        QRCodeWatermark watermark = new QRCodeWatermark("我是中国人");
        watermark.setStyle(watermarkStyle);

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/pdf.pdf");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/position-qr-code-watermark.pdf")) {
            WatermarkUtils.setWatermarkForPdf(inputStream, outputStream, watermark);
        }
    }

    /**
     * 添加重复的二维码水印
     */
    @Test
    public void testSetRepeatQRCodeWatermark() throws IOException {
        RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
        watermarkStyle.setFormat(WatermarkStyle.Format.OBLIQUE);

        QRCodeWatermark watermark = new QRCodeWatermark("我是中国人");
        watermark.setStyle(watermarkStyle);

        try (InputStream inputStream = new FileInputStream(BASE_PATH + "/pdf.pdf");
             OutputStream outputStream = new FileOutputStream(BASE_PATH + "/repeat-qr-code-watermark.pdf")) {
            WatermarkUtils.setWatermarkForPdf(inputStream, outputStream, watermark);
        }
    }

}
