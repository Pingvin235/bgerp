package org.bgerp.itest.helper;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.message.MessageTest;
import org.bgerp.itest.kernel.user.UserTest;

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

    /**
     * Appends to a process a test description message, that content has to be placed in ClassName.message.txt file in same package as class of {@code test} object.
     * @param processId the process ID.
     * @param test test class instance.
     * @throws Exception
     */
    public static void addHowToTestNoteMessage(int processId, Object test) throws Exception {
        addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ofSeconds(0), "How to test", ResourceHelper.getResource(test, "howto.txt"));
    }

    public static Message addCallMessage(int processId, int userId, Duration timeOffset, String fromNumber, String toNumber, String subject, String text) throws Exception {
        var m = new Message()
            .withTypeId(MessageTest.messageTypeCall.getId())
            .withSystemId(Date.from(Instant.now().plus(timeOffset)).toString())
            .withDirection(Message.DIRECTION_INCOMING)
            .withProcessId(processId)
            .withFromTime(Date.from(Instant.now().plus(timeOffset)))
            .withFrom(fromNumber)
            .withToTime(Date.from(Instant.now().plus(timeOffset)))
            .withTo(toNumber)
            .withUserId(userId)
            .withSubject(subject).withText(text);
        new MessageDAO(DbTest.conRoot).updateMessage(m);
        return m;
    }
}
