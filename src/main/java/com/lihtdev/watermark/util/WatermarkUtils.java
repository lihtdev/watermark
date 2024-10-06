package com.lihtdev.watermark.util;

import com.lihtdev.watermark.creator.WatermarkCreator;
import com.lihtdev.watermark.creator.impl.*;
import com.lihtdev.watermark.creator.impl.*;
import com.lihtdev.watermark.model.Watermark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        try (WatermarkCreator watermarkCreator = new WordLayerWatermarkCreator()) {
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
        try (WatermarkCreator watermarkCreator = new ExcelWatermarkCreator()) {
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
        try (WatermarkCreator watermarkCreator = new PPTWatermarkCreator()) {
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
        try (WatermarkCreator watermarkCreator = new PdfWatermarkCreator()) {
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
        try (WatermarkCreator watermarkCreator = new PictureWatermarkCreator()) {
            watermarkCreator.create(inputStream, outputStream, watermark);
        }
    }

}
