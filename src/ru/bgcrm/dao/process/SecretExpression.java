package ru.bgcrm.dao.process;

import java.util.Map;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/*
 * Process accessing secret JEXL expression.
 * Used for dynamically connecting plugin functionality.
 * 
 * @author Shamil Vakhitov
 */
public class SecretExpression {
    public static final String PARAM_SECRET = "secret";

    private final String expression;

    public SecretExpression(String expression) {
        this.expression = expression;
    }

    /**
     * Checks if no secret set or passed secret correct.
     * @param process
     * @param form
     * @return
     */
    public boolean check(Process process, DynActionForm form) {
        var secret = get(process);
        return secret == null || secret.equals(form.getParam(PARAM_SECRET));
    }

    /**
     * Create or update existing access secret for process.
     * @param process
     * @return new secret or {@code null} if no secret protection configured.
     */
    private String update(Process process) {
        return (String) new Expression(Map.of(
            "key", Process.OBJECT_TYPE + "-" + process.getId(),
            "update", true
        )).executeScript(expression);
    }

    /**
     * Retrieves secret.
     * @param process
     * @return stored secret or {@code null} if no secret protection configured.
     */
    private String get(Process process) {
        if (Utils.isBlankString(expression))
            return null;

        return (String) new Expression(Map.of(
            "key", Process.OBJECT_TYPE + "-" + process.getId(),
            "update", false
        )).executeScript(expression);
    }

    /**
     * Query string including secret, if configured. Missing secret is generated.
     * @param process
     * @return '?' starting query string when secret has configured or empty string.
     */
    public String queryString(Process process) {
        if (Utils.isBlankString(expression))
            return "";

        var secret = get(process);
        if (secret == null)
            secret = update(process);

        return "?" + PARAM_SECRET + "=" + secret;
    }
}
