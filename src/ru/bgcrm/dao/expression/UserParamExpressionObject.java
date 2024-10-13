package ru.bgcrm.dao.expression;

import java.sql.Connection;
import java.util.Map;

import ru.bgcrm.model.user.User;

public class UserParamExpressionObject {
    public static final String KEY = User.OBJECT_TYPE + "Param";
    public static final String KEY_SHORT = "up";

    public static void context(Map<String, Object> context, Connection con, User user) {
        ParamExpressionObject pp = new ParamExpressionObject(con, user.getId());
        context.put(KEY, pp);
        context.put(KEY_SHORT, pp);
    }
}
