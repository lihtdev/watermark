package com.agile.watermark;

import com.agile.watermark.model.*;
import com.agile.watermark.util.WatermarkUtils;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 给 Word 文件添加水印测试类
 *
 * @author lihaitao
 * @since 2020/7/6
 */
public class WordWatermarkTests {

    /**
     * 添加固定位置的文本水印
     */
    @Test
    public void testSetPositionTextWatermarkForWord() throws IOException {

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

        String filepath = "/Users/lihaitao/temp/watermark/";
        WatermarkUtils.setWatermarkForWord(new FileInputStream(filepath + "/file.docx"),
                new FileOutputStream(filepath + "/watermark.docx"), watermark);
    }

    /**
     * 添加重复的文本水印
     */
    @Test
    public void testSetRepeatTextWatermarkForWord() throws IOException {
        RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
        watermarkStyle.setYStart(-80);
        watermarkStyle.setFormat(WatermarkStyle.Format.OBLIQUE);

        TextWatermark watermark = new TextWatermark();
        watermark.setText("禁止复制");
        watermark.setFontFamily("楷体");
        watermark.setColor("green");
        watermark.setStyle(watermarkStyle);

        String filepath = "/Users/lihaitao/temp/watermark/";
        WatermarkUtils.setWatermarkForWord(new FileInputStream(filepath + "/file.docx"),
                new FileOutputStream(filepath + "/watermark.docx"), watermark);

    }

    /**
     * 添加图片水印
     */
    @Test
    public void testSetImageWatermarkForWord() throws IOException {
        String filepath = "/Users/lihaitao/temp/watermark/";
        ImageWatermark watermark = new ImageWatermark(new FileInputStream(filepath + "/watermark.png"));
        watermark.setWidth(300);
        watermark.setHeight(200);
        WatermarkUtils.setWatermarkForWord(new FileInputStream(filepath + "/file.docx"),
                new FileOutputStream(filepath + "/watermark.docx"), watermark);
    }

}
