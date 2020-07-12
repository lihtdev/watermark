package com.agile.watermark.creator.impl;

import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.model.ImageWatermark;
import com.agile.watermark.model.TextWatermark;
import com.agile.watermark.model.Watermark;
import com.agile.watermark.util.ColorUtils;
import com.agile.watermark.util.FontUtils;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
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
            doc.showTextAligned(paragraph, x, y, i, TextAlignment.CENTER, VerticalAlignment.TOP, 0);
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
