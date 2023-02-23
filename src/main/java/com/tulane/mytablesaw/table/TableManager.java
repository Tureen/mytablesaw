package com.tulane.mytablesaw.table;

import com.tulane.mytablesaw.excel.model.XmlData;
import com.tulane.mytablesaw.excel.XmlResolve;
import com.tulane.mytablesaw.table.index.TableIndexRow;
import com.tulane.mytablesaw.table.index.TableIndexRowManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表格管理类
 * @author Tulane
 */
public class TableManager {

    /**
     * 表格标识行集合
     */
    private TableIndexRowManager tableIndexRowManager;

    private List<XmlData> store;
    private Map<Integer, LinkedList<XmlData>> storesGroupByRow;
    private Map<Integer, LinkedList<XmlData>> storesGroupByCol;

    private List<TableData> tableDatas;

    public TableManager(Map<String, List<String>> tableTitleIndexs, List<XmlData> store) {
        this.tableIndexRowManager = new TableIndexRowManager(tableTitleIndexs);
        this.store = store;
        this.tableDatas = new LinkedList<>();
    }

    public void init(){
        tableIndexRowManager.init(getStoresGroupByRow());
        createTableData();
    }

    public void createTableData(){
        for (TableIndexRow tableIndexRow : tableIndexRowManager.getTableIndexRows()) {
            Map<Integer, LinkedList<XmlData>> datas = buildTableDatas(tableIndexRow);
            tableDatas.add(new TableData(tableIndexRow, datas));
        }
    }

    /**
     * 生成表格数据
     * @param tableIndexRow
     * @return
     */
    public Map<Integer, LinkedList<XmlData>> buildTableDatas(TableIndexRow tableIndexRow){
        Map<Integer, LinkedList<XmlData>> tableXmlDatas = new LinkedHashMap<>();
        for (XmlData indexXmlData : tableIndexRow.getTableIndexXmlDatas()) {
            int col = indexXmlData.getXmlPosition().getCol();
            LinkedList<XmlData> xmlDatas = getStoresGroupByCol().get(col);
            xmlDatas = xmlDatas.stream()
                    .filter(x -> {
                        int currentRow = x.getXmlPosition().getRow();
                        boolean isStart = currentRow > tableIndexRow.getIndexRow();
                        boolean isEnd = tableIndexRow.getTableBorderRow() == -1 || currentRow < tableIndexRow.getTableBorderRow();
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
    private void filterTableDataWhichEmptyRow(Map<Integer, LinkedList<XmlData>> tableXmlDatas) {
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

    public Map<Integer, LinkedList<XmlData>> getStoresGroupByRow() {
        if(storesGroupByRow == null){
            storesGroupByRow = XmlResolve.getStoresGroupByRow(store);
        }
        return storesGroupByRow;
    }

    public Map<Integer, LinkedList<XmlData>> getStoresGroupByCol() {
        if(storesGroupByCol == null){
            storesGroupByCol = XmlResolve.getStoresGroupByCol(store);
        }
        return storesGroupByCol;
    }

    public List<TableData> getTableDatas() {
        return tableDatas;
    }
}
