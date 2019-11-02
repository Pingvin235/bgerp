package ru.bgerp.plugin.blow.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.util.ParameterMap;

public class BoardConfig extends IdTitle {
    private final Queue queue;
    private final String columnHeadExpression;
    private final String cellExpression;
    private final String openUrl;
    private final List<BoardFilter> filters = new ArrayList<BoardConfig.BoardFilter>();
    
    BoardConfig(int id, ParameterMap config) {
        super(id, config.get("title"));
        this.queue = ProcessQueueCache.getQueue(config.getInt("queueId"));
        this.cellExpression = config.get(Expression.STRING_MAKE_EXPRESSION_CONFIG_KEY + "Cell", "process.getDescription()");
        this.columnHeadExpression = config.get(Expression.STRING_MAKE_EXPRESSION_CONFIG_KEY + "ColumnHead");
        for (Map.Entry<Integer, ParameterMap> me : config.subIndexed("filter.").entrySet())
            filters.add(new BoardFilter(me.getKey(), me.getValue()));
        this.openUrl = config.get("openUrl");
    }
    
    public Queue getQueue() {
        return queue;
    }
    
    public String getOpenUrl() {
        return openUrl;
    }

    /**
     * Возвращает вычисленные для фильтров значения. 
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
     * Возвращает HTML содержимое ячейки с применением JEXL выражения.
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
    
    public String getHeadContent() {
        // TODO
        return "";
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

}
