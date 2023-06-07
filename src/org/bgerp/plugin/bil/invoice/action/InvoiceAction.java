package org.bgerp.plugin.bil.invoice.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;
import org.bgerp.plugin.bil.invoice.Config;
import org.bgerp.plugin.bil.invoice.Plugin;
import org.bgerp.plugin.bil.invoice.dao.InvoiceDAO;
import org.bgerp.plugin.bil.invoice.dao.InvoiceSearchDAO;
import org.bgerp.plugin.bil.invoice.model.Invoice;
import org.bgerp.plugin.bil.invoice.model.InvoiceType;
import org.bgerp.plugin.bil.invoice.model.Position;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/invoice/invoice")
public class InvoiceAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward list(DynActionForm form, ConnectionSet conSet) throws Exception {
        new InvoiceSearchDAO(conSet.getSlaveConnection())
            .withProcessId(form.getParamInt("processId"))
            .orderFromDate()
            .orderDesc()
            .search(new Pageable<>(form));

        form.setRequestAttribute("config", setup.getConfig(Config.class));

        return html(conSet, form, PATH_JSP + "/process/list.jsp");
    }

    public ActionForward create(DynActionForm form, ConnectionSet conSet) throws Exception {
        int typeId = form.getParamInt("typeId");
        if (typeId > 0) {
            var config = setup.getConfig(Config.class);

            int processId = form.getParamInt("processId", val -> val > 0);
            var month = form.getParamYearMonth("dateFrom", Objects::nonNull);

            form.setResponseData("invoice", config.getType(typeId).invoice(conSet, processId, month));
            form.setResponseData("positions", config.getPositions());

            return html(conSet, form, PATH_JSP + "/edit.jsp");
        } else {
            form.setRequestAttribute("types", setup.getConfig(Config.class).getTypes());
            return html(conSet, form, PATH_JSP + "/process/create.jsp");
        }
    }

    public ActionForward get(DynActionForm form, ConnectionSet conSet) throws Exception {
        var invoice = new InvoiceDAO(conSet.getConnection()).getOrThrow(form.getId());
        form.setResponseData("invoice", invoice);
        form.setResponseData("positions", setup.getConfig(Config.class).getPositions());
        return html(conSet, form, PATH_JSP + "/edit.jsp");
    }

    public ActionForward update(DynActionForm form, ConnectionSet conSet) throws Exception {
        var dao = new InvoiceDAO(conSet.getConnection());

        Invoice invoice;
        if (form.getId() <= 0) {
            int typeId = form.getParamInt("typeId", val -> val > 0);
            int processId = form.getParamInt("processId", val -> val > 0);
            var month = form.getParamYearMonth("dateFrom", Objects::nonNull);

            var type = setup.getConfig(Config.class).getType(typeId);
            invoice =  type.invoice(conSet, processId, month);
            type.getNumberProvider().number(conSet.getConnection(), type, invoice);
        } else {
            invoice = dao.get(form.getId());
        }

        var positions = new ArrayList<Position>();

        var ids = form.getSelectedValuesListStr("pos_id");
        var titles = form.getSelectedValuesListStr("pos_title");
        var amounts = form.getSelectedValuesListStr("pos_amount");
        var units = form.getSelectedValuesListStr("pos_unit");
        var quantities = form.getSelectedValuesList("pos_quantity");

        for (int i = 0; i < ids.size(); i++)
            positions.add(new Position(ids.get(i),
                titles.get(i),
                Utils.parseBigDecimal(amounts.get(i)),
                units.get(i),
                quantities.get(i)
            ));

        invoice.setPositions(positions);
        invoice.amount();

        dao.update(invoice);

        return json(conSet, form);
    }

    public ActionForward delete(DynActionForm form, ConnectionSet conSet) throws Exception {
        new InvoiceDAO(conSet.getConnection()).delete(form.getId());
        return json(conSet, form);
    }

    /**
     * Prepares all required data for print form generation.
     * @param conSet
     * @param form all data are set as response data to the form.
     * @param id invoice ID.
     * @return type of invoice.
     * @throws Exception
     */
    public static InvoiceType doc(ConnectionSet conSet, DynActionForm form, int id) throws Exception {
        Connection slaveCon = conSet.getSlaveConnection();

        var invoice = new InvoiceDAO(slaveCon).getOrThrow(id);
        var process = new ProcessDAO(slaveCon).getProcess(invoice.getProcessId());

        var customerDao = new CustomerDAO(slaveCon);
        var linkDao = new ProcessLinkDAO(slaveCon);
        var paramDao = new ParamValueDAO(slaveCon);

        var config = Setup.getSetup().getConfig(Config.class);
        var type = config.getType(invoice.getTypeId());

        form.setResponseData("invoice", invoice);

        var invoiceCustomer = customerDao.getCustomerById(type.getCustomerId());
        if (invoiceCustomer != null) {
            form.setResponseData("invoiceCustomer", invoiceCustomer);
            form.setResponseData("invoiceCustomerParam", paramDao.parameters(invoiceCustomer));
        }

        form.setResponseData("process", process);
        form.setResponseData("processParam", paramDao.parameters(process));

        var customer = Utils.getFirst(linkDao.getLinkCustomers(process.getId(), null));
        if (customer != null) {
            form.setResponseData("customer", customer);
            form.setResponseData("customerParam", paramDao.parameters(customer));
        }

        return type;
    }

    public ActionForward doc(DynActionForm form, ConnectionSet conSet) throws Exception {
        var type = doc(conSet, form, form.getId());
        return html(conSet, form, type.getJsp());
    }

    public ActionForward paid(DynActionForm form, ConnectionSet conSet) throws Exception {
        var dao = new InvoiceDAO(conSet.getConnection());

        var invoice = dao.getOrThrow(form.getId());
        invoice.setPaymentDate(form.getParamDate("date", new Date()));
        invoice.setPaymentUserId(form.getUserId());

        dao.update(invoice);

        // the process is taken without isolation, as the action method is called from report
        var process = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(invoice.getProcessId());
        var customer = Utils.getFirst(new ProcessLinkDAO(conSet.getSlaveConnection(), form).getLinkCustomers(process.getId(), null));
        String customerTitle = customer != null ? customer.getTitle() : "???";
        var message = new Message()
            .withDirection(Message.DIRECTION_INCOMING)
            .withText(l.l("invoice.paid.notification.message",
                invoice.getNumber(),
                customerTitle,
                TimeUtils.format(invoice.getDateFrom(), "yyyy.MM"),
                Utils.format(invoice.getAmount()),
                TimeUtils.format(invoice.getCreatedTime(), TimeUtils.FORMAT_TYPE_YMD)));
        EventProcessor.processEvent(new ProcessMessageAddedEvent(form, message, process), conSet);

        return json(conSet, form);
    }

    public ActionForward unpaid(DynActionForm form, ConnectionSet conSet) throws Exception {
        var dao = new InvoiceDAO(conSet.getConnection());

        var invoice = dao.getOrThrow(form.getId());
        invoice.setPaymentDate(null);
        invoice.setPaymentUserId(0);

        dao.update(invoice);

        return json(conSet, form);
    }
}
