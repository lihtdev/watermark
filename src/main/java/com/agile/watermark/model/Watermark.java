package com.agile.watermark.model;

import lombok.Data;

/**
 * 水印父类
 *
 * @author lihaitao
 * @since 2020/7/3
 */
@Data
public abstract class Watermark {

    /**
     * 水印风格
     */
    private WatermarkStyle style = new PositionWatermarkStyle();

}
