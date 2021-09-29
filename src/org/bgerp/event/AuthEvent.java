package org.bgerp.event;

import ru.bgcrm.event.Event;
import ru.bgcrm.model.user.User;

/**
 * User authorization event.
 * 
 * @author Shamil Vakhitov
 */
public class AuthEvent implements Event {
    private final String login;
    private final String password;
    private User user;
    private boolean processed;

    public AuthEvent(String login, String password, User user) {
        this.login = login;
        this.password = password;
        this.user = user;
    }

    /**
     * @return user login.
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return user password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the event was processed, newly created user available via {@link #getUser()}.
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Sets the event processed.
     * @param processed
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    /**
     * @return already existing local user with {@link #getLogin()}.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets newly created local user.
     * @param user
     */
    public void setUser(User user) {
        this.user = user;
    }
}
