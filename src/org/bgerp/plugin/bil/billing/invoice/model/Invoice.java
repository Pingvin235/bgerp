package org.bgerp.plugin.bil.billing.invoice.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.bgcrm.model.Id;
import ru.bgcrm.util.Utils;

public class Invoice extends Id {
    private int typeId;
    private int typeTitle;
    private int processId;
    /** First day of payed period. */
    private Date fromDate;
    /** Generated number. */
    private String number;
    /** Creation date. */
    private Date createdDate;
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date dateFrom) {
        this.fromDate = dateFrom;
    }

    public Date getCreatedTime() {
        return createdDate;
    }

    public void setCreatedTime(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(int createdUserId) {
        this.createdUserId = createdUserId;
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

    public void setSentUserId(int sentUserId) {
        this.sentUserId = sentUserId;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public int getPaymentUserId() {
        return paymentUserId;
    }

    public void setPaymentUserId(int paymentUserId) {
        this.paymentUserId = paymentUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public void addPosition(String id, String title, String amount) {
        positions.add(new Position(id, title, Utils.parseBigDecimal(amount)));
    }
}
