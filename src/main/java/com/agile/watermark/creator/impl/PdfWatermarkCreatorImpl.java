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
    private static final int POSITION_WATERMARK_PADDING = 100;

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
        // transparency
        PdfExtGState extGState = new PdfExtGState();
        extGState.setFillOpacity(textWatermark.getStyle().getOpacity());
        double radians = Math.toRadians(-textWatermark.getStyle().getFormat().getRotation());
        // loop over every page
        // Implement transformation matrix usage in order to scale image
        for (int i = 1; i <= pages; i++) {
            PdfPage pdfPage = pdfDoc.getPage(i);
            Rectangle pageSize = pdfPage.getPageSize();
            float width = pageSize.getLeft() + pageSize.getRight();
            float height = pageSize.getTop() + pageSize.getBottom();
            float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
            float y = (pageSize.getTop() + pageSize.getBottom()) / 2;
            PdfCanvas over = new PdfCanvas(pdfPage);
            over.saveState();
            over.setExtGState(extGState);
            // 左上
            doc.showTextAligned(paragraph, 0, height, i, TextAlignment.LEFT, VerticalAlignment.TOP, (float) radians);
            // 左下
            doc.showTextAligned(paragraph, 0, 0, i, TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float) radians);
            // 居中
            doc.showTextAligned(paragraph, x, y, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, (float) radians);
            // 右上
            doc.showTextAligned(paragraph, width, height, i, TextAlignment.RIGHT, VerticalAlignment.TOP, (float) radians);
            // 右下
            doc.showTextAligned(paragraph, width, 0, i, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, (float) radians);

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

    /*private void createPositionWatermark(Paragraph watermarkParagraph, PositionWatermarkStyle positionWatermarkStyle,
                                          float pageWidth, float pageHeight) {
        double radians = Math.toRadians(-positionWatermarkStyle.getFormat().getRotation());
        for (PositionWatermarkStyle.Position position : positionWatermarkStyle.getPositions()) {
            switch (position) {
                case LEFT_TOP:
                    float x = 0 + POSITION_WATERMARK_PADDING;
                    doc.showTextAligned(paragraph, 0, height, i, TextAlignment.LEFT, VerticalAlignment.TOP, (float) radians);
            }
        }
        // 左下
        doc.showTextAligned(paragraph, 0, 0, i, TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float) radians);
        // 居中
        doc.showTextAligned(paragraph, x, y, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, (float) radians);
        // 右上
        doc.showTextAligned(paragraph, width, height, i, TextAlignment.RIGHT, VerticalAlignment.TOP, (float) radians);
        // 右下
        doc.showTextAligned(paragraph, width, 0, i, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, (float) radians);
    }*/

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
