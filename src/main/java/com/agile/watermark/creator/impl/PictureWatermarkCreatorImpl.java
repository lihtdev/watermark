package com.agile.watermark.creator.impl;

import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.model.ImageWatermark;
import com.agile.watermark.model.TextWatermark;
import com.agile.watermark.model.Watermark;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
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

        BufferedImage srcBufferedImage = new BufferedImage(srcImage.getWidth(null), srcImage.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = srcBufferedImage.createGraphics();
        // 设置对线段的锯齿状边缘处理
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(srcImage.getScaledInstance(srcImage.getWidth(null), srcImage.getHeight(null),
                Image.SCALE_SMOOTH), 0,0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, textWatermark.getStyle().getOpacity()));
        g.drawString(textWatermark.getText(), 150, 300);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.dispose();

        ImageIO.write(srcBufferedImage, "PNG", outputStream);
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
                Image.SCALE_SMOOTH), 0,0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, imageWatermark.getStyle().getOpacity()));
        g.drawImage(watermarkImage, 150, 300, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.dispose();

        ImageIO.write(srcBufferedImage, imageWatermark.getFormat().toString(), outputStream);
    }

    private Color decodeColor(String color) {
        if (color.startsWith("#")) {
            return Color.decode(color);
        } else {
            return getColor(color);
        }
    }

    private static final Color getColor(String colorName) {
        Map<String, Color> colorMap = new HashMap<String, Color>(19) {
            public Map<String, Color> init() {
                put(TextWatermark.Color.WHITE.toString(), Color.WHITE);
                put(TextWatermark.Color.LIGHT_GRAY.toString(), Color.LIGHT_GRAY);
                put(TextWatermark.Color.GRAY.toString(), Color.GRAY);
                put(TextWatermark.Color.DARK_GRAY.toString(), Color.DARK_GRAY);
                put(TextWatermark.Color.BLACK.toString(), Color.BLACK);
                put(TextWatermark.Color.RED.toString(), Color.RED);
                put(TextWatermark.Color.PINK.toString(), Color.PINK);
                put(TextWatermark.Color.ORANGE.toString(), Color.ORANGE);
                put(TextWatermark.Color.YELLOW.toString(), Color.YELLOW);
                put(TextWatermark.Color.GREEN.toString(), Color.GREEN);
                put(TextWatermark.Color.MAGENTA.toString(), Color.MAGENTA);
                put(TextWatermark.Color.CYAN.toString(), Color.CYAN);
                put(TextWatermark.Color.BLUE.toString(), Color.BLUE);
                return this;
            }
        }.init();
        return colorMap.get(colorName);
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
