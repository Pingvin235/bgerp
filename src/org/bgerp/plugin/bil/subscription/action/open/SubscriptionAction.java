package org.bgerp.plugin.bil.subscription.action.open;

import java.math.BigDecimal;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.cache.ProcessQueueCache;
import org.bgerp.plugin.bil.subscription.Config;
import org.bgerp.plugin.bil.subscription.Plugin;
import org.bgerp.plugin.bil.subscription.dao.SubscriptionDAO;

import javassist.NotFoundException;
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

        String uri = form.getRequestURI();
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
            form.setRequestAttribute("limits", org.bgerp.cache.ParameterCache.getListParamValues(config.getParamLimitId()));

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
}
