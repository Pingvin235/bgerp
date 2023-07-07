package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForward;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.plugin.bgbilling.dao.MessageTypeHelpDesk;
import ru.bgcrm.plugin.bgbilling.proto.dao.HelpDeskDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.helpdesk.HdTopic;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/bgbilling/proto/helpdesk")
public class HelpDeskAction extends BaseAction {
    public ActionForward getAttach(DynActionForm form, ConnectionSet conSet) throws Exception {
        int processId = form.getParamInt("processId");
        String billingId = form.getParam("billingId");
        int attachId = form.getParamInt("id");
        String title = form.getParam("title");

        MessageTypeHelpDesk mt = (MessageTypeHelpDesk) setup.getConfig(MessageTypeConfig.class).getTypeMap().values().stream()
            .filter(type -> type instanceof MessageTypeHelpDesk && ((MessageTypeHelpDesk) type).getBillingId().equals(billingId))
            .findFirst().orElse(null);

        if (mt != null) {
            HelpDeskDAO hdDao = new HelpDeskDAO(mt.getUser(), mt.getDbInfo());

            var links = new ProcessLinkDAO(conSet.getConnection()).getObjectLinksWithType(processId, null);

            var topicLink = links.stream().filter(link -> link.getLinkedObjectType().equals(mt.getObjectType())).findFirst().orElse(null);
            if (topicLink == null)
                throw new BGException("К процессу не привязан топик HelpDesk.");

            var contractLink = links.stream().filter(link -> ("contract:" + billingId).equals(link.getLinkedObjectType())).findFirst().orElse(null);
            if (contractLink == null)
                throw new BGException("К процессу не привязан договор BGBilling.");

            var pair = hdDao.getTopicWithMessages(topicLink.getLinkedObjectId());

            HdTopic topic = pair != null ? pair.getFirst() : null;
            if (topic == null)
                throw new BGException("Не найдена тема HelpDesk с кодом: " + topicLink.getLinkedObjectId());

            byte[] attach = hdDao.getAttach(contractLink.getLinkedObjectId(), attachId);

            HttpServletResponse response = form.getHttpResponse();

            Utils.setFileNameHeaders(response, title);

            OutputStream out = response.getOutputStream();

            IOUtils.copy(new ByteArrayInputStream(attach), out);

            out.flush();
        }

        return null;
    }

    public ActionForward markMessageRead(DynActionForm form, ConnectionSet conSet) throws Exception {
        int messageId = form.getParamInt("messageId");
        if (messageId <= 0) {
            throw new BGIllegalArgumentException();
        }

        MessageDAO messageDao = new MessageDAO(conSet.getConnection());

        Message message = messageDao.getMessageById(messageId);
        if (message == null) {
            throw new BGException("Сообщение не найдено");
        }

        MessageTypeConfig config = setup.getConfig(MessageTypeConfig.class);

        MessageType mt = config.getTypeMap().get(message.getTypeId());
        if (mt == null || !(mt instanceof MessageTypeHelpDesk)) {
            throw new BGException("Не найден тип сообщения либо это не HelpDesk сообщение");
        }

        MessageTypeHelpDesk mtHd = (MessageTypeHelpDesk) mt;

        HelpDeskDAO hdDao = new HelpDeskDAO(form.getUser(), mtHd.getDbInfo());
        hdDao.markMessageRead(Utils.parseInt(message.getSystemId()));

        message.setToTime(new Date());
        message.setUserId(form.getUserId());

        messageDao.updateMessageProcess(message);

        return json(conSet, form);
    }
}