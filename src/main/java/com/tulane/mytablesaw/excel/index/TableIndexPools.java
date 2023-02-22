package com.tulane.mytablesaw.excel.index;

import com.tulane.mytablesaw.excel.TableSawManager;
import com.tulane.mytablesaw.excel.XmlResolve;

import java.util.*;
import java.util.function.Consumer;

public class TableIndexPools {
    private List<TableIndexPool> tableIndexPools;
    private Map<String, List<String>> tableTitleIndexs;

    public TableIndexPools(Map<String, List<String>> tableTitleIndexs) {
        this.tableIndexPools = new ArrayList<>();
        this.tableTitleIndexs = tableTitleIndexs;
        init();
    }

    private void init() {
        for (Map.Entry<String, List<String>> titleIndexsEntry : tableTitleIndexs.entrySet()) {
            tableIndexPools.add(new TableIndexPool(titleIndexsEntry.getKey(), titleIndexsEntry.getValue()));
        }
    }

    public void tryMatchIndexs(List<XmlResolve.XmlData> storesGroup, Consumer<List<TableIndexPool>> consumer) {
        for (XmlResolve.XmlData xmlData : storesGroup) {
            tryMatchIndex(xmlData);
        }
        List<TableIndexPool> matchAllIndex = isMatchAllIndex();
        if (!matchAllIndex.isEmpty()) {
            consumer.accept(matchAllIndex);
        }
        clearPools();
    }

    private void tryMatchIndex(XmlResolve.XmlData xmlData) {
        for (TableIndexPool tableIndexPool : tableIndexPools) {
            tableIndexPool.tryMatchIndex(xmlData);
        }
    }

    private List<TableIndexPool> isMatchAllIndex() {
        List<TableIndexPool> matchTableIndexPools = new ArrayList<>();
        for (TableIndexPool tableIndexPool : tableIndexPools) {
            if (tableIndexPool.isMatchAllIndex()) {
                matchTableIndexPools.add(tableIndexPool);
            }
        }
        return matchTableIndexPools;
    }

    private void clearPools() {
        for (TableIndexPool tableIndexPool : tableIndexPools) {
            tableIndexPool.clearPool();
        }
    }

    public static class TableIndexPool {
        private String name;
        private List<String> titleIndex;
        private Map<String, Integer> titleIndexsByCount;
        private Map<String, Queue<XmlResolve.XmlData>> data;

        public TableIndexPool(String name, List<String> titleIndex) {
            this.name = name;
            this.titleIndex = titleIndex;
            this.titleIndexsByCount = new HashMap<>();
            this.data = new LinkedHashMap<>();
            init();
        }

        private void init(){
            for (String index : titleIndex) {
                Integer integer = titleIndexsByCount.get(index);
                if(integer == null){
                    integer = 1;
                }
                titleIndexsByCount.put(index, ++integer);
            }

            for (String indexStr : titleIndex) {
                data.put(indexStr, null);
            }
        }

        public void tryMatchIndex(XmlResolve.XmlData xmlData){
            if(!data.containsKey(xmlData.getContext())){
                return;
            }
            data.computeIfAbsent(xmlData.getContext(), x -> new LinkedList<>()).add(xmlData);
        }

        public boolean isMatchAllIndex(){
            Optional<Map.Entry<String, Queue<XmlResolve.XmlData>>> optionalEntry = data.entrySet().stream().filter(x -> x.getValue() == null).findAny();
            if(optionalEntry.isPresent()){
                return false;
            }
            for (Map.Entry<String, Integer> entry : titleIndexsByCount.entrySet()) {
                String index = entry.getKey();
                Integer count = entry.getValue();
                Queue<XmlResolve.XmlData> xmlDataQueue = data.get(index);
                if(xmlDataQueue == null || xmlDataQueue.size() < count){
                    return false;
                }
            }
            return true;
        }

        public void clearPool(){
            data.replaceAll((k, v) -> null);
        }

        public List<XmlResolve.XmlData> buildXmlDataForTableIndex(){
            List<XmlResolve.XmlData> xmlDatas = new LinkedList<>();
            for (String index : titleIndex) {
                XmlResolve.XmlData xmlData = data.get(index).poll();
                xmlDatas.add(xmlData);
            }
            return xmlDatas;
        }

        public String getName() {
            return name;
        }
    }
}
