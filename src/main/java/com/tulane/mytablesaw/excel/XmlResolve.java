package com.tulane.mytablesaw.excel;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class XmlResolve {

    private SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat msdatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");//毫秒日期格式
    private final DataFormatter formatter = new DataFormatter();

    private final static int DATE_FORMAT_INDEX28 = 28;//poi中文日期格式编码：m月d日:dataFormat=28
    private final static int DATE_FORMAT_INDEX31 = 31;//yyyy年m月d日:dataFormat=31

    private static Pattern SpecCharPattern = Pattern
            .compile("[\n`~!@#$^&*()+=|{}':;',\\[\\]<>/?~！@#￥……&*（）+|{}【】‘；：”“’。， 、？]");

    private List<XmlData> stores = new LinkedList<>();
    private Map<Integer, List<XmlData>> storesGroupByRow = new LinkedHashMap<>();
    private Map<Integer, List<XmlData>> storesGroupByCol = new LinkedHashMap<>();

    private Stack<XmlData> dataStack = new Stack<>();

    class XmlData{
        private XSSFDataType dataType;
        private short formatIndex;
        private String formatString;
        private String context;

        private XmlRow xmlRow;

        public XmlData(XSSFDataType dataType, short formatIndex, String formatString, XmlRow xmlRow) {
            this.dataType = dataType;
            this.formatIndex = formatIndex;
            this.formatString = formatString;
            this.xmlRow = xmlRow;
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

        public XmlRow getXmlRow() {
            return xmlRow;
        }
    }

    class XmlRow{
        private int row;
        private int col;

        public XmlRow(int row, int col) {
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

    public void startElement(StylesTable stylesTable, String name, Attributes attributes){
        if (name.equals("c")) {
            preExecCell(stylesTable, attributes);
        }
    }

    public void characters(char[] ch, int start, int length){
        XmlData xmlData = dataStack.peek();
        xmlData.setContext(new String(ch, start, length));
    }

    public void endElement(SharedStringsTable sharedStringsTable, String name){
        if (name.equals("c")) {
            execCell(sharedStringsTable);
        }
    }

    private void preExecCell(StylesTable stylesTable, Attributes attributes) {
        XSSFDataType dataType = XSSFDataType.NUMBER;
        //单元格值的类型
        String cellType = attributes.getValue("t");
        //单元格的样式
        String cellStyleStr = attributes.getValue("s");
        String formatString = StringUtils.EMPTY;
        short formatIndex = 0;

        if ("b".equals(cellType)) {
            dataType = XSSFDataType.BOOL;
        } else if ("e".equals(cellType)) {
            dataType = XSSFDataType.ERROR;
        } else if ("inlineStr".equals(cellType)) {
            dataType = XSSFDataType.INLINESTR;
        } else if ("s".equals(cellType)) {//t="s"表示值为字符串
            dataType = XSSFDataType.SSTINDEX;
        } else if ("str".equals(cellType)) {
            dataType = XSSFDataType.FORMULA;
        } else if (cellStyleStr != null) {
            // It's a number, but almost certainly one
            // with a special style or format
            int styleIndex = Integer.parseInt(cellStyleStr);
            XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
            formatIndex = style.getDataFormat();
            formatString = style.getDataFormatString();
            if (formatString.equals(StringUtils.EMPTY)) {
                formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
            }
        }
        // r属性表示单元格位置，例如A2,C3
        String coordinate = attributes.getValue("r");
        CellReference cellReference = new CellReference(coordinate);
        // 根据r属性获取其列下标，从0开始
        int col = cellReference.getCol();
        int row = cellReference.getRow();
        XmlRow xmlRow = new XmlRow(row, col);

        XmlData xmlData = new XmlData(dataType, formatIndex, formatString, xmlRow);
        dataStack.push(xmlData);
    }

    private void execCell(SharedStringsTable sharedStringsTable) {
        XmlData xmlData = dataStack.pop();
        stores.add(xmlData);

        String context = xmlData.getContext();
        switch (xmlData.getDataType()) {
            case BOOL:
                char first = context.charAt(0);
                context = first == '0' ? "FALSE" : "TRUE";
                break;

            case ERROR:
                context = "\"ERROR:" + context + '"';
                break;

            case FORMULA:
                // A formula could result in a string value,
                // so always add double-quote characters.
                // lastContents = lastContents;
                break;

            case INLINESTR:
                // TODO: have seen an example of this, so it's untested.
                XSSFRichTextString rtsi = new XSSFRichTextString(context);
                context = rtsi.toString();
                break;

            case SSTINDEX:
                String sstIndex = context;
                try {
                    int idx = Integer.parseInt(sstIndex);
                    //XSSFRichTextString rtss = new XSSFRichTextString(sst.getEntryAt(idx));
                    //rtss.toString()
                    //根据下标，去取sharedStrings.xml文件中的内容
                    RichTextString richTextString = sharedStringsTable.getItemAt(idx);
                    String string = richTextString.getString();
                    context = string == null ? "" : string;
                } catch (NumberFormatException ex) {
                    System.out.println("Failed to parse SST index '" + sstIndex + "': " + ex.toString());
                }
                break;

            case NUMBER:
                if (StringUtils.isNotBlank(context)) {
                    double n = Double.parseDouble(context);
                    //m月d日:dataFormat=28,yyyy年m月d日:dataFormat=31
                    boolean isADateFormat = false;
                    try {
                        isADateFormat = DateUtil.isADateFormat(xmlData.getFormatIndex(), xmlData.getFormatString());
                    } catch (Exception e) {
                        System.out.println("this.formatIndex" + xmlData.formatIndex + "-this.formatString:" + xmlData.formatString);
                        throw e;
                    }
                    if (isADateFormat
                            || DATE_FORMAT_INDEX31 == xmlData.formatIndex || DATE_FORMAT_INDEX28 == xmlData.formatIndex) {
                        Date date = DateUtil.getJavaDate(n);
                        /**
                         * Excel传入的日期如果解析不了的话，date的值为null
                         * 直接传Excel的内容到日期解析类的之后会做错误信息处理
                         */
                        if (date == null) {
                            context = String.valueOf(n);
                            break;
                        }
                        String timestamp = String.valueOf(date.getTime());//日期类型通过getTime()方法获取从1970年1月1日至今的毫秒值
                        /**
                         * 如果 ms(毫秒值) 是以“000”结尾的，说明该日期的最小单位是 s（秒）
                         * 如果 ms(毫秒值) 是以类似“123”结尾的，说明该日期的最小单位是 ms（毫秒）
                         */
                        if (timestamp.endsWith("000")) {
                            context = datetimeFormat.format(date);
                        } else {
                            context = msdatetimeFormat.format(date);
                        }
                        // 短日期格式不正确start
                        if (StringUtils.equals("yyyy\\-mm\\-dd", xmlData.formatString)) {
                            context = new SimpleDateFormat("yyyy-MM-dd").format(date);
                        }
                        // 短日期格式不正确end
                    } else {
                        if (xmlData.formatIndex >= 0 && StringUtils.isNotBlank(xmlData.formatString)) {
                            context = SpecCharPattern
                                    .matcher(formatter.formatRawCellContents(n, xmlData.formatIndex, xmlData.formatString))
                                    .replaceAll("");
//                                if (context != null && context.endsWith("%")) {
//                                    context = DecimalProp.percentToDoubleString(lastContents);
//                                }

                            boolean isNegative = n < 0;
                            n = Double.parseDouble(context);
                            if (isNegative && n >= 0) {
                                n = -n;
                            }
                        }
                        context = new BigDecimal(Double.toString(n)).stripTrailingZeros().toPlainString();
                    }
                }
                break;
        }
        xmlData.setContext(context);
    }

    public List<XmlData> getStores() {
        return stores;
    }

    public Map<Integer, List<XmlData>> getStoresGroupByRow() {
        return stores.stream().collect(
                Collectors.groupingBy(xmlData -> xmlData.getXmlRow().getRow(), LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
    }

    public Map<Integer, List<XmlData>> getStoresGroupByCol() {
        return stores.stream().collect(
                Collectors.groupingBy(xmlData -> xmlData.getXmlRow().getCol(), LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
    }
}
