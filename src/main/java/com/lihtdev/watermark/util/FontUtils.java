package com.lihtdev.watermark.util;

import com.lihtdev.watermark.enums.ChineseFont;
import com.lihtdev.watermark.exception.WatermarkException;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.util.ResourceUtil;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字体工具类
 *
 * @author lihaitao
 * @since 2020/7/12
 */
public class FontUtils {

    /**
     * 字体文件路径
     */
    private static final String BASE_FONT_PATH = "font-chinese/";

    /**
     * Java 字体对象缓存
     */
    private static final Map<String, Font> fontMap = new ConcurrentHashMap<>();

    /**
     * 获取Java字体对象
     *
     * @param fontName  字体名
     * @param fontStyle 字体样式
     * @param fontSize  字体大小
     * @author lihaitao
     * @since 2020/7/12
     */
    public static Font getFont(String fontName, int fontStyle, int fontSize) {
        Font font = fontMap.get(fontName);
        if (font == null) {
            return new Font(fontName, fontStyle, fontSize);
        }
        return font.deriveFont(fontStyle, fontSize);
    }

    /**
     * 获取PDF字体对象
     *
     * @param fontName 字体名称
     * @author lihaitao
     * @since 2020/7/12
     */
    public static PdfFont getPdfFont(String fontName) {
        PdfFont pdfFont = getPdfFontFromResource(fontName);
        if (pdfFont == null) {
            try {
                return PdfFontFactory.createFont(fontName, PdfEncodings.IDENTITY_H, true);
            } catch (IOException e) {
                throw new WatermarkException("获取中文字体失败", e);
            }
        }
        return pdfFont;
    }

    /*
     * 从 resource 加载中文字体文件
     */
    private static PdfFont getPdfFontFromResource(String fontName) {
        ChineseFont chineseFont = ChineseFont.values()[0].getChineseFontByFontName(fontName);
        try (InputStream resourceStream = ResourceUtil.getResourceStream(BASE_FONT_PATH + chineseFont.getFileName())) {
            byte[] fontBytes = StreamUtil.inputStreamToArray(resourceStream);
            if (chineseFont.isTtc()) {
                return PdfFontFactory.createTtcFont(fontBytes, 0, PdfEncodings.IDENTITY_H, true, true);
            } else {
                return PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, true);
            }
        } catch (IOException e) {
            throw new WatermarkException("加载中文字体失败", e);
        }
    }

    /*
     * 从 resource 加载中文字体文件
     */
    static {
        ChineseFont[] chineseFonts = ChineseFont.values();
        Arrays.stream(chineseFonts).forEach(chineseFont -> {
            try (InputStream resourceStream = ResourceUtil.getResourceStream(BASE_FONT_PATH + chineseFont.getFileName())) {
                byte[] fontBytes = StreamUtil.inputStreamToArray(resourceStream);
                ByteArrayInputStream fontByteArrayInputStream = new ByteArrayInputStream(fontBytes);
                Font font = Font.createFont(Font.TRUETYPE_FONT, fontByteArrayInputStream);
                fontMap.put(chineseFont.getFontName(), font);
            } catch (IOException | FontFormatException e) {
                throw new WatermarkException("加载中文字体失败", e);
            }
        });
    }

}
