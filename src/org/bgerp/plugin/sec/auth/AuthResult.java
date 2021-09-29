package org.bgerp.plugin.sec.auth;

import ru.bgcrm.model.user.User;

/**
 * Auth result.
 * 
 * @author Shamil Vakhitov
 */
public class AuthResult {
    private final boolean success;
    private final User user;
    private final Throwable exception;

    /**
     * Successful auth constructor. 
     * @param user
     */
    public AuthResult(User user) {
        this.success = true;
        this.user = user;
        this.exception = null;
    }

    /**
     * Auth error.
     * @param exception
     */
    public AuthResult(Throwable exception) {
        this.success = false;
        this.user = null;
        this.exception = exception;
    }

    /**
     * @return auth success.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return user in case of {@link #isSuccess()}.
     */
    public User getUser() {
        return user;
    }

    /**
     * @return exception in case of not {@link #isSuccess()}.
     */
    public Throwable getException() {
        return exception;
    }
}
