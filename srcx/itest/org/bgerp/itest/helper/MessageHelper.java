package org.bgerp.itest.helper;

import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.message.MessageTest;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.util.TimeUtils;

public class MessageHelper {

    public static void addMessage(Message message) throws Exception {
        new MessageDAO(DbTest.conRoot).updateMessage(message);
    }
    
    public static Message addNoteMessage(int processId, int userId, int timeOffset, String subject, String text) throws Exception {
        var m = new Message()
            .withTypeId(MessageTest.messageTypeNote.getId()).withDirection(Message.DIRECTION_INCOMING).withProcessId(processId)
            .withFromTime(TimeUtils.getDateWithOffset(timeOffset)).withToTime(TimeUtils.getDateWithOffset(timeOffset)).withUserId(userId)
            .withSubject(subject).withText(text);
        new MessageDAO(DbTest.conRoot).updateMessage(m);
        return m;
    }
}
