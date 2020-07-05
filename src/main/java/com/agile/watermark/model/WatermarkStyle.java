package com.agile.watermark.model;

import lombok.Data;

/**
 * 水印样式父类
 *
 * @author lihaitao
 * @since 2020-07-03
 */
@Data
public abstract class WatermarkStyle {

    /**
     * 水印版式
     */
    private Format format = Format.OBLIQUE;

    /**
     * 不透明度（范围 0~1: 0-完全透明，1-完全不透明）
     */
    private double opacity = 0.5;

    /**
     * 水印版式
     *
     * @author lihaitao
     * @since 2020/7/3
     */
    public enum Format {
        /**
         * 水平
         */
        HORIZONTAL(0),
        /**
         * 垂直
         */
        VERTICAL(-90),
        /**
         * 斜式
         */
        OBLIQUE(-45);

        /**
         * 旋转度
         */
        private int rotation;

        Format(int rotation) {
            this.rotation = rotation;
        }

        public int getRotation() {
            return this.rotation;
        }
    }

}
