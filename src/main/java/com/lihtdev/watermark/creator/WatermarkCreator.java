package com.lihtdev.watermark.creator;

import com.lihtdev.watermark.model.Watermark;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 水印创建接口
 *
 * @author lihaitao
 * @since 2020/7/4
 */
public interface WatermarkCreator extends Closeable {

    /**
     * 创建水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 添加水印后的文件输出流
     * @param watermark    水印
     */
    void create(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException;

}
