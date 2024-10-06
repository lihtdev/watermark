package com.lihtdev.watermark.util;

/**
 * 文本工具类
 *
 * @author lihaitao
 * @since 2020/07/14
 */
public class TextUtils {

    /**
     * 获取文字水印的宽度和高度
     * 中文字符的宽高比例为 1:1，英文字符的宽高比例为 1:
     * 一个中文字符占3个字节，一个英文字符占1个字节
     *
     * @param fontSize 字体大小
     * @param text     水印文本
     * @author lihaitao
     * @since 2020/07/14
     */
    public static int[] getTextWidthAndHeight(int fontSize, String text) {
        int length = text.length();
        for (int i = 0; i < text.length(); i++) {
            String s = String.valueOf(text.charAt(i));
            if (s.getBytes().length > 1) {
                length++;
            }
        }
        length = length % 2 == 0 ? length / 2 : length / 2 + 1;
        int width = fontSize * length;
        int height = fontSize;
        return new int[]{width, height};
    }

}
