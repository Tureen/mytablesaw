package com.tulane.mytablesaw.excel;

import com.tulane.mytablesaw.excel.index.TableIndexRow;
import com.tulane.mytablesaw.excel.index.TableIndexRows;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) {
        ExcelAnalyze analyze = new ExcelAnalyze();
        InputStream inputStream = Test.class.getClassLoader().getResourceAsStream("测试_同页单体.xlsx");
        analyze.exec(inputStream);

        Map<String, List<String>> tableTitleIndexs = new LinkedHashMap<>();
        tableTitleIndexs.put("单头-客户信息", Arrays.asList("客户编号", "客户年龄", "客户电话"));
        tableTitleIndexs.put("单体-客户流水", Arrays.asList("客户编号", "银行流水", "转账日期"));
        tableTitleIndexs.put("单体-客户喜好", Arrays.asList("客户编号", "客户爱好"));
        tableTitleIndexs.put("单头-银行信息", Arrays.asList("银行ID", "银行账号"));

        TableSawManager tablesawManager = new TableSawManager(tableTitleIndexs);
        Map<Integer, LinkedList<XmlData>> storesGroupByRow = XmlResolve.getStoresGroupByRow(analyze.getXmlResolve().getStores());

        // 构建标识行对象, 每个标识行对象 (TableIndexRow) 就是一个表格
        TableIndexRows tableIndexRows = new TableIndexRows();
        storesGroupByRow.forEach((row, storesByRow) -> {
            Map<String, List<XmlData>> titleIndex = tablesawManager.findTitleIndex(storesByRow);
            titleIndex.forEach((name, xmlDatas) ->
                    tableIndexRows.addTableIndexRow(new TableIndexRow(row, name, xmlDatas)));
        });

        // 计算表格边界
        tableIndexRows.calculateTableBorderRow();

        List<Table> tables = new LinkedList<>();
        Map<Integer, LinkedList<XmlData>> storesGroupByCol = XmlResolve.getStoresGroupByCol(analyze.getXmlResolve().getStores());
        for (TableIndexRow tableIndexRow : tableIndexRows.getTableIndexRows()) {

            Map<Integer, List<XmlData>> tableIndexRowGroup = tableIndexRow.getTableIndexXmlDatas().stream()
                    .collect(Collectors.groupingBy(xmlData -> xmlData.getXmlPosition().getCol()));

            Map<Integer, LinkedList<XmlData>> datas = tableIndexRow.buildTableXmlDatas(storesGroupByCol);


            Table table = Table.create(UUID.randomUUID().toString().replaceAll("-", ""));
            tables.add(table);
            datas.forEach((col, xmlData) -> {
                XmlData indexXmlData = tableIndexRowGroup.get(col).get(0);
                StringColumn strings = StringColumn.create(
                        indexXmlData.getContext(),
                        xmlData.stream()
                                .map(XmlData::getContext)
                                .collect(Collectors.toList()));
                table.addColumns(strings);
            });
        }

        for (Table table : tables) {
            System.out.println(table.toString());
            System.out.println();
        }
    }
}
