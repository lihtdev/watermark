package com.agile.watermark.model;

import lombok.Data;

/**
 * 文字水印
 *
 * @author lihaitao
 * @since 2020/7/3
 */
@Data
public class TextWatermark extends Watermark {

    /**
     * 水印内容
     */
    private String text = "文字水印";

    /**
     * 颜色（色彩代码，如：black, red, #d0d0d0）
     */
    private String color = "#d0d0d0";

    /**
     * 字体，如：微软雅黑，宋体，楷体
     */
    private String fontFamily = "楷体";

    /**
     * 字体大小（单位：pt）
     */
    private int fontSize = 20;

    /**
     * 设置颜色代码（如：black, red, #d0d0d0）
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * 设置颜色名称
     */
    public void setColor(Color color) {
        this.color = color.toString();
    }

    /**
     * 颜色
     *
     * @author lihaitao
     * @since 2020/7/7
     */
    public enum Color {
        WHITE,
        LIGHT_GRAY,
        GRAY,
        DARK_GRAY,
        BLACK,
        RED,
        PINK,
        ORANGE,
        YELLOW,
        GREEN,
        MAGENTA,
        CYAN,
        BLUE;

        /*public String getName() {

        }*/
    }

}
