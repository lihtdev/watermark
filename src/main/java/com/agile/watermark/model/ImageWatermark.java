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
     * 宽度（单位：px）
     */
    private int width = 300;

    /**
     * 高度（单位：px）
     */
    private int height = 200;

    /**
     * 图片输入流
     */
    private InputStream imageStream;

    /**
     * 图片格式（默认为 png 格式）
     */
    private Type type = Type.PNG;

    /**
     * 不允许调用无参构造方法
     *
     * @see #ImageWatermark(InputStream imageStream)
     */
    private ImageWatermark() {
    }

    public ImageWatermark(InputStream imageStream) {
        this.imageStream = imageStream;
    }

    /**
     * 图片格式枚举类
     *
     * @author lihaitao
     * @since 2020/7/3
     */
    public enum Type {
        /**
         * Extended windows meta file
         */
        EMF,

        /**
         * Windows Meta File
         */
        WMF,

        /**
         * Mac PICT format
         */
        PICT,

        /**
         * JPEG format
         */
        JPEG,

        /**
         * PNG format
         */
        PNG,

        /**
         * Device independent bitmap
         */
        DIB,

        /**
         * GIF image format
         */
        GIF,

        /**
         * Tag Image File (.tiff)
         */
        TIFF,

        /**
         * Encapsulated Postscript (.eps)
         */
        EPS,

        /**
         * Windows Bitmap (.bmp)
         */
        BMP,

        /**
         * WordPerfect graphics (.wpg)
         */
        WPG;
    }

}
