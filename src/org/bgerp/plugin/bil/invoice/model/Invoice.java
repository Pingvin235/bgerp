package org.bgerp.plugin.bil.invoice.model;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.bgerp.model.base.Id;
import org.bgerp.util.Log;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class Invoice extends Id {
    private static final Log log = Log.getLog();

    private int typeId;
    private int typeTitle;
    private int processId;
    /** First day of paid period. */
    private Date dateFrom;
    /** Last day of paid period. */
    private Date dateTo;
    /** Counter, used for number generation. */
    private int numberCnt;
    /** Generated number. */
    private String number;
    /** Creation date. */
    private Date createdTime;
    /** Created user. */
    private int createdUserId;
    /** Sent to customer. */
    private Date sentTime;
    /** Sent by user. */
    private int sentUserId;
    /** Date of payment. */
    private Date paymentDate;
    /** User accepted payment. */
    private int paymentUserId;

    private BigDecimal amount = BigDecimal.ZERO;

    private List<Position> positions = new ArrayList<>();

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeTitle() {
        return typeTitle;
    }

    public void setTypeTitle(int typeTitle) {
        this.typeTitle = typeTitle;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getNumberCnt() {
        return numberCnt;
    }

    public void setNumberCnt(int numberCnt) {
        this.numberCnt = numberCnt;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date value) {
        this.dateFrom = value;
    }

    public Date getDateTo() {
        return dateTo == null ? TimeUtils.getEndMonth(dateFrom) : dateTo;
    }

    public void setDateTo(Date value) {
        this.dateTo = value;
    }

    /**
     * {@link TextStyle#FULL} display name of the month of {@link #dateFrom}.
     * @param lang language, e.g. 'en', 'ru'.
     * @return
     */
    public String dateFromMonthDisplayName(String lang) {
        return monthDisplayName(lang, dateFrom);
    }

    /**
     * Year of {@link #dateFrom}.
     * @return
     */
    public int dateFromYear() {
        return TimeConvert.toYearMonth(dateFrom).getYear();
    }

    /**
     * Formatted period of invoice months: {@code MonthFromName YearFromNumber - MonthToName YearToNumber}
     * with month names for a specified language. If {@link #dateFrom} and {@link #dateTo} are in the same month,
     * then only it is shown without range.
     * @param lang the language of months names.
     * @return
     */
    public String monthsPeriod(String lang) {
        var result = new StringBuilder(40)
            .append(dateFromMonthDisplayName(lang))
            .append(" ")
            .append(dateFromYear());

        Date dateTo = getDateTo();
        if (!TimeConvert.toYearMonth(dateFrom).equals(TimeConvert.toYearMonth(dateTo)))
            result
                .append(" - ")
                .append(monthDisplayName(lang, dateTo))
                .append(" ")
                .append(TimeConvert.toYearMonth(dateTo).getYear());

        return result.toString();
    }

    private String monthDisplayName(String lang, Date date) {
        var month = TimeConvert.toYearMonth(date).getMonth();
        // TextStyle.FULL - января
        return month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag(lang));
    }

    /**
     * @return count of invoice months, {@code 1} or more.
     */
    public long months() {
        return ChronoUnit.MONTHS.between(TimeConvert.toYearMonth(dateFrom), TimeConvert.toYearMonth(getDateTo())) + 1;
    }

    @Deprecated
    public String getDateFromMonthDisplayName(String lang) {
        log.warn("Used deprecated call 'getDateFromMonthDisplayName', use 'dateFromMonthDisplayName' instead.");
        return dateFromMonthDisplayName(lang);
    }

    @Deprecated
    public int getDateFromYear() {
        log.warn("Used deprecated call 'getDateFromYear', use 'dateFromYear' instead.");
        return dateFromYear();
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date value) {
        this.createdTime = value;
    }

    public int getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(int value) {
        this.createdUserId = value;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date value) {
        this.sentTime = value;
    }

    public int getSentUserId() {
        return sentUserId;
    }

    public void setSentUserId(int value) {
        this.sentUserId = value;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date value) {
        this.paymentDate = value;
    }

    public int getPaymentUserId() {
        return paymentUserId;
    }

    public void setPaymentUserId(int value) {
        this.paymentUserId = value;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    /**
     * Updates amount out of positions.
     * @return the new value.
     */
    public BigDecimal amount() {
        return amount = positions.stream().map(Position::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public void addPosition(String id, String title, BigDecimal amount, String unit, int quantity) {
        positions.add(new Position(id, title, amount, unit, quantity));
    }

    public void addPosition(String id, String title, String amount, String unit, int quantity) {
        positions.add(new Position(id, title, Utils.parseBigDecimal(amount), unit, quantity));
    }
}
