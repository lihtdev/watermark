package com.agile.watermark.util;

import com.agile.watermark.creator.impl.PPTWatermarkCreatorImpl;
import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.creator.impl.ExcelWatermarkCreatorImpl;
import com.agile.watermark.creator.impl.PdfWatermarkCreatorImpl;
import com.agile.watermark.creator.impl.PictureWatermarkCreatorImpl;
import com.agile.watermark.creator.impl.WordWatermarkCreatorImpl;
import com.agile.watermark.model.RepeatWatermarkStyle;
import com.agile.watermark.model.TextWatermark;
import com.agile.watermark.model.Watermark;

import java.io.*;

/**
 * 水印工具类
 *
 * @author lihaitao
 * @since 2020-07-03
 */
public class WatermarkUtils {

    /**
     * 为 Word 文件设置水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 加水印后的文件输出流
     * @param watermark    水印
     * @author lihaitao
     * @since 2020/7/3
     */
    public static void setWatermarkForWord(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        try (WatermarkCreator watermarkCreator = new WordWatermarkCreatorImpl()) {
            watermarkCreator.create(inputStream, outputStream, watermark);
        }
    }

    /**
     * 为 Excel 文件设置水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 加水印后的文件输出流
     * @param watermark    水印
     * @author lihaitao
     * @since 2020/7/3
     */
    public static void setWatermarkForExcel(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        try (WatermarkCreator watermarkCreator = new ExcelWatermarkCreatorImpl()) {
            watermarkCreator.create(inputStream, outputStream, watermark);
        }
    }

    /**
     * 为 ppt 文件设置水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 加水印后的文件输出流
     * @param watermark    水印
     * @author lihaitao
     * @since 2020/7/3
     */
    public static void setWatermarkForPPT(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        try (WatermarkCreator watermarkCreator = new PPTWatermarkCreatorImpl()) {
            watermarkCreator.create(inputStream, outputStream, watermark);
        }
    }

    /**
     * 为 PDF 文件设置水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 加水印后的文件输出流
     * @param watermark    水印
     * @author lihaitao
     * @since 2020/7/3
     */
    public static void setWatermarkForPdf(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        try (WatermarkCreator watermarkCreator = new PdfWatermarkCreatorImpl()) {
            watermarkCreator.create(inputStream, outputStream, watermark);
        }
    }

    /**
     * 为图片设置水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 加水印后的文件输出流
     * @param watermark    水印
     * @author lihaitao
     * @since 2020/7/3
     */
    public static void setWatermarkForPicture(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        try (WatermarkCreator watermarkCreator = new PictureWatermarkCreatorImpl()) {
            watermarkCreator.create(inputStream, outputStream, watermark);
        }
    }

    public static void main(String[] args) throws IOException {
        TextWatermark watermark = new TextWatermark();
        watermark.setText("禁止复制");
        RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
        watermarkStyle.setYStart(-80);
        watermark.setStyle(watermarkStyle);
        String filepath = "/Users/lihaitao/temp/watermark/";
        WatermarkUtils.setWatermarkForWord(new FileInputStream(filepath + "/file.docx"), new FileOutputStream(filepath + "/watermark.docx"), watermark);
    }

}

