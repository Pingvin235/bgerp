package org.bgerp.dao.expression;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Strings;
import org.bgerp.util.text.CallsFinder;

import ru.bgcrm.model.process.Process;

/**
 * Expression object for accessing process parameters
 *
 * @author Shamil Vakhitov
 */
public class ProcessParamExpressionObject extends ParamExpressionObject {
    private static final String KEY = Process.OBJECT_TYPE + "Param";
    private static final String KEY_SHORT = "pp";

    public static final boolean called(String expression, int paramId) {
        boolean result = Strings.CS.containsAny(expression, KEY_SHORT + ".", KEY + ".");

        if (result && paramId > 0) {
            result = expression.contains(String.valueOf(paramId));

            if (result) {
                result = new CallsFinder(Set.of(KEY, KEY_SHORT), Set.of(
                    "addressValues(" + paramId,
                    "addressCityIds(" + paramId + ")",
                    "addressStreetIds(" + paramId + ")",
                    "addressQuarterIds(" + paramId + ")",
                    "addressAreaIds(" + paramId + ")",
                    "listValueIds(" + paramId + ")",
                    "val(" + paramId + ")",
                    "getValue(" + paramId + ")"
                )).find(expression);
            }
        }

        return result;
    }

    public ProcessParamExpressionObject(Connection con, int processId) {
        super(con, processId);
    }

    @Override
    public void toContext(Map<String, Object> context) {
        context.put(KEY, this);
        context.put(KEY_SHORT, this);
    }
}
