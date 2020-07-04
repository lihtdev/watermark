package com.agile.watermark.model;

import lombok.Data;

/**
 * 固定位置水印
 *
 * @author lihaitao
 * @since 2020/7/3
 */
@Data
public class PositionWatermarkStyle extends WatermarkStyle {

    /**
     * 水印位置（多选）
     */
    private PositionWatermarkStyle.Position[] positions = {PositionWatermarkStyle.Position.CENTER};

    /**
     * 水印位置枚举
     */
    public enum Position {

        /**
         * 左上
         */
        LEFT_TOP("left", "top"),

        /**
         * 右上
         */
        RIGHT_TOP("right", "top"),

        /**
         * 左下
         */
        LEFT_BOTTOM("left", "bottom"),

        /**
         * 右下
         */
        RIGHT_BOTTOM("right", "bottom"),

        /**
         * 居中
         */
        CENTER("center", "center");

        /**
         * 水平位置
         */
        private String horizontal;

        /**
         * 垂直位置
         */
        private String vertical;

        Position(String horizontal, String vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public String getHorizontal() {
            return this.horizontal;
        }

        public String getVertical() {
            return this.vertical;
        }
    }

}
