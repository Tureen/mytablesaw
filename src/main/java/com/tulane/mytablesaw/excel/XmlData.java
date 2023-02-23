package com.tulane.mytablesaw.excel;

public class XmlData {
    private final XSSFDataType dataType;
    private final short formatIndex;
    private final String formatString;
    private String context;

    private final XmlPosition xmlPosition;

    public XmlData(XSSFDataType dataType, short formatIndex, String formatString, XmlPosition xmlPosition) {
        this.dataType = dataType;
        this.formatIndex = formatIndex;
        this.formatString = formatString;
        this.xmlPosition = xmlPosition;
    }

    public static XmlData createVirtualXmlData(XmlPosition xmlPosition){
        return new XmlData(null, (short) 0, null, xmlPosition);
    }

    public void setContext(String context) {
        this.context = context;
    }

    public XSSFDataType getDataType() {
        return dataType;
    }

    public short getFormatIndex() {
        return formatIndex;
    }

    public String getFormatString() {
        return formatString;
    }

    public String getContext() {
        return context;
    }

    public XmlPosition getXmlPosition() {
        return xmlPosition;
    }

    public static class XmlPosition {
        private final int row;
        private final int col;

        public XmlPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }
}
