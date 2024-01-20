package org.bgerp.plugin.bil.subscription.action.open;

import java.math.BigDecimal;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.plugin.bil.subscription.Config;
import org.bgerp.plugin.bil.subscription.Plugin;
import org.bgerp.plugin.bil.subscription.dao.SubscriptionDAO;

import javassist.NotFoundException;
import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/plugin/subscription/subscription")
public class SubscriptionAction extends BaseAction {
    private static final String JSP_PATH = Plugin.PATH_JSP_OPEN;

    // not more often request from a single IP as one per 10 min.
    // private static final AntiSpam ORDER_ANTI_SPAM = new AntiSpam(Duration.ofMinutes(10));

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        int queueId = form.getParamInt("queueId");
        var queue = ProcessQueueCache.getQueue(queueId);
        if (queue == null)
            throw new NotFoundException("Not found process queue with ID=" + queueId);

        String uri = form.getHttpRequestURI();
        var processor = queue.getProcessorByPageUrl(uri);
        if (processor == null)
            throw new NotFoundException("Not found processor for page URL: " + uri);

        var config = setup.getConfig(Config.class);

        int subscriptionId = processor.getConfigMap().getInt("subscription.value");
        if (subscriptionId > 0)
            form.setRequestAttribute("subscriptionId", subscriptionId);
        else
            form.setRequestAttribute("subscriptions", config.getSubscriptions());

        if (config.getParamLimitId() > 0)
            form.setRequestAttribute("limits", ru.bgcrm.cache.ParameterCache.getListParamValues(config.getParamLimitId()));

        return html(conSet, form, JSP_PATH + "/subscription.jsp");
    }

    public ActionForward calc(DynActionForm form, ConnectionSet conSet) throws Exception {
        var subscription = setup.getConfig(Config.class).getSubscriptionOrThrow(form.getParamInt("subscriptionId"));
        int limitId = form.getParamInt("limitId");
        var processIds = Utils.toIntegerSet(form.getParam("processIds"));

        BigDecimal cost = new SubscriptionDAO(conSet.getSlaveConnection()).getCost(subscription, limitId, processIds);
        form.setResponseData("cost", cost);

        return html(conSet, form, JSP_PATH + "/subscription_calc.jsp");
    }

    /* public ActionForward order(DynActionForm form, ConnectionSet conSet) throws Exception {
        orderAntiSpam(form);

        var config = setup.getConfig(Config.class);
        var subscription = config.getSubscription(form.getParamInt("subscriptionId"));
        int limitId = form.getParamInt("limitId");
        var processIds = Utils.toIntegerSet(form.getParam("processId"));

        var email = form.getParam("email", Utils::isValidEmail);

        var con = conSet.getConnection();

        // process creation with generation all the events
        var process = new Process();
        process.setTypeId(subscription.getProcessTypeId());
        ProcessAction.processCreate(form, con, process);

        // may be extracted to a separated file in log4j.properties
        log.info("Created subscription process: {}", process.getId());

        // adding parameters without events
        var paramDao = new ParamValueDAO(con);
        paramDao.updateParamList(process.getId(), config.getParamSubscriptionId(), Set.of(subscription.getId()));
        paramDao.updateParamList(process.getId(), config.getParamLimitId(), Set.of(limitId));
        paramDao.updateParamEmail(process.getId(), config.getParamEmailId(), 0, new ParameterEmailValue(email));

        var linkDao = new ProcessLinkDAO(con);
        for (int processId : processIds) {
            linkDao.addLink(new ProcessLinkProcess.Depend(process.getId(), processId));
        }

        // email(conSet, email, process, processIds);

        return json(con, form);
    } */

    /* private void email(ConnectionSet conSet, Config config, String email, Process process,
            Set<Integer> processIds) throws Exception {
        var query =
            "SELECT description FROM " + Tables.TABLE_PROCESS + " AS p " +
            "WHERE id IN (" + Utils.toString(processIds) + ")";

        var m = new Message()
            .withProcessId(process.getId())
            .withTypeId(config.getMessageTypeEmailId())
            .withDirection(Message.DIRECTION_OUTGOING)
            .withFromTime(new Date())
            .withSubject(l.l("subscription.main.subject"))
            .withText(l.l("subscription.mail.text"))
            .withTo(email);
        new MessageDAO(conSet.getConnection()).updateMessage(m);
    } */

    /* private void orderAntiSpam(DynActionForm form) throws BGMessageException {
        var ip = form.getHttpRequestRemoteAddr();
        if (Utils.isBlankString(ip))
            throw new BGMessageException("IP адрес не определён.");

        var wait = ORDER_ANTI_SPAM.getWaitTimeout(ip);
        if (wait > 0)
            throw new BGMessageException("Следующий заказ с этого IP можно сделать через: {}",
                TimeUtils.formatDeltaTime(wait));
    } */
}
