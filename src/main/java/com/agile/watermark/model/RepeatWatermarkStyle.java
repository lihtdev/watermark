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
    private int xSpace = 160;

    /**
     * 纵向间距（单位：px）
     */
    private int ySpace = 160;

    /**
     * 水印起始位置X轴坐标
     */
    private int xStart = 0;

    /**
     * 水印起始位置Y轴坐标
     */
    private int yStart = 0;

}
