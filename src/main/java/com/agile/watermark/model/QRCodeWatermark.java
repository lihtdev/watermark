package com.agile.watermark.model;

import lombok.Data;

/**
 * 二维码水印
 *
 * @author lihaitao
 * @since 2020/7/16
 */
@Data
public class QRCodeWatermark extends Watermark {

    /**
     * 二维码内容
     */
    private String content;

    /**
     * 宽度
     */
    private int width = 300;

    /**
     * 高度
     */
    private int height = 300;

    public QRCodeWatermark(String content) {
        this.content = content;
    }
}
