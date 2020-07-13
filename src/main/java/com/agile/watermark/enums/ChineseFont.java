package com.agile.watermark.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * 中文字体
 *
 * @author lihaitao
 * @since 2020-07-12
 */
public enum ChineseFont {
    SONG_TI("宋体", "simsun.ttc", true),
    FANG_SONG("仿宋", "simfang.ttf", false),
    HEI_TI("黑体", "simhei.ttf", false),
    KAI_TI("楷体", "simkai.ttf", false),
    MS_YA_HEI("微软雅黑", "msyh.ttc", true),
    ALIBABA_PU_HUI_TI("阿里巴巴普惠体", "AlibabaPuHuiTiMedium.ttf", false);

    ChineseFont(String fontName, String fileName, boolean isTtc) {
        this.fontName = fontName;
        this.fileName = fileName;
        this.isTtc = isTtc;
    }

    /**
     * 字体名称
     */
    private String fontName;

    /**
     * 字体文件名
     */
    private String fileName;

    /**
     * 是否 .ttc 格式
     */
    private boolean isTtc;

    /**
     * 字体名称
     */
    public String getFontName() {
        return this.fontName;
    }

    /**
     * 字体文件名
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * 是否 .ttc 格式
     */
    public boolean isTtc() {
        return this.isTtc;
    }

    /**
     * 根据字体名称获取字体
     *
     * @author lihaitao
     * @since 2020-07-12
     */
    public ChineseFont getChineseFontByFontName(String fontName) {
        return Arrays.stream(ChineseFont.values()).filter(font -> Objects.equals(fontName, font.getFontName()))
                .findFirst().orElse(null);
    }
}