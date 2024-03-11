package org.bgerp.plugin.bil.invoice.action;

import static org.bgerp.plugin.bil.invoice.dao.Tables.TABLE_INVOICE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.time.LocalDate;
import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.l10n.Localization;
import org.bgerp.plugin.bil.invoice.Config;
import org.bgerp.plugin.bil.invoice.Plugin;
import org.bgerp.plugin.report.action.ReportActionBase;
import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Columns;
import org.bgerp.plugin.report.model.Data;
import org.bgerp.util.TimeConvert;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/report/plugin/invoice/register")
public class ReportRegisterAction extends ReportActionBase {
    private static final Columns COLUMNS = new Columns(
        new Column.ColumnInteger("process_id", null, "Process"),
        new Column.ColumnInteger("invoice_id", null, null),
        new Column.ColumnString("invoice_type", null, "Type"),
        new Column.ColumnDecimal("invoice_amount", null, "Amount"),
        new Column.ColumnDateTime("invoice_created_date", null, "Created", TimeUtils.FORMAT_TYPE_YMDHM),
        new Column.ColumnString("invoice_number", null, "Number"),
        new Column.ColumnDateTime("invoice_payment_date", null, "Paid", TimeUtils.FORMAT_TYPE_YMD),
        new Column.ColumnInteger("customer_id", null, null),
        new Column.ColumnString("customer_title", null, "Customer")
    );

    @Override
    public ActionForward unspecified(final DynActionForm form, final ConnectionSet conSet) throws Exception {
        return super.unspecified(form, conSet);
    }

    @Override
    public String getTitle() {
        return Localization.getLocalizer(Localization.getLang(), Plugin.ID).l("Invoice Register");
    }

    @Override
    protected String getHref() {
        return "report/invoice/register";
    }

    @Override
    protected String getJsp() {
        return Plugin.PATH_JSP_USER + "/report/register.jsp";
    }

    @Override
    public Columns getColumns() {
        return COLUMNS;
    }

    @Override
    protected Selector getSelector() {
        return new Selector() {
            @Override
            protected void select(final ConnectionSet conSet, final Data data) throws Exception {
                final var con = conSet.getConnection();

                final var form = data.getForm();

                final int userId = form.getUserId();
                final Date date = form.getParamDate("dateFrom");
                if (date == null) {
                    form.setParam("dateFrom", TimeUtils.format(TimeConvert.toDate(LocalDate.now().withDayOfMonth(1)), TimeUtils.FORMAT_TYPE_YMD));
                    return;
                }

                var config = setup.getConfig(Config.class);

                try (var pq = new PreparedQuery(con)) {
                    pq.addQuery(
                        SQL_SELECT_COUNT_ROWS +
                        "invoice.id, invoice.type_id, invoice.amount, invoice.create_dt, invoice.number, invoice.payment_date, invoice.process_id, invoice_customer.object_id, invoice_customer.object_title" +
                        SQL_FROM +
                        TABLE_INVOICE + "AS invoice" +
                        SQL_LEFT_JOIN + TABLE_PROCESS_LINK + "AS invoice_customer ON invoice.process_id=invoice_customer.process_id AND invoice_customer.object_type=?"
                    );
                    pq.addString(Customer.OBJECT_TYPE);

                    pq.addQuery(
                        SQL_WHERE + "?<=invoice.date_from AND invoice.date_from<=? AND invoice.payment_user_id IN (0,?)" +
                        SQL_ORDER_BY + "invoice.payment_date"
                    );
                    pq.addDate(date);
                    pq.addDate(TimeUtils.getEndMonth(date));
                    pq.addInt(userId);

                    final var rs = pq.executeQuery();
                    while (rs.next()) {
                        final var record = data.addRecord();

                        record.add(rs.getInt("invoice.process_id"));
                        record.add(rs.getInt("invoice.id"));
                        record.add(config.getType(rs.getInt("invoice.type_id")).getTitle());
                        record.add(rs.getBigDecimal("invoice.amount"));
                        record.add(rs.getTimestamp("invoice.create_dt"));
                        record.add(rs.getString("invoice.number"));
                        record.add(rs.getDate("invoice.payment_date"));
                        record.add(rs.getInt("invoice_customer.object_id"));
                        record.add(rs.getString("invoice_customer.object_title"));
                    }
                }
            }
        };
    }
}
