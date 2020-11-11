package ru.bgcrm.model.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.Id;
import ru.bgcrm.model.process.Process;

/**
 * Одно входящее или исходящее сообщение.
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
    // флаг, что сообщение обработано    
    private boolean processed;
    // прикрепленные файлы
    private List<FileData> attaches = new ArrayList<>();
    
    public String getSystemId() {
        return systemId;
    }

    public Message setSystemId(String value) {
        this.systemId = value;
        return this;
    }

    public int getProcessId() {
        return processId;
    }

    public Message setProcessId(int value) {
        this.processId = value;
        return this;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
        this.processId = process.getId();
    }

    public Date getFromTime() {
        return fromTime;
    }

    public Message setFromTime(Date value) {
        this.fromTime = value;
        return this;
    }

    public String getText() {
        return text;
    }

    public Message setText(String value) {
        this.text = value;
        return this;
    }

    public int getTypeId() {
        return typeId;
    }

    public Message setTypeId(int value) {
        this.typeId = value;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Message setUserId(int value) {
        this.userId = value;
        return this;
    }

    public Date getToTime() {
        return toTime;
    }

    public Message setToTime(Date value) {
        this.toTime = value;
        return this;
    }

    public String getUserTitle() {
        return UserCache.getUser(userId).getTitle();
    }

    public int getDirection() {
        return direction;
    }

    public Message setDirection(int value) {
        this.direction = value;
        return this;
    }

    public boolean isIncoming() {
        return direction == DIRECTION_INCOMING;
    }

    public String getFrom() {
        return from;
    }

    public Message setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Message setTo(String value) {
        this.to = value;
        return this;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getSubject() {
        return subject;
    }

    public Message setSubject(String value) {
        this.subject = value;
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