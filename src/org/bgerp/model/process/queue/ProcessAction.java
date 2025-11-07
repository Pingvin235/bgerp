package org.bgerp.model.process.queue;

import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.dao.expression.Expression;

import ru.bgcrm.util.Utils;

/**
 * Action shown in queues' {@code action} column
 *
 * @author Shamil Vakhitov
 */
public class ProcessAction {
    private final String title;
    private final String shortcut;
    private final String style;
    private final Set<Integer> statusIds;
    private final String doExpression;
    private final String commands;

    public ProcessAction(ConfigMap config) {
        title = config.get("title", "");
        shortcut = config.get("shortcut", "*");
        style = config.get("style", "");
        statusIds = Utils.toIntegerSet(config.get("statusIds"));
        doExpression = config.get(Expression.DO_EXPRESSION_CONFIG_KEY);
        commands = config.get("commands", "");
    }

    public String getTitle() {
        return title;
    }

    public String getShortcut() {
        return shortcut;
    }

    public String getStyle() {
        return style;
    }

    public Set<Integer> getStatusIds() {
        return statusIds;
    }

    public String getDoExpression() {
        return doExpression;
    }

    public String getCommands() {
        return commands;
    }
}
