package org.bgerp.plugin.telegram;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.cache.UserCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.util.Log;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

/**
 * Telegram JEXL API for sending messages to chats.
 * Generic method {@link #sendMessage(Collection, String, String)} sends message in different formats to chat ID.
 * There are three formats available, more about those: https://core.telegram.org/bots/api#formatting-options
 * Chat IDs for sending can be a also stored in user or process {@code text} parameter with ID configured as {@code telegram:userParamId} or {@code telegram:processParamId} respectively.
 */
public class ExpressionObject implements org.bgerp.dao.expression.ExpressionObject {
    private static final Log log = Log.getLog();

    private static final Set<Character> SPECIAL_CHARACTERS_MD = Set.of('(', ')');

    private static final Set<String> PARSE_MODES = Set.of("MarkdownV2", "HTML");

    /**
     * Escape a Markdown text from {@link #SPECIAL_CHARACTERS_MD}
     * @param text the Markdown text
     * @return the {@code text} with escaped special characters
     */
    public static String escapeMarkdown(String text) {
        StringBuilder result = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (SPECIAL_CHARACTERS_MD.contains(c))
                result.append("\\");
            result.append(c);
        }

        return result.toString();
    }

    /**
     * Public constructor, can be used in Custom
     */
    public ExpressionObject() {}

    @Override
    public void toContext(Map<String, Object> context) {
        context.put(Plugin.ID, this);
    }

    /**
     * Send a plain text message to a chat
     * @param chatId the chat ID
     * @param text the message
     */
    public void sendMessage(String chatId, String text) {
        sendMessage(chatId, text, null);
    }

    /**
     * Send a message to a chat with a specific format
     * @param chatId the chat ID
     * @param text the message
     * @param parseMode the format: {@code null} - plain text, {@code MarkdownV2}, {@code HTML}
     */
    public void sendMessage(String chatId, String text, String parseMode) {
        Bot bot = Bot.getInstance();
        if (bot == null)
            throw new IllegalStateException("Telegram bot is not running");

        if (Utils.isBlankString(chatId))
            throw new IllegalArgumentException("chatId is not defined");

        if (parseMode != null && !PARSE_MODES.contains(parseMode))
            throw new BGException("Unsupported parseMode: {}", parseMode);

        if (chatId != null && !chatId.trim().isEmpty()) {
            log.debug("Send message: {}, chatId: {}, parseMode: {}", text, chatId, parseMode);

            if (parseMode == null)
                bot.sendMessage(chatId, text);
            else
                bot.sendMessage(chatId, text, parseMode);
        }
    }

    /**
     * Send a plain text message to users
     * @param userIds the user IDs
     * @param text the message
     */
    public void sendMessage(Collection<Integer> userIds, String text) {
        sendMessage(userIds, text, null);
    }

    /**
     * Send a message with a specific format to users
     * @param userIds the user IDs
     * @param text the message
     * @param parseMode the format: {@code null} - plain text, {@code MarkdownV2}, {@code HTML}
     */
    public void sendMessage(Collection<Integer> userIds, String text, String parseMode) {
        Config config = Setup.getSetup().getConfig(Config.class);

        Collection<Integer> activeUserIds = userIds.stream()
                .map(UserCache::getUser)
                .filter(user -> user != null && user.getStatus() == User.STATUS_ACTIVE)
                .map(User::getId)
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return;
        }

        sendMessageForObject(activeUserIds, config.getUserParamId(), text, parseMode);
    }

    /**
     * Send a plain text message to a process executors
     * @param process the process with the executor user IDs
     * @param text the message
     */
    public void sendMessage(Process process, String text) {
        sendMessage(process.getExecutorIds(), text, null);
    }

    /**
     * Send a message with a specific format to a process executors
     * @param process the process with the executor user IDs
     * @param text the message
     * @param parseMode the format: {@code null} - plain text, {@code MarkdownV2}, {@code HTML}
     */
    public void sendMessage(Process process, String text, String parseMode) {
        sendMessage(process.getExecutorIds(), text, parseMode);
    }

    /**
     * Send a plain text message to a process
     * @param process the process
     * @param text the message
     */
    public void sendMessageForProcess(Process process, String text) {
        Config config = Setup.getSetup().getConfig(Config.class);
        sendMessageForObject(Collections.singletonList(process.getId()), config.getProcessParamId(), text, null);
    }

    private void sendMessageForObject(Collection<Integer> objectIds, int paramId, String text, String parseMode) {
        try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            ParamValueDAO paramDAO = new ParamValueDAO(con);
            for (int objectId : objectIds) {
                String chatId = paramDAO.getParamText(objectId, paramId);
                if (Utils.notBlankString(chatId))
                    sendMessage(chatId, text, parseMode);
            }
        } catch (SQLException ex) {
            log.error(ex);
        }
    }
}