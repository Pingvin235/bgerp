package ru.bgcrm.dyn;

import org.apache.log4j.Logger;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.authentication.UserAuthenticationEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Демонстрационный динамический класс, 
 * Может быть создан в переменной конфигурации createOnStart
 * createOnStart+=,ru.bgcrm.dyn.ExampleCreateOnStart
 */
public class ExampleCreateOnStart {
	private static final Logger log = Logger.getLogger(ExampleCreateOnStart.class);
	
	public ExampleCreateOnStart() {
		log.info("Подписка на события.");
		EventProcessor.subscribe((e, connectionSet) -> {
			processEvent(connectionSet, e);
		}, UserAuthenticationEvent.class);
	}
	
	private void processEvent(ConnectionSet connectionSet, UserAuthenticationEvent event) throws BGException {
		log.info("Авторизован пользователь: " + event.getUser());
	}
}
