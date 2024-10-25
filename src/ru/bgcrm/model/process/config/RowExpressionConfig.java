package ru.bgcrm.model.process.config;

import java.util.HashMap;
import java.util.Map;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.util.Utils;

public class RowExpressionConfig extends Config {
    private Map<String, Expressions> mediaExpressions = new HashMap<>();

    public RowExpressionConfig(ConfigMap config) {
        super(null);

        for (ConfigMap conf : config.subIndexed("rowConfig.").values()) {
            String media = conf.get("media");
            Expressions expressions = new Expressions(conf.getSok(Expression.EXPRESSION_CONFIG_KEY + "Head", "stringExpressionHead"),
                    conf.getSok(Expression.EXPRESSION_CONFIG_KEY + "Row", "stringExpressionRow"));

            if (Utils.isBlankString(media) || Utils.isBlankString(expressions.headRowExpression) || Utils.isBlankString(expressions.rowExpression))
                continue;

            mediaExpressions.put(media, expressions);
        }
    }

    public String getHead(String media, Map<String, Object> data) {
        String result = "";

        Expressions expressions = mediaExpressions.get(media);
        if (expressions != null)
            result = new Expression(data).executeGetString(expressions.headRowExpression);

        return result;
    }

    public String getRow(String media, Map<String, Object> data) {
        String result = "";

        Expressions expressions = mediaExpressions.get(media);
        if (expressions != null)
            result = new Expression(data).executeGetString(expressions.rowExpression);

        return result;
    }

    private static class Expressions {
        private String headRowExpression;
        private String rowExpression;

        private Expressions(String headRowExpression, String rowExpression) {
            this.headRowExpression = headRowExpression;
            this.rowExpression = rowExpression;
        }
    }
}
