package ru.bgcrm.dao.message;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.ConnectionSet;

public class MessageTypeCall extends MessageType {
    // статические поля с данными регистрации, т.к. MessageTypeCall перезагружается при правке конфигурации
    private static final Map<Integer, Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>>> registedMap = new HashMap<>();

    public static class CallRegistration {
        private int userId;
        private String number;

        private Date lastPooling;
        private Integer messageForOpenId;

        public int getUserId() {
            return userId;
        }

        public String getNumber() {
            return number;
        }

        public Date getLastPooling() {
            return lastPooling;
        }

        public void setLastPooling(Date lastPooling) {
            this.lastPooling = lastPooling;
        }

        public Integer getMessageForOpenId() {
            return messageForOpenId;
        }

        public void setMessageForOpenId(Integer messageForOpenId) {
            this.messageForOpenId = messageForOpenId;
        }
    }
    
    private final String checkExpressionCallStore;

    public MessageTypeCall(Setup setup, int id, ParameterMap config) throws BGException {
        super(setup, id, config.get("title"), config);
        checkExpressionCallStore = config.get(Expression.CHECK_EXPRESSION_CONFIG_KEY + "CallStore");
    }

    @Override
    public boolean isProcessChangeSupport() {
        return true;
    }

    private Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>> getRegMaps() {
        Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>> result = registedMap.get(id);
        if (result == null) {
            registedMap.put(id, result = new Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>>());
            result.setFirst(new HashMap<String, CallRegistration>());
            result.setSecond(new HashMap<Integer, CallRegistration>());
        }
        return result;
    }

    public void numberRegister(int userId, String number) {
        Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>> regMaps = getRegMaps();

        CallRegistration reg = regMaps.getFirst().get(number);
        if (reg != null) {
            reg.lastPooling = new Date();
        } else {
            reg = new CallRegistration();
            reg.userId = userId;
            reg.number = number;
            reg.lastPooling = new Date();

            regMaps.getFirst().put(number, reg);
            regMaps.getSecond().put(userId, reg);
        }
    }

    public void numberFree(int userId) {
        Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>> regMaps = getRegMaps();

        CallRegistration reg = regMaps.getSecond().get(userId);
        if (reg != null) {
            regMaps.getFirst().remove(reg.number);
            regMaps.getSecond().remove(reg.userId);
        }
    }

    public CallRegistration getRegistrationByUser(int userId) {
        return getRegMaps().getSecond().get(userId);
    }

    public CallRegistration getRegistrationByNumber(String number) {
        return getRegMaps().getFirst().get(number);
    }
    
    public String getCheckExpressionCallStore() {
        return checkExpressionCallStore;
    }

    @Override
    public void updateMessage(Connection con, DynActionForm form, Message message) throws BGException {
        new MessageDAO(con).updateMessage(message);
    }

    @Override
    public List<Message> newMessageList(ConnectionSet conSet) throws BGException {
        SearchResult<Message> searchResult = new SearchResult<Message>();

        MessageDAO messageDao = new MessageDAO(conSet.getConnection());
        messageDao.searchMessageList(searchResult, null, id, Message.DIRECTION_INCOMING, false, null, null, null, null);

        return searchResult.getList();
    }

    @Override
    public Message newMessageGet(ConnectionSet conSet, String messageId) throws BGException {
        return new MessageDAO(conSet.getConnection()).getMessageBySystemId(id, messageId);
    }

    @Override
    public void messageDelete(ConnectionSet conSet, String... messageIds) throws BGException {
        MessageDAO messageDao = new MessageDAO(conSet.getConnection());

        for (String messageId : messageIds) {
            Message message = messageDao.getMessageBySystemId(id, messageId);
            if (message != null) {
                messageDao.deleteMessage(message.getId());
            }
        }
    }

    @Override
    public Message newMessageLoad(Connection con, String messageId) throws BGException {
        Message result = null;

        MessageDAO messageDao = new MessageDAO(con);

        result = messageDao.getMessageBySystemId(id, messageId);
        result.setProcessed(true);

        messageDao.updateMessage(result);

        return result;
    }
}