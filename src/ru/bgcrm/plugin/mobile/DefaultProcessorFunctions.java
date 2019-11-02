package ru.bgcrm.plugin.mobile;

import java.sql.Connection;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.expression.ExpressionBasedFunction;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.mobile.dao.MobileDAO;
import ru.bgcrm.plugin.mobile.model.Account;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

public class DefaultProcessorFunctions extends ExpressionBasedFunction {

	private static final Logger log = Logger.getLogger(DefaultProcessorFunctions.class);
	
	public static final String URL_PREFIX = "http://mob.bgcrm.ru/cgi/server.py";

	public DefaultProcessorFunctions() {}

	/**
	 * Отправляет сообщение на приложение BGERP исполнителям данного процесса за исключением пользователя,
	 * осуществляющего текущее изменение.
	 * @param subject тема сообщения. 
	 * @param text текст.
	 * @throws BGException
	 */
	public void sendMessageToExecutors(String subject, String text) throws BGException {
		Process process = (Process)expression.getContextObject(Process.OBJECT_TYPE);
		DynActionForm form = (DynActionForm)expression.getContextObject(DynActionForm.KEY);
				
		Collection<Integer> userIds = 
				process.getExecutorIds().stream()
				// не высылать сообщение изменившему процесс пользователю
				.filter(userId -> userId != form.getUserId())
				.collect(Collectors.toList());
			
		sendMessageToUsers(subject, text, userIds);
	}

	/**
	 * Отправляет сообщение на приложение BGERP указанным подльзвоателям.
	 * @param subject тема сообщения.
	 * @param text текст.
	 * @param userIds коды пользователей BGERP.
	 * @throws BGException
	 */
	public void sendMessageToUsers(String subject, String text, Collection<Integer> userIds) throws BGException {
		Connection con = Setup.getSetup().getDBConnectionFromPool();
		try {
			GMS gms = Setup.getSetup().getConfig(GMS.class);
			for (int userId : userIds) {
				Account account = new MobileDAO(con).findAccount(User.OBJECT_TYPE, userId);
				if (account == null) {
					log.debug("User: " + userId + " isn't logged in.");
					continue;
				}
				gms.sendMessage(account.getKey(), subject, text);
			}
		} finally {
			SQLUtils.closeConnection(con);
		}
	}

}
