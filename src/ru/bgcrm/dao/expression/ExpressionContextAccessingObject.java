package ru.bgcrm.dao.expression;

/**
 * JEXL context variable, having access to the context.
 *
 * @author Shamil Vakhitov
 */
public abstract class ExpressionContextAccessingObject implements ExpressionObject {
    protected Expression expression;

    public void setExpression(Expression expression) {
        this.expression = expression;
    }
}
