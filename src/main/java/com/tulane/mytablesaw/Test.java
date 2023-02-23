package com.tulane.mytablesaw;

import com.tulane.mytablesaw.excel.ExcelAnalyze;
import com.tulane.mytablesaw.excel.model.XmlData;
import com.tulane.mytablesaw.table.TableData;
import com.tulane.mytablesaw.table.TableManager;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        List<XmlData> stores = analyze.getXmlResolve().getStores();

        // 构建标识行对象, 每个标识行对象 (TableIndexRow) 就是一个表格
        TableManager tableManager = new TableManager(tableTitleIndexs, stores);
        tableManager.init();


        List<Table> tables = new LinkedList<>();

        for (TableData tableData : tableManager.getTableDatas()) {

            Map<Integer, List<XmlData>> tableIndexRowGroup = tableData.getTableIndexRow().getTableIndexXmlDatas().stream()
                    .collect(Collectors.groupingBy(xmlData -> xmlData.getXmlPosition().getCol()));

            Table table = Table.create(UUID.randomUUID().toString().replaceAll("-", ""));
            tables.add(table);
            tableData.getTableDatasByCol().forEach((col, xmlData) -> {
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
