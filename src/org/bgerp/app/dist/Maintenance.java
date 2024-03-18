package org.bgerp.app.dist;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bgerp.app.servlet.user.LoginStat;
import org.bgerp.util.Log;

import ru.bgcrm.model.user.User;

/**
 * Maintenance application' state. Blocking logging in and logging off users before the app's restart.
 *
 * @author Shamil Vakhitov
 */
public class Maintenance {
    private static final Log log = Log.getLog();

    private static volatile Maintenance instance;

    /**
     * @return the current maintenance instance or {@code null}, if its missing.
     */
    public static Maintenance instance() {
        return instance;
    }

    /**
     * Starts maintenance.
     * @param user the user.
     * @param logoffDelay delay before logging users off.
     * @return the created maintenance instance.
     * @throws IllegalStateException if maintenance is already running.
     */
    public static Maintenance start(User user, Duration logoffDelay) {
        if (instance != null)
            throw new IllegalStateException("Maintenance is already running");
        log.info("Started by: {}, logoff delay: {}", user.getTitle(), logoffDelay);
        return instance = new Maintenance(user, logoffDelay);
    }

    /**
     * Cancels maintenance.
     * @return the cancelled maintenance instance.
     */
    public static Maintenance cancel() {
        log.info("Cancel");
        if (instance != null)
            instance.scheduledFuture.cancel(false);
        var result = instance;
        instance = null;
        return result;
    }

    // end of static part
    private final ScheduledFuture<?> scheduledFuture;
    private final User user;
    private final LocalDateTime startTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private final LocalDateTime logoffTime;

    private Maintenance(User user, Duration logoffDelay) {
        this.user = user;
        this.logoffTime = startTime.plus(logoffDelay);
        this.scheduledFuture = schedule(logoffDelay);
    }

    private ScheduledFuture<?> schedule(Duration logoffDelay) {
        return Executors.newScheduledThreadPool(1).schedule(() -> {
            log.info("Logging user sessions off");
            LoginStat.instance().loggedUsersWithSessions()
                .values().stream().flatMap(List::stream)
                .filter(session -> session.getUser().getId() != user.getId())
                .forEach(session -> {
                    session.getSession().invalidate();
                });
        }, logoffDelay.toSeconds(), TimeUnit.SECONDS);
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getLogoffTime() {
        return logoffTime;
    }
}
