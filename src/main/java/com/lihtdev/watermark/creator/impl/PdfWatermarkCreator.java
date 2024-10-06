package com.lihtdev.watermark.creator.impl;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.lihtdev.watermark.creator.WatermarkCreator;
import com.lihtdev.watermark.model.*;
import com.lihtdev.watermark.util.FontUtils;
import com.lihtdev.watermark.util.TextUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 给 PDF 文件添加水印
 *
 * @author lihaitao
 * @since 2020/7/5
 */
public class PdfWatermarkCreator implements WatermarkCreator {

    /**
     * 固定位置水印边距
     */
    private static final int POSITION_WATERMARK_PADDING = 20;

    private InputStream inputStream;

    private OutputStream outputStream;

    private InputStream imageStream;

    private Document doc;

    /**
     * 给 PDF 文件添加水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 添加水印后的文件输出流
     * @param watermark    水印
     * @author lihaitao
     * @since 2020/07/16
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
     * 设置文本水印
     *
     * @param textWatermark 文本水印
     * @author lihaitao
     * @since 2020/07/16
     */
    private void setTextWatermark(TextWatermark textWatermark) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputStream), new PdfWriter(outputStream));
        this.doc = new Document(pdfDoc);

        int pages = pdfDoc.getNumberOfPages();
        PdfFont font = FontUtils.getPdfFont(textWatermark.getFontFamily());
        DeviceRgb deviceRgb = new DeviceRgb(textWatermark.getAwtColor());
        Paragraph paragraph = new Paragraph(textWatermark.getText()).setFont(font)
                .setFontColor(deviceRgb, textWatermark.getStyle().getOpacity())
                .setFontSize(textWatermark.getFontSize());

        int[] watermarkWidthAndHeight = TextUtils.getTextWidthAndHeight(textWatermark.getFontSize(), textWatermark.getText());
        float watermarkWidth = watermarkWidthAndHeight[0];
        float watermarkHeight = watermarkWidthAndHeight[1];

        // transparency
        PdfExtGState extGState = new PdfExtGState();
        extGState.setFillOpacity(textWatermark.getStyle().getOpacity());
        double rotation = Math.toRadians(-textWatermark.getStyle().getFormat().getRotation());
        // loop over every page
        // Implement transformation matrix usage in order to scale image
        for (int pageNumber = 1; pageNumber <= pages; pageNumber++) {
            PdfPage pdfPage = pdfDoc.getPage(pageNumber);
            Rectangle pageSize = pdfPage.getPageSize();
            float pageWidth = pageSize.getLeft() + pageSize.getRight();
            float pageHeight = pageSize.getTop() + pageSize.getBottom();
            PdfCanvas over = new PdfCanvas(pdfPage);
            over.saveState();
            over.setExtGState(extGState);
            WatermarkStyle watermarkStyle = textWatermark.getStyle();
            if (watermarkStyle instanceof PositionWatermarkStyle) {
                createPositionTextWatermark(paragraph, (PositionWatermarkStyle) watermarkStyle, pageNumber, pageWidth, pageHeight);
            } else if (watermarkStyle instanceof RepeatWatermarkStyle) {
                // 设置旋转度，只有重复水印支持旋转，其他类型水印旋转后样式不好
                over.concatMatrix(AffineTransform.getRotateInstance(rotation));
                createRepeatTextWatermark(paragraph, (RepeatWatermarkStyle) watermarkStyle, pageNumber, watermarkWidth, watermarkHeight);
            }
            over.restoreState();
        }
    }

    /**
     * 设置图片水印
     *
     * @param imageWatermark 图片水印
     * @author lihaitao
     * @since 2020/07/16
     */
    private void setImageWatermark(ImageWatermark imageWatermark) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputStream), new PdfWriter(outputStream));
        this.doc = new Document(pdfDoc);
        this.imageStream = imageWatermark.getImageStream();

        int pages = pdfDoc.getNumberOfPages();
        // image watermark
        Image watermarkImage = ImageIO.read(imageStream);
        ImageData watermarkImageData = ImageDataFactory.create(watermarkImage, null);
        //  Implement transformation matrix usage in order to scale image
        float watermarkWidth = watermarkImageData.getWidth();
        float watermarkHeight = watermarkImageData.getHeight();
        // transparency
        PdfExtGState extGState = new PdfExtGState();
        extGState.setFillOpacity(imageWatermark.getStyle().getOpacity());
        double rotation = Math.toRadians(-imageWatermark.getStyle().getFormat().getRotation());
        // loop over every page
        // Implement transformation matrix usage in order to scale image
        for (int i = 1; i <= pages; i++) {
            PdfPage pdfPage = pdfDoc.getPage(i);
            Rectangle pageSize = pdfPage.getPageSize();
            float pageWidth = pageSize.getLeft() + pageSize.getRight();
            float pageHeight = pageSize.getTop() + pageSize.getBottom();
            PdfCanvas over = new PdfCanvas(pdfPage);
            over.saveState();
            over.setExtGState(extGState);

            WatermarkStyle watermarkStyle = imageWatermark.getStyle();
            if (watermarkStyle instanceof PositionWatermarkStyle) {
                createPositionImageWatermark(over, watermarkImageData, (PositionWatermarkStyle) watermarkStyle, pageWidth, pageHeight, watermarkWidth, watermarkHeight);
            } else if (watermarkStyle instanceof RepeatWatermarkStyle) {
                // 设置旋转度，只有重复水印支持旋转，其他类型水印旋转后样式不好
                over.concatMatrix(AffineTransform.getRotateInstance(rotation));
                createRepeatImageWatermark(over, watermarkImageData, (RepeatWatermarkStyle) watermarkStyle, watermarkWidth, watermarkHeight);
            }
            over.restoreState();
        }
    }

    /**
     * 添加固定位置的文本水印（固定位置的水印只支持水平板式，不支持斜式和垂直）
     *
     * @param watermarkParagraph     水印段落
     * @param positionWatermarkStyle 固定位置水印样式
     * @param pageNumber             页码
     * @param pageWidth              页面宽度
     * @param pageHeight             页面高度
     * @author lihaitao
     * @date 2020/7/16
     */
    private void createPositionTextWatermark(Paragraph watermarkParagraph, PositionWatermarkStyle positionWatermarkStyle,
                                             int pageNumber, float pageWidth, float pageHeight) {
        for (PositionWatermarkStyle.Position position : positionWatermarkStyle.getPositions()) {
            switch (position) {
                case LEFT_TOP: {
                    float x = POSITION_WATERMARK_PADDING;
                    float y = pageHeight - POSITION_WATERMARK_PADDING;
                    doc.showTextAligned(watermarkParagraph, x, y, pageNumber, TextAlignment.LEFT, VerticalAlignment.TOP, 0);
                    break;
                }
                case LEFT_BOTTOM: {
                    float x = POSITION_WATERMARK_PADDING;
                    float y = POSITION_WATERMARK_PADDING;
                    doc.showTextAligned(watermarkParagraph, x, y, pageNumber, TextAlignment.LEFT, VerticalAlignment.BOTTOM, 0);
                    break;
                }
                case RIGHT_TOP: {
                    float x = pageWidth - POSITION_WATERMARK_PADDING;
                    float y = pageHeight - POSITION_WATERMARK_PADDING;
                    doc.showTextAligned(watermarkParagraph, x, y, pageNumber, TextAlignment.RIGHT, VerticalAlignment.TOP, 0);
                    break;
                }
                case RIGHT_BOTTOM: {
                    float x = pageWidth - POSITION_WATERMARK_PADDING;
                    float y = POSITION_WATERMARK_PADDING;
                    doc.showTextAligned(watermarkParagraph, x, y, pageNumber, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, 0);
                    break;
                }
                default: {
                    // CENTER
                    float x = pageWidth / 2;
                    float y = pageHeight / 2;
                    doc.showTextAligned(watermarkParagraph, x, y, pageNumber, TextAlignment.CENTER, VerticalAlignment.MIDDLE, 0);
                }
            }
        }
    }

    /**
     * 添加重复的文本水印
     *
     * @param watermarkParagraph   水印段落
     * @param repeatWatermarkStyle 重复水印样式
     * @param pageNumber           页码
     * @param watermarkWidth       水印宽度
     * @param watermarkHeight      水印高度
     * @author lihaitao
     * @date 2020/7/16
     */
    private void createRepeatTextWatermark(Paragraph watermarkParagraph, RepeatWatermarkStyle repeatWatermarkStyle,
                                           int pageNumber, float watermarkWidth, float watermarkHeight) {
        for (int row = -3; row < repeatWatermarkStyle.getRows(); row++) {
            for (int col = -3; col < repeatWatermarkStyle.getCols(); col++) {
                float x = (watermarkWidth + repeatWatermarkStyle.getXSpace()) * col + repeatWatermarkStyle.getXStart();
                float y = (watermarkHeight + repeatWatermarkStyle.getYSpace()) * row + repeatWatermarkStyle.getYStart();
                doc.showTextAligned(watermarkParagraph, x, y, pageNumber, TextAlignment.LEFT, VerticalAlignment.BOTTOM, 0);
            }
        }
    }

    /**
     * 添加固定位置的图片水印（PDF文档的原点在左下角，图片水印的原点在左下角）
     *
     * @param over                   水印画布
     * @param watermarkImageData     水印图片
     * @param positionWatermarkStyle 固定位置水印样式
     * @param pageWidth              页面宽度
     * @param pageHeight             页面高度
     * @param watermarkWidth         水印宽度
     * @param watermarkHeight        水印高度
     * @author lihaitao
     * @since 2020/7/16
     */
    private void createPositionImageWatermark(PdfCanvas over, ImageData watermarkImageData, PositionWatermarkStyle positionWatermarkStyle,
                                              float pageWidth, float pageHeight, float watermarkWidth, float watermarkHeight) {
        for (PositionWatermarkStyle.Position position : positionWatermarkStyle.getPositions()) {
            float x, y;
            switch (position) {
                case LEFT_TOP:
                    x = POSITION_WATERMARK_PADDING;
                    y = pageHeight - watermarkHeight - POSITION_WATERMARK_PADDING;
                    break;
                case LEFT_BOTTOM:
                    x = POSITION_WATERMARK_PADDING;
                    y = POSITION_WATERMARK_PADDING;
                    break;
                case RIGHT_TOP:
                    x = pageWidth - watermarkWidth - POSITION_WATERMARK_PADDING;
                    y = pageHeight - watermarkHeight - POSITION_WATERMARK_PADDING;
                    break;
                case RIGHT_BOTTOM:
                    x = pageWidth - watermarkWidth - POSITION_WATERMARK_PADDING;
                    y = POSITION_WATERMARK_PADDING;
                    break;
                default:
                    // CENTER
                    x = (pageWidth - watermarkWidth) / 2;
                    y = (pageHeight - watermarkHeight) / 2;
            }
            over.addImage(watermarkImageData, watermarkWidth, 0, 0, watermarkHeight, x, y, true);
        }
    }

    /**
     * 添加重复的图片水印（PDF 文档的原点在左下角，图片水印的原点在左下角）
     *
     * @param over                 水印画布
     * @param watermarkImageData   水印图片
     * @param repeatWatermarkStyle 重复水印样式
     * @param watermarkWidth       水印宽度
     * @param watermarkHeight      水印高度
     * @author lihaitao
     * @since 2020/7/16
     */
    private void createRepeatImageWatermark(PdfCanvas over, ImageData watermarkImageData, RepeatWatermarkStyle repeatWatermarkStyle,
                                            float watermarkWidth, float watermarkHeight) {
        for (int row = -3; row < repeatWatermarkStyle.getRows(); row++) {
            for (int col = -3; col < repeatWatermarkStyle.getCols(); col++) {
                float x = (watermarkWidth + repeatWatermarkStyle.getXSpace()) * col + repeatWatermarkStyle.getXStart();
                float y = (watermarkHeight + repeatWatermarkStyle.getYSpace()) * row + repeatWatermarkStyle.getYStart();
                over.addImage(watermarkImageData, watermarkWidth, 0, 0, watermarkHeight, x, y, true);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (doc != null) {
            doc.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        if (imageStream != null) {
            imageStream.close();
        }
        // outputStream 不需要关闭，因添加水印后要返回给调用者
    }

}
