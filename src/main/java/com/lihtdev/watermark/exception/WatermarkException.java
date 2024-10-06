package com.lihtdev.watermark.exception;

/**
 * 水印异常类
 *
 * @author lihaitao
 * @since 2020/7/4
 */
public class WatermarkException extends RuntimeException {

    public WatermarkException(String message) {
        super(message);
    }

    public WatermarkException(String message, Throwable cause) {
        super(message, cause);
    }

}
