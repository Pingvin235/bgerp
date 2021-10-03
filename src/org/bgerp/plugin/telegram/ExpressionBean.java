package org.bgerp.plugin.telegram;

import java.util.Collection;
import java.util.Collections;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgerp.util.Log;

public class ExpressionBean {
    private static final Log log = Log.getLog();

    /**
     * Send message in a chat.
     *
     * @param chatId telegram chatId
     * @param text   text message
     */
    public void sendMessage(String chatId, String text) {
        if (chatId != null && !chatId.trim().isEmpty()) {
            Bot bot = Bot.getInstance();
            if (bot == null) {
                return;
            }
            log.debug("Send message: {}, chatId: {}", text, chatId);
            bot.sendMessage(chatId, text);
        }
    }

    /**
     * Send message to users.
     *
     * @param userIds user IDs
     * @param text    text message
     */
    public void sendMessage(Collection<Integer> userIds, String text) {
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
     */

    public void sendMessage(Process process, String text) {
        sendMessage(process.getExecutorIds(), text);
    }

    /**
     * Send message in chat process.
     *
     * @param process object process
     * @param text    text message
     */
    public void sendMessageForProcess(Process process, String text) {
        Config config = Setup.getSetup().getConfig(Config.class);
        sendMessageForObject(Collections.singletonList(process.getId()), config.getProcessParamId(), text);
    }

    private void sendMessageForObject(Collection<Integer> objectIds, int paramId, String text) {
        Bot bot = Bot.getInstance();
        if (bot == null) {
            return;
        }

        try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            ParamValueDAO paramDAO = new ParamValueDAO(con);
            for (int userId : objectIds) {
                String chatId = paramDAO.getParamText(userId, paramId);
                if (Utils.notBlankString(chatId)) {
                    bot.sendMessage(chatId, text);
                }
            }
        } catch (Exception ex) {
            log.error("Error send message in telegram", ex);
        }
    }

}