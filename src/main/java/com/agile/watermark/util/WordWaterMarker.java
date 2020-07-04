package com.agile.watermark.util;

import com.microsoft.schemas.office.office.CTLock;
import com.microsoft.schemas.vml.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.*;
import java.util.stream.Stream;

/**
 * 微软office Word 水印机.
 */
public class WordWaterMarker {

    private String customText; // 水印文字
    private String fontName = "华文行楷"; // word字体
    private int fontSize = 30; // 字体大小
    private String fontColor = "#d0d0d0"; // 字体颜色
    private String styleTop = "0"; // 与顶部的间距
    private String styleRotation = "45"; // 文本旋转角度

    public WordWaterMarker(String customText) {
        customText = customText + repeatString(" ", 8); // 水印文字之间使用8个空格分隔
        this.customText = repeatString(customText, 10); // 一行水印重复水印文字次数
    }

    /**
     * 【核心方法】将输入流中的docx文档加载添加水印后输出到输出流中.
     *
     * @param inputStream  docx文档输入流
     * @param outputStream 添加水印后docx文档的输出流
     */
    public void makeSlopeWaterMark(InputStream inputStream, OutputStream outputStream) {
        XWPFDocument doc = null;
        try {
            doc = new XWPFDocument(inputStream);
            // 遍历文档，添加水印
            for (int lineIndex = -10, i = 0; lineIndex < 10; lineIndex++, i++) {
                styleTop = 200 * lineIndex + "";
                waterMarkDocXDocument(doc, i);
            }
            doc.write(outputStream); // 写出添加水印后的文档
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(doc);
        }
    }

    /**
     * 为文档添加水印<br />
     * 实现参考了{@link org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy#getWatermarkParagraph(String, int)}
     *
     * @param doc 需要被处理的docx文档对象
     */
    private void waterMarkDocXDocument(XWPFDocument doc, int index) {
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT); // 如果之前已经创建过 DEFAULT 的Header，将会复用之
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
        shape.setStyle(getShapeStyle()); // 设置形状样式（旋转，位置，相对路径等参数）
        shape.setFillcolor(fontColor);
        shape.setStroked(STTrueFalse.FALSE); // 字体设置为实心
        CTTextPath shapeTextPath = shape.addNewTextpath(); // 绘制文本的路径
        shapeTextPath.setStyle("font-family:" + fontName + ";font-size:1pt"); // 设置文本字体与大小
        shapeTextPath.setString(customText);
        CTPicture pict = ctr.addNewPict();
        pict.set(group);
    }

    // 构建Shape的样式参数
    private String getShapeStyle() {
        StringBuilder sb = new StringBuilder();
        sb.append("position: absolute"); // 文本path绘制的定位方式
        sb.append(";width: ").append(customText.length() * fontSize / 2).append("pt"); // 计算文本占用的长度（文本总个数*单字长度）
        sb.append(";height: ").append(fontSize).append("pt"); // 字体高度
        sb.append(";z-index: -251654144");
        sb.append(";mso-wrap-edited: f");
        // 设置水印的间隔，这是一个大坑，不能用 top，必须要 margin-top
        sb.append(";margin-top: ").append(styleTop);
        sb.append(";mso-position-horizontal-relative: ").append("page");
        sb.append(";mso-position-vertical-relative: ").append("page");
        sb.append(";mso-position-vertical: ").append("left");
        sb.append(";mso-position-horizontal: ").append("center");
//        sb.append(";rotation: ").append(styleRotation);
        return sb.toString();
    }

    /**
     * 将指定的字符串重复repeats次.
     */
    private String repeatString(String pattern, int repeats) {
        StringBuilder buffer = new StringBuilder(pattern.length() * repeats);
        Stream.generate(() -> pattern).limit(repeats).forEach(buffer::append);
        return new String(buffer);
    }

    public static void main(String[] args) throws FileNotFoundException {
        new WordWaterMarker("禁止复制").makeSlopeWaterMark(new FileInputStream("D:\\pdf\\app1.docx"), new FileOutputStream("D:\\pdf\\app6.docx"));
    }
}