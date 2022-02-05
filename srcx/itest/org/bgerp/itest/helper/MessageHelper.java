package org.bgerp.itest.helper;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.message.MessageTest;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.model.message.Message;

public class MessageHelper {

    public static void addMessage(Message message) throws Exception {
        new MessageDAO(DbTest.conRoot).updateMessage(message);
    }

    public static Message addNoteMessage(int processId, int userId, Duration timeOffset, String subject, String text) throws Exception {
        var m = new Message()
            .withTypeId(MessageTest.messageTypeNote.getId())
            .withDirection(Message.DIRECTION_INCOMING)
            .withProcessId(processId)
            .withFromTime(Date.from(Instant.now().plus(timeOffset)))
            .withToTime(Date.from(Instant.now().plus(timeOffset)))
            .withUserId(userId)
            .withSubject(subject).withText(text);
        new MessageDAO(DbTest.conRoot).updateMessage(m);
        return m;
    }
}
