package ru.bgcrm.dao.expression;

import java.util.Map;

import ru.bgcrm.model.user.User;

public class UserExpressionObject implements ExpressionObject {
    public static final String KEY = User.OBJECT_TYPE;
    // DO NOT CREATE KEY_SHORT = "u", will conflict to Utils

    private final User user;

    public UserExpressionObject(User user) {
        this.user = user;
    }

    @Override
    public void toContext(Map<String, Object> context) {
       context.put(KEY, user);
    }
}
