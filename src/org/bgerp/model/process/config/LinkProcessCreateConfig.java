package org.bgerp.model.process.config;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ParamValueFunction;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class LinkProcessCreateConfig extends Config {
    private static final Log log = Log.getLog();

    private List<LinkProcessCreateConfigItem> itemList = new ArrayList<LinkProcessCreateConfigItem>();
    private Map<Integer, LinkProcessCreateConfigItem> itemMap = new HashMap<Integer, LinkProcessCreateConfigItem>();

    public LinkProcessCreateConfig(ParameterMap config) {
        super(null);

        for (Map.Entry<Integer, ParameterMap> me : config.subIndexed("processCreateLink.").entrySet()) {
            try {
                LinkProcessCreateConfigItem item = new LinkProcessCreateConfigItem(me.getKey(), me.getValue());
                itemMap.put(item.getId(), item);
                itemList.add(item);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public List<LinkProcessCreateConfigItem> getItemList(Connection con, Process process) {
        Map<String, Object> context = new HashMap<String, Object>(3);
        context.put(Process.OBJECT_TYPE, process);

        final String newKey = Process.OBJECT_TYPE + ParamValueFunction.PARAM_FUNCTION_SUFFIX;
        context.put(newKey, new ParamValueFunction(con, process.getId()));
        // TODO: Подумать, может такой способ вызова убрать, т.к. не единообразно
        // как-то.
        context.put("param", context.get(newKey));
        // TODO: Use DefaultProcessChangeListener#initExpression()
        Expression checker = new Expression(context);

        List<LinkProcessCreateConfigItem> result = new ArrayList<LinkProcessCreateConfigItem>();
        for (LinkProcessCreateConfigItem item : itemList) {
            if (Utils.isBlankString(item.getExpression()) || checker.check(item.getExpression())) {
                result.add(item);
            }
        }

        return result;
    }

    public LinkProcessCreateConfigItem getItem(int id) {
        return itemMap.get(id);
    }
}