package org.bgerp.plugin.sec.auth;

import java.sql.Connection;

import org.bgerp.plugin.sec.auth.config.UpdateExpression;

import ru.bgcrm.model.user.User;

/**
 * Auth result
 *
 * @author Shamil Vakhitov
 */
public class AuthResult {
    private final boolean success;
    private final User user;
    private final UpdateExpression updateExpression;
    private final Throwable exception;

    /**
     * Successful auth constructor
     * @param user
     * @param updateExpression
     */
    public AuthResult(User user, UpdateExpression updateExpression) {
        this.success = true;
        this.user = user;
        this.updateExpression = updateExpression;
        this.exception = null;
    }

    /**
     * Auth error
     * @param exception
     */
    public AuthResult(Throwable exception) {
        this.success = false;
        this.user = null;
        this.updateExpression = null;
        this.exception = exception;
    }

    /**
     * @return auth success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return user in case of {@link #isSuccess()}
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the result has a JEXL update expression
     */
    public boolean hasUpdateExpression() {
        return updateExpression != null;
    }

    /**
     * Execute JEXL update expression in case of {@link #isSuccess()} and {@link #hasUpdateExpression()}
     * @param con DB connection
     * @param user the user
     */
    public void doUpdateExpression(Connection con, User user) {
        updateExpression.update(con, user);
    }

    /**
     * @return exception in case of not {@link #isSuccess()}
     */
    public Throwable getException() {
        return exception;
    }
}
