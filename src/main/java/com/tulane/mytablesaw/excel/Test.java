package com.tulane.mytablesaw.excel;

import tech.tablesaw.api.Table;

import java.io.InputStream;
import java.util.*;

public class Test {

    public static void main(String[] args) {
        ExcelAnalyze analyze = new ExcelAnalyze();
        InputStream inputStream = Test.class.getClassLoader().getResourceAsStream("测试_同页单体.xlsx");
        analyze.exec(inputStream);

        Map<String, List<String>> tableTitleIndexs = new LinkedHashMap<>();
        tableTitleIndexs.put("单头-客户信息", Arrays.asList("客户编号", "客户年龄", "客户电话"));
        tableTitleIndexs.put("单体-客户流水", Arrays.asList("客户编号", "银行流水", "转账日期"));

        TableSawManager tablesawManager = new TableSawManager(tableTitleIndexs);
        Map<Integer, List<XmlResolve.XmlData>> storesGroupByRow = XmlResolve.getStoresGroupByRow(analyze.getXmlResolve().getStores());

        storesGroupByRow.forEach((row, storesByRow) -> {
            Map<String, List<XmlResolve.XmlData>> titleIndex = tablesawManager.findTitleIndex(storesByRow);
            System.out.println(titleIndex);
        });


//        for (Table table : tablesawManager.getTables()) {
//            System.out.println(table.toString());
//        }
    }
}
