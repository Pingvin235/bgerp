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
            .setTypeId(MessageTest.messageTypeNote.getId()).setDirection(Message.DIRECTION_INCOMING).setProcessId(processId)
            .setFromTime(TimeUtils.getDateWithOffset(timeOffset)).setToTime(TimeUtils.getDateWithOffset(timeOffset)).setUserId(userId)
            .setSubject(subject).setText(text);
        new MessageDAO(DbTest.conRoot).updateMessage(m);
        return m;
    }
}
