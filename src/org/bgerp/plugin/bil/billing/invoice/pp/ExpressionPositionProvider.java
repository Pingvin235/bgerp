package org.bgerp.plugin.bil.billing.invoice.pp;

import java.util.Map;

import org.bgerp.plugin.bil.billing.invoice.model.Invoice;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.util.ParameterMap;

/**
 * Expression provider running JEXL script.
 *
 * @author Shamil Vakhitov
 */
public class ExpressionPositionProvider extends PositionProvider {
    private final String expression;

    protected ExpressionPositionProvider(ParameterMap config) {
        super(null);
        expression = config.get("expression", "");
    }

    @Override
    public void addPositions(Invoice invoice) {
        new Expression(Map.of("invoice", invoice)).executeScript(expression);
    }
}
