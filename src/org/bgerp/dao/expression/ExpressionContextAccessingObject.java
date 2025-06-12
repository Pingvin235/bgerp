package org.bgerp.dao.expression;

import java.util.Map;

/**
 * JEXL context variable, having access to the context.
 *
 * @author Shamil Vakhitov
 */
public abstract class ExpressionContextAccessingObject implements ExpressionObject {
    protected Map<String, Object> context;

    @Override
    public void toContext(Map<String, Object> context) {
        this.context = context;
    }
}
