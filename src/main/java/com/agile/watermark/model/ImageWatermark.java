package com.agile.watermark.model;

import lombok.Data;

import java.io.InputStream;

/**
 * 图片水印
 *
 * @author lihaitao
 * @since 2020/7/3
 */
@Data
public class ImageWatermark extends Watermark {

    /**
     * 宽度
     */
    private int width;

    /**
     * 高度
     */
    private int height;

    /**
     * 图片输入流
     */
    private InputStream imageStream;

}
