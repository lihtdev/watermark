package com.agile.watermark.creator.impl;

import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.model.Watermark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 给 Excel 文件添加水印
 *
 * @author lihaitao
 * @since 2020/7/5
 */
public class ExcelWatermarkCreatorImpl implements WatermarkCreator {

    @Override
    public void create(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
