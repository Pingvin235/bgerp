package org.bgerp.plugin.calendar.model.event;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;

public class EventTest {
    @Test
    public void testGetDuration() {
        var e = new Event()
            .withDate(LocalDate.now())
            .withMinuteFrom(10)
            .withMinuteTo(25);
        Assert.assertEquals(15, e.getDuration());

        /* e = new Event()
            .withDate(LocalDate.of(2021, 10, 19))
            .withMinuteFrom(20)
            .withDateTo(LocalDate.of(2021, 10, 20))
            .withMinuteTo(21);
        Assert.assertEquals(Duration.ofDays(1).toMinutes() + 1, e.getDuration().toMinutes()); */
    }
}
