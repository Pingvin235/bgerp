package ru.bgcrm.dao.message;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.dao.expression.Expression;
import org.bgerp.dao.expression.ProcessExpressionObject;
import org.bgerp.dao.expression.ProcessParamExpressionObject;
import org.bgerp.dao.message.MessageSearchDAO;
import org.bgerp.dao.message.call.CallRegistration;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.msg.Message;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import ru.bgcrm.model.Pair;
import ru.bgcrm.model.param.ParameterPhoneValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Bean
public class MessageTypeCall extends MessageType {
    private static final Log log = Log.getLog();

    /**
     * Registered numbers. Static field because of reload the message type on configuration change.
     * Top-level key is the message type ID, value is pair of maps, registrations by number and user ID.
    */
    private static final Map<Integer, Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>>> REGISTER = new ConcurrentHashMap<>();

    public MessageTypeCall(Setup setup, int id, ConfigMap config) {
        super(setup, id, config.get("title"), config);
    }

    @Override
    public boolean isProcessChangeSupport() {
        return true;
    }

    /**
     * Retrieves user offered number from text parameter.
     * @param userId user entity ID.
     * @return parameter value or empty string.
     */
    @Dynamic
    public String getUserOfferedNumber(int userId) {
        int paramId = configMap.getInt("offerNumberFromParamId");
        if (paramId > 0) {
            try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
                String value = new ParamValueDAO(con).getParamText(userId, paramId);
                return Utils.maskNull(value);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return "";
    }

    @Dynamic
    public void outNumbersPreprocess(ParameterPhoneValue value, Process process) {
        String expression = configMap.get(Expression.EXPRESSION_CONFIG_KEY + "OutNumberPreprocess");
        if (Utils.isBlankString(expression))
            return;

        try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            var context = new HashMap<String, Object>();
            context.put("value", value);
            if (process != null) {
                new ProcessExpressionObject(process).toContext(context);
                new ProcessParamExpressionObject(con, process.getId()).toContext(context);
            }
            new Expression(context).execute(expression);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    private Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>> getRegMaps() {
        return REGISTER.computeIfAbsent(id, unused -> new Pair<>(new ConcurrentHashMap<>(), new ConcurrentHashMap<>()));
    }

    public void numberRegister(int userId, String number) {
        Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>> regMaps = getRegMaps();

        CallRegistration reg = regMaps.getFirst().get(number);
        if (reg == null) {
            reg = new CallRegistration(userId, number);
            regMaps.getFirst().put(number, reg);
            regMaps.getSecond().put(userId, reg);
        }
    }

    public void numberFree(int userId) {
        Pair<Map<String, CallRegistration>, Map<Integer, CallRegistration>> regMaps = getRegMaps();

        CallRegistration reg = regMaps.getSecond().get(userId);
        if (reg != null) {
            regMaps.getFirst().remove(reg.getNumber());
            regMaps.getSecond().remove(reg.getUserId());
        }
    }

    public CallRegistration getRegistrationByUser(int userId) {
        return getRegMaps().getSecond().get(userId);
    }

    public CallRegistration getRegistrationByNumber(String number) {
        return getRegMaps().getFirst().get(number);
    }

    @Override
    public void updateMessage(Connection con, DynActionForm form, Message message) throws SQLException {
        new MessageDAO(con).updateMessage(message);
    }

    @Override
    public List<Message> newMessageList(ConnectionSet conSet) throws SQLException {
        Pageable<Message> searchResult = new Pageable<>();

        new MessageSearchDAO(conSet.getConnection())
            .withTypeId(id)
            .withDirection(Message.DIRECTION_INCOMING)
            .withProcessed(false)
            .search(searchResult);

        return searchResult.getList();
    }

    @Override
    public Message newMessageGet(ConnectionSet conSet, String messageId) throws Exception {
        return new MessageDAO(conSet.getConnection()).getMessageBySystemId(id, messageId);
    }

    @Override
    public void messageDelete(ConnectionSet conSet, String... messageIds) throws Exception {
        MessageDAO messageDao = new MessageDAO(conSet.getConnection());

        for (String messageId : messageIds) {
            Message message = messageDao.getMessageBySystemId(id, messageId);
            if (message != null) {
                messageDao.deleteMessage(message.getId());
            }
        }
    }

    @Override
    public Message newMessageLoad(Connection con, String messageId) throws Exception {
        Message result = null;

        MessageDAO messageDao = new MessageDAO(con);

        result = messageDao.getMessageBySystemId(id, messageId);

        messageDao.updateMessage(result);

        return result;
    }
}