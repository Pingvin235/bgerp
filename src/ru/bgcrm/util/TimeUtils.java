package ru.bgcrm.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.PeriodSet;

public class TimeUtils {
    // переделать, чтобы настраивалось
    public static final String[] monthNames = { "январь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сентябрь", "октябрь",
            "ноябрь", "декабрь" };
    public static final String[] monthNamesRod = { "января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября",
            "ноября", "декабря" };

    public static final String[] shortDayNames = { "пн", "вт", "ср", "чт", "пт", "сб", "вс" };

    public static final String PATTERN_DDMMYYYY = "dd.MM.yyyy";
    public static final String PATTERN_YYYYMMDD = "yyyy-MM-dd";
    public static final String PATTERN_YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DDMMYYYYHHMMSS = "dd.MM.yyyy HH:mm:ss";

    public static final String FORMAT_TYPE_YMD = "ymd";
    public static final String FORMAT_TYPE_YMDH = "ymdh";
    public static final String FORMAT_TYPE_YMDHM = "ymdhm";
    public static final String FORMAT_TYPE_YMDHMS = "ymdhms";

    /**
     * Возвращает формат даты в формате {@link SimpleDateFormat}.
     * @param type типа даты {@link #FORMAT_TYPE_YMD},{@link #FORMAT_TYPE_YMDH}, {@link #FORMAT_TYPE_YMDHM}, {@link #FORMAT_TYPE_YMDHMS}. 
     * @return
     */
    public static String getTypeFormat(String type) {
        if (FORMAT_TYPE_YMD.equals(type)) {
            return Setup.getSetup().get("date.default.format.ymd", "dd.MM.yyyy");
        }
        if (FORMAT_TYPE_YMDHM.equals(type)) {
            return Setup.getSetup().get("date.default.format.ymdhm", "dd.MM.yyyy HH:mm");
        }
        if (FORMAT_TYPE_YMDH.equals(type)) {
            return Setup.getSetup().get("date.default.format.ymdh", "dd.MM.yyyy HH");
        }
        if (FORMAT_TYPE_YMDHMS.equals(type)) {
            return Setup.getSetup().get("date.default.format.ymdhms", "dd.MM.yyyy HH:mm:ss");
        }
        return type;
    }

    // ########################################################################################
    // # Битовые маски
    // ########################################################################################
    //
    /**
     * Возвращает битовую маску дня недели.
     * @param date дата
     * @return 1(понедельник), 2(вторник), ... 64(воскресенье)
     */
    public static final int getDayOfWeekMask(Calendar date) {
        return 1 << (getDayOfWeekPosition(date));
    }

    /**
     * Возвращает порядок дня недели от понедельника, начиная с 1.
     * @param date
     * @return
     */
    public static final int getDayOfWeekPosition(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7 + 1;
    }

    public static final int getDayOfWeekPosition(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return getDayOfWeekPosition(calendar);
    }

    /**
     * Битовая маска дня месяца.
     * @param date дата
     * @return 1(01), 2(02), 4(03), 8(04), ... 2147483648(31)
     */
    public static final int getDayOfMonthMask(Calendar date) {
        return 1 << (date.get(Calendar.DAY_OF_MONTH) - 1);
    }

    /**
     * Битовая маска часа суток.
     * @param date
     * @return 1(00), 2(01), 4(02), ... 8388608(23)
     */
    public static final int getHourOfDayMask(Calendar date) {
        return 1 << date.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Битовая маска месяца года.
     * @param date
     * @return 1(январь), 2(февраль), ... 2048(декабрь)
     */
    public static final int getMonthOfYearMask(Calendar date) {
        return 1 << date.get(Calendar.MONTH);
    }

    /**
     * Проверка соответствует ли указанная дата, всем заданным маскам (если
     * маска равна 0, в проверке не участвует).
     * @param date проверяемая дата
     * @param hourOfDay  часа
     * @param dayOfWeek маска дня неделе
     * @param monthOfYear маска месяца года
     * @param dayOfMonth маска дня месяца
     * @return true - соотвествует или false - если нет
     */
    public static final boolean checkMasks(Calendar date, int hourOfDay, int dayOfWeek, int monthOfYear, int dayOfMonth) {
        boolean result = true;
        if (hourOfDay > 0) {
            result = (hourOfDay & getHourOfDayMask(date)) > 0;
        }
        if (result && dayOfWeek > 0) {
            result = (dayOfWeek & getDayOfWeekMask(date)) > 0;
        }
        if (result && dayOfMonth > 0) {
            result = (dayOfMonth & getDayOfMonthMask(date)) > 0;
        }
        if (result && monthOfYear > 0) {
            result = (monthOfYear & getMonthOfYearMask(date)) > 0;
        }
        return result;
    }

    // ########################################################################################
    // # Формирование и парсинг строк.
    // ########################################################################################

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
     * Форматирует разницу во времени
     * @param delta разница во времени в миллисекундах
     * @return
     */
    public static final String formatDeltaTime(long delta) {
        int days = (int) (delta / 86400);
        delta -= days * 86400;
        int hours = (int) (delta / 3600);
        delta -= hours * 3600;
        int min = (int) (delta / 60);
        delta -= min * 60;
        int sec = (int) (delta);
        StringBuffer result = new StringBuffer(30);
        DecimalFormat dfTime = new DecimalFormat("00");
        result.append(days);
        result.append(" d ");
        result.append(dfTime.format(hours));
        result.append(":");
        result.append(dfTime.format(min));
        result.append(":");
        result.append(dfTime.format(sec));
        return result.toString();
    }

    /**
     * Форматирует дату явно указанным шаблоном.
     * Можно использовать {@link #format(Date, String)} - по виду шаблона разбирает сам, тип это или формат.
     * 
     * @param date
     * @param pattern
     * @return
     */
    @Deprecated
    public static final String formatDateWithPattern(Date date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).format(date);
        } catch (Exception e) {
        }

        return "";
    }

    /**
     * Парсит дату из строки с явно указанным шаблоном.
     * Можно использовать {@link TimeUtils#parse(String, String)} - по виду шаблона разбирает сам, тип это или формат.
     * 
     * @param date
     * @param pattern
     * @return
     */
    @Deprecated
    public static final Date parseDateWithPattern(String date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * Форматирует дату в вид 'yyyy-MM-dd' для подстановки в SQL запрос, сразу окружённую кавычками.
     * @param date
     * @return
     */
    public static final String formatSqlDate(Date date) {
        return formatDateWithPattern(date, "''yyyy-MM-dd''");
    }

    public static final String formatSqlDateNoQuote(Date date) {
        return formatDateWithPattern(date, "yyyy-MM-dd");
    }

    /**
     * Форматирует дату + время в вид 'yyyy-MM-dd' для подстановки в SQL запрос, сразу окружённую кавычками.
     * @param date
     * @return
     */
    public static final String formatSqlDatetime(Date date) {
        return new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss''").format(date);
    }

    public static final String formatSqlDatetimeNoQuote(Date date) {
        String result = null;
        if (date != null) {
            result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        }
        return result;
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
     * Преобразует java.sql.TimeStamp в java.util.Date.
     * @param time преобразуемый объект.
     * @return null - если аргумент null.
     */
    public static final Date convertTimestampToDate(Timestamp time) {
        Date result = null;
        if (time != null) {
            result = new Date(time.getTime());
        }
        return result;
    }

    /**
     * Превращает SQL-дату в календарь.
     * @param date дата.
     * @return 
     */
    public static final java.util.Calendar convertSqlDateToCalendar(java.sql.Date date) {
        return convertDateToCalendar(convertSqlDateToDate(date));
    }

    /**
     * Преобразование объекта Calendar в java.sql.Date.
     * @param calendar исходный объект.
     * @return java.sql.Date, если calendar != null, иначе null.
     */
    public static final java.sql.Date convertCalendarToSqlDate(Calendar calendar) {
        java.sql.Date result = null;
        if (calendar != null) {
            result = new java.sql.Date(calendar.getTimeInMillis());
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
     * Преобразование объекта java.util.Date в java.sql.Timestamp.
     * @param date исходный объект.
     * @return java.sql.Timestamp, если date != null, иначе null.
     */
    public static final java.sql.Timestamp convertDateToTimestamp(Date date) {
        java.sql.Timestamp result = null;
        if (date != null) {
            result = new java.sql.Timestamp(date.getTime());
        }
        return result;
    }

    public static final XMLGregorianCalendar convertDateToXMLCalendar(Date date) throws BGException {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return convertCalendarToXMLCalendar(calendar);
    }

    public static final XMLGregorianCalendar convertCalendarToXMLCalendar(GregorianCalendar calendar) throws BGException {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (DatatypeConfigurationException e) {
            throw new BGException(e);
        }
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
     * Возвращает разницу в днях
     * @param date1 
     * @param date2
     * @return
     */
    public static final int getDays(Calendar date1, Calendar date2) {
        int days = (date2.get(Calendar.YEAR) - date1.get(Calendar.YEAR)) * 365 + date2.get(Calendar.DAY_OF_YEAR) - date1.get(Calendar.DAY_OF_YEAR);
        return days;
    }

    /**
     * Возвращает разницу в днях.
     * @param dayFrom 
     * @param dayTo
     * @return
     */
    public static final int daysDelta(Calendar dayFrom, Calendar dayTo) {
        long time1 = dayFrom.getTimeInMillis();
        long time2 = dayTo.getTimeInMillis();
        int days1 = (int) (time1 / 86400000L);
        int days2 = (int) (time2 / 86400000L);
        return days2 - days1;
    }

    /**
     * Возвращает длительность периода между hourFrom и hourTo в часах.
     * @param hourFrom дата начала периода.
     * @param hourTo дата окончания периода.
     * @return длительность периода в часах.
     */
    public static final int hourDelta(Calendar hourFrom, Calendar hourTo) {
        long delta = (hourTo.getTimeInMillis() - hourFrom.getTimeInMillis()) / 1000L;
        return (int) (delta / 3600);
    }

    /**
     * Возвращает длительность периода между dateFrom и dateTo в месяцах.
     * @param dateFrom дата начала периода.
     * @param dateTo дате окончания периода.
     * @return длительность периода в месяцах.
     */
    public static final int monthsDelta(Date dateFrom, Date dateTo) {
        return monthsDelta(convertDateToCalendar(dateFrom), convertDateToCalendar(dateTo));
    }

    /**
     * Возвращает длительность периода между dateFrom и dateTo в месяцах.
     * @param dateFrom дата начала периода.
     * @param dateTo дате окончания периода.
     * @return длительность периода в месяцах.
     */
    public static final int monthsDelta(Calendar dateFrom, Calendar dateTo) {
        return (dateTo.get(Calendar.YEAR) - dateFrom.get(Calendar.YEAR)) * 12 + (dateTo.get(Calendar.MONTH) - dateFrom.get(Calendar.MONTH));
    }

    // ########################################################################################
    // # проверки
    // ########################################################################################
    /**
     * Проверка date1 < date2 (С ТОЧНОСТЬЮ ДО ДНЯ!!!)
     * @param date1 первая дата.
     * @param date2 вторая дата.
     * @return true - date1 < date2, иначе false.
     */
    public static boolean dateBefore(final Calendar date1, final Calendar date2) {
        if (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)) {
            return date1.get(Calendar.DAY_OF_YEAR) < date2.get(Calendar.DAY_OF_YEAR);
        } else {
            return date1.get(Calendar.YEAR) < date2.get(Calendar.YEAR);
        }
    }

    /**
     * Проверка date1 < date2 (С ТОЧНОСТЬЮ ДО ДНЯ!!!).
     * @param date1 первая дата.
     * @param date2 вторая дата.
     * @return true - date1 < date2, иначе false
     */
    public static boolean dateBefore(final Date date1, final Date date2) {
        return dateBefore(convertDateToCalendar(date1), convertDateToCalendar(date2));
    }

    /**
     * Проверка date1 == date2 (С ТОЧНОСТЬЮ ДО ДНЯ!!!).
     * @param date1 первая дата.
     * @param date2 вторая дата.
     * @return true - date1 == date2, иначе false.
     */
    public static boolean dateEqual(Calendar date1, Calendar date2) {
        return (date1 == date2) || (date1 != null && date2 != null && (date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR))
                && (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)));
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

    /**
     * Проверка date1 == date2 (С ТОЧНОСТЬЮ ДО ЧАСА ДНЯ!!!).
     * @param dtime1 первая дата + время.
     * @param dtime2 вторая дата + время.
     * @return true - date1 == date2, иначе false.
     */
    public static boolean dateHourEqual(Calendar dtime1, Calendar dtime2) {
        return dateEqual(dtime1, dtime2) && dtime1.get(Calendar.HOUR_OF_DAY) == dtime2.get(Calendar.HOUR_OF_DAY);
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
     * Устанавливает минуты, секунды и миллисекунды в ноль.
     * @param time исходный объект.
     * @return
     */
    public static final Calendar clear_MIN_MIL_SEC(Calendar time) {
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
    public static final Calendar clear_HOUR_MIN_MIL_SEC(Calendar time) {
        clear_MIN_MIL_SEC(time);
        time.set(Calendar.HOUR_OF_DAY, 0);
        return time;
    }

    /**
     * Сброс времени на начало дня.
     * @param time исходная дата.
     * @return сброшенная дата (часы=минуты=секунды=мсек=0).
     */
    public static final Date clear_HOUR_MIN_MIL_SEC(Date time) {
        if (time != null) {
            return clear_HOUR_MIN_MIL_SEC(convertDateToCalendar(time)).getTime();
        }
        return time;
    }

    /**
     * Возвращает дату начала месяца, соответствующего входной дате.
     * @param date дата
     * @return
     */
    public static final Calendar getStartMonth(Calendar date) {
        Calendar result = (Calendar) date.clone();
        result.set(Calendar.DAY_OF_MONTH, 1);
        return result;
    }

    /**
     * Возвращает дату конца месяца, соответствующего входной дате.
     * @param date дата
     * @return
     */
    public static final Date getEndMonth(Date date) {
        return getEndMonth(convertDateToCalendar(date)).getTime();
    }

    /**
     * Возвращает дату конца месяца, соответствующего входной дате
     * @param date дата
     * @return
     */
    public static final Calendar getEndMonth(Calendar date) {
        Calendar result = (Calendar) date.clone();
        result.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));
        return result;
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
     * Returns the date with offset in days from now.
     * @param offset positive or negative offset
     * @return date with the offset
     */
    public static final Date getDateWithOffset(int offset) {
        Calendar now = new GregorianCalendar();
        now.add(Calendar.DAY_OF_YEAR, offset);
        return now.getTime(); 
    }

    /**
     * Дата с каким-либо часом.
     */
    public static Date getDateHour(Date date, int hour) {
        Calendar result = TimeUtils.convertDateToCalendar(date);
        if (result != null) {
            result.set(Calendar.HOUR, hour);
        }
        return TimeUtils.convertCalendarToDate(result);
    }

    /**
     * Возвращает сокращенное название дня недели (пн - понедельник, вт - вторник и т.д.)
     * @param date Дата, на основании которой определяется день недели
     * @return String сокращенное название дня недели
     */
    public static String getShortDateName(Date date) {
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