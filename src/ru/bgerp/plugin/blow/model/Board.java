package ru.bgerp.plugin.blow.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgerp.util.Log;

/**
 * Blow board.
 * 
 * @author Shamil Vakhitov
 */
public class Board {
    private static final Log log = Log.getLog();
    
    private boolean userMode = true;
    
    public boolean isUserMode() {
        return userMode;
    }
    
    // корневой элемент
    private final Item root = new Item(this, null);
    
    private final BoardConfig config;
    
    // очереди задач по исполнителям, 0 - общая
    private final Map<Integer, List<Item>> queues = new HashMap<>();
    // все элементы
    private final Iterable<Item> items;
    // максимально занятый из юнитов в каждой из очередей
    private final int lastIndex;
    
    public Board(BoardConfig config, List<Pair<Process, Map<String, Object>>> processes, Collection<CommonObjectLink> links) {
        log.debug("## Build board, processes: %s; links: %s", processes.size(), links.size());
        this.config = config;
        this.items = buildTree(processes, links);
        this.lastIndex = addItem(root, -1, -1);
    }

    private Iterable<Item> buildTree(List<Pair<Process, Map<String, Object>>> processes, Collection<CommonObjectLink> links) {
        List<Item> itemList = processes.stream().map(p -> new Item(this, p)).collect(Collectors.toList());
        Map<Integer, Item> itemMap = itemList.stream().collect(
                Collectors.toMap(item -> item.getProcess().getId(), 
                        Function.identity()));

        for (Item item : itemList) {
            Item parent = root;
            // поиск ссылок на данный процесс
            for (CommonObjectLink link : links)
                if (item.getProcess().getId() == link.getLinkedObjectId() && link.getLinkedObjectType().equals(Process.LINK_TYPE_MADE)) {
                    parent = itemMap.get(link.getObjectId());
                    break;
                }
            parent.addChild(item);
        }        
        return itemList;
    }
    
    public BoardConfig getConfig() {
        return config;
    }
    
    public Item getRoot() {
        return root;
    }
    
    public Iterable<Item> getItems() {
        return items;
    }
    
    private int addItem(Item item, int lastIndex, int maxLastIndex) {
        if (item.getProcess() != null) {
            List<Item> queueCommon = queues.computeIfAbsent(0, id -> new ArrayList<>());
            List<Item> queue =  queues.computeIfAbsent(item.getExecutorId(), id -> new ArrayList<>());
            
            log.debug("Adding: " + item.getProcess().getId() + "; lastIndex: " + lastIndex + "; maxLastIndex: " + maxLastIndex 
                    + "; executorId: " + item.getExecutorId() + "; queueSize: " + queue.size() + "; queueCommonSize: " + queueCommon.size());
            
            if (item.getExecutorId() == 0) {
                lastIndex = maxLastIndex + 1;
                
                while (queue.size() < lastIndex)
                    queue.add(null);                
                queue.add(item);
            } else {
                while (queue.size() <= lastIndex || queue.size() < queueCommon.size())
                    queue.add(null);
                queue.add(item);
                
                lastIndex = queue.size() - 1;
            }
            
            log.debug("Added: " + item.getProcess().getId() + "; lastIndex: " + lastIndex);
        }
        
        if (maxLastIndex < lastIndex) {
            maxLastIndex = lastIndex;
            log.debug("maxLastIndex: " + maxLastIndex);
        }
        
        for (Item child : item.getChildren())
            maxLastIndex = addItem(child, lastIndex, maxLastIndex);
        
        return maxLastIndex;
    }
    
    /**
     * Возвращает максимальный номер строки среди очередей исполнителей.
     * @return
     */
    public int getLastIndex() {
        return lastIndex;
    }
    
    /**
     * Возвращает столбцы с процессами по исполнителям либо общую очередь. 
     * @return
     */
    public Map<Integer, List<Item>> getQueues() {
        return queues;
    }
    
    /**
     * Возвращает количество процессов на исполнителе.
     * @param executorId
     * @return
     */
    public int getExecutorItemCount(int executorId) {
        int cnt = 0;
        for (Item item : items)
            if (item.hasExecutor(executorId))
                cnt++;
        return cnt;
    }
    
    /**
     * Возвращает сортированный список исполнителей.
     * @return
     */
    public List<IdTitle> getExecutors() {
        List<IdTitle> executorColumns = new ArrayList<>(queues.size());
        if (isUserMode()) {
            for (User user : UserCache.getUserList()) {
                if (!queues.containsKey(user.getId())) 
                    continue;
                executorColumns.add(user);
            }
        } 
        return executorColumns;
    }
    
}