package com.tulane.mytablesaw.excel;

import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableSawManager {

    private final XmlResolve xmlResolve;
    private Table table;

    public TableSawManager(XmlResolve xmlResolve) {
        this.xmlResolve = xmlResolve;
    }

    public void init() {
        Map<Integer, List<XmlResolve.XmlData>> storesGroupByCol = xmlResolve.getStoresGroupByCol();

        table = Table.create("123");
        storesGroupByCol.forEach((row, xmlData) -> {
            List<String> collect = xmlData.stream().map(XmlResolve.XmlData::getContext).collect(Collectors.toList());

            StringColumn strings = StringColumn.create(
                    xmlData.get(0).getContext(),
                    collect);
            table.addColumns(strings);
        });
    }
}
