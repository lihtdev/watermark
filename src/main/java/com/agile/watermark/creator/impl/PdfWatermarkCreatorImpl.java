package com.agile.watermark.creator.impl;

import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.model.*;
import com.agile.watermark.util.FontUtils;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
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
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;

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
public class PdfWatermarkCreatorImpl implements WatermarkCreator {

    /**
     * 固定位置水印边距
     */
    private static final int POSITION_WATERMARK_PADDING = 20;

    private InputStream inputStream;

    private OutputStream outputStream;

    private InputStream imageStream;

    private Document doc;

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
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputStream), new PdfWriter(outputStream));
        this.doc = new Document(pdfDoc);
        int pages = pdfDoc.getNumberOfPages();
        PdfFont font = FontUtils.getPdfFont(textWatermark.getFontFamily());
        DeviceRgb deviceRgb = new DeviceRgb(textWatermark.getAwtColor());
        Paragraph paragraph = new Paragraph(textWatermark.getText()).setFont(font)
                .setFontColor(deviceRgb, textWatermark.getStyle().getOpacity())
                .setFontSize(textWatermark.getFontSize());

        int[] watermarkWidthAndHeight = getTextWatermarkWidthAndHeight(textWatermark.getFontSize(), textWatermark.getText());
        float watermarkWidth = watermarkWidthAndHeight[0];
        float watermarkHeight = watermarkWidthAndHeight[1];

        // transparency
        PdfExtGState extGState = new PdfExtGState();
        extGState.setFillOpacity(textWatermark.getStyle().getOpacity());
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
                createRepeatTextWatermark(paragraph, (RepeatWatermarkStyle) watermarkStyle, pageNumber, watermarkWidth, watermarkHeight);
            }
            over.restoreState();
        }
    }

    private void setImageWatermark(ImageWatermark imageWatermark) throws IOException {
        this.imageStream = imageWatermark.getImageStream();

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputStream), new PdfWriter(outputStream));
        this.doc = new Document(pdfDoc);
        int pages = pdfDoc.getNumberOfPages();
        // image watermark
        Image watermarkImage = ImageIO.read(imageStream);
        ImageData watermarkImageData = ImageDataFactory.create(watermarkImage, null);
        watermarkImageData.setRotation(imageWatermark.getStyle().getFormat().getRotation());
        //  Implement transformation matrix usage in order to scale image
        float width = watermarkImageData.getWidth();
        float height = watermarkImageData.getHeight();
        // transparency
        PdfExtGState extGState = new PdfExtGState();
        extGState.setFillOpacity(imageWatermark.getStyle().getOpacity());
        // loop over every page
        // Implement transformation matrix usage in order to scale image
        for (int i = 1; i <= pages; i++) {
            PdfPage pdfPage = pdfDoc.getPage(i);
            Rectangle pageSize = pdfPage.getPageSize();
            float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
            float y = (pageSize.getTop() + pageSize.getBottom()) / 2;
            PdfCanvas over = new PdfCanvas(pdfPage);
            over.saveState();
            over.setExtGState(extGState);
            over.addImage(watermarkImageData, width, 0, 0, height, x - (width / 2), y - (height / 2), true);
            over.restoreState();
        }
    }

    /**
     * 添加固定位置的文本水印（固定位置的水印只支持水平板式，不支持斜式和垂直）
     *
     * @param watermarkParagraph 水印段落
     * @param positionWatermarkStyle 固定位置水印样式
     * @param pageNumber 页码
     * @param pageWidth 页面宽度
     * @param pageHeight 页面高度
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

    private void createRepeatTextWatermark(Paragraph watermarkParagraph, RepeatWatermarkStyle repeatWatermarkStyle,
                                           int pageNumber, float watermarkWidth, float watermarkHeight) {
        double radians = Math.toRadians(-repeatWatermarkStyle.getFormat().getRotation());
        for (int row = 0; row < repeatWatermarkStyle.getRows(); row++) {
            for (int col = 0; col < repeatWatermarkStyle.getCols(); col++) {
                float x = (watermarkWidth + repeatWatermarkStyle.getXSpace()) * col;
                float y = (watermarkHeight + repeatWatermarkStyle.getYSpace()) * row;
                doc.showTextAligned(watermarkParagraph, x, y, pageNumber, TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float) radians);
            }
        }
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
