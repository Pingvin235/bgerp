package org.bgerp.model.msg.config;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.cfg.bean.Bean;
import org.bgerp.model.msg.Message;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@org.bgerp.app.cfg.bean.annotation.Bean
public class MessageTypeConfig extends Config {
    private static final Log log = Log.getLog();

    private SortedMap<Integer, MessageType> typeMap = new TreeMap<>() {
        @Override
        public MessageType get(Object key) {
            MessageType result = super.get(key);
            if (result == null) {
                try {
                    result = new MessageTypeUnknown((Integer) key);
                } catch (Exception e) {
                    log.error(e);
                }
            }
            return result;
        }
    };

    protected MessageTypeConfig(ConfigMap config) {
        super(null);
        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("messageType.").entrySet()) {
            int id = me.getKey();
            ConfigMap pm = me.getValue();

            try {
                @SuppressWarnings("unchecked")
                Class<? extends MessageType> typeClass = (Class<? extends MessageType>) Bean.getClass(pm.get("class"));

                MessageType type = typeClass.getConstructor(Setup.class, int.class, ConfigMap.class).newInstance(Setup.getSetup(), id, pm);

                type.setId(id);

                typeMap.put(type.getId(), type);
            } catch (InvocationTargetException e) {
                if (!(e.getCause() instanceof InitStopException)) {
                    log.error(e);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    @Dynamic
    public SortedMap<Integer, MessageType> getTypeMap() {
        return typeMap;
    }

    public void setTypeMap(SortedMap<Integer, MessageType> typeMap) {
        this.typeMap = typeMap;
    }

    @SuppressWarnings("unchecked")
    public <T extends MessageType> T getMessageType(Class<T> clazz) {
        return (T) typeMap.values().stream().filter(o -> clazz.isInstance(o)).findAny().orElse(null);
    }

    public int getUnprocessedMessagesCount() {
        int result = 0;
        for (MessageType type : typeMap.values()) {
            if (type.getUnprocessedMessagesCount() != null)
                result += type.getUnprocessedMessagesCount();
        }
        return result;
    }

    private static class MessageTypeUnknown extends MessageType {
        public MessageTypeUnknown(int id) {
            super(null, id, "??? " + id, new Preferences());
        }

        @Override
        public void process() {
        }

        @Override
        public boolean isAnswerSupport() {
            return false;
        }

        @Override
        public void updateMessage(Connection con, DynActionForm form, Message message) {
        }

        @Override
        public List<Message> newMessageList(ConnectionSet conSet) {
            return Collections.emptyList();
        }

        @Override
        public boolean isRemovable(Message message) {
            return true;
        }

        @Override
        public void messageDelete(ConnectionSet conSet, String... messageIds) throws Exception {
            for (String messageId : messageIds)
                new MessageDAO(conSet.getConnection()).deleteMessage(Utils.parseInt(messageId));
        }
    }
}