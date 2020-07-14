package com.agile.watermark.creator.impl;

import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.model.*;
import com.agile.watermark.util.FontUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 给图片添加水印
 *
 * @author lihaitao
 * @since 2020/7/5
 */
public class PictureWatermarkCreatorImpl implements WatermarkCreator {

    private InputStream inputStream;

    private OutputStream outputStream;

    private InputStream imageStream;

    /**
     * 给图片添加水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 添加水印后的文件输出流
     * @param watermark    水印
     */
    @Override
    public void create(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        if (watermark instanceof ImageWatermark) {
            setImageWatermark((ImageWatermark) watermark);
        } else if (watermark instanceof TextWatermark) {
            setTextWatermark((TextWatermark) watermark);
        }
    }

    /**
     * 设置文字水印（固定位置水印不支持斜式）
     *
     * @param textWatermark 文字水印
     * @author lihaitao
     * @since 2020/07/13
     */
    private void setTextWatermark(TextWatermark textWatermark) throws IOException {
        Image srcImage = ImageIO.read(inputStream);

        int srcImageWidth = srcImage.getWidth(null);
        int srcImageHeight = srcImage.getHeight(null);

        BufferedImage srcBufferedImage = new BufferedImage(srcImageWidth, srcImageHeight,
                BufferedImage.TYPE_INT_RGB);

        int[] watermarkWidthAndHeight = getTextWatermarkWidthAndHeight(textWatermark.getFontSize(), textWatermark.getText());
        int watermarkWidth = watermarkWidthAndHeight[0];
        int watermarkHeight = watermarkWidthAndHeight[1];

        Graphics2D g = srcBufferedImage.createGraphics();
        // 设置对线段的锯齿状边缘处理
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(srcImage.getScaledInstance(srcImageWidth, srcImageHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, textWatermark.getStyle().getOpacity()));
        g.setColor(textWatermark.getAwtColor());
        g.setFont(FontUtils.getFont(textWatermark.getFontFamily(), Font.BOLD, textWatermark.getFontSize()));

        WatermarkStyle watermarkStyle = textWatermark.getStyle();
        double radians = Math.toRadians(watermarkStyle.getFormat().getRotation());

        if (watermarkStyle instanceof PositionWatermarkStyle) {
            PositionWatermarkStyle positionWatermarkStyle = (PositionWatermarkStyle) watermarkStyle;
            for (PositionWatermarkStyle.Position position : positionWatermarkStyle.getPositions()) {
                int[] coordinates = getPositionTextCoordinate(position, srcImageWidth, srcImageHeight, watermarkWidth, watermarkHeight);
                g.drawString(textWatermark.getText(), coordinates[0], coordinates[1]);
            }
        } else if (watermarkStyle instanceof RepeatWatermarkStyle) {
            g.rotate(radians, srcImageWidth / 2.0, srcImageHeight / 2.0);
            RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) watermarkStyle;
            for (int rowIndex = -5; rowIndex < repeatWatermarkStyle.getRows(); rowIndex++) {
                for (int colIndex = -5; colIndex < repeatWatermarkStyle.getCols(); colIndex++) {
                    int[] coordinates = getRepeatTextCoordinate(repeatWatermarkStyle, watermarkWidth, watermarkHeight, rowIndex, colIndex);
                    g.drawString(textWatermark.getText(), coordinates[0], coordinates[1]);
                }
            }
        }
        g.dispose();
        ImageIO.write(srcBufferedImage, "png", outputStream);
    }

    /**
     * 设置图片水印（固定位置水印不支持斜式）
     *
     * @param imageWatermark 图片水印
     * @author lihaitao
     * @since 2020/07/13
     */
    private void setImageWatermark(ImageWatermark imageWatermark) throws IOException {
        this.imageStream = imageWatermark.getImageStream();

        Image srcImage = ImageIO.read(inputStream);
        Image watermarkImage = ImageIO.read(imageStream);

        int srcImageWidth = srcImage.getWidth(null);
        int srcImageHeight = srcImage.getHeight(null);
        int watermarkWidth = imageWatermark.getWidth();
        int watermarkHeight = imageWatermark.getHeight();

        BufferedImage srcBufferedImage = new BufferedImage(srcImageWidth, srcImageHeight,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = srcBufferedImage.createGraphics();
        // 设置对线段的锯齿状边缘处理
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(srcImage.getScaledInstance(srcImageWidth, srcImageHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, imageWatermark.getStyle().getOpacity()));

        WatermarkStyle watermarkStyle = imageWatermark.getStyle();
        double radians = Math.toRadians(watermarkStyle.getFormat().getRotation());

        Image watermarkScaledInstance = watermarkImage.getScaledInstance(watermarkWidth, watermarkHeight, Image.SCALE_SMOOTH);

        if (watermarkStyle instanceof PositionWatermarkStyle) {
            PositionWatermarkStyle positionWatermarkStyle = (PositionWatermarkStyle) watermarkStyle;
            for (PositionWatermarkStyle.Position position : positionWatermarkStyle.getPositions()) {
                int[] coordinates = getPositionImageCoordinate(position, srcImageWidth, srcImageHeight, watermarkWidth, watermarkHeight);
                g.drawImage(watermarkScaledInstance, coordinates[0], coordinates[1], null);
            }
        } else if (watermarkStyle instanceof RepeatWatermarkStyle) {
            g.rotate(radians, srcImageWidth / 2.0, srcImageHeight / 2.0);
            RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) watermarkStyle;
            for (int rowIndex = -5; rowIndex < repeatWatermarkStyle.getRows(); rowIndex++) {
                for (int colIndex = -5; colIndex < repeatWatermarkStyle.getCols(); colIndex++) {
                    int[] coordinates = getRepeatImageCoordinate(repeatWatermarkStyle, watermarkWidth, watermarkHeight, rowIndex, colIndex);
                    g.drawImage(watermarkScaledInstance, coordinates[0], coordinates[1], null);
                }
            }
        }
        g.dispose();
        ImageIO.write(srcBufferedImage, "png", outputStream);
    }

    /**
     * 获取固定位置文字水印 x, y 坐标（原点在文字水印左边和下边的交点）
     *
     * @param position        位置
     * @param srcImageWidth   原图片宽度
     * @param srcImageHeight  原图片高度
     * @param watermarkWidth  水印宽度
     * @param watermarkHeight 水印高度
     * @author lihaitao
     * @since 2020/07/13
     */
    private int[] getPositionTextCoordinate(PositionWatermarkStyle.Position position,
                                            int srcImageWidth, int srcImageHeight, int watermarkWidth, int watermarkHeight) {
        int padding = 100;
        int x, y;
        switch (position) {
            case LEFT_TOP:
                x = padding;
                y = watermarkHeight + padding;
                break;
            case RIGHT_TOP:
                x = srcImageWidth - watermarkWidth - padding;
                y = watermarkHeight + padding;
                break;
            case LEFT_BOTTOM:
                x = padding;
                y = srcImageHeight - padding;
                break;
            case RIGHT_BOTTOM:
                x = srcImageWidth - watermarkWidth - padding;
                y = srcImageHeight - padding;
                break;
            default:
                // CENTER
                x = (srcImageWidth - watermarkWidth) / 2;
                y = (srcImageHeight + watermarkHeight) / 2;
        }
        return new int[]{x, y};
    }

    /**
     * 获取固定位置图片水印 x, y 坐标（原点在图片水印左边和上边的交点）
     *
     * @param position        位置
     * @param srcImageWidth   原图片宽度
     * @param srcImageHeight  原图片高度
     * @param watermarkWidth  水印宽度
     * @param watermarkHeight 水印高度
     * @author lihaitao
     * @since 2020/07/13
     */
    private int[] getPositionImageCoordinate(PositionWatermarkStyle.Position position,
                                             int srcImageWidth, int srcImageHeight, int watermarkWidth, int watermarkHeight) {
        int padding = 100;
        int x, y;
        switch (position) {
            case LEFT_TOP:
                x = padding;
                y = padding;
                break;
            case RIGHT_TOP:
                x = srcImageWidth - watermarkWidth - padding;
                y = padding;
                break;
            case LEFT_BOTTOM:
                x = padding;
                y = srcImageHeight - watermarkHeight - padding;
                break;
            case RIGHT_BOTTOM:
                x = srcImageWidth - watermarkWidth - padding;
                y = srcImageHeight - watermarkHeight - padding;
                break;
            default:
                // CENTER
                x = (srcImageWidth - watermarkWidth) / 2;
                y = (srcImageHeight - watermarkHeight) / 2;
        }
        return new int[]{x, y};
    }

    /**
     * 获取重复文字水印 x, y 坐标（原点在文字水印左边和下边的交点）
     *
     * @param repeatWatermarkStyle 重复水印
     * @param watermarkWidth       水印宽度
     * @param watermarkHeight      水印高度
     * @param rowIndex             行号
     * @param colIndex             列号
     * @author lihaitao
     * @since 2020/07/13
     */
    private int[] getRepeatTextCoordinate(RepeatWatermarkStyle repeatWatermarkStyle,
                                          int watermarkWidth, int watermarkHeight, int rowIndex, int colIndex) {
        int xSpace = repeatWatermarkStyle.getXSpace();
        int ySpace = repeatWatermarkStyle.getYSpace();
        int x = (watermarkWidth + xSpace) * colIndex;
        int y = (watermarkHeight + ySpace) * rowIndex + watermarkHeight;
        return new int[]{x, y};
    }

    /**
     * 获取重复图片水印 x, y 坐标（原点在图片水印左边和上边的交点）
     *
     * @param repeatWatermarkStyle 重复水印
     * @param watermarkWidth       水印宽度
     * @param watermarkHeight      水印高度
     * @param rowIndex             行号
     * @param colIndex             列号
     * @author lihaitao
     * @since 2020/07/13
     */
    private int[] getRepeatImageCoordinate(RepeatWatermarkStyle repeatWatermarkStyle,
                                           int watermarkWidth, int watermarkHeight, int rowIndex, int colIndex) {
        int xSpace = repeatWatermarkStyle.getXSpace();
        int ySpace = repeatWatermarkStyle.getYSpace();
        int x = (watermarkWidth + xSpace) * colIndex;
        int y = (watermarkHeight + ySpace) * rowIndex;
        return new int[]{x, y};
    }

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
    private int[] getTextWatermarkWidthAndHeight(int fontSize, String text) {
        int length = text.length();
        for (int i = 0; i < text.length(); i++) {
            String s = String.valueOf(text.charAt(i));
            if (s.getBytes().length > 1) {
                length++;
            }
        }
        length = length % 2 == 0 ? length / 2 : length / 2 + 1;
        int watermarkWidth = fontSize * length;
        int watermarkHeight = fontSize;
        return new int[]{watermarkWidth, watermarkHeight};
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (imageStream != null) {
            imageStream.close();
        }
        // outputStream 不需要关闭，因添加水印后要返回给调用者
    }
}
