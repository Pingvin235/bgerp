package ru.bgcrm.dao.expression;

/**
 * Функция JEXL, имеющаая доступ к контексту.
 * Позволяет сократить запись функции.
 */
public class ExpressionBasedFunction {
	protected Expression expression;

	public void setExpression(Expression expression) {
		this.expression = expression;
	}
}
