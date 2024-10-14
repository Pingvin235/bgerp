package ru.bgcrm.dao.expression;

import java.sql.Connection;
import java.util.Map;

import ru.bgcrm.model.user.User;

public class UserParamExpressionObject extends ParamExpressionObject {
    private static final String KEY = User.OBJECT_TYPE + "Param";
    private static final String KEY_SHORT = "up";

    protected UserParamExpressionObject(Connection con, int userId) {
        super(con, userId);
    }

    @Override
    public void toContext(Map<String, Object> context) {
        context.put(KEY, this);
        context.put(KEY_SHORT, this);
    }
}
