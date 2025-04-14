package org.bgerp.app.exception.alarm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import org.bgerp.model.file.FileData;

/**
 * Alarm sent to a configured email
 *
 * @author Shamil Vakhitov
 */
class AlarmMessage {
    private final Date time = new Date();
    /** Key for grouping many similar alarms */
    private final String key;
    private final String subject;
    private final String text;
    private final Throwable exception;
    private final List<FileData> attachments;

    AlarmMessage(String key, String subject, String text, Throwable ex, List<FileData> attachments) {
        this.subject = subject;
        this.text = text;
        this.key = key;
        this.exception = ex;
        this.attachments = attachments;
    }

    Date getTime() {
        return time;
    }

    String getKey() {
        return key;
    }

    String getSubject() {
        return subject;
    }

    String getText() {
        StringBuilder result = new StringBuilder(text);
        if (exception != null) {
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            result.append("\n\n");
            result.append(sw.toString());
        }
        return result.toString();
    }

    public List<FileData> getAttachments() {
        return attachments;
    }
}