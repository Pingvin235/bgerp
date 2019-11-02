package ru.bgcrm.plugin.bgbilling.proto.model.cerbercrypt;

import java.util.Date;

public class CardPacket {
    private int id;
    private int card;
    private String changeDate;
    private String comment;
    private Date dateFrom;
    private Date dateTo;
    private String packet;
    private int packetId;
    private String period;
    private String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCard() {
        return card;
    }

    public void setCard(int card) {
        this.card = card;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(String changeDate) {
        this.changeDate = changeDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getPacket() {
        return packet;
    }

    public void setPacket(String packet) {
        this.packet = packet;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }
}
