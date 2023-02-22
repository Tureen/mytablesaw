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
        storesGroupByCol.forEach((col, xmlData) -> {
            StringColumn strings = StringColumn.create(
                    xmlData.get(0).getContext(),
                    xmlData.stream()
                            .filter(x -> x.getXmlRow().getRow() != 0)
                            .map(XmlResolve.XmlData::getContext)
                            .collect(Collectors.toList()));
            table.addColumns(strings);
        });
    }

    public Table getTable() {
        return table;
    }
}
