package com.tulane.mytablesaw.excel;

import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {

    private SharedStringsTable sharedStringsTable;
    private StylesTable stylesTable;
    private XmlResolve xmlResolve;

    public XmlHandler(XmlResolve xmlResolve) {
        this.xmlResolve = xmlResolve;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        xmlResolve.startElement(stylesTable, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        xmlResolve.endElement(sharedStringsTable, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        xmlResolve.characters(ch, start, length);
    }

    public void setSharedStringsTable(SharedStringsTable sharedStringsTable) {
        this.sharedStringsTable = sharedStringsTable;
    }

    public void setStylesTable(StylesTable stylesTable) {
        this.stylesTable = stylesTable;
    }
}
