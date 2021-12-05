package org.bgerp.util;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

/**
 * Date ant time converting utils.
 *
 * @author Shamil Vakhitov
 */
public class TimeConvert {
    /**
     * Converts date to month of year.
     * @param value
     * @return {@code null} or month, containing {@code value}.
     */
    public static final YearMonth toYearMonth(Date value) {
        return
            value != null ?
            YearMonth.from(value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) :
            null;
    }

    /**
     * Converts month to date of the first day.
     * @param value
     * @return {@code null} or date of the first day of month {@code value}.
     */
    public static final Date toDate(YearMonth value) {
        return
            value != null ?
            Date.from(value.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()) :
            null;
    }
}
