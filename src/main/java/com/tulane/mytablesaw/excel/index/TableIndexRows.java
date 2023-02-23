package com.tulane.mytablesaw.excel.index;

import com.tulane.mytablesaw.excel.XmlData;
import com.tulane.mytablesaw.excel.XmlResolve;

import java.util.*;

public class TableIndexRows {

    private List<TableIndexRow> tableIndexRows;

    public TableIndexRows() {
        this.tableIndexRows = new LinkedList<>();
    }

    public void addTableIndexRow(TableIndexRow tableIndexRow){
        tableIndexRows.add(tableIndexRow);
    }

    public void calculateTableBorderRow(){
        Map<XmlData, Integer> xmlDatasBorderRow = calculateXmlDataBorderRow();
        // 表格边界计算: 同表格的所有标识行单元格, 找到最小值, 赋值为表格边界
        for (TableIndexRow tableIndexRow : tableIndexRows) {
            int tableBorderRow = Integer.MAX_VALUE;
            for (XmlData xmlData : tableIndexRow.getTableIndexXmlDatas()) {
                Integer borderRow = xmlDatasBorderRow.get(xmlData);
                if(borderRow == null){
                    continue;
                }
                tableBorderRow = Math.min(tableBorderRow, borderRow);
            }
            if(Integer.MAX_VALUE != tableBorderRow){
                tableIndexRow.setTableBorderRow(tableBorderRow);
            }
        }
    }

    private Map<XmlData, Integer> calculateXmlDataBorderRow(){
        // 聚合同列的标识行, 用于计算表格边界
        Map<Integer, List<XmlData>> xmlDataFromOverColumn = getTogetherXmlDataWithOverColumn();
        // 连接表间空隙列
        List<XmlData> virtualXmlDatas = buildVirtualXmlDataByLinkTableRow(xmlDataFromOverColumn);
        // 填充表间空隙列
        for (XmlData virtualXmlData : virtualXmlDatas) {
            xmlDataFromOverColumn.computeIfAbsent(virtualXmlData.getXmlPosition().getCol(), x -> new LinkedList<>()).add(virtualXmlData);
        }
        // 收集每个标识行单元格的边界
        Map<XmlData, Integer> xmlDataColumnBorderRow = new HashMap<>();
        for (Map.Entry<Integer, List<XmlData>> entry : xmlDataFromOverColumn.entrySet()) {
            List<XmlData> xmlDatas = entry.getValue();
            // 同列标识行排序: 按照行号, 升序排序
            xmlDatas.sort(Comparator.comparingInt(o -> o.getXmlPosition().getRow()));
            // 计算每个标识行单元格的边界
            XmlData previousXmlData = null;
            for (XmlData xmlData : xmlDatas) {
                if(previousXmlData != null){
                    xmlDataColumnBorderRow.put(previousXmlData, xmlData.getXmlPosition().getRow());
                }
                previousXmlData = xmlData;
            }
        }
        return xmlDataColumnBorderRow;
    }

    /**
     * 聚合同列的标识行, 用于计算表格边界
     * @return
     */
    private Map<Integer, List<XmlData>> getTogetherXmlDataWithOverColumn() {
        Map<Integer, List<XmlData>> xmlDataFromColumn = new HashMap<>();
        for (TableIndexRow tableIndexRow : tableIndexRows) {
            for (XmlData xmlData : tableIndexRow.getTableIndexXmlDatas()) {
                int col = xmlData.getXmlPosition().getCol();
                xmlDataFromColumn.computeIfAbsent(col, x -> new LinkedList<>()).add(xmlData);
            }
        }
        return xmlDataFromColumn;
    }

    private List<XmlData> buildVirtualXmlDataByLinkTableRow(Map<Integer, List<XmlData>> xmlDataFromOverColumn){
        List<XmlData> data = new LinkedList<>();
        for (Map.Entry<Integer, List<XmlData>> entry : xmlDataFromOverColumn.entrySet()) {
            List<XmlData> xmlDatas = entry.getValue();
            data.addAll(xmlDatas);
        }

        List<XmlData> allVirtualXmlDatas = new LinkedList<>();
        Map<Integer, LinkedList<XmlData>> storesGroupByRow = XmlResolve.getStoresGroupByRow(data);
        for (Map.Entry<Integer, LinkedList<XmlData>> entry : storesGroupByRow.entrySet()) {
            List<XmlData> xmlDatas = entry.getValue();
            XmlData previousXmlData = null;
            for (XmlData xmlData : xmlDatas) {
                if(previousXmlData != null){
                    if((xmlData.getXmlPosition().getCol() - previousXmlData.getXmlPosition().getCol()) > 1){
                        List<XmlData> virtualXmlDatas = createVirtualXmlData(previousXmlData, xmlData);
                        allVirtualXmlDatas.addAll(virtualXmlDatas);
                    }
                }
                previousXmlData = xmlData;
            }
        }
        return allVirtualXmlDatas;
    }

    private List<XmlData> createVirtualXmlData(XmlData previousXmlData, XmlData xmlData) {
        int beginCol = previousXmlData.getXmlPosition().getCol();
        int endCol = xmlData.getXmlPosition().getCol();

        List<XmlData> virtualXmlData = new LinkedList<>();
        for (int i = beginCol+1; i < endCol; i++) {
            XmlData.XmlPosition xmlPosition = new XmlData.XmlPosition(xmlData.getXmlPosition().getRow(), i);
            virtualXmlData.add(XmlData.createVirtualXmlData(xmlPosition));
        }
        return virtualXmlData;
    }

    public List<TableIndexRow> getTableIndexRows() {
        return tableIndexRows;
    }
}
