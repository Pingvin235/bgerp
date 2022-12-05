package org.bgerp.plugin.telegram;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.util.Log;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

public class ExpressionObject {
    private static final Log log = Log.getLog();

    private static final Set<Character> SPECIAL_CHARACTERS_MD = Set.of('(', ')');

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
     * Send message in a chat with specific formatting message
     *
     * @param chatId    telegram chatId
     * @param text      text message
     * @param parseMode ParseMode
     */
    public void sendMessage(String chatId, String text, String parseMode) {
        if (chatId != null && !chatId.trim().isEmpty() && !parseMode.trim().isEmpty()) {
            Bot bot = Bot.getInstance();
            if (bot == null) {
                return;
            }

            log.debug("Send message: {}, chatId: {}", text, chatId);

            bot.sendMessage(chatId, text, parseMode);
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

        Collection<Integer> activeUserIds = userIds.stream()
                .map(UserCache::getUser)
                .filter(user -> user != null && user.getStatus() == User.STATUS_ACTIVE)
                .map(User::getId)
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return;
        }

        sendMessageForObject(activeUserIds, config.getParamId(), text);
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
            for (int objectId : objectIds) {
                String chatId = paramDAO.getParamText(objectId, paramId);
                if (Utils.notBlankString(chatId)) {
                    bot.sendMessage(chatId, text);
                }
            }
        } catch (Exception ex) {
            log.error("Error send message in telegram", ex);
        }
    }

    /**
     * Escapes Markdown characters from {@link #SPECIAL_CHARACTERS_MD}.
     *
     * @param text
     * @return
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
}