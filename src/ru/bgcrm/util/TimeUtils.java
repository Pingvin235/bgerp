package ru.bgcrm.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.bgerp.util.TimeConvert;

import ru.bgcrm.model.PeriodSet;

public class TimeUtils {
    public static final String CONF_KEY_FORMAT_YMD = "format.ymd";
    public static final String CONF_KEY_FORMAT_YMDH = "format.ymdh";
    public static final String CONF_KEY_FORMAT_YMDHM = "format.ymdhm";
    public static final String CONF_KEY_FORMAT_YMDHMS = "format.ymdhms";

    public static final String PATTERN_DDMMYYYY = "dd.MM.yyyy";
    public static final String PATTERN_YYYYMMDD = "yyyy-MM-dd";
    public static final String PATTERN_YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DDMMYYYYHHMMSS = "dd.MM.yyyy HH:mm:ss";

    public static final String FORMAT_TYPE_YMD = "ymd";
    public static final String FORMAT_TYPE_YMDH = "ymdh";
    public static final String FORMAT_TYPE_YMDHM = "ymdhm";
    public static final String FORMAT_TYPE_YMDHMS = "ymdhms";

    /**
     * Date and time format string compatible with {@link SimpleDateFormat}
     * @param type type of date-time: {@link #FORMAT_TYPE_YMD},{@link #FORMAT_TYPE_YMDH}, {@link #FORMAT_TYPE_YMDHM}, {@link #FORMAT_TYPE_YMDHMS}
     * @return matching date format or {@code type} itself
     */
    public static String getTypeFormat(String type) {
        switch (type) {
            case FORMAT_TYPE_YMD:
                return Utils.getSystemProperty(CONF_KEY_FORMAT_YMD, "dd.MM.yyyy");
            case FORMAT_TYPE_YMDH:
                return Utils.getSystemProperty(CONF_KEY_FORMAT_YMDH, "dd.MM.yyyy HH");
            case FORMAT_TYPE_YMDHM:
                return Utils.getSystemProperty(CONF_KEY_FORMAT_YMDHM, "dd.MM.yyyy HH:mm");
            case FORMAT_TYPE_YMDHMS:
                return Utils.getSystemProperty(CONF_KEY_FORMAT_YMDHMS, "dd.MM.yyyy HH:mm:ss");
            default:
                return type;
        }
    }

    /**
     * Formats a string with the date by the given pattern
     * @param date the input date
     * @param patternType the date pattern: {@link #FORMAT_TYPE_YMD}, {@link #FORMAT_TYPE_YMDH}, {@link #FORMAT_TYPE_YMDHM}, {@link #FORMAT_TYPE_YMDHMS}
     * @return the string if the input date is not {@code null} and the pattern is correct, otherwise {@code null}
     */
    public static final String format(java.util.Date date, String patternType) {
        DateFormat format = new SimpleDateFormat(getTypeFormat(patternType));
        return date == null ? "" : format.format(date);
    }

    /**
     * Formats a date as 'yyyy-MM-dd' for substitution into an SQL query, already wrapped in quotes
     * @param date the input date
     * @return the formatted string
     */
    public static final String formatSqlDate(Date date) {
        return new SimpleDateFormat( "''yyyy-MM-dd''").format(date);
    }

    /**
     * Formats a duration object to a human-readable string like {@code 5h 15m}
     * @param duration the duration
     * @return the formatted string
     */
    public static final String format(Duration duration) {
        return duration.toString()
            .substring(2)
            .replaceAll("(\\d[HMS])(?!$)", "$1 ")
            .toLowerCase();
    }

    public static final String formatPeriod(java.util.Date dateFrom, java.util.Date dateTo, String patternType) {
        return format(dateFrom, patternType) + " - " + format(dateTo, patternType);
    }

    /**
     * Formats a period string
     * @param date1 the period start
     * @param date2 the period end
     * @return the string "dd.MM.yyyy-dd.MM.yyyy"; if one of the parameters is {@code null}, an empty string is printed instead of the corresponding date, e.g. "-dd.MM.yyyy", "dd.MM.yyyy-", "-"
     */
    public static final String formatPeriod(Date date1, Date date2) {
        return format(date1, FORMAT_TYPE_YMD) + "-" + format(date2, FORMAT_TYPE_YMD);
    }

    public static final Date parse(String date, String patternType) {
        DateFormat format = new SimpleDateFormat(getTypeFormat(patternType));
        try {
            return format.parse(date);
        } catch (Exception e) {
        }
        return null;
    }

    public static final Date parse(String date, String patternType, Date defaultValue) {
        DateFormat format = new SimpleDateFormat(getTypeFormat(patternType));
        try {
            return format.parse(date);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public static final void parsePeriod(String period, PeriodSet periodSet) {
        parsePeriod(period, FORMAT_TYPE_YMD, periodSet);
    }

    public static final void parsePeriod(String period, String patternType, PeriodSet periodSet) {
        String[] tokens = period.split("-");
        if (tokens.length >= 1) {
            periodSet.setDateFrom(TimeUtils.parse(tokens[0].trim(), patternType));
        }
        if (tokens.length > 1) {
            periodSet.setDateTo(TimeUtils.parse(tokens[1].trim(), patternType));
        }
    }

    // ########################################################################################
    // # Object conversion
    // ########################################################################################
    //
    /**
     * Converts a {@link Calendar} object to {@link java.util.Date}
     * @param calendar the input {@link Calendar} object
     * @return the {@link java.util.Date} object, or {@code null} if the input object is {@code null}
     */
    public static final java.util.Date convertCalendarToDate(Calendar calendar) {
        java.util.Date result = null;
        if (calendar != null) {
            result = calendar.getTime();
        }
        return result;
    }

    /**
     * Converts a {@link java.sql.Date} object to {@link java.util.Date}
     * @param date the input object
     * @return the {@link java.util.Date} object, or {@code null} if the input object is {@code null}
     */
    public static final java.util.Date convertSqlDateToDate(java.sql.Date date) {
        java.util.Date outDate = null;
        if (date != null) {
            outDate = new java.util.Date(date.getTime());
        }
        return outDate;
    }

    /**
     * Converts {@link java.util.Date} to {@link java.util.Calendar}
     * @param date the input object
     * @return the {@link Calendar}, if {@code date} is not {@code null}, otherwise {@code null}
     */
    public static final Calendar convertDateToCalendar(java.util.Date date) {
        Calendar result = null;
        if (date != null) {
            result = new GregorianCalendar();
            result.setTime(date);
        }
        return result;
    }

    /**
     * Converts {@link Timestamp} to {@link java.util.Calendar}
     * @param time the input object
     * @return the {@link Calendar}, if {@code time} is not {@code null}, otherwise {@code null}
     */
    public static final Calendar convertTimestampToCalendar(Timestamp time) {
        Calendar result = null;
        if (time != null) {
            result = new GregorianCalendar();
            result.setTimeInMillis(time.getTime());
        }
        return result;
    }

    /**
     * Converts a {@link java.util.Date} object to {@link java.sql.Date}
     * @param date the input object
     * @return the {@link java.sql.Date}, if {@code date} is not {@code null}, otherwise {@code null}
     */
    public static final java.sql.Date convertDateToSqlDate(Date date) {
        return date != null ? new java.sql.Date(date.getTime()) : null;
    }

    /**
     * Converts a {@link Calendar} object to {@link Timestamp}
     * @param calendar the input object
     * @return the {@link Timestamp}, if {@code calendar} is not {@code null}, otherwise {@code null}
     */
    public static final java.sql.Timestamp convertCalendarToTimestamp(Calendar calendar) {
        java.sql.Timestamp result = null;
        if (calendar != null) {
            result = new java.sql.Timestamp(calendar.getTimeInMillis());
        }
        return result;
    }

    /**
     * Converts {@code long} to {@link Timestamp}
     * @param millis the input value
     * @return the {@link Timestamp}
     */
    public static final java.sql.Timestamp convertLongToTimestamp(long millis) {
        return new java.sql.Timestamp(millis);
    }

    // ########################################################################################
    // # Difference calculations
    // ########################################################################################
    /**
     * 24h days difference between two dates. The method is time-proven, but
     * since Java 8 there is also available: {@code ChronoUnit.DAYS.between(Temporal, Temporal)}
     * @param dayFrom the first day
     * @param dayTo the second day
     * @return the difference in days
     */
    public static final int daysDelta(Date dayFrom, Date dayTo) {
        long time1 = dayFrom.getTime();
        long time2 = dayTo.getTime();
        int days1 = (int) (time1 / 86400000L);
        int days2 = (int) (time2 / 86400000L);
        return days2 - days1;
    }

    /**
     * Day of week starting from Monday = 1
     * @param date the date
     * @return the day of week position
     */
    public static final int getDayOfWeekPosition(Date date) {
        Calendar calendar = convertDateToCalendar(date);
        return (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7 + 1;
    }

    // ########################################################################################
    // # Checks
    // ########################################################################################
    /**
     * Checks {@code date1 < date2} (day precision)
     * @param date1 the first date
     * @param date2 the second date
     * @return {@code true} if {@code date1 < date2}, otherwise {@code false}
     */
    public static boolean dateBefore(final Date date1, final Date date2) {
        return dateBefore(convertDateToCalendar(date1), convertDateToCalendar(date2));
    }

    private static boolean dateBefore(final Calendar date1, final Calendar date2) {
        if (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)) {
            return date1.get(Calendar.DAY_OF_YEAR) < date2.get(Calendar.DAY_OF_YEAR);
        } else {
            return date1.get(Calendar.YEAR) < date2.get(Calendar.YEAR);
        }
    }

    /**
     * Checks {@code date1 == date2} (day precision)
     * @param date1 the first date
     * @param date2 the second date
     * @return {@code true} if {@code date1 == date2}, otherwise {@code false}
     */
    public static boolean dateEqual(Date date1, Date date2) {
        return dateEqual(convertDateToCalendar(date1), convertDateToCalendar(date2));
    }

    private static boolean dateEqual(Calendar date1, Calendar date2) {
        return (date1 == date2) || (date1 != null && date2 != null && (date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR))
                && (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)));
    }

    /**
     * Checks {@code date1 <= date2} (day precision)
     * @param date1 the first date
     * @param date2 the second date
     * @return {@code true} if {@code date1 <= date2}, otherwise {@code false}
     */
    public static boolean dateBeforeOrEq(Calendar date1, Calendar date2) {
        return dateBefore(date1, date2) || dateEqual(date1, date2);
    }

    /**
     * Checks {@code date1 <= date2} (day precision)
     * @param date1 the first date
     * @param date2 the second date
     * @return {@code true} if {@code date1 <= date2}, otherwise {@code false}
     */
    public static boolean dateBeforeOrEq(Date date1, Date date2) {
        return dateBeforeOrEq(convertDateToCalendar(date1), convertDateToCalendar(date2));
    }

    /**
     * Checks whether the checked date falls within the given period
     * @param checking the checked date
     * @param date1 the period start
     * @param date2 the period end
     * @return {@code true} if it falls within, {@code false} otherwise
     */
    public static final boolean dateInRange(Calendar checking, Calendar date1, Calendar date2) {
        return checking != null && (date1 == null || dateBeforeOrEq(date1, checking)) && (date2 == null || dateBeforeOrEq(checking, date2));
    }

    /**
     * Checks whether the checked date falls within the given period
     * @param checking the checked date
     * @param date1 the period start
     * @param date2 the period end
     * @return {@code true} if it falls within, {@code false} otherwise
     */
    public static final boolean dateInRange(Date checking, Date date1, Date date2) {
        return dateInRange(convertDateToCalendar(checking), convertDateToCalendar(date1), convertDateToCalendar(date2));
    }

    /**
     * Checks whether the checked period falls within the given one
     * @param checkingDate1 the checked period start
     * @param checkingDate2 the checked period end
     * @param date1 the given period start
     * @param date2 the given period end
     * @return {@code true} if it falls within, {@code false} otherwise
     */
    public static final boolean periodInRange(Calendar checkingDate1, Calendar checkingDate2, Calendar date1, Calendar date2) {
        boolean result = date1 == null || (checkingDate1 != null && dateBeforeOrEq(date1, checkingDate1));
        if (result) {
            result = date2 == null || (checkingDate2 != null && dateBeforeOrEq(checkingDate2, date2));
        }
        return result;
    }

    /**
    * Checks the intersection of two date intervals
    *
    * @param date1 the left bound of the first interval
    * @param date2 the right bound of the first interval
    * @param dateFrom the left bound of the second interval
    * @param dateTo the right bound of the second interval
    * @return {@code true} if the intervals intersect, or one of them has both bounds {@code null}
    */
    public static boolean checkDateIntervalsIntersection(Calendar date1, Calendar date2, Calendar dateFrom, Calendar dateTo) {
        // in both cases, an attempt to find an intersection with the entire time axis
        if (date1 == null && date2 == null || dateFrom == null && dateTo == null) {
            return true;
        }

        return (date1 == null || dateTo == null || dateBefore(date1, dateTo)) && (dateFrom == null || date2 == null || dateBefore(dateFrom, date2));
    }

    // ########################################################################################
    // # Object modifications
    // ########################################################################################
    /**
     * Sets the date to the last day of the month
     * @param date the input date
     */
    public static final void moveToEndOfMonth(Calendar date) {
        if (date != null) {
            date.add(Calendar.MONTH, 1);
            date.set(Calendar.DAY_OF_MONTH, 1);
            date.add(Calendar.HOUR_OF_DAY, -1);
        }
    }

    /**
     * Increases date by 1 day and resets the time to 00:00:00.0
     * @param date the input date
     */
    public static final void moveToStartNextDay(Calendar date) {
        if (date != null) {
            date.add(Calendar.DAY_OF_YEAR, 1);
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
        }
    }

    /**
     * Resets the time to the start of the day
     * @param time the input date
     * @return the reset date (hours=minutes=seconds=millis=0)
     */
    @Deprecated
    public static final Calendar clear_HOUR_MIN_MIL_SEC(Calendar time) {
        clear_MIN_MIL_SEC(time);
        time.set(Calendar.HOUR_OF_DAY, 0);
        return time;
    }

    private static final Calendar clear_MIN_MIL_SEC(Calendar time) {
        time.set(Calendar.MILLISECOND, 0);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MINUTE, 0);
        return time;
    }

    /**
     * Resets the time to the start of the day
     * @param time the input date
     * @return the reset date (hours=minutes=seconds=millis=0)
     */
    @Deprecated
    public static final Date clear_HOUR_MIN_MIL_SEC(Date time) {
        if (time != null) {
            return clear_HOUR_MIN_MIL_SEC(convertDateToCalendar(time)).getTime();
        }
        return time;
    }

    /**
     * Last day of a month
     * @param date any day of the month
     * @return {@code null} if {@code date} is {@code null}, else the last day of the month
     */
    public static final Date getEndMonth(Date date) {
        if (date == null)
            return null;
        return TimeConvert.toDate(TimeConvert.toYearMonth(date).atEndOfMonth());
    }

    /**
     * The next day from the date
     * @param date the date
     * @return the next day
     */
    public static final Calendar getNextDay(Calendar date) {
        date = (Calendar) date.clone();
        moveToStartNextDay(date);
        return date;
    }

    /**
     * The previous day from the date
     * @param date the date
     * @return the previous day
     */
    public static final Calendar getPrevDay(Calendar date) {
        date = (Calendar) date.clone();
        date.add(Calendar.DAY_OF_YEAR, -1);
        return date;
    }

    /**
     * The next day from the date
     * @param date the date
     * @return the next day
     */
    public static final Date getNextDay(Date date) {
        return convertCalendarToDate(getNextDay(convertDateToCalendar(date)));
    }

    /**
     * The previous day from the date
     * @param date the date
     * @return the previous day
     */
    public static final Date getPrevDay(Date date) {
        return convertCalendarToDate(getPrevDay(convertDateToCalendar(date)));
    }

    /**
     * @return first day in the previous month from the current time
     */
    public static final Date getPrevMonth() {
        return Date.from(YearMonth.now().minusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Returns the abbreviated day-of-week name ("пн" - Monday, "вт" - Tuesday, etc.)
     * @param date the date used to determine the day of week
     * @return the abbreviated day-of-week name
     */
    @Deprecated
    public static String getShortDateName(Date date) {
        // TODO: Use Java API
        final String[] shortDayNames = { "пн", "вт", "ср", "чт", "пт", "сб", "вс" };
        return shortDayNames[getDayOfWeekPosition(date) - 1];
    }

    /**
     * Adjusts a {@link Date} object so that the same local time results in a different timezone
     * @param time the input {@link Date} object with some time for timezone {@code fromTz}
     * @param fromTz the source timezone
     * @param toTz the target timezone
     * @return the adjusted date
     */
    public static Date timezoneChange(Date time, TimeZone fromTz, TimeZone toTz) {
        if (time == null) {
            return null;
        }

        return new Date(time.getTime() - toTz.getRawOffset() + fromTz.getRawOffset());
    }
}
