package org.bgerp.plugin.sec.auth.config;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.bgerp.dao.expression.Expression;
import org.bgerp.dao.expression.UserParamExpressionObject;
import org.bgerp.util.Log;

import ru.bgcrm.model.user.User;

/**
 * User update JEXL expression with context
 *
 * @author Shamil Vakhitov
 */
public class UpdateExpression {
    private static final Log log = Log.getLog();

    private final String expression;
    private final Map<String, Object> context;

    UpdateExpression(String expression, Map<String, Object> context) {
        this.expression = expression;
        this.context = context;
    }

    public void update(Connection con, User user) {
        log.debug("Running update expression for user {}: {}", user.getId(), expression);

        var context = new HashMap<>(this.context);
        new UserParamExpressionObject(con, user.getId()).toContext(context);

        new Expression(context).execute(expression);
    }
}
