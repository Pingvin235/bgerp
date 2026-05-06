package org.bgerp.plugin.msg.max;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.Setup;
import org.bgerp.cache.UserCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.util.Log;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

/**
 * MAX JEXL API for sending messages to chats.
 * Same API structure as telegram ExpressionObject.
 *
 * Usage in doExpression:
 * <pre>
 *   max.sendMessage(chatId, "Hello")
 *   max.sendMessage(process, "Status changed")
 *   max.sendMessage(userIds, "Hello")
 *   max.sendMessageForProcess(process, "Hello")
 * </pre>
 */
public class ExpressionObject implements org.bgerp.dao.expression.ExpressionObject {
    private static final Log log = Log.getLog();

    public ExpressionObject() {}

    @Override
    public void toContext(Map<String, Object> context) {
        context.put(Plugin.ID, this);
    }

    /**
     * Send a plain text message to a chat.
     * @param chatId the chat ID
     * @param text the message
     * @return true if sent successfully
     */
    public boolean sendMessage(String chatId, String text) {
        return sendMessage(chatId, text, null);
    }

    /**
     * Send a message to a chat with a specific format.
     * @param chatId the chat ID
     * @param text the message
     * @param parseMode the format: {@code null} - plain text, {@code MarkdownV2}, {@code HTML}
     * @return true if sent successfully
     */
    public boolean sendMessage(String chatId, String text, String parseMode) {
        Bot bot = Bot.getInstance();
        if (bot == null)
            throw new IllegalStateException("MAX bot is not running");

        if (Utils.isBlankString(chatId))
            throw new IllegalArgumentException("chatId is not defined");

        if (chatId != null && !chatId.trim().isEmpty()) {
            log.debug("MAX send message: {}, chatId: {}, parseMode: {}", text, chatId, parseMode);
            return bot.sendMessage(chatId, text, parseMode);
        }

        return false;
    }

    /**
     * Send a plain text message to users.
     * @param userIds the user IDs
     * @param text the message
     */
    public boolean sendMessage(Collection<Integer> userIds, String text) {
        return sendMessage(userIds, text, null);
    }

    /**
     * Send a message with a specific format to users.
     * @param userIds the user IDs
     * @param text the message
     * @param parseMode the format
     */
    public boolean sendMessage(Collection<Integer> userIds, String text, String parseMode) {
        Config config = Setup.getSetup().getConfig(Config.class);

        Collection<Integer> activeUserIds = userIds.stream()
                .map(UserCache::getUser)
                .filter(user -> user != null && user.getStatus() == User.STATUS_ACTIVE)
                .map(User::getId)
                .collect(Collectors.toList());

        if (activeUserIds.isEmpty())
            return false;

        return sendMessageForObject(activeUserIds, config.getUserParamId(), text, parseMode);
    }

    /**
     * Send a plain text message to process executors.
     * @param process the process
     * @param text the message
     * @return true if all messages sent successfully
     */
    public boolean sendMessage(Process process, String text) {
        return sendMessage(process.getExecutorIds(), text, null);
    }

    /**
     * Send a message with a specific format to process executors.
     * @param process the process
     * @param text the message
     * @param parseMode the format
     * @return true if all messages sent successfully
     */
    public boolean sendMessage(Process process, String text, String parseMode) {
        return sendMessage(process.getExecutorIds(), text, parseMode);
    }

    /**
     * Send a plain text message to a process by process chatId.
     * @param process the process
     * @param text the message
     * @return true if sent successfully
     */
    public boolean sendMessageForProcess(Process process, String text) {
        Config config = Setup.getSetup().getConfig(Config.class);
        return sendMessageForObject(Collections.singletonList(process.getId()), config.getProcessParamId(), text, null);
    }

    private boolean sendMessageForObject(Collection<Integer> objectIds, int paramId, String text, String parseMode) {
        boolean allSent = true;
        try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            ParamValueDAO paramDAO = new ParamValueDAO(con);
            for (int objectId : objectIds) {
                String chatId = paramDAO.getParamText(objectId, paramId);
                if (Utils.notBlankString(chatId)) {
                    if (!sendMessage(chatId, text, parseMode))
                        allSent = false;
                }
            }
        } catch (SQLException ex) {
            log.error(ex);
            return false;
        }
        return allSent;
    }
}
