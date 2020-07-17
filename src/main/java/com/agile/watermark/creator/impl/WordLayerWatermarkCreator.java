package com.agile.watermark.creator.impl;

import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.model.Watermark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 给 Word 文件添加图层水印的实现类（图层水印以图片的形式悬浮于文字之上，不易删除，需挨个选中删除）
 *
 * @author lihaitao
 * @since 2020/7/17
 */
public class WordLayerWatermarkCreator implements WatermarkCreator {

    @Override
    public void create(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

}
