package ru.bgerp.plugin.telegram;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.expression.ExpressionBasedFunction;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgerp.plugin.telegram.bot.BgerpBot;

public class DefaultProcessorFunctions extends ExpressionBasedFunction {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(DefaultProcessorFunctions.class);

    public DefaultProcessorFunctions() {
    }

    /**
     * Отправляет сообщение пользователям.
     * 
     * @param userIds
     * @param text
     * @throws BGException
     */
    public void sendMessage(Collection<Integer> userIds, String text) {
        Config config = Setup.getSetup().getConfig(Config.class);
        sendMassageForObject(userIds, config.getParamId(), text);
    }

    /**
     * Отправляет сообщение исполнителям процесса.
     * 
     * @param process
     * @param text
     * @throws BGException
     */

    public void sendMessage(Process process, String text) throws BGException {
        sendMessage(process.getExecutorIds(), text);
        //а также если есть чат привязанный к процесу то и в него 
        sendMassageForProcess(process,text);
    }
    /**
     * Отправляет сообщение в чат процесса.
     * 
     * @param process
     * @param text
     * @throws BGException
     */
    public void sendMassageForProcess (Process process, String text)
    {
        Config config = Setup.getSetup().getConfig(Config.class);
        sendMassageForObject(Collections.singletonList(process.getId()), config.getProcessParamId(), text);
    }
    
    private void sendMassageForObject (Collection<Integer> objectIds, int paramId, String text)
    {
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        BgerpBot bot = BgerpBot.getInstance();
        if (bot == null) {
            log.info("В конфигурации сервера бот выключен(telegram.bot_start)");
            return;
        }
        try {
            ParamValueDAO paramDAO = new ParamValueDAO(con);
            for (int userId : objectIds) {
                Long chatId = Utils.parseLong(paramDAO.getParamText(userId, paramId), -1L);
                if (chatId != -1L) {
                    bot.sendMessage(chatId, text);
                }
            }
        } catch (Exception ex) {
            log.error("Ошибка отправки сообщения в telegram", ex);
        } finally {
            SQLUtils.closeConnection(con);
        } 
    }
    
    
    

}