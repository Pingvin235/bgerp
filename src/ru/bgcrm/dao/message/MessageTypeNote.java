package ru.bgcrm.dao.message;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.struts.action.FileAction.FileInfo;
import ru.bgcrm.struts.action.FileAction.SessionTemporaryFiles;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionConnectionSet;

public class MessageTypeNote extends MessageType {
	private static final Logger log = Logger.getLogger(MessageTypeNote.class);

	public MessageTypeNote(int id, ParameterMap config) throws BGException {
		super(id, config.get("title"), config);
	}

	@Override
	public void updateMessage(Connection con, DynActionForm form, Message message) throws Exception {
		message.setSystemId("");
		message.setFrom("");
		message.setProcessed(true);
		message.setToTime(new Date());
		message.setDirection(Message.DIRECTION_INCOMING);

		Map<Integer, FileInfo> tmpFiles = processMessageAttaches(con, form, message);

		new MessageDAO(con).updateMessage(message);

		SessionTemporaryFiles.deleteFiles(form, tmpFiles.keySet());

		// генерация события

		int processId = message.getProcessId();

		// определение кода процесса
		if (processId > 0) {
			Process process = new ProcessDAO(con).getProcess(processId);

			ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
			if (type == null) {
				log.error("Not found process type with id:" + process.getTypeId());
			} else {
				EventProcessor.processEvent(new ProcessMessageAddedEvent(form, message, process),
						type.getProperties().getActualScriptName(), new SingleConnectionConnectionSet(con));
			}
		}
	}

	@Override
	public void messageDelete(ConnectionSet conSet, String... messageIds) throws BGException {
		for (String messageId : messageIds)
			new MessageDAO(conSet.getConnection()).deleteMessage(Utils.parseInt(messageId));
	}

	@Override
	public boolean isAnswerSupport() {
		return true;
	}

	@Override
	public boolean isEditable(Message message) {
		return true;
	}
	
	@Override
	public boolean isRemovable(Message message) {
		return true;
	}

	@Override
	public boolean isProcessChangeSupport() {
		return true;
	}

	@Override
	public String getProcessMessageHeaderColor(Message message) {
		return "#e6fb9d";
	}
}
