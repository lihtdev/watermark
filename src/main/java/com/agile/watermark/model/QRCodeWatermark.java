package com.agile.watermark.model;

import cn.hutool.extra.qrcode.QrCodeUtil;
import com.agile.watermark.exception.WatermarkException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 二维码水印
 *
 * @author lihaitao
 * @since 2020/7/16
 */
public class QRCodeWatermark extends ImageWatermark {

    /**
     * 默认宽度
     */
    private static final int DEFAULT_WIDTH = 300;

    /**
     * 默认高度
     */
    private static final int DEFAULT_HEIGHT = 300;

    /**
     * 创建二维码水印实例
     *
     * @param content 二维码内容
     */
    public QRCodeWatermark(String content) {
        super(null);
        init(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 创建二维码水印实例
     *
     * @param content 二维码内容
     * @param width   二维码宽度
     * @param height  二维码高度
     */
    public QRCodeWatermark(String content, int width, int height) {
        super(null);
        init(content, width, height);
    }

    /**
     * 初始化二维码水印
     *
     * @param content 二维码内容
     * @param width   二维码宽度
     * @param height  二维码高度
     * @author lihaitao
     * @since 2020/7/17
     */
    private void init(String content, int width, int height) {
        try (ByteArrayOutputStream qrCodeOutputStream = new ByteArrayOutputStream()) {
            QrCodeUtil.generate(content, width, height, Type.PNG.toString(), qrCodeOutputStream);
            ByteArrayInputStream qrCodeInputStream = new ByteArrayInputStream(qrCodeOutputStream.toByteArray());
            super.setImageStream(qrCodeInputStream);
            super.setWidth(width);
            super.setHeight(height);
            super.setType(Type.PNG);
        } catch (IOException e) {
            throw new WatermarkException("创建二维码水印时发生错误", e);
        }
    }

    /**
     * 不能调用无参构造方法
     */
    private QRCodeWatermark() {
        super(null);
    }

    @Override
    @Deprecated
    public void setWidth(int width) {
        throw new WatermarkException("此方法不能调用，请调用构造方法");
    }

    @Override
    @Deprecated
    public void setHeight(int height) {
        throw new WatermarkException("此方法不能调用，请调用构造方法");
    }

    @Override
    @Deprecated
    public void setType(Type type) {
        throw new WatermarkException("此方法不能调用");
    }

    @Override
    @Deprecated
    public void setImageStream(InputStream imageStream) {
        throw new WatermarkException("此方法不能调用");
    }

}
