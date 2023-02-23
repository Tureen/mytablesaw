package com.tulane.mytablesaw.table.index;

import com.tulane.mytablesaw.excel.XmlResolve;
import com.tulane.mytablesaw.excel.model.XmlData;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 表格标识行管理类
 *
 * @author Tulane
 * @date 2023/2/23
 */
public class TableIndexRowManager {

    /**
     * 表格标识行集合
     */
    private List<TableIndexRow> tableIndexRows;

    private TableIndexPools tableIndexPools;

    public TableIndexRowManager(Map<String, List<String>> tableTitleIndexs) {
        this.tableIndexPools = new TableIndexPools(tableTitleIndexs);
        this.tableIndexRows = new LinkedList<>();
    }

    public void init(Map<Integer, LinkedList<XmlData>> storesGroupByRow){
        // 生成表格标识行集合
        generateTableIndexRows(storesGroupByRow);
        // 计算表格底部边界
        calculateTableBorderRow();
    }

    /**
     * 生成表格标识行集合
     * (根据用户填入的标识行名称组合, 匹配上的单元格构建成标识行对象)
     * @param storesGroupByRow
     */
    private void generateTableIndexRows(Map<Integer, LinkedList<XmlData>> storesGroupByRow) {
        storesGroupByRow.forEach((row, storesByRow) -> {
            Map<String, List<XmlData>> xmlDatasWhichMatchIndex = findXmlDatasWhichMatchIndex(storesByRow);
            xmlDatasWhichMatchIndex.forEach((name, xmlDatas) ->
                    tableIndexRows.add(new TableIndexRow(row, name, xmlDatas)));
        });
    }

    /**
     * 检索完全匹配标识行的单元格集合
     * @param storesGroup
     * @return
     */
    private Map<String, List<XmlData>> findXmlDatasWhichMatchIndex(List<XmlData> storesGroup){
        Map<String, List<XmlData>> xmlDataForTableIndex = new LinkedHashMap<>();
        // 记录已作为表格标识行的单元格, 防止重复用在不同表格
        Set<XmlData> useXmlData = new HashSet<>();
        tableIndexPools.tryMatchIndexs(storesGroup, matchTableIndexPools -> {
            for (TableIndexPools.TableIndexPool matchTableIndexPool : matchTableIndexPools) {
                // 从完全匹配标识行的池子中, 获取匹配的单元格
                Optional<List<XmlData>> tableIndexXmlDatas = matchTableIndexPool.buildXmlDataForTableIndex(useXmlData);
                tableIndexXmlDatas.ifPresent(xmlDatas ->
                        xmlDataForTableIndex.put(matchTableIndexPool.getName(), xmlDatas));

            }
        });
        return xmlDataForTableIndex;
    }

    /**
     * 计算表格底部边界
     */
    public void calculateTableBorderRow(){
        // 计算所有单元格的底部边界
        Map<XmlData, Integer> xmlDatasBorderRow = calculateXmlDataBorderRow();
        // 计算每个表格的底部边界: 同表格的所有标识行单元格, 找到最小值, 赋值为表格边界
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

    /**
     * 计算所有单元格底部边界
     * 1. 划分出每个表格标识行的占用位置
     * 2. 遍历标识行: 计算不重叠的底部边界
     * @return
     */
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
