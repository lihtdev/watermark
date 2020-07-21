package com.agile.watermark.creator.impl;

import cn.hutool.core.img.ImgUtil;
import com.agile.watermark.creator.WatermarkCreator;
import com.agile.watermark.exception.WatermarkException;
import com.agile.watermark.model.ImageWatermark;
import com.agile.watermark.model.TextWatermark;
import com.agile.watermark.model.Watermark;
import com.agile.watermark.util.ImageUtils;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.POIXMLTypeLoader;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlToken;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.drawingml.x2006.picture.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 给 Word 文件添加图层水印的实现类（图层水印以图片的形式悬浮于文字之上，不易删除，需挨个选中删除）
 *
 * @author lihaitao
 * @since 2020/7/17
 */
public class WordLayerWatermarkCreator implements WatermarkCreator {

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

    /**
     * 原文件输入流
     */
    private InputStream inputStream;

    /**
     * 图片水印输入流
     */
    private InputStream imageStream;

    /**
     * .docx文档对象
     */
    private XWPFDocument doc;

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

    private void setTextWatermark(TextWatermark textWatermark) {
        BufferedImage bufferedImage = ImageUtils.createImage(textWatermark);
        ImageWatermark imageWatermark = new ImageWatermark(null);
        imageWatermark.setStyle(textWatermark.getStyle());
        imageWatermark.setWidth(bufferedImage.getWidth());
        imageWatermark.setHeight(bufferedImage.getHeight());
        imageWatermark.setType(ImageWatermark.Type.PNG);
        imageWatermark.setImageStream(ImgUtil.toStream(bufferedImage, imageWatermark.getType().name()));
        setImageWatermark(imageWatermark);
    }

    private void setImageWatermark(ImageWatermark imageWatermark) {
        this.imageStream = imageWatermark.getImageStream();
        int pictureType = getPictureType(imageWatermark);
        int cx = Units.toEMU(imageWatermark.getWidth());
        int cy = Units.toEMU(imageWatermark.getHeight());

        this.doc.getParagraphs().stream().filter(XWPFParagraph::isPageBreak).forEach(pageBreak -> {
            XWPFParagraph paragraph = this.doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            CTR ctr = run.getCTR();

            try {
                // Work out what to add the picture to, then add both the
                //  picture and the relationship for it
                String relationId = this.doc.addPictureData(imageStream, pictureType);

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
            } catch (InvalidFormatException | IOException e) {
                throw new WatermarkException("水印图片类型错误：" + e.getMessage(), e);
            }
        });
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
    private List<CTPicture> getCTPictures(XmlObject o) {
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
        switch (imageWatermark.getType()) {
            case EMF:
                return Document.PICTURE_TYPE_EMF;
            case WMF:
                return Document.PICTURE_TYPE_WMF;
            case PICT:
                return Document.PICTURE_TYPE_PICT;
            case JPEG:
                return Document.PICTURE_TYPE_JPEG;
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
                // 默认为 png 格式
                return Document.PICTURE_TYPE_PNG;
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
