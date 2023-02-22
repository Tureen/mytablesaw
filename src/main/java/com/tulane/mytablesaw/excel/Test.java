package com.tulane.mytablesaw.excel;

import java.io.InputStream;

public class Test {

    public static void main(String[] args) {
        ExcelAnalyze analyze = new ExcelAnalyze();
        InputStream inputStream = Test.class.getClassLoader().getResourceAsStream("测试.xlsx");
        analyze.exec(inputStream);
        TableSawManager tablesawManager = new TableSawManager(analyze.getXmlResolve());
        tablesawManager.init();
        System.out.println(tablesawManager.getTable().toString());
    }
}
