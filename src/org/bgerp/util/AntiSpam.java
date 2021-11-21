package org.bgerp.util;

import java.time.Duration;

import ru.bgcrm.util.Utils;

/**
 * Simple IP based anti-spam.
 * Prevents sequential requests from single IP address more often than some timeout.
 *
 * @author Shamil Vakhitov
 */
public class AntiSpam {
    /** Maximum timeout in milliseconds. */
    private final long timeout;

    private volatile String lastIp;
    private volatile long lastTime;

    /**
     * Constructor.
     * @param timeout - timeout in milliseconds.
     */
    public AntiSpam(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Constructor.
     * @param timeout
     */
    public AntiSpam(Duration timeout) {
        this(timeout.toMillis());
    }

    /**
     * How many milliseconds wait until next request from the IP.
     * @param ip IP address.
     * @return
     */
    public long getWaitTimeout(String ip) {
        if (Utils.isBlankString(ip))
            return timeout;

        if (!ip.equals(lastIp))
            return reset(ip);

        long wentTime = System.currentTimeMillis() - lastTime;
        if (wentTime > timeout)
            return reset(ip);
        else
            return timeout - wentTime;
    }

    private long reset(String ip) {
        this.lastIp = ip;
        this.lastTime = System.currentTimeMillis();
        return 0L;
    }
}
