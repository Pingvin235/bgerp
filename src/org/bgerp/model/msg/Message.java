package org.bgerp.model.msg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bgerp.cache.UserCache;
import org.bgerp.model.base.Id;
import org.bgerp.model.file.FileData;

import ru.bgcrm.model.process.Process;

/**
 * Message
 *
 * @author Shamil Vakhitov
 */
public class Message extends Id {
    public static final String OBJECT_TYPE = "message";

    public static final int DIRECTION_INCOMING = 1;
    public static final int DIRECTION_OUTGOING = 2;

    // system identifier
    private String systemId = "";

    private int processId;
    private Process process;

    private int customerId;

    // message type
    private int typeId = -1;
    // message direction
    private int direction = DIRECTION_INCOMING;

    // for an outgoing call - the ID of the user who called
    // for an incoming call - the ID of the user who answered
    // for an incoming HD, EMail - the ID of the user who read it
    // for an outgoing HD, EMail - the ID of the user who replied
    private int userId;

    // for a call - start time, for HD - message creation time
    // for incoming EMail - time the message was received by the daemon
    // for outgoing EMail - message creation time
    private Date fromTime;

    // for a call - end time, for HD - time read,
    // for incoming EMail - time read
    // for outgoing EMail - time sent
    private Date toTime;

    // for a call - from number, for EMail - from address, for HD - customer ID as a string
    private String from = "";
    // for a call - to number, for EMail - to address, for HD - customer ID as a string
    private String to = "";

    // for HD/Email - message subject
    private String subject = "";

    // for HD/Email - message text, for phone - brief description
    private String text = "";

    // attached files
    private List<FileData> attaches = new ArrayList<>();

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String value) {
        this.systemId = value;
    }

    public Message withSystemId(String value) {
        setSystemId(value);
        return this;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int value) {
        this.processId = value;
    }

    public Message withProcessId(int value) {
        setProcessId(value);
        return this;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process value) {
        this.process = value;
        this.processId = value.getId();
    }

    public Message withProcess(Process value) {
        setProcess(value);
        return this;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date value) {
        this.fromTime = value;
    }

    public Message withFromTime(Date value) {
        setFromTime(value);
        return this;
    }

    public String getText() {
        return text;
    }

    public void setText(String value) {
        this.text = value;
    }

    public Message withText(String value) {
        setText(value);
        return this;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int value) {
        this.typeId = value;
    }

    public Message withTypeId(int value) {
        setTypeId(value);
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int value) {
        this.userId = value;
    }

    public Message withUserId(int value) {
        setUserId(value);
        return this;
    }

    public String getUserTitle() {
        return UserCache.getUser(userId).getTitle();
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date value) {
        this.toTime = value;
    }

    public Message withToTime(Date value) {
        setToTime(value);
        return this;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int value) {
        this.direction = value;
    }

    public Message withDirection(int value) {
        setDirection(value);
        return this;
    }

    public boolean isIncoming() {
        return direction == DIRECTION_INCOMING;
    }

    /**
     * @return {@link #direction} equals {@link #DIRECTION_INCOMING} and {@link #toTime} is not {@code null}
     */
    public boolean isRead() {
        return toTime != null && direction == DIRECTION_INCOMING;
    }

    /**
     * @return {@link #direction} equals {@link #DIRECTION_INCOMING} and {@link #toTime} is {@code null}
     */
    public boolean isUnread() {
        return toTime == null && direction == DIRECTION_INCOMING;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Message withFrom(String from) {
        setFrom(from);
        return this;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String value) {
        this.to = value;
    }

    public Message withTo(String value) {
        setTo(value);
        return this;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String value) {
        this.subject = value;
    }

    public Message withSubject(String value) {
        setSubject(value);
        return this;
    }

    /**
     * @return list of attached files
     */
    public List<FileData> getAttachList() {
        return attaches;
    }

    /**
     * Adds a file to the message
     * @param messageAttach
     */
    public void addAttach(FileData messageAttach) {
        this.attaches.add(messageAttach);
    }

    public String getLockEdit() {
        return "message_edit_" + id;
    }
}