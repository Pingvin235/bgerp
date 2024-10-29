package ru.bgcrm.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
     * Date and time format string compatible with {@link SimpleDateFormat}.
     * @param type type of date-time: {@link #FORMAT_TYPE_YMD},{@link #FORMAT_TYPE_YMDH}, {@link #FORMAT_TYPE_YMDHM}, {@link #FORMAT_TYPE_YMDHMS}.
     * @return matching date format or {@param type} itself.
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
     * Day of week starting from Monday = 1
     * @param date
     * @return
     */
    public static final int getDayOfWeekPosition(Date date) {
        Calendar calendar = convertDateToCalendar(date);
        return (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7 + 1;
    }


    /**
     * Формирует строку с датой по заданному шаблону.
     * @param date исходная дата
     * @param patternType шаблон даты {@link #FORMAT_TYPE_YMD},{@link #FORMAT_TYPE_YMDH}, {@link #FORMAT_TYPE_YMDHM}, {@link #FORMAT_TYPE_YMDHMS}.
     * @return строка если исходная дата != null и шаблон задан корректно иначе null.
     */
    public static final String format(java.util.Date date, String patternType) {
        DateFormat format = new SimpleDateFormat(getTypeFormat(patternType));
        return date == null ? "" : format.format(date);
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

    public static final String formatPeriod(java.util.Date dateFrom, java.util.Date dateTo, String patternType) {
        return format(dateFrom, patternType) + "-" + format(dateTo, patternType);
    }

    /**
     * Формирует строку период дат.
     * @param date1 начало периода.
     * @param date2 конец периода.
     * @return строка "dd.MM.yyyy-dd.MM.yyyy", если один из парамеметров равен
     *         null, вместо соответствующей даты выводиться пустая строка,
     *         например "-dd.MM.yyyy", "dd.MM.yyyy-", "-"
     */
    public static final String formatPeriod(Calendar date1, Calendar date2) {
        return format(TimeUtils.convertCalendarToDate(date1), FORMAT_TYPE_YMD) + "-"
                + format(TimeUtils.convertCalendarToDate(date2), FORMAT_TYPE_YMD);
    }

    /**
     * Формирует строку период.
     * @param date1 начала периода.
     * @param date2 конец периода.
     * @return строка "dd.MM.yyyy-dd.MM.yyyy", если один из парамеметров равен
     *         null, вместо соответствующей даты выводиться пустая строка,
     *         например "-dd.MM.yyyy", "dd.MM.yyyy-", "-".
     */
    public static final String formatPeriod(Date date1, Date date2) {
        return format(date1, FORMAT_TYPE_YMD) + "-" + format(date2, FORMAT_TYPE_YMD);
    }

    /**
     * Форматирует дату в вид 'yyyy-MM-dd' для подстановки в SQL запрос, сразу окружённую кавычками.
     * @param date
     * @return
     */
    public static final String formatSqlDate(Date date) {
        return new SimpleDateFormat( "''yyyy-MM-dd''").format(date);
    }


    // ########################################################################################
    // # Преобразование объектов
    // ########################################################################################
    //
    /**
     * Преобразование объекта Calendar в java.util.Date.
     * @param calendar исходный объект Calendar.
     * @return объект типа java.util.Date или null если исходный объект null.
     */
    public static final java.util.Date convertCalendarToDate(Calendar calendar) {
        java.util.Date result = null;
        if (calendar != null) {
            result = calendar.getTime();
        }
        return result;
    }

    /**
     * Преобразование объекта java.sql.Date в java.util.Date.
     * @param date исходный объект.
     * @return объект типа java.util.Date или null если исходный объект null.
     */
    public static final java.util.Date convertSqlDateToDate(java.sql.Date date) {
        java.util.Date outDate = null;
        if (date != null) {
            outDate = new java.util.Date(date.getTime());
        }
        return outDate;
    }

    /**
     * Преобразование java.util.Date в java.util.Calendar.
     * @param date исходный объект.
     * @return java.sql.Calendar, если date != null, иначе null.
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
     * Преобразование java.sql.Timestamp в java.util.Calendar.
     * @param time исходный объект.
     * @return java.sql.Calendar, если time != null, иначе null.
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
     * Преобразование объекта java.util.Date в java.sql.Date.
     * @param date исходный объект.
     * @return java.sql.Date, если date != null, иначе null.
     */
    public static final java.sql.Date convertDateToSqlDate(Date date) {
        return date != null ? new java.sql.Date(date.getTime()) : null;
    }

    /**
     * Преобразование объекта java.util.Calendar в java.sql.Timestamp
     * @param calendar исходный объект.
     * @return java.sql.Timestamp, если calendar != null, иначе null.
     */
    public static final java.sql.Timestamp convertCalendarToTimestamp(Calendar calendar) {
        java.sql.Timestamp result = null;
        if (calendar != null) {
            result = new java.sql.Timestamp(calendar.getTimeInMillis());
        }
        return result;
    }

    /**
     * Преобразование long в java.sql.Timestamp.
     * @param calendar исходный объект.
     * @return java.sql.Timestamp.
     */
    public static final java.sql.Timestamp convertLongToTimestamp(long millis) {
        return new java.sql.Timestamp(millis);
    }

    /**
     * Преобразование java.lang.Long в java.sql.Timestamp.
     * @param millis сходный объект.
     * @return java.sql.Timestamp, если millis != null, иначе null.
     */
    public static final java.sql.Timestamp convertLongToTimestamp(Long millis) {
        java.sql.Timestamp result = null;
        if (millis != null) {
            result = new java.sql.Timestamp(millis.longValue());
        }
        return result;
    }

    // ########################################################################################
    // # вычисления разниц
    // ########################################################################################
    /**
     * 24h days difference between to dates. The method is time-proven, but
     * since Java 8 there is also available: {@link java.time.temporal.ChronoUnit#DAYS#between(java.time.temporal.Temporal, java.time.temporal.Temporal)}
     * @param dayFrom
     * @param dayTo
     * @return
     */
    public static final int daysDelta(Date dayFrom, Date dayTo) {
        long time1 = dayFrom.getTime();
        long time2 = dayTo.getTime();
        int days1 = (int) (time1 / 86400000L);
        int days2 = (int) (time2 / 86400000L);
        return days2 - days1;
    }

    // ########################################################################################
    // # проверки
    // ########################################################################################
    /**
     * Проверка date1 < date2 (С ТОЧНОСТЬЮ ДО ДНЯ!!!).
     * @param date1 первая дата.
     * @param date2 вторая дата.
     * @return true - date1 < date2, иначе false
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
     * Проверка date1 == date2 (С ТОЧНОСТЬЮ ДО ДНЯ!!!).
     * @param date1 первая дата.
     * @param date2 вторая дата.
     * @return true - date1 == date2, иначе false
     */
    public static boolean dateEqual(Date date1, Date date2) {
        return dateEqual(convertDateToCalendar(date1), convertDateToCalendar(date2));
    }

    private static boolean dateEqual(Calendar date1, Calendar date2) {
        return (date1 == date2) || (date1 != null && date2 != null && (date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR))
                && (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)));
    }

    /**
     * Проверка date1 <= date2 (С ТОЧНОСТЬЮ ДО ДНЯ!!!).
     * @param date1 первая дата.
     * @param date2 вторая дата.
     * @return true - date1 <= date2, иначе false.
     */
    public static boolean dateBeforeOrEq(Calendar date1, Calendar date2) {
        return dateBefore(date1, date2) || dateEqual(date1, date2);
    }

    /**
     * Проверка date1 <= date2 (С ТОЧНОСТЬЮ ДО ДНЯ!!!).
     * @param date1 первая дата.
     * @param date2 вторая дата.
     * @return true - date1 <= date2, иначе false
     */
    public static boolean dateBeforeOrEq(Date date1, Date date2) {
        return dateBeforeOrEq(convertDateToCalendar(date1), convertDateToCalendar(date2));
    }

    /**
     * Проверка входит ли проверяемая дата в заданный период.
     * @param checking проверяемая дата.
     * @param date1 начало заданого периода.
     * @param date2 конец заданого периода.
     * @return true - входит, false - нет.
     */
    public static final boolean dateInRange(Calendar checking, Calendar date1, Calendar date2) {
        return checking != null && (date1 == null || dateBeforeOrEq(date1, checking)) && (date2 == null || dateBeforeOrEq(checking, date2));
    }

    /**
     * Проверка входит ли проверяемая дата в заданный период.
     * @param checking проверяемая дата.
     * @param date1 начало заданого периода.
     * @param date2 конец заданого периода.
     * @return true - входит, false - нет.
     */
    public static final boolean dateInRange(Date checking, Date date1, Date date2) {
        return dateInRange(convertDateToCalendar(checking), convertDateToCalendar(date1), convertDateToCalendar(date2));
    }

    /**
     * Проверка входит ли проверяемый период в заданный.
     * @param checkingDate1 начала проверяемого период.
     * @param checkingDate2 конец проверяемого периода.
     * @param date1 начало заданого периода.
     * @param date2 конец заданого периода.
     * @return true - входит, false - нет.
     */
    public static final boolean periodInRange(Calendar checkingDate1, Calendar checkingDate2, Calendar date1, Calendar date2) {
        boolean result = date1 == null || (checkingDate1 != null && dateBeforeOrEq(date1, checkingDate1));
        if (result) {
            result = date2 == null || (checkingDate2 != null && dateBeforeOrEq(checkingDate2, date2));
        }
        return result;
    }

    /**
    * Проверка пересечения двух интервалов дат.
    *
    * @param date1 левая граница первого интервала
    * @param date2 правая граница первого интервала
    * @param dateFrom левая граница второго интервала
    * @param dateTo правай граница второго интервала
    * @return
    */
    public static boolean checkDateIntervalsIntersection(Calendar date1, Calendar date2, Calendar dateFrom, Calendar dateTo) {
        //в обоих случаях попытка поиска пересечения со всей временной осью
        if (date1 == null && date2 == null || dateFrom == null && dateTo == null) {
            return true;
        }

        return (date1 == null || dateTo == null || dateBefore(date1, dateTo)) && (dateFrom == null || date2 == null || dateBefore(dateFrom, date2));
    }

    // ########################################################################################
    // # изменения объектов
    // ########################################################################################
    /**
     * Устанавливает дату на последний день месяца.
     * @param date исходная дата.
     */
    public static final void moveToEndOfMonth(Calendar date) {
        if (date != null) {
            date.add(Calendar.MONTH, 1);
            date.set(Calendar.DAY_OF_MONTH, 1);
            date.add(Calendar.HOUR_OF_DAY, -1);
        }
    }

    /**
     * Увеличивает date на 1 день и сбрасывает время в 00:00:00.0.
     * @param date исходная дата.
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
     * Сброс времени на начало дня.
     * @param time исходная дата.
     * @return сброшенная дата (часы=минуты=секунды=мсек=0).
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
     * Сброс времени на начало дня.
     * @param time исходная дата.
     * @return сброшенная дата (часы=минуты=секунды=мсек=0).
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
     * @return {@code null} if {@param date} is null, else the last day of the month
     */
    public static final Date getEndMonth(Date date) {
        if (date == null)
            return null;
        return TimeConvert.toDate(TimeConvert.toYearMonth(date).atEndOfMonth());
    }

    /**
     * Следующий от даты день.
     * @param date дата
     * @return
     */
    public static final Calendar getNextDay(Calendar date) {
        date = (Calendar) date.clone();
        moveToStartNextDay(date);
        return date;
    }

    /**
     * Предыдущий от даты день.
     * @param date
     * @return
     */
    public static final Calendar getPrevDay(Calendar date) {
        date = (Calendar) date.clone();
        date.add(Calendar.DAY_OF_YEAR, -1);
        return date;
    }

    /**
     * Следующий от даты день.
     * @param date дата
     * @return
     */
    public static final Date getNextDay(Date date) {
        return convertCalendarToDate(getNextDay(convertDateToCalendar(date)));
    }

    /**
     * Предыдущий от даты день.
     * @param date
     * @return
     */
    public static final Date getPrevDay(Date date) {
        return convertCalendarToDate(getPrevDay(convertDateToCalendar(date)));
    }

    /**
     * @return first day in the prevous month from the current time.
     */
    public static final Date getPrevMonth() {
        return Date.from(YearMonth.now().minusMonths(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Возвращает сокращенное название дня недели (пн - понедельник, вт - вторник и т.д.)
     * @param date Дата, на основании которой определяется день недели
     * @return String сокращенное название дня недели
     */
    @Deprecated
    public static String getShortDateName(Date date) {
        // TODO: Use Java API
        final String[] shortDayNames = { "пн", "вт", "ср", "чт", "пт", "сб", "вс" };
        return shortDayNames[getDayOfWeekPosition(date) - 1];
    }

    /**
     * Корректирует объект Date, так, чтобы получилось такое же локальное время но в другой таймзоне.
     * @param time исходный объект Date с каким-то временем для таймзоны fromTz.
     * @param fromTz исходная таймзона.
     * @param toTz целевая таймзона.
     * @return
     */
    public static Date timezoneChange(Date time, TimeZone fromTz, TimeZone toTz) {
        if (time == null) {
            return null;
        }

        return new Date(time.getTime() - toTz.getRawOffset() + fromTz.getRawOffset());
    }
}