package com.tulane.mytablesaw.excel.index;

import com.tulane.mytablesaw.excel.XmlData;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<Integer, LinkedList<XmlData>> buildTableXmlDatas(Map<Integer, LinkedList<XmlData>> storesGroupByCol){
        Map<Integer, LinkedList<XmlData>> tableXmlDatas = new LinkedHashMap<>();
        for (XmlData indexXmlData : tableIndexXmlDatas) {
            int col = indexXmlData.getXmlPosition().getCol();
            LinkedList<XmlData> xmlDatas = storesGroupByCol.get(col);
            xmlDatas = xmlDatas.stream()
                    .filter(x -> {
                        int currentRow = x.getXmlPosition().getRow();
                        boolean isStart = currentRow > indexRow;
                        boolean isEnd = tableBorderRow == -1 || currentRow < tableBorderRow;
                        return isStart && isEnd;
                    })
                    .collect(Collectors.toCollection(LinkedList::new));
            tableXmlDatas.put(col, xmlDatas);
        }

        filterTableDataWhichOutBorder(tableXmlDatas);
        return tableXmlDatas;
    }

    /**
     * 过滤掉超出边界的数据
     * 1. 空行之后的数据直接丢弃
     * 2. 所有列中的最小行, 为表格最小行边界
     * @param tableXmlDatas
     */
    private void filterTableDataWhichOutBorder(Map<Integer, LinkedList<XmlData>> tableXmlDatas) {
        filterTableDataWhichEmptyRow(tableXmlDatas);
        filterTableDataWhichOutMinCol(tableXmlDatas);
    }

    /**
     * 过滤掉超出边界的数据: 空行之后的数据直接丢弃
     * @param tableXmlDatas
     */
    private static void filterTableDataWhichEmptyRow(Map<Integer, LinkedList<XmlData>> tableXmlDatas) {
        for (Map.Entry<Integer, LinkedList<XmlData>> entry : tableXmlDatas.entrySet()) {
            LinkedList<XmlData> newTableXmlDatas = new LinkedList<>();
            for (XmlData xmlData : entry.getValue()) {
                if(newTableXmlDatas.isEmpty()){
                    newTableXmlDatas.add(xmlData);
                    continue;
                }
                if((xmlData.getXmlPosition().getRow() - newTableXmlDatas.peekLast().getXmlPosition().getRow()) != 1){
                    break;
                }
                newTableXmlDatas.add(xmlData);
            }
            tableXmlDatas.put(entry.getKey(), newTableXmlDatas);
        }
    }

    /**
     * 过滤掉超出边界的数据: 所有列中的最小行, 为表格最小行边界
     * @param tableXmlDatas
     */
    private void filterTableDataWhichOutMinCol(Map<Integer, LinkedList<XmlData>> tableXmlDatas) {
        int minRow = Integer.MAX_VALUE;
        for (Map.Entry<Integer, LinkedList<XmlData>> entry : tableXmlDatas.entrySet()) {
            XmlData xmlData = entry.getValue().peekLast();
            minRow = Math.min(minRow, xmlData.getXmlPosition().getRow());
        }

        for (Map.Entry<Integer, LinkedList<XmlData>> entry : tableXmlDatas.entrySet()) {
            LinkedList<XmlData> newTableXmlDatas = new LinkedList<>();
            for (XmlData xmlData : entry.getValue()) {
                if(xmlData.getXmlPosition().getRow() > minRow){
                    break;
                }
                newTableXmlDatas.add(xmlData);
            }
            tableXmlDatas.put(entry.getKey(), newTableXmlDatas);
        }
    }

    public String getName() {
        return name;
    }
}
