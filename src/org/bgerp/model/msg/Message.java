package org.bgerp.model.msg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bgerp.cache.UserCache;
import org.bgerp.model.base.Id;

import ru.bgcrm.model.FileData;
import ru.bgcrm.model.process.Process;

/**
 * Message.
 *
 * @author Shamil Vakhitov
 */
public class Message extends Id {
    public static final String OBJECT_TYPE = "message";

    public static final int DIRECTION_INCOMING = 1;
    public static final int DIRECTION_OUTGOING = 2;

    // системный идентификатор
    private String systemId = "";

    private int processId;
    private Process process;

    // тип сообщения
    private int typeId = -1;
    // направение сообщения
    private int direction = DIRECTION_INCOMING;

    // для исходящего звонка - код звонившего пользователя
    // для входящего звонка - код принявшего пользователя
    // для входящего HD, EMail - код прочитавшего пользователя
    // для исходящего HD, EMail - код отписавшего пользователя
    private int userId;

    // для звонка - время начала, для HD - время создания сообщения
    // для входящих EMail - время получения сообщения демоном
    // для исходящих EMail - время создания сообщения
    private Date fromTime;

    // для звонка - время окончания, для HD - время прочтения,
    // для входящих EMail - время прочтения
    // для исходящих EMail - время отправки
    private Date toTime;

    // для звонка - с номера, для EMail - с адреса, для HD - код контрагента в виде строки
    private String from = "";
    // для звонка - на номер, для EMail - на адрес, для HD - код контрагента в виде строки
    private String to = "";

    // для HD/E-Mail - тема сообщения
    private String subject = "";

    // для HD/E-Mail - текст сообщения, для телефона - краткое описание.
    private String text = "";

    // прикрепленные файлы
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
     * @return {@link #direction} equals {@link #DIRECTION_INCOMING} and {@link #toTime} is nul {@code null}.
     */
    public boolean isRead() {
        return toTime != null && direction == DIRECTION_INCOMING;
    }

    /**
     * @return {@link #direction} equals {@link #DIRECTION_INCOMING} and {@link #toTime} is {@code null}.
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
     * Получить список прикрепленных файлов
     * @return список файлов
     */
    public List<FileData> getAttachList() {
        return attaches;
    }

    /**
     * Добавляет к сообщению файл
     * @param messageAttach
     */
    public void addAttach(FileData messageAttach) {
        this.attaches.add(messageAttach);
    }

    public String getLockEdit() {
        return "message_edit_" + id;
    }
}