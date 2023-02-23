package com.tulane.mytablesaw.table;

import com.tulane.mytablesaw.excel.model.XmlData;
import com.tulane.mytablesaw.table.index.TableIndexRow;

import java.util.LinkedList;
import java.util.Map;

/**
 * 表格数据
 * @author Tulane
 */
public class TableData {

    private TableIndexRow tableIndexRow;
    private Map<Integer, LinkedList<XmlData>> tableDatasByCol;

    public TableData(TableIndexRow tableIndexRow, Map<Integer, LinkedList<XmlData>> tableDatasByCol) {
        this.tableIndexRow = tableIndexRow;
        this.tableDatasByCol = tableDatasByCol;
    }

    public TableIndexRow getTableIndexRow() {
        return tableIndexRow;
    }

    public Map<Integer, LinkedList<XmlData>> getTableDatasByCol() {
        return tableDatasByCol;
    }
}
