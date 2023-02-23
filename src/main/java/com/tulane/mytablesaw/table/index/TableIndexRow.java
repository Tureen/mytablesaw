package com.tulane.mytablesaw.table.index;

import com.tulane.mytablesaw.excel.model.XmlData;

import java.util.List;

/**
 * 表格标识行
 * @author Tulane
 */
public class TableIndexRow {

    private int indexRow;
    private String name;
    private List<XmlData> tableIndexXmlDatas;
    private int tableBorderRow;

    public TableIndexRow(int indexRow, String name, List<XmlData> tableIndexXmlDatas) {
        this.indexRow = indexRow;
        this.name = name;
        this.tableIndexXmlDatas = tableIndexXmlDatas;
        this.tableBorderRow = -1; // 数据无边界
    }

    public List<XmlData> getTableIndexXmlDatas() {
        return tableIndexXmlDatas;
    }

    public void setTableBorderRow(int tableBorderRow) {
        this.tableBorderRow = tableBorderRow;
    }

    public int getIndexRow() {
        return indexRow;
    }

    public int getTableBorderRow() {
        return tableBorderRow;
    }
}
