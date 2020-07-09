package com.agile.watermark.creator.impl;

import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

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

    private void setTextWatermark(TextWatermark textWatermark) throws IOException {
        Image srcImage = ImageIO.read(inputStream);

        int srcImageWidth = srcImage.getWidth(null);
        int srcImageHeight = srcImage.getHeight(null);

        BufferedImage srcBufferedImage = new BufferedImage(srcImageWidth, srcImageHeight,
                BufferedImage.TYPE_INT_RGB);

        int fontSize = textWatermark.getFontSize();
        float fontMargin = 0.1f * fontSize;
        int watermarkWidth = Math.round((fontSize + fontMargin * 2) * textWatermark.getText().length());
        int watermarkHeight = Math.round(fontSize + fontMargin * 2);

        Graphics2D g = srcBufferedImage.createGraphics();
        // 设置对线段的锯齿状边缘处理
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(srcImage.getScaledInstance(srcImageWidth, srcImageHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, textWatermark.getStyle().getOpacity()));
        g.setColor(getColor(textWatermark.getColor()));
        g.setFont(new Font(textWatermark.getFontFamily(), Font.BOLD, textWatermark.getFontSize()));

        WatermarkStyle watermarkStyle = textWatermark.getStyle();
        double radians = Math.toRadians(watermarkStyle.getFormat().getRotation());

        if (watermarkStyle instanceof PositionWatermarkStyle) {
            PositionWatermarkStyle positionWatermarkStyle = (PositionWatermarkStyle) watermarkStyle;
            for (PositionWatermarkStyle.Position position : positionWatermarkStyle.getPositions()) {
                int[] coordinates = getPositionCoordinate(position, srcImageWidth, srcImageHeight, watermarkWidth, watermarkHeight);
                g.drawString(textWatermark.getText(), coordinates[0], coordinates[1]);
            }
        } else if (watermarkStyle instanceof RepeatWatermarkStyle) {
            g.rotate(radians, srcImageWidth / 2.0, srcImageHeight / 2.0);
            RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) watermarkStyle;
            for (int rowIndex = -5; rowIndex < repeatWatermarkStyle.getRows(); rowIndex++) {
                for (int colIndex = -5; colIndex < repeatWatermarkStyle.getCols(); colIndex++) {
                    int[] coordinates = getRepeatCoordinate(repeatWatermarkStyle, watermarkWidth, watermarkHeight, rowIndex, colIndex);
                    g.drawString(textWatermark.getText(), coordinates[0], coordinates[1]);
                }
            }
        }
        g.dispose();
        ImageIO.write(srcBufferedImage, "png", outputStream);
    }

    // 原点在左边和下边的交点
    private int[] getPositionCoordinate(PositionWatermarkStyle.Position position,
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
                y = (srcImageHeight - watermarkHeight) / 2;
        }
        return new int[]{x, y};
    }

    private int[] getRepeatCoordinate(RepeatWatermarkStyle repeatWatermarkStyle,
            int watermarkWidth, int watermarkHeight, int rowIndex, int colIndex) {
        int xSpace = repeatWatermarkStyle.getXSpace();
        int ySpace = repeatWatermarkStyle.getYSpace();
        int x = (watermarkWidth + xSpace) * colIndex;
        int y = (watermarkHeight + ySpace) * (rowIndex + 1);
        return new int[]{x, y};
    }

    private void setImageWatermark(ImageWatermark imageWatermark) throws IOException {
        this.imageStream = imageWatermark.getImageStream();

        Image srcImage = ImageIO.read(inputStream);
        Image watermarkImage = ImageIO.read(imageStream);

        BufferedImage srcBufferedImage = new BufferedImage(srcImage.getWidth(null), srcImage.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = srcBufferedImage.createGraphics();
        // 设置对线段的锯齿状边缘处理
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(srcImage.getScaledInstance(srcImage.getWidth(null), srcImage.getHeight(null),
                Image.SCALE_SMOOTH), 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, imageWatermark.getStyle().getOpacity()));

        g.drawImage(watermarkImage, 150, 300, null);
        g.dispose();

        ImageIO.write(srcBufferedImage, imageWatermark.getFormat().toString(), outputStream);
    }

    private Color getColor(String color) {
        if (color.startsWith("#")) {
            return Color.decode(color);
        } else {
            return colorMap.get(color);
        }
    }

    private int ptToPx(int pt) {
        // window 96 dpi, Mac 72 dpi
        int dpi = 96;
        return pt * 72 / dpi;
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
