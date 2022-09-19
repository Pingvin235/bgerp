package org.bgerp.plugin.msg.email.action;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.msg.email.config.RecipientsConfig;
import org.bgerp.plugin.msg.email.dao.EMailDAO;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/email/email")
public class EMailAction extends BaseAction {
    public ActionForward recipients(DynActionForm form, ConnectionSet conSet) throws Exception {
        int processId = form.getParamInt("processId", Utils::isPositive);

        var config = setup.getConfig(RecipientsConfig.class);

        var values = Collections.synchronizedSortedSet(new TreeSet<>());

        var executors = Executors.newFixedThreadPool(config.areas().size());

        for (var area : config.areas()) {
            executors.submit(() -> {
                try (var con = setup.getDBSlaveConnectionFromPool()) {
                    var dao = new EMailDAO(con);

                    List<ParameterEmailValue> emails = null;

                    if (RecipientsConfig.AREA_USERS.equals(area)) {
                        emails = dao.getUserEmails(null);
                    } else if (RecipientsConfig.AREA_PROCESS.equals(area)) {
                        emails = dao.getProcessEmails(processId);
                    } else if (RecipientsConfig.AREA_EXECUTORS.equals(area)) {
                        var process = new ProcessDAO(con).getProcessOrThrow(processId);
                        emails = dao.getUserEmails(process.getExecutorIds());
                    } else if (RecipientsConfig.AREA_PROCESS_CUSTOMERS.equals(area)) {
                        var ids = new ProcessLinkDAO(con).getLinkCustomers(processId, Customer.OBJECT_TYPE + "%").stream()
                            .map(Customer::getId)
                            .collect(Collectors.toSet());
                        if (!ids.isEmpty())
                            emails = dao.getCustomerEmails(ids);
                    } else
                        throw new Exception("Unknown area: " + area);

                    if (emails != null)
                        values.addAll(emails.stream().map(ParameterEmailValue::toString).collect(Collectors.toList()));

                } catch (Exception e) {
                    log.error(e);
                }
            });
        }

        executors.shutdown();

        if (!executors.awaitTermination(2, TimeUnit.MINUTES))
            log.error("Timeout waiting threads");

        form.setResponseData("values", values);

        return json(conSet, form);
    }

}
