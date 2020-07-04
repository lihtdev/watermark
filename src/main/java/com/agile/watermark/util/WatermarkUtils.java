package com.agile.watermark.util;

import com.agile.watermark.model.*;
import com.microsoft.schemas.office.office.CTLock;
import com.microsoft.schemas.vml.*;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.util.stream.Stream;

/**
 * 水印工具类
 *
 * @author lihaitao
 * @since 2020-07-03
 */
public class WatermarkUtils {

    /**
     * 为 Word 文档设置水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 加水印后的文件输出流
     * @param watermark    水印
     * @author lihaitao
     * @since 2020/7/3
     */
    public static void setWatermarkForWord(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        if (watermark instanceof TextWatermark) {
            setTextWatermarkForWord(inputStream, outputStream, (TextWatermark) watermark);
        } else if (watermark instanceof ImageWatermark) {
            // 图片水印暂未实现
        }
    }

    public static void setWatermarkForExcel(InputStream inputStream, OutputStream outputStream, Watermark watermark) {

    }

    public static void setWatermarkForPPT(InputStream inputStream, OutputStream outputStream, Watermark watermark) {

    }

    public static void setWatermarkForPDF(InputStream inputStream, OutputStream outputStream, Watermark watermark) {

    }

    public static void setWatermarkForPicture(InputStream inputStream, OutputStream outputStream, Watermark watermark) {

    }

    private static void setTextWatermarkForWord(InputStream inputStream, OutputStream outputStream, TextWatermark textWatermark) throws IOException {
        XWPFDocument doc = new XWPFDocument(inputStream);
        WatermarkStyle watermarkStyle = textWatermark.getStyle();
        if (watermarkStyle instanceof RepeatWatermarkStyle) {
            RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) watermarkStyle;
            String text = repeatString(textWatermark.getText() + repeatString(" ", 8), repeatWatermarkStyle.getCols());
            textWatermark.setText(text);
            int rows = repeatWatermarkStyle.getRows();
            for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
                createTextWatermarkForWord(doc, textWatermark, getRepeatWatermarkStyle(textWatermark, rowIndex), rowIndex);
            }
        } else {
            createTextWatermarkForWord(doc, textWatermark, getPositionWatermarkStyle(textWatermark, 0), 0);
        }
        doc.write(outputStream);
        doc.close();
    }

    /**
     * 将指定的字符串重复repeats次.
     */
    private static String repeatString(String pattern, int repeats) {
        StringBuilder buffer = new StringBuilder(pattern.length() * repeats);
        Stream.generate(() -> pattern).limit(repeats).forEach(buffer::append);
        return new String(buffer);
    }

    private static void createTextWatermarkForWord(XWPFDocument doc, TextWatermark textWatermark, String style, int index) {
        // 如果之前已经创建过 DEFAULT 的 Header，将会复用之
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
        int size = header.getParagraphs().size();
        if (size == 0) {
            header.createParagraph();
        }
        CTP ctp = header.getParagraphArray(0).getCTP();
        byte[] rsidR = doc.getDocument().getBody().getPArray(0).getRsidR();
        byte[] rsidRDefault = doc.getDocument().getBody().getPArray(0).getRsidRDefault();
        ctp.setRsidP(rsidR);
        ctp.setRsidRDefault(rsidRDefault);
        CTPPr ppr = ctp.addNewPPr();
        ppr.addNewPStyle().setVal("Header");
        // 开始加水印
        CTR ctr = ctp.addNewR();
        CTRPr ctrpr = ctr.addNewRPr();
        ctrpr.addNewNoProof();
        CTGroup group = CTGroup.Factory.newInstance();
        CTShapetype shapeType = group.addNewShapetype();
        CTTextPath shapeTypeTextPath = shapeType.addNewTextpath();
        shapeTypeTextPath.setOn(STTrueFalse.T);
        shapeTypeTextPath.setFitshape(STTrueFalse.T);
        CTLock lock = shapeType.addNewLock();
        lock.setExt(STExt.VIEW);
        CTShape shape = group.addNewShape();
        shape.setId("PowerPlusWaterMarkObject" + index);
        shape.setSpid("_x0000_s102" + (4 + index));
        shape.setType("#_x0000_t136");
        shape.setStyle(style); // 设置形状样式（旋转，位置，相对路径等参数）
        shape.setFillcolor(textWatermark.getColor());
        shape.setStroked(STTrueFalse.FALSE); // 字体设置为实心
        CTTextPath shapeTextPath = shape.addNewTextpath(); // 绘制文本的路径
        shapeTextPath.setStyle("font-family:" + textWatermark.getFontFamily() + ";font-size:" + textWatermark.getFontSize() + "pt"); // 设置文本字体与大小
        shapeTextPath.setString(textWatermark.getText());
        CTPicture pict = ctr.addNewPict();
        System.out.println(shape);
        pict.set(group);
    }

    private static String getPositionWatermarkStyle(TextWatermark textWatermark, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("position:absolute");
        // 计算文本占用的长度（文本总个数 * 单字长度）
        sb.append(";width:").append(textWatermark.getText().length() * textWatermark.getFontSize()).append("pt");
        sb.append(";height:").append(textWatermark.getFontSize()).append("pt");
        sb.append(";z-index:-251654144");
        sb.append(";mso-wrap-edited:f");
        // 设置水印的间隔，这是一个大坑，不能用 top，必须要 margin-top
        sb.append(";margin-top:").append(0);
        sb.append(";margin-left:").append(0);
        sb.append(";mso-position-horizontal-relative:").append("page");
        sb.append(";mso-position-vertical-relative:").append("page");
        WatermarkStyle watermarkStyle = textWatermark.getStyle();
        sb.append(";rotation:").append(watermarkStyle.getFormat().getRotation());
        sb.append(";opacity:").append(watermarkStyle.getOpacity());
        PositionWatermarkStyle positionWatermarkStyle = (PositionWatermarkStyle) watermarkStyle;
        sb.append(";mso-position-vertical:").append(positionWatermarkStyle.getPositions()[index].getVertical());
        sb.append(";mso-position-horizontal:").append(positionWatermarkStyle.getPositions()[index].getHorizontal());
        return sb.toString();
    }

    private static String getRepeatWatermarkStyle(TextWatermark textWatermark, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append("position:absolute");
        // 计算文本占用的长度（文本总个数 * 单字长度）
        sb.append(";width:").append(textWatermark.getText().length() * textWatermark.getFontSize() / 2).append("pt");
        sb.append(";height:").append(textWatermark.getFontSize()).append("pt");
        sb.append(";z-index:-251654144");
        sb.append(";mso-wrap-edited:f");
        WatermarkStyle watermarkStyle = textWatermark.getStyle();
        sb.append(";rotation:").append(watermarkStyle.getFormat().getRotation());
        sb.append(";opacity:").append(watermarkStyle.getOpacity());
        sb.append(";mso-position-horizontal-relative:").append("page");
        sb.append(";mso-position-vertical-relative:").append("page");
        sb.append(";margin-left:").append(0);
        RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) watermarkStyle;
        int marginTop = repeatWatermarkStyle.getYSpace() * index;
        sb.append(";margin-top:").append(marginTop).append("px");
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        TextWatermark watermark = new TextWatermark();
        watermark.setText("禁止复制");
        RepeatWatermarkStyle watermarkStyle = new RepeatWatermarkStyle();
        watermark.setStyle(watermarkStyle);
        WatermarkUtils.setWatermarkForWord(new FileInputStream("d:/pdf/app1.docx"), new FileOutputStream("d:/pdf/app9.docx"), watermark);
    }

}

