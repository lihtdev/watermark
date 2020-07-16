package com.agile.watermark.util;

import com.agile.watermark.exception.WatermarkException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 二维码工具类
 *
 * @author lihaitao
 * @since 2020/7/17
 */
public class QRCodeUtils {

    /**
     * 生成二维码
     *
     * @param content      二维码内容
     * @param width        宽度
     * @param height       高度
     * @param outputStream 输出流
     */
    public static void encode(String content, int width, int height, OutputStream outputStream) throws IOException {
        Map<EncodeHintType, String> hints = new ConcurrentHashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        BitMatrix matrix;
        try {
            matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        } catch (WriterException e) {
            throw new WatermarkException("生成二维码时发生错误", e);
        }
        MatrixToImageWriter.writeToStream(matrix, "png", outputStream);
    }

}
