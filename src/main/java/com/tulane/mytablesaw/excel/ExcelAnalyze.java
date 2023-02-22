package com.tulane.mytablesaw.excel;


import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ExcelAnalyze {

    private XmlHandler xmlSheetHandler;
    private XmlResolve xmlResolve;

    public ExcelAnalyze() {
        this.xmlResolve = new XmlResolve();
        this.xmlSheetHandler = new XmlHandler(xmlResolve);
    }

    public void exec(File file) {
        try {
            XSSFReader.SheetIterator sheets = getSheets(xmlSheetHandler, file);
            XMLReader parser = getParser(xmlSheetHandler);
            while (sheets.hasNext()) {
                try(InputStream sheet = sheets.next()){
                    InputSource sheetSource = new InputSource(sheet);
                    if (StringUtils.isNotBlank(sheets.getSheetName())) {
                        parser.parse(sheetSource);
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private XSSFReader.SheetIterator getSheets(XmlHandler sheetHandler, File tempFile) throws Exception {
        OPCPackage pkg = OPCPackage.open(tempFile);
        XSSFReader r = new XSSFReader(pkg);

        r.setUseReadOnlySharedStringsTable(false);

        // XML文件传递: 共享字符表格 & 样式表
        sheetHandler.setSharedStringsTable((SharedStringsTable) r.getSharedStringsTable());
        sheetHandler.setStylesTable(r.getStylesTable());
        return (XSSFReader.SheetIterator) r.getSheetsData();
    }

    private XMLReader getParser(XmlHandler sheetHandler) throws Exception {
        XMLReader parser = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
        parser.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
        parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser.setContentHandler(sheetHandler);
        return parser;
    }

    public XmlResolve getXmlResolve() {
        return xmlResolve;
    }
}
