package com.lihtdev.watermark.util;

import com.lihtdev.watermark.model.TextWatermark;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lihaitao
 * @since 2020/7/12
 */
public class ColorUtils {

    /**
     * 水印颜色对应的map（初始容量定为19，实际存储为13个，initialCapacity = (13 / 0.75) + 1）
     */
    private static final Map<String, Color> colorMap = new HashMap<String, Color>(19) {
        public Map<String, Color> init() {
            put(TextWatermark.Color.WHITE.getName(), Color.WHITE);
            put(TextWatermark.Color.LIGHT_GRAY.getName(), Color.LIGHT_GRAY);
            put(TextWatermark.Color.GRAY.getName(), Color.GRAY);
            put(TextWatermark.Color.DARK_GRAY.getName(), Color.DARK_GRAY);
            put(TextWatermark.Color.BLACK.getName(), Color.BLACK);
            put(TextWatermark.Color.RED.getName(), Color.RED);
            put(TextWatermark.Color.PINK.getName(), Color.PINK);
            put(TextWatermark.Color.ORANGE.getName(), Color.ORANGE);
            put(TextWatermark.Color.YELLOW.getName(), Color.YELLOW);
            put(TextWatermark.Color.GREEN.getName(), Color.GREEN);
            put(TextWatermark.Color.MAGENTA.getName(), Color.MAGENTA);
            put(TextWatermark.Color.CYAN.getName(), Color.CYAN);
            put(TextWatermark.Color.BLUE.getName(), Color.BLUE);
            return this;
        }
    }.init();

    /**
     * 获取颜色对象
     *
     * @param color 颜色名称或颜色代码
     * @author lihaitao
     * @since 2020/7/12
     */
    public static Color getColor(String color) {
        if (color.startsWith("#")) {
            return Color.decode(color);
        } else {
            return colorMap.get(color.toLowerCase());
        }
    }

    /**
     * 将普通颜色换为透明色
     *
     * @param color 颜色
     * @param alpha 透明度
     * @author lihaitao
     * @since 2020/7/17
     */
    public static Color toArgbColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

}
