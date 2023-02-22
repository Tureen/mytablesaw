package com.tulane.mytablesaw.excel;

import tech.tablesaw.api.Table;

import java.io.InputStream;

public class Test {

    public static void main(String[] args) {
        ExcelAnalyze analyze = new ExcelAnalyze();
//        InputStream inputStream = Test.class.getClassLoader().getResourceAsStream("测试.xlsx");
        InputStream inputStream = Test.class.getClassLoader().getResourceAsStream("测试_同页单体.xlsx");
        analyze.exec(inputStream);
        TableSawManager tablesawManager = new TableSawManager(analyze.getXmlResolve());
        tablesawManager.init();

        for (Table table : tablesawManager.getTables()) {
            System.out.println(table.toString());
        }
    }
}
