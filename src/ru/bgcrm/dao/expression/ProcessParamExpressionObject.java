package ru.bgcrm.dao.expression;

import java.sql.Connection;
import java.util.Map;

import ru.bgcrm.model.process.Process;

public class ProcessParamExpressionObject {
    public static final String KEY = Process.OBJECT_TYPE + "Param";
    public static final String KEY_SHORT = "pp";

    public static void context(Map<String, Object> context, Connection con, Process process) {
        ParamExpressionObject pp = new ParamExpressionObject(con, process.getId());
        context.put(KEY, pp);
        context.put(KEY_SHORT, pp);
    }
}
