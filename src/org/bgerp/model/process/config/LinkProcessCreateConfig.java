package org.bgerp.model.process.config;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

public class LinkProcessCreateConfig extends Config {
    private static final Log log = Log.getLog();

    private final List<LinkProcessCreateConfigItem> itemList = new ArrayList<>();
    private final Map<Integer, LinkProcessCreateConfigItem> itemMap = new HashMap<>();

    public LinkProcessCreateConfig(ConfigMap config) {
        super(null);

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("processCreateLink.").entrySet()) {
            try {
                LinkProcessCreateConfigItem item = new LinkProcessCreateConfigItem(me.getKey(), me.getValue());
                itemMap.put(item.getId(), item);
                itemList.add(item);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    /**
     * List of pairs with process creation item on first places and item enable state of seconds.
     * @param form context form.
     * @param con context connection.
     * @param process context process.
     * @return
     */
    public List<Pair<LinkProcessCreateConfigItem, Boolean>> getItemList(DynActionForm form, Connection con, Process process) {
        List<Pair<LinkProcessCreateConfigItem, Boolean>> result = new ArrayList<>();

        var context = Expression.context(new SingleConnectionSet(con), form, null, process);

        for (LinkProcessCreateConfigItem item : itemList)
            result.add(new Pair<>(item, isEnabled(context, item)));

        return result;
    }

    /**
     * Pair of an process creation item and it's enabling state.
     * @param form context form.
     * @param con context connection.
     * @param process context process.
     * @param id item ID.
     * @return
     */
    public Pair<LinkProcessCreateConfigItem, Boolean> getItem(DynActionForm form, Connection con, Process process, int id) {
        var item = itemMap.get(id);
        if (item == null)
            return null;

        var context = Expression.context(new SingleConnectionSet(con), form, null, process);

        return new Pair<>(item, isEnabled(context, item));
    }

    private boolean isEnabled(Map<String, Object> context, LinkProcessCreateConfigItem item) {
        return Utils.isBlankString(item.getExpression()) || new Expression(context).check(item.getExpression());
    }
}