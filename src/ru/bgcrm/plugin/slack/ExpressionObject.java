package ru.bgcrm.plugin.slack;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.msg.Message;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.slack.dao.MessageTypeChannel;
import ru.bgcrm.plugin.slack.dao.SlackProto;
import ru.bgcrm.struts.action.LinkAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class ExpressionObject implements org.bgerp.dao.expression.ExpressionObject {
    @Override
    public void toContext(Map<String, Object> context) {
        context.put(Plugin.ID, this);
    }

    /**
     * Вызывает {@link #linkChannel(Process, String)} c name = null.
     * @param process
     */
    public void linkChannel(Process process) {
        linkChannel(process, null);
    }

    /**
     * Привязывает процесс к каналу Slack, если ещё не привязан.
     * @param process процесса.
     * @param channelName наименование канала, если null - будет использован код процесса.
     */
    public void linkChannel(Process process, String channelName) {
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            ProcessLinkDAO linkDao = new ProcessLinkDAO(con);
            MessageTypeChannel type = MessageTypeChannel.getMessageType();

            SlackProto proto = new SlackProto(type.getToken());

            CommonObjectLink link = Utils
                    .getFirst(linkDao.getObjectLinksWithType(process.getId(), Plugin.LINK_TYPE_CHANNEL));
            if (link == null) {
                String channelId = proto.channelCreate(channelName != null ? channelName : String.valueOf(process.getId()), true);
                link = new CommonObjectLink(Process.OBJECT_TYPE, process.getId(), Plugin.LINK_TYPE_CHANNEL,
                        1, channelId);
                LinkAction.addLink(DynActionForm.SYSTEM_FORM, con, link);
            }
            con.commit();
        } catch (BGException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BGException(ex);
        } finally {
            SQLUtils.closeConnection(con);
        }
    }

    /**
     * Приглашает исполнителей процесса в привязанный канал.
     * @param process
     */
    public void inviteExecutors(Process process) {
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            ProcessLinkDAO linkDao = new ProcessLinkDAO(con);
            ParamValueDAO paramDAO = new ParamValueDAO(con);

            MessageTypeChannel type = MessageTypeChannel.getMessageType();
            SlackProto proto = new SlackProto(type.getToken());

            // приглашение исполнителей
            if (type.getAccountParam() != null && !process.getExecutorIds().isEmpty()) {
                CommonObjectLink link = Utils
                        .getFirst(linkDao.getObjectLinksWithType(process.getId(), Plugin.LINK_TYPE_CHANNEL));
                if (link == null)
                    return;

                String channelId = link.getLinkObjectTitle();

                // ключ - имя аккаунта, значение - должен быть код для использования в API
                Map<String, String> accountMap = new HashMap<>();

                for (int userId : process.getExecutorIds()) {
                    String slackAccount = paramDAO.getParamText(userId, type.getAccountParam().getId());
                    if (Utils.notBlankString(slackAccount))
                        accountMap.put(slackAccount, null);
                }

                // преобразование аккаунтов в коды
                if (!accountMap.isEmpty()) {
                    proto.userList().get("members").forEach(n -> {
                        String name = n.get("name").asText();
                        if (accountMap.containsKey(name))
                            accountMap.put(name, n.get("id").asText());
                    });
                }

                List<String> userIds = new ArrayList<>();
                for (Map.Entry<String, String> me : accountMap.entrySet()) {
                    if (me.getValue() == null)
                        throw new BGMessageException("Не найден аккаунт Slack: " + me.getKey());
                    userIds.add(me.getValue());
                }

                if (!userIds.isEmpty())
                    proto.channelInviteUsers(channelId, userIds);
            }
        } catch (Exception ex) {
            throw new BGException(ex);
        } finally {
            SQLUtils.closeConnection(con);
        }
    }

    /**
     * Установливает топик привязанного к процессу канала.
     * @param process
     * @param topic
     */
    public void setTopic(Process process, String topic) {
        findChannelAndDo(process, (proto, channelId) -> {
            proto.channelSetTopic(channelId, topic);
        });
    }

    /**
     * Установливает назачение привязанного к процессу канала.
     * @param process
     * @param purpose
     */
    public void setPurpose(Process process, String purpose) {
        findChannelAndDo(process, (proto, channelId) -> {
            proto.channelSetPurpose(channelId, purpose);
        });
    }

    /**
     * Изменяет статус архив / не архив привязанного к процессу канала.
     * @param process
     * @param archive - статус.
     */
    public void archive(Process process, boolean archive) {
        findChannelAndDo(process, (proto, channelId) -> {
            if (archive)
                proto.channelArchive(channelId);
            else
                proto.channelUnArchive(channelId);
        });
    }

    /**
     * Отправляет сообщение в привязанный к процессу канал.
     * @param process
     * @param userId
     * @param messageText
     */
    public void sendMessage(Process process, int userId, String messageText) {
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            MessageTypeChannel type = MessageTypeChannel.getMessageType();

            Message message = new Message();
            message.setUserId(userId);
            message.setTypeId(type.getId());
            message.setDirection(Message.DIRECTION_OUTGOING);
            message.setFromTime(new Date());
            message.setProcessId(process.getId());
            message.setSubject("");
            message.setTo("");
            message.setText(messageText);

            type.updateMessage(con, DynActionForm.SYSTEM_FORM, message);
        } catch (Exception ex) {
            throw new BGException(ex);
        } finally {
            SQLUtils.closeConnection(con);
        }
    }

    private static interface ChannelOperation {

        public void perform(SlackProto proto, String channelId) throws Exception;

    }

    private void findChannelAndDo(Process process, ChannelOperation operation) {
        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            ProcessLinkDAO linkDao = new ProcessLinkDAO(con);
            MessageTypeChannel type = MessageTypeChannel.getMessageType();

            SlackProto proto = new SlackProto(type.getToken());
            CommonObjectLink link = Utils
                    .getFirst(linkDao.getObjectLinksWithType(process.getId(), Plugin.LINK_TYPE_CHANNEL));
            if (link != null) {
                String channelId = link.getLinkObjectTitle();
                operation.perform(proto, channelId);
            }
        } catch (Exception ex) {
            throw new BGException(ex);
        } finally {
            SQLUtils.closeConnection(con);
        }
    }
}