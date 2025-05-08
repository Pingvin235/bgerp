package org.bgerp.dao.message.call;

import java.time.Duration;
import java.time.Instant;

public class OutCall {
    private static final Duration VALID = Duration.ofMinutes(30);

    private final Instant init = Instant.now();
    private final String number;
    private final int processId;

    OutCall(String number, int processId) {
        this.number = number;
        this.processId = processId;
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
}