package com.agile.watermark.creator.impl;

import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.exception.WatermarkException;
import com.agile.watermark.model.*;
import com.microsoft.schemas.office.office.CTLock;
import com.microsoft.schemas.vml.*;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLTypeLoader;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlToken;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPicture;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 给 Word 文档添加水印
 * 1.只支持 .docx 文件，暂不支持 .doc 文件
 * 2.图片水印暂不支持重复
 *
 * @author lihaitao
 * @since 2020/7/4
 */
public class WordWatermarkCreatorImpl implements WatermarkCreator {

    /**
     * 旋转度数参数比例
     */
    private static final int ROT_RATIO = 60000;

    /**
     * 亮度
     */
    private static final int BRIGHT = 69998;

    /**
     * 对比度
     */
    private static final int CONTRAST = -70001;


    private InputStream inputStream;

    private InputStream imageStream;

    private XWPFDocument doc;

    /**
     * 给 .docx 文件添加水印
     *
     * @param inputStream  文件输入流
     * @param outputStream 添加水印后的文件输出流
     * @param watermark    水印
     * @author lihaitao
     * @since 2020-07-05
     */
    @Override
    public void create(InputStream inputStream, OutputStream outputStream, Watermark watermark) throws IOException {
        this.inputStream = inputStream;
        this.doc = new XWPFDocument(inputStream);

        if (watermark instanceof TextWatermark) {
            setTextWatermark((TextWatermark) watermark);
        } else if (watermark instanceof ImageWatermark) {
            setImageWatermark((ImageWatermark) watermark);
        }

        doc.write(outputStream);
    }

    /**
     * 设置文字水印
     *
     * @param textWatermark 文字水印
     * @author lihaitao
     * @since 2020-07-05
     */
    private void setTextWatermark(TextWatermark textWatermark) {
        WatermarkStyle watermarkStyle = textWatermark.getStyle();
        if (watermarkStyle instanceof RepeatWatermarkStyle) {
            RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) watermarkStyle;
            for (int rowIndex = 0; rowIndex < repeatWatermarkStyle.getRows(); rowIndex++) {
                for (int colIndex = 0; colIndex < repeatWatermarkStyle.getCols(); colIndex++) {
                    int index = rowIndex * repeatWatermarkStyle.getCols() + colIndex;
                    createTextWatermark(textWatermark, getRepeatTextWatermarkStyle(textWatermark, rowIndex, colIndex), index);
                }
            }
        } else if (watermarkStyle instanceof PositionWatermarkStyle) {
            PositionWatermarkStyle positionWatermarkStyle = (PositionWatermarkStyle) watermarkStyle;
            for (int i = 0; i < positionWatermarkStyle.getPositions().length; i++) {
                createTextWatermark(textWatermark, getPositionTextWatermarkStyle(textWatermark, i), i);
            }
        }
    }

    /**
     * 设置图片水印
     *
     * @param imageWatermark 图片水印
     * @author lihaitao
     * @since 2020-07-05
     */
    private void setImageWatermark(ImageWatermark imageWatermark) throws IOException {
        createImageWatermark(imageWatermark);
    }

    /**
     * 创建文字水印
     *
     * @param textWatermark 文字水印
     * @param style         水印样式字符串
     * @param index         水印索引
     * @author lihaitao
     * @since 2020-07-05
     */
    private void createTextWatermark(TextWatermark textWatermark, String style, int index) {
        XWPFParagraph paragraph = createDefaultHeaderParagraph();
        XWPFRun run = paragraph.createRun();
        CTR ctr = run.getCTR();
        // 开始加水印
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
        shape.setStyle(style);
        shape.setFillcolor(textWatermark.getColor());
        shape.setStroked(STTrueFalse.FALSE); // 字体设置为实心
        CTTextPath shapeTextPath = shape.addNewTextpath();
        shapeTextPath.setStyle("font-family:" + textWatermark.getFontFamily() + ";font-size:" + textWatermark.getFontSize() + "pt");
        shapeTextPath.setString(textWatermark.getText());
        CTPicture pict = ctr.addNewPict();
        pict.set(group);
    }

    /**
     * 创建默认的页眉
     *
     * @author lihaitao
     * @since 2020-07-05
     */
    private XWPFParagraph createDefaultHeaderParagraph() {
        // 如果之前已经创建过 DEFAULT 的 Header，将会复用之
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
        int size = header.getParagraphs().size();
        if (size == 0) {
            header.createParagraph();
        }
        return header.getParagraphArray(0);
    }

    /**
     * 获取固定位置的文字水印样式
     *
     * @param textWatermark 文字水印
     * @param index         水印索引
     * @author lihaitao
     * @since 2020-07-05
     */
    private String getPositionTextWatermarkStyle(TextWatermark textWatermark, int index) {
        StringBuilder style = getTextWatermarkStyle(textWatermark);
        // 设置水印的间隔，这是一个大坑，不能用 top, left 必须要 margin-top, margin-left
        style.append(";margin-top:").append(0);
        style.append(";margin-left:").append(0);
        PositionWatermarkStyle positionWatermarkStyle = (PositionWatermarkStyle) textWatermark.getStyle();
        style.append(";mso-position-vertical:").append(positionWatermarkStyle.getPositions()[index].getVertical());
        style.append(";mso-position-horizontal:").append(positionWatermarkStyle.getPositions()[index].getHorizontal());
        return style.toString();
    }

    /**
     * 获取重复的文字水印的样式
     *
     * @param textWatermark 文字水印
     * @param rowIndex      行号
     * @param colIndex      列号
     * @author lihaitao
     * @since 2020-07-05
     */
    private String getRepeatTextWatermarkStyle(TextWatermark textWatermark, int rowIndex, int colIndex) {
        StringBuilder style = getTextWatermarkStyle(textWatermark);
        RepeatWatermarkStyle repeatWatermarkStyle = (RepeatWatermarkStyle) textWatermark.getStyle();
        int marginTop = repeatWatermarkStyle.getYSpace() * rowIndex + repeatWatermarkStyle.getYStart();
        int marginLeft = repeatWatermarkStyle.getXSpace() * colIndex + repeatWatermarkStyle.getXStart();
        // 设置水印的间隔，这是一个大坑，不能用 top, left 必须要 margin-top, margin-left
        style.append(";margin-left:").append(marginLeft).append("px");
        style.append(";margin-top:").append(marginTop).append("px");
        return style.toString();
    }

    /**
     * 获取文字水印的样式，返回位置水印和重复水印的公共部分
     *
     * @param textWatermark 文字水印
     * @author lihaitao
     * @since 2020-07-05
     */
    private StringBuilder getTextWatermarkStyle(TextWatermark textWatermark) {
        StringBuilder style = new StringBuilder();
        style.append("position:absolute");
        // 计算文本占用的长度（文本总个数 * 单字长度）
        style.append(";width:").append(textWatermark.getText().length() * textWatermark.getFontSize()).append("pt");
        style.append(";height:").append(textWatermark.getFontSize()).append("pt");
        style.append(";z-index:-251654144");
        style.append(";mso-wrap-edited:f");
        style.append(";mso-position-horizontal-relative:").append("page");
        style.append(";mso-position-vertical-relative:").append("page");
        WatermarkStyle watermarkStyle = textWatermark.getStyle();
        style.append(";rotation:").append(watermarkStyle.getFormat().getRotation());
        style.append(";alpha:").append(watermarkStyle.getOpacity());
        return style;
    }

    /**
     * 创建图片水印
     *
     * @param imageWatermark 图片水印
     * @author lihaitao
     * @since 2020-07-05
     */
    private void createImageWatermark(ImageWatermark imageWatermark) throws IOException {
        this.imageStream = imageWatermark.getImageStream();
        int pictureType = getPictureType(imageWatermark);
        int cx = Units.toEMU(imageWatermark.getWidth());
        int cy = Units.toEMU(imageWatermark.getHeight());

        XWPFParagraph paragraph = createDefaultHeaderParagraph();
        XWPFRun run = paragraph.createRun();
        CTR ctr = run.getCTR();

        try {
            // Work out what to add the picture to, then add both the
            //  picture and the relationship for it
            XWPFHeader header = (XWPFHeader) paragraph.getPart();
            String relationId = header.addPictureData(imageStream, pictureType);

            // Create the drawing entry for it
            CTDrawing drawing = ctr.addNewDrawing();
            CTAnchor anchor = drawing.addNewAnchor();
            addNewGraphic(anchor);

            // Setup the anchor
            anchor.setDistT(0);
            anchor.setDistR(0);
            anchor.setDistB(0);
            anchor.setDistL(0);
            anchor.setAllowOverlap(true);
            anchor.setBehindDoc(true);
            anchor.setLayoutInCell(true);
            anchor.setLocked(false);

            CTPosH posH = anchor.addNewPositionH();
            posH.setRelativeFrom(STRelFromH.MARGIN);
            posH.setAlign(STAlignH.CENTER);
            CTPosV posV = anchor.addNewPositionV();
            posV.setRelativeFrom(STRelFromV.MARGIN);
            posV.setAlign(STAlignV.CENTER);

            CTNonVisualDrawingProps docPr = anchor.addNewDocPr();
            long id = paragraph.getDocument().getNextPicNameNumber(pictureType);
            docPr.setId(id);
            /* This name is not visible in Word 2010 anywhere. */
            docPr.setName("Drawing " + id);
            docPr.setDescr("WordPictureWatermark");

            CTPositiveSize2D extent = anchor.addNewExtent();
            extent.setCx(cx);
            extent.setCy(cy);

            // Grab the picture object
            CTGraphicalObject graphic = anchor.getGraphic();
            CTGraphicalObjectData graphicData = graphic.getGraphicData();
            org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture pic = getCTPictures(graphicData).get(0);

            // Set it up
            org.openxmlformats.schemas.drawingml.x2006.picture.CTPictureNonVisual nvPicPr = pic.addNewNvPicPr();

            CTNonVisualDrawingProps cNvPr = nvPicPr.addNewCNvPr();
            /* use "0" for the id. See ECM-576, 20.2.2.3 */
            cNvPr.setId(0L);
            /* This name is not visible in Word 2010 anywhere */
            cNvPr.setName("Picture " + id);
            cNvPr.setDescr("WordPictureWatermark");

            CTNonVisualPictureProperties cNvPicPr = nvPicPr.addNewCNvPicPr();
            cNvPicPr.addNewPicLocks().setNoChangeAspect(true);

            CTBlipFillProperties blipFill = pic.addNewBlipFill();
            blipFill.addNewStretch().addNewFillRect();
            CTBlip blip = blipFill.addNewBlip();
            blip.setEmbed(relationId);
            CTLuminanceEffect luminanceEffect = blip.addNewLum();
            // 亮度
            luminanceEffect.setBright(BRIGHT);
            // 对比度
            luminanceEffect.setContrast(CONTRAST);

            CTShapeProperties spPr = pic.addNewSpPr();
            CTTransform2D xfrm = spPr.addNewXfrm();
            // 旋转
            xfrm.setRot(imageWatermark.getStyle().getFormat().getRotation() * ROT_RATIO);

            CTPoint2D off = xfrm.addNewOff();
            off.setX(0);
            off.setY(0);

            CTPositiveSize2D ext = xfrm.addNewExt();
            ext.setCx(cx);
            ext.setCy(cy);

            CTPresetGeometry2D presetGeometry2D = spPr.addNewPrstGeom();
            presetGeometry2D.setPrst(STShapeType.RECT);
            presetGeometry2D.addNewAvLst();
        } catch (InvalidFormatException e) {
            throw new WatermarkException("水印图片类型错误：" + e.getMessage(), e);
        }
    }

    /**
     * 给 <wp:anchor></wp:anchor> 节点下添加 <a:graphic></a:graphic> 节点
     *
     * @param anchor <wp:anchor></wp:anchor> 节点
     * @author lihaitao
     * @since 2020/7/5
     */
    private void addNewGraphic(CTAnchor anchor) throws IOException {
        // Do the fiddly namespace bits on the inline
        // (We need full control of what goes where and as what)
        String xml =
                "<a:graphic xmlns:a=\"" + CTGraphicalObject.type.getName().getNamespaceURI() + "\">" +
                        "<a:graphicData uri=\"" + org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture.type.getName().getNamespaceURI() + "\">" +
                        "<pic:pic xmlns:pic=\"" + org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture.type.getName().getNamespaceURI() + "\" />" +
                        "</a:graphicData>" +
                        "</a:graphic>";
        InputSource is = new InputSource(new StringReader(xml));
        try {
            org.w3c.dom.Document document = DocumentHelper.readDocument(is);
            anchor.set(XmlToken.Factory.parse(document.getDocumentElement(), POIXMLTypeLoader.DEFAULT_XML_OPTIONS));
        } catch (SAXException | XmlException e) {
            throw new WatermarkException("给Word文件添加图片水印时发生错误：" + e.getMessage(), e);
        }
    }

    /**
     * 获取 <pic:pic></pic:pic> 节点
     *
     * @author lihaitao
     * @since 2020-07-05
     */
    private List<org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture> getCTPictures(XmlObject o) {
        List<org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture> pics = new ArrayList<>();
        String path = "declare namespace pic='" + org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture.type.getName().getNamespaceURI() + "' .//pic:pic";
        XmlObject[] picts = o.selectPath(path);
        for (XmlObject pict : picts) {
            if (pict instanceof XmlAnyTypeImpl) {
                // Pesky XmlBeans bug - see Bugzilla #49934
                try {
                    pict = org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture.Factory.parse(pict.toString(), POIXMLTypeLoader.DEFAULT_XML_OPTIONS);
                } catch (XmlException e) {
                    throw new POIXMLException(e);
                }
            }
            if (pict instanceof org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture) {
                pics.add((org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture) pict);
            }
        }
        return pics;
    }

    /**
     * 获取图片类型代码
     *
     * @param imageWatermark 图片水印
     * @author lihaitao
     * @since 2020-07-05
     */
    private int getPictureType(ImageWatermark imageWatermark) {
        switch (imageWatermark.getFormat()) {
            case EMF:
                return Document.PICTURE_TYPE_EMF;
            case WMF:
                return Document.PICTURE_TYPE_WMF;
            case PICT:
                return Document.PICTURE_TYPE_PICT;
            case PNG:
                return Document.PICTURE_TYPE_PNG;
            case DIB:
                return Document.PICTURE_TYPE_DIB;
            case GIF:
                return Document.PICTURE_TYPE_GIF;
            case TIFF:
                return Document.PICTURE_TYPE_TIFF;
            case EPS:
                return Document.PICTURE_TYPE_EPS;
            case BMP:
                return Document.PICTURE_TYPE_BMP;
            case WPG:
                return Document.PICTURE_TYPE_WPG;
            default:
                // 默认为 jpeg 格式
                return Document.PICTURE_TYPE_JPEG;
        }
    }

    /**
     * 实现自动关闭流的方法
     *
     * @author lihaitao
     * @since 2020-07-05
     */
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
