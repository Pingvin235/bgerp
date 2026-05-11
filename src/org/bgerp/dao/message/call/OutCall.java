package org.bgerp.dao.message.call;

import java.time.Duration;
import java.time.Instant;

/**
 * An outgoing call initiated from process or customer parameters.
 * Respectively, {@link #processId} or {@link #customerId} should be set.
 *
 * @author Shamil Vakhitov
 */
public class OutCall {
    private static final Duration VALID = Duration.ofMinutes(30);

    private final Instant init = Instant.now();
    private final String number;
    private final int processId;
    private final int customerId;

    OutCall(String number, int processId, int customerId) {
        this.number = number;
        this.processId = processId;
        this.customerId = customerId;
    }

    /**
     * @return the out call can be finished
     */
    public boolean isValid() {
        return Duration.between(init, Instant.now()).compareTo(VALID) < 0;
    }

    public String getNumber() {
        return number;
    }

    public int getProcessId() {
        return processId;
    }

    public int getCustomerId() {
        return customerId;
    }
}