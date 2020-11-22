package ru.bgerp.plugin.blow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class BoardConfig extends IdTitle {
    private final int queueId;
    private final String cellExpression;
    private final String openUrl;
    private final List<BoardFilter> filters = new ArrayList<>();
    private final List<ItemComparator> comparators;
    private final Set<Integer> executorGroupIds;
    
    BoardConfig(int id, ParameterMap config) {
        super(id, config.get("title"));
        this.queueId = config.getInt("queueId");
        this.cellExpression = config.get(Expression.STRING_MAKE_EXPRESSION_CONFIG_KEY + "Cell", "process.getDescription()");
        for (Map.Entry<Integer, ParameterMap> me : config.subIndexed("filter.").entrySet())
            filters.add(new BoardFilter(me.getKey(), me.getValue()));
        this.openUrl = config.get("openUrl");
        this.comparators = parseComparators(config.get("sort", 
            Utils.toString(List.of(ItemComparator.HAS_EXECUTOR, ItemComparator.HAS_CHILDREN, ItemComparator.PRIORITY, ItemComparator.STATUS_POS))));
        this.executorGroupIds = Utils.toIntegerSet(config.get("executor.groupIds"));
    }

    private List<ItemComparator> parseComparators(String config) {
        var result = new ArrayList<ItemComparator>();
        for (String pair : Utils.toList(config, ";,")) {
            String[] tokens = pair.split(":");
            result.add(new ItemComparator(tokens[0], tokens.length > 1 ? tokens[1] : null));
        }
        return Collections.unmodifiableList(result);
    }
    
    public Queue getQueue() {
        return ProcessQueueCache.getQueue(queueId);
    }
    
    public String getOpenUrl() {
        return openUrl;
    }

    /**
     * Returns calculated filters values. 
     * @param items
     * @return
     */
    public List<Pair<BoardFilter, String>> getFilterValues(Iterable<Item> items) {
        List<Pair<BoardFilter, String>> result = new ArrayList<>(filters.size());
        for (BoardFilter filter : filters) {
            Map<String, Object> context = new HashMap<>();
            context.put("items", items);
            context.put("filter", filter);
            context.putAll(SetRequestParamsFilter.getContextVariables(null));
            String text = new Expression(context).getString(filter.getStringExpression());
            
            result.add(new Pair<>(filter, text));
        }        
        return result;
    }

    /**
     * Builds HTML cell's content using JEXL expression.
     * @param item
     * @return
     */
    public String getCellContent(Item item) {
        Map<String, Object> context = new HashMap<>();
        context.put(Process.OBJECT_TYPE, item.getProcess());
        context.put("params", item.getParams());
        context.putAll(SetRequestParamsFilter.getContextVariables(null));
        return new Expression(context).getString(cellExpression);
    }

    public Set<Integer> getExecutorGroupIds() {
        return executorGroupIds;
    }

    public static class BoardFilter extends IdTitle {
        private final String stringExpression;
        private final String color;
        
        private BoardFilter(int id, ParameterMap config) {
            super(id, config.get("title", "NONAME"));
            this.stringExpression = config.get(Expression.STRING_MAKE_EXPRESSION_CONFIG_KEY);
            this.color = config.get("color");
        }
        
        public String getColor() {
            return color;
        }
        
        public String getStringExpression() {
            return stringExpression;
        }
    }

    public Comparator<Item> getItemComparator() {
        return (o1, o2) -> {
            for (var c : comparators) {
                var result = c.compare(o1, o2);
                if (result != 0)
                    return result;
            }
            return 0;
        };
    }
}
