package com.tulane.mytablesaw.excel;

import java.io.File;

public class Test {

    public static void main(String[] args) {
        ExcelAnalyze analyze = new ExcelAnalyze();
        analyze.exec(new File("/Users/Tulane/tmp/excel/测试.xlsx"));
        TableSawManager tablesawManager = new TableSawManager(analyze.getXmlResolve());
        tablesawManager.init();
    }
}
