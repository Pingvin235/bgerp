package org.bgerp.plugin.msg.feedback.action.open;

import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.action.open.ProcessAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGSecurityException;
import org.bgerp.model.Pageable;
import org.bgerp.model.msg.Message;
import org.bgerp.model.msg.config.MessageTypeConfig;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.plugin.msg.email.message.MessageTypeEmail;
import org.bgerp.plugin.msg.feedback.Plugin;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/plugin/feedback/message")
public class MessageAction extends BaseAction {
    /**
     * Configuration for open feedback messages.
     */
    public static class Config extends org.bgerp.app.cfg.Config {
        /** Message type, used for creating incoming messages. */
        private final MessageTypeEmail messageTypeEmail;

        protected Config(ConfigMap setup, boolean validate) throws Exception {
            super(null, validate);
            messageTypeEmail = loadMessageTypeEmail(setup);
        }

        private MessageTypeEmail loadMessageTypeEmail(ConfigMap config) throws Exception {
            var messageTypeConfig = config.getConfig(MessageTypeConfig.class);

            config = config.sub(Plugin.ID + ":");

            var messageTypeEmailId = config.getInt("messageTypeEmailId");
            initWhen(messageTypeEmailId > 0);

            var messageTypeEmail = messageTypeConfig.getTypeMap().get(messageTypeEmailId);
            if (messageTypeEmail == null)
                throw new BGMessageException("No message type with ID: {} found", messageTypeEmailId);
            if (!(messageTypeEmail instanceof MessageTypeEmail))
                throw new BGMessageException("Wrong class instead of MessageTypeEmail: {}", messageTypeEmail.getClass());

            return (MessageTypeEmail) messageTypeEmail;
        }

        public MessageTypeEmail getMessageTypeEmail() {
            return messageTypeEmail;
        }
    }

    public ActionForward edit(DynActionForm form, ConnectionSet conSet) throws Exception {
        return html(conSet, null, Plugin.PATH_JSP_OPEN + "/editor.jsp");
    }

    public ActionForward add(DynActionForm form, ConnectionSet conSet) throws Exception {
        var configProcessOpen = setup.getConfig(ProcessAction.Config.class);

        int processId = form.getParamInt("processId");
        var process = new ProcessDAO(conSet.getSlaveConnection()).getProcess(processId);

        if (configProcessOpen == null || !configProcessOpen.isOpen(process, form))
            throw new BGSecurityException("Process is not open", form);

        var config = setup.getConfig(Config.class);
        if (config == null)
            throw new BGSecurityException("Feedback is not enabled", form);

        var subject = form.getParam("subject", Utils::notBlankString);
        var email = form.getParam("email", Utils::isValidEmail);
        var text = form.getParam("text", Utils::notBlankString);

        var messageType = config.getMessageTypeEmail();

        var message = new Message()
            .withUserId(form.getUserId())
            .withTypeId(messageType.getId())
            .withDirection(Message.DIRECTION_INCOMING)
            .withFrom(email)
            .withFromTime(new Date())
            .withTo(messageType.getEmail())
            .withProcessId(processId)
            .withSubject(subject)
            .withText(text);

        new MessageDAO(conSet.getConnection()).updateMessage(message);

        EventProcessor.processEvent(new ProcessMessageAddedEvent(form, message, process), conSet);

        linkCustomers(conSet, email, processId);

        form.getResponse().setMessage(l.l("Сообщение получено, ждите ответа на указанный Email"));

        return json(conSet, form);
    }

    private void linkCustomers(ConnectionSet conSet, String email, int processId) {
        var searchResult = new Pageable<ParameterSearchedObject<Customer>>();
        new CustomerDAO(conSet.getSlaveConnection()).searchCustomerListByEmail(searchResult, null, email);
        if (!searchResult.getList().isEmpty()) {
            var linkDao = new ProcessLinkDAO(conSet.getConnection());
            for (var link : searchResult.getList())
                linkDao.addLinkIfNotExist(new ProcessLink(processId, Customer.OBJECT_TYPE, link.getObject().getId(), link.getObject().getTitle()));
        }
    }
}
