package ru.bgcrm.dao.message.config;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageTypeConfig extends Config {
	private static final Logger log = Logger.getLogger(MessageTypeConfig.class);

	@SuppressWarnings("serial")
	private SortedMap<Integer, MessageType> typeMap = new TreeMap<Integer, MessageType>() {
		@Override
		public MessageType get(Object key) {
			MessageType result = super.get(key);
			if (result == null) {
				try {
					result = new MessageTypeUncknown((Integer) key);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			return result;
		}
	};

	public MessageTypeConfig(ParameterMap setup) {
		super(setup);
		for (Map.Entry<Integer, ParameterMap> me : setup.subIndexed("messageType.").entrySet()) {
			int id = me.getKey();
			ParameterMap config = me.getValue();

			try {
				@SuppressWarnings("unchecked")
				Class<? extends MessageType> typeClass = (Class<? extends MessageType>) DynamicClassManager
						.getClass(config.get("class"));

				MessageType type = typeClass.getConstructor(int.class, ParameterMap.class).newInstance(id, config);
				type.setId(id);

				typeMap.put(type.getId(), type);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public SortedMap<Integer, MessageType> getTypeMap() {
		return typeMap;
	}

	public void setTypeMap(SortedMap<Integer, MessageType> typeMap) {
		this.typeMap = typeMap;
	}
	
	@SuppressWarnings("unchecked")
	public<T extends MessageType> T getMessageType(Class<T> clazz) {
		return (T)typeMap.values().stream().filter(o -> clazz.isInstance(o)).findAny().orElse(null);
	}

	private static class MessageTypeUncknown extends MessageType {
		public MessageTypeUncknown(int id) throws BGException {
			super(id, "??? " + id, new Preferences());
		}

		@Override
		public void process() {
		}

		@Override
		public boolean isAnswerSupport() {
			return false;
		}

		@Override
		public void updateMessage(Connection con, DynActionForm form, Message message) throws BGException {
		}

		@Override
		public List<Message> newMessageList(ConnectionSet conSet) {
			return Collections.emptyList();
		}
	}
}