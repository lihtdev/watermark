package com.agile.watermark.model;

import lombok.Data;

/**
 * 重复水印
 *
 * @author lihaitao
 * @since 2020/7/3
 */
@Data
public class RepeatWatermarkStyle extends WatermarkStyle {

    /**
     * 横向个数
     */
    private int cols = 10;

    /**
     * 纵向个数
     */
    private int rows = 10;

    /**
     * 横向间距（单位：px）
     */
    private int xSpace = 100;

    /**
     * 纵向间距（单位：px）
     */
    private int ySpace = 200;

}
