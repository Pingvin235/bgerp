package org.bgerp.plugin.pln.blow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.ProcessQueueCache;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ProcessExpressionObject;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.util.Utils;

public class BoardConfig extends IdTitle {
    private final int queueId;
    private final String cellExpression;
    private final String openUrl;
    private final List<BoardFilter> filters = new ArrayList<>();
    private final List<ItemComparator> comparators;
    private final Set<Integer> executorGroupIds;
    private final Set<Integer> executorRoleIds;

    BoardConfig(int id, ConfigMap config) throws BGMessageException {
        super(id, config.get("title"));
        this.queueId = config.getInt("queueId");
        this.cellExpression = config.getSok("process.getDescription()", false, Expression.EXPRESSION_CONFIG_KEY + "Cell", "stringExpressionCell");
        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("filter.").entrySet())
            filters.add(new BoardFilter(me.getKey(), me.getValue()));
        this.openUrl = config.get("openUrl");
        this.comparators = parseComparators(config.get("sort",
            Utils.toString(List.of(ItemComparator.PRIORITY, ItemComparator.HAS_EXECUTOR, ItemComparator.STATUS_POS, ItemComparator.HAS_CHILDREN))));
        this.executorGroupIds = Utils.toIntegerSet(config.getSok("executor.groups", "executor.groupIds"));
        this.executorRoleIds = Utils.toIntegerSet(config.get("executor.roles", "0"));
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
            String text = new Expression(context).executeGetString(filter.getStringExpression());

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
        new ProcessExpressionObject(item.getProcess()).toContext(context);
        context.put("params", item.getParams());
        context.putAll(SetRequestParamsFilter.getContextVariables(null));
        return new Expression(context).executeGetString(cellExpression);
    }

    public Set<Integer> getExecutorGroupIds() {
        return executorGroupIds;
    }

    public Set<Integer> getExecutorRoleIds() {
        return executorRoleIds;
    }

    public static class BoardFilter extends IdTitle {
        private final String stringExpression;
        private final String color;

        private BoardFilter(int id, ConfigMap config) {
            super(id, config.get("title", "NONAME"));
            this.stringExpression = config.getSok(Expression.EXPRESSION_CONFIG_KEY, "stringExpression");
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
