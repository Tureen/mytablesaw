package com.tulane.mytablesaw.table.index;

import com.tulane.mytablesaw.excel.model.XmlData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 标识行占用池, 用于匹配单元格标识行
 * @author Tulane
 */
public class TableIndexPools {
    private List<TableIndexPool> tableIndexPools;
    private Map<String, List<String>> tableTitleIndexs;

    public TableIndexPools(Map<String, List<String>> tableTitleIndexs) {
        this.tableIndexPools = new ArrayList<>();
        this.tableTitleIndexs = tableTitleIndexs;
        init();
    }

    public void init() {
        for (Map.Entry<String, List<String>> titleIndexsEntry : tableTitleIndexs.entrySet()) {
            tableIndexPools.add(new TableIndexPool(titleIndexsEntry.getKey(), titleIndexsEntry.getValue()));
        }
    }

    /**
     * 尝试匹配标识行
     * @param storesGroup 某一行的单元格集合
     * @param consumer 存在完全匹配的池子则调用函数, 传入匹配标识行的池子
     */
    public void tryMatchIndexs(List<XmlData> storesGroup, Consumer<List<TableIndexPool>> consumer) {
        // 遍历: 单元格匹配标识行
        for (XmlData xmlData : storesGroup) {
            tryMatchIndex(xmlData);
        }
        // 获取完全匹配标识行的池子
        List<TableIndexPool> matchTableIndex = getAllMatchTableIndex();
        // 若存在完全匹配的池子, 调用函数
        // (函数目前用于得到匹配标识行的单元格对象, 用于生成标识行对象)
        if (!matchTableIndex.isEmpty()) {
            consumer.accept(matchTableIndex);
        }
        // 清空池子, 用于下一行的匹配
        clearPools();
    }

    private void tryMatchIndex(XmlData xmlData) {
        for (TableIndexPool tableIndexPool : tableIndexPools) {
            tableIndexPool.tryMatchIndex(xmlData);
        }
    }

    private List<TableIndexPool> getAllMatchTableIndex() {
        List<TableIndexPool> matchTableIndexPools = new LinkedList<>();
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
        private Map<String, Queue<XmlData>> data;

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
                    integer = 0;
                }
                titleIndexsByCount.put(index, ++integer);
            }

            for (String indexStr : titleIndex) {
                data.put(indexStr, null);
            }
        }

        public boolean tryMatchIndex(XmlData xmlData){
            if(!data.containsKey(xmlData.getContext())){
                return false;
            }
            data.computeIfAbsent(xmlData.getContext(), x -> new LinkedList<>()).offer(xmlData);
            return true;
        }

        public boolean isMatchAllIndex(){
            Optional<Map.Entry<String, Queue<XmlData>>> optionalEntry = data.entrySet().stream().filter(x -> x.getValue() == null).findAny();
            if(optionalEntry.isPresent()){
                return false;
            }
            for (Map.Entry<String, Integer> entry : titleIndexsByCount.entrySet()) {
                String index = entry.getKey();
                Integer count = entry.getValue();
                Queue<XmlData> xmlDataQueue = data.get(index);
                if(xmlDataQueue == null || xmlDataQueue.size() < count){
                    return false;
                }
            }
            return true;
        }

        public void clearPool(){
            data.replaceAll((k, v) -> null);
        }

        public Optional<List<XmlData>> buildXmlDataForTableIndex(Set<XmlData> useXmlData){
            List<XmlData> xmlDatas = new LinkedList<>();
            for (String index : titleIndex) {
                while(true){
                    // 如果队列用完, 代表未检索到对应标识行的单元格, 此表格标识行无法构建
                    if(data.isEmpty()){
                        return Optional.empty();
                    }
                    XmlData xmlData = data.get(index).poll();
                    if(!useXmlData.contains(xmlData)){
                        useXmlData.add(xmlData);
                        xmlDatas.add(xmlData);
                        break;
                    }
                }
            }
            return Optional.of(xmlDatas);
        }

        public String getName() {
            return name;
        }
    }
}
