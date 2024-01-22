package org.bgerp.plugin.bil.invoice.pos;

import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.plugin.bil.invoice.model.Invoice;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Expression provider running JEXL script.
 *
 * @author Shamil Vakhitov
 */
public class ExpressionPositionProvider extends PositionProvider {
    private final String expression;

    protected ExpressionPositionProvider(ConfigMap config) {
        super(null);
        expression = config.get("expression", "");
    }

    @Override
    public void addPositions(ConnectionSet conSet, Invoice invoice) throws Exception {
        var slaveCon = conSet.getSlaveConnection();

        var process = new ProcessDAO(slaveCon).getProcess(invoice.getProcessId());
        var processParam = new ParamValueDAO(slaveCon).parameters(process);

        new Expression(Map.of(
            "invoice", invoice,
            "process", process,
            "processParam", processParam
        )).executeScript(expression);
    }
}
