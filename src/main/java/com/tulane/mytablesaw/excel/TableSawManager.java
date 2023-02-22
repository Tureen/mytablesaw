package com.tulane.mytablesaw.excel;

import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.*;
import java.util.stream.Collectors;

public class TableSawManager {

    private final XmlResolve xmlResolve;
    private final List<Table> tables;

    public TableSawManager(XmlResolve xmlResolve) {
        this.xmlResolve = xmlResolve;
        this.tables = new LinkedList<>();
    }

    public void init() {
        // 切分表: 空行切分, 列数不等切分
        XmlDatasGroup xmlDatasGroup = splitStores(XmlResolve.getStoresGroupByRow(xmlResolve.getStores()));

        xmlDatasGroup.getData().forEach((tablename, xmlDatas) -> {
            Map<Integer, List<XmlResolve.XmlData>> storesGroupByCol = XmlResolve.getStoresGroupByCol(xmlDatas);

            Table table = Table.create(UUID.randomUUID().toString().replaceAll("-", ""));
            tables.add(table);
            storesGroupByCol.forEach((col, xmlData) -> {
                StringColumn strings = StringColumn.create(
                        xmlData.get(0).getContext(),
                        xmlData.stream()
                                .filter(x -> x.getXmlRow().getRow() != 0)
                                .map(XmlResolve.XmlData::getContext)
                                .collect(Collectors.toList()));
                table.addColumns(strings);
            });
        });

    }

    private XmlDatasGroup splitStores(Map<Integer, List<XmlResolve.XmlData>> storesGroupByRow) {
        LinkedList<List<XmlResolve.XmlData>> xmlDatasGroup = new LinkedList<>();
        xmlDatasGroup.add(new LinkedList<>());

        int lastRow = -1;
        int lastColCount = -1;
        for (Map.Entry<Integer, List<XmlResolve.XmlData>> entry : storesGroupByRow.entrySet()) {
            int currentRow = entry.getKey();
            List<XmlResolve.XmlData> xmlDatas = entry.getValue();
            if(lastRow != -1 && (((currentRow - lastRow) > 1) || (xmlDatas.size() != lastColCount))){
                xmlDatasGroup.add(new LinkedList<>());
            }
            List<XmlResolve.XmlData> lastXmlDatas = xmlDatasGroup.getLast();
            lastXmlDatas.addAll(xmlDatas);

            lastRow = currentRow;
            lastColCount = xmlDatas.size();
        }

        XmlDatasGroup datasGroup = new XmlDatasGroup();
        datasGroup.init(xmlDatasGroup);
        return datasGroup;
    }

    public List<Table> getTables() {
        return tables;
    }

    static class XmlDatasGroup {

        private final Map<String, List<XmlResolve.XmlData>> data;

        public XmlDatasGroup() {
            data = new LinkedHashMap<>();
        }

        public void init(List<List<XmlResolve.XmlData>> xmlDatasGroup){
            xmlDatasGroup.forEach(xmlDatas ->
                    data.put(UUID.randomUUID().toString().replaceAll("-", ""), xmlDatas));
        }

        public Map<String, List<XmlResolve.XmlData>> getData() {
            return data;
        }
    }
}
