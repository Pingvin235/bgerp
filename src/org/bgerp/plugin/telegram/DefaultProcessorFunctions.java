package org.bgerp.plugin.telegram;

import org.bgerp.plugin.telegram.bot.BgerpBot;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.expression.ExpressionBasedFunction;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgerp.util.Log;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;

public class DefaultProcessorFunctions extends ExpressionBasedFunction {

    private static final Log log = Log.getLog();

    public DefaultProcessorFunctions() {}

    /**
     * Send message to users.
     *
     * @param userIds user IDs
     * @param text    text message
     * @throws BGException
     */
    public void sendMessage(Collection<Integer> userIds, String text) throws BGException {
        Config config = Setup.getSetup().getConfig(Config.class);

        if (userIds.isEmpty()) {
            userIds.add(0);
        }
        sendMessageForObject(userIds, config.getParamId(), text);
    }

    /**
     * Send a message to the executor of the process.
     *
     * @param process object process
     * @param text    text message
     * @throws BGException
     */

    public void sendMessage(Process process, String text) throws BGException {
        sendMessage(process.getExecutorIds(), text);
    }

    /**
     * Send message in chat process.
     *
     * @param process object process
     * @param text    text message
     * @throws BGException
     */
    public void sendMessageForProcess(Process process, String text) throws BGException {
        Config config = Setup.getSetup().getConfig(Config.class);
        sendMessageForObject(Collections.singletonList(process.getId()), config.getProcessParamId(), text);
    }

    private void sendMessageForObject(Collection<Integer> objectIds, int paramId, String text) {
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        BgerpBot bot = BgerpBot.getInstance();
        if (bot == null) {
            log.info("In config server not enable bot (telegram:botStart)");
            return;
        }
        try {
            ParamValueDAO paramDAO = new ParamValueDAO(con);
            for (int userId : objectIds) {
                Long chatId = Utils.parseLong(paramDAO.getParamText(userId, paramId), -1L);
                if (chatId != -1L) {
                    log.debug("Send message: %s; chatId: %s", text, chatId);
                    bot.sendMessage(chatId, text);
                }
            }
        } catch (Exception ex) {
            log.error("Error send message in telegram", ex);
        } finally {
            SQLUtils.closeConnection(con);
        }
    }

}