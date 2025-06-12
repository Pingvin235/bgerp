package org.bgerp.dao.expression;

import java.sql.Connection;
import java.util.Map;

import ru.bgcrm.model.process.Process;

public class ProcessParamExpressionObject extends ParamExpressionObject {
    private static final String KEY = Process.OBJECT_TYPE + "Param";
    private static final String KEY_SHORT = "pp";

    public ProcessParamExpressionObject(Connection con, int processId) {
        super(con, processId);
    }

    @Override
    public void toContext(Map<String, Object> context) {
        context.put(KEY, this);
        context.put(KEY_SHORT, this);
    }
}
