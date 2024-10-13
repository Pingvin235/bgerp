package ru.bgcrm.dao.expression;

import ru.bgcrm.model.user.User;

public class UserExpressionObject {
    public static final String KEY = User.OBJECT_TYPE;
    // DO NOT CREATE KEY_SHORT = "u", will conflict to Utils
}
