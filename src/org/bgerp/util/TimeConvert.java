package org.bgerp.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

/**
 * Date ant time converting utils.
 *
 * @author Shamil Vakhitov
 */
public class TimeConvert {
    private static final LocalTime ZERO_TIME = LocalTime.of(0, 0);

    /**
     * Converts date to month of year.
     * @param value
     * @return {@code null} or month, containing {@code value}.
     */
    public static final YearMonth toYearMonth(Date value) {
        return
            value != null ?
            YearMonth.from(Instant.ofEpochMilli(value.getTime()).atZone(ZoneId.systemDefault()).toLocalDate()) :
            null;
    }

    /**
     * Converts the first day of a month to date.
     * @param value the month.
     * @return {@code null} or date of the first day of the month {@code value}.
     */
    public static final Date toDate(YearMonth value) {
        return
            value != null ?
            Date.from(value.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()) :
            null;
    }

    /**
     * Converts beginning of a day to date.
     * @param value the day.
     * @return {@code null} or date of the beginning of the day {@code value}.
     */
    public static final Date toDate(LocalDate value) {
        return
            value != null ?
            Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant()) :
            null;
    }

    /**
     * Converts date to local date.
     * @param value
     * @return {@code null} or converted value.
     */
    public static final LocalDate toLocalDate(Date value) {
        if (value == null)
            return null;

        return Instant.ofEpochMilli(value.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Converts date to SQL timestamp.
     * @param value
     * @return {@code null} or converted value.
     */
    public static final java.sql.Timestamp toTimestamp(Date value) {
        if (value == null)
            return null;
        return new java.sql.Timestamp(value.getTime());
    }

    /**
     * Converts date and time to instant.
     * @param value
     * @return {@code null} or converted value.
     */
    public static final Instant toInstant(LocalDateTime value) {
        return
            value == null ?
            null :
            value.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Converts a local time to duration between 0 and the time
     * @param value
     * @return
     */
    public static Duration toDuration(LocalTime value) {
        return Duration.between(ZERO_TIME, value);
    }

    /**
     * Converts a duration to time after adding the duration to 0
     * @param duration
     * @return
     */
    public static LocalTime toLocalTime(Duration duration) {
        return ZERO_TIME.plus(duration);
    }
}
