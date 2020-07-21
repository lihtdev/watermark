package com.agile.watermark.util;

import cn.hutool.core.img.ImgUtil;
import com.agile.watermark.model.ImageWatermark;
import com.agile.watermark.model.TextWatermark;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 图片工具类
 *
 * @author lihaitao
 * @since 2020/07/14
 */
public class ImageUtils {

    /**
     * 创建文本水印图片
     *
     * @param textWatermark 文本水印
     * @author lihaitao
     * @since 2020/7/21
     */
    public static BufferedImage createImage(TextWatermark textWatermark) {
        java.awt.Font font = FontUtils.getFont(textWatermark.getFontFamily(), java.awt.Font.BOLD, textWatermark.getFontSize());
        int alpha = Math.round(255 * textWatermark.getStyle().getOpacity());
        Color color = ColorUtils.toArgbColor(textWatermark.getAwtColor(), alpha);
        return ImgUtil.createImage(textWatermark.getText(), font, null, color, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * 创建图片水印图片
     *
     * @param imageWatermark 图片水印
     * @author lihaitao
     * @since 2020/7/21
     */
    public static BufferedImage createImage(ImageWatermark imageWatermark) {
        BufferedImage bufferedImage = ImgUtil.read(imageWatermark.getImageStream());
        Graphics2D graphics = bufferedImage.createGraphics();
        // 设置对线段的锯齿状边缘处理
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, imageWatermark.getStyle().getOpacity()));
        graphics.dispose();
        return bufferedImage;
    }

}
