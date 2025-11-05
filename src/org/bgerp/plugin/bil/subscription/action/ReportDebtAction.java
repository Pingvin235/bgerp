package org.bgerp.plugin.bil.subscription.action;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.l10n.Localization;
import org.bgerp.plugin.bil.subscription.Config;
import org.bgerp.plugin.bil.subscription.Plugin;
import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Columns;
import org.bgerp.plugin.report.model.Data;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/report/plugin/subscription/debt")
public class ReportDebtAction extends ReportPaymentAction {
    private static final Columns COLUMNS = new Columns(
        new Column.ColumnInteger("subscription_id", null, null),
        new Column.ColumnInteger("process_id", null, "Subscription Process"),
        new Column.ColumnInteger("customer_id", null, null),
        new Column.ColumnString("customer_title", null, "Customer"),
        new Column.ColumnDecimal("payment_amount", null, "Amount"),
        new Column.ColumnInteger("payment_user_id", null, null),
        new Column.ColumnString("payment_user_title", null, "Payment User"),
        new Column.ColumnDecimal("payment_user_tax", null, "Incoming Tax"),
        new Column.ColumnInteger("months", null, "Months"),
        new Column.ColumnDecimal("service_cost", null, "Service Cost"),
        new Column.ColumnDecimal("discount", null, "Discount"),
        new Column.ColumnDecimal("owners_amount", null, "Owners Amount"),
        new Column.ColumnInteger("product_id", null, null),
        new Column.ColumnString("product_description", null, "Product Process"),
        new Column.ColumnDecimal("product_cost", null, "Product Cost"),
        new Column.ColumnDecimal("product_cost_part", null, "Product Cost Part")
    );

    private static final Selector SELECTOR = new Selector();

    private static final class Selector extends ReportPaymentAction.Selector {
        @Override
        protected void select(final ConnectionSet conSet, final Data data) throws Exception {
            final var con = conSet.getSlaveConnection();

            final var form = data.getForm();

            final var config = Setup.getSetup().getConfig(Config.class);
            form.setRequestAttribute("config", config);

            final Date date = form.getParamDate("dateFrom");
            if (date == null) {
                form.setParam("dateFrom", TimeUtils.format(TimeUtils.getPrevMonth(), TimeUtils.FORMAT_TYPE_YMD));
                return;
            }

            final int userId = form.getUserId();

            // key - subscription ID, value - per user amounts
            final Map<Integer, Map<Integer, BigDecimal>> subscriptionUserAmounts = new TreeMap<>();
            form.setResponseData("subscriptionUserAmounts", subscriptionUserAmounts);

            for (var subscription : config.getSubscriptions()) {
                 // key - user ID, value - amount
                final Map<Integer, BigDecimal> userAmounts = new TreeMap<>();

                // subscription process ID, for that added service costs
                final Set<Integer> serviceCostAddedProcessIds = new TreeSet<>();

                // primary data: payed invoices, amounts, subscription costs
                try (var pq = query(con, config, subscription, date, -1)) {
                    final var rs = pq.executeQuery();
                    while (rs.next()) {
                        final int subscriptionProcessId = rs.getInt("invoice.process_id");
                        final int serviceConsultantId = rs.getInt("service_consultant.user_id");
                        final int productOwnerId = rs.getInt("product_owner.user_id");

                        if (!((userId == serviceConsultantId && serviceCostAddedProcessIds.add(subscriptionProcessId)) || userId == productOwnerId))
                            continue;

                        final var amount = rs.getBigDecimal("invoice.amount");
                        final Date dateFrom = rs.getDate("invoice.date_from");
                        final Date dateTo = Utils.maskNull(rs.getDate("invoice.date_to"), dateFrom);
                        final var months = BigDecimal.valueOf(ChronoUnit.MONTHS.between(TimeConvert.toYearMonth(dateFrom), TimeConvert.toYearMonth(dateTo)) + 1);
                        final BigDecimal incomingTaxPercent = rs.getBigDecimal("invoice_tax.value");
                        final var serviceCost = Utils.maskNullDecimal(rs.getBigDecimal("service_cost.value")).multiply(months);

                        final int paymentUserId = rs.getInt("invoice.payment_user_id");

                        final var record = data.addRecord();

                        record.add(subscription.getId());
                        record.add(subscriptionProcessId);
                        record.add(rs.getInt("invoice_customer.object_id"));
                        record.add(rs.getString("invoice_customer.object_title"));
                        record.add(amount);
                        record.add(paymentUserId);
                        record.add(null);
                        record.add(incomingTaxPercent);
                        record.add(months.intValue());

                         // consulter's part
                        if (userId == serviceConsultantId) {
                            final BigDecimal userAmount = userAmounts
                                .computeIfAbsent(paymentUserId, unused -> BigDecimal.ZERO)
                                .add(incomingTax(incomingTaxPercent, serviceCost));
                            userAmounts.put(paymentUserId, userAmount);

                            record.add(serviceCost);
                        } else {
                            record.add(null);
                        }

                        // owners' part
                        if (userId == productOwnerId) {
                            final var discount = Utils.maskNullDecimal(rs.getBigDecimal("discount.value")).multiply(months);
                            final var productCost = Utils.maskNullDecimal(rs.getBigDecimal("product_cost.count")).multiply(months);

                            final var ownersAmount = amount.subtract(serviceCost);
                            final var fullCost = ownersAmount.add(discount);
                            final var ownersAmountAfterTax = incomingTax(incomingTaxPercent, ownersAmount);

                            final var ownerPart = productCost
                                .multiply(ownersAmountAfterTax)
                                .divide(fullCost, RoundingMode.HALF_UP)
                                .setScale(2, RoundingMode.HALF_UP);

                            final BigDecimal userAmount = userAmounts
                                .computeIfAbsent(paymentUserId, unused -> BigDecimal.ZERO)
                                .add(ownerPart);
                            userAmounts.put(paymentUserId, userAmount);

                            record.add(discount);
                            record.add(ownersAmount);
                            record.add(rs.getInt("product.id"));
                            record.add(rs.getString("product.description"));
                            record.add(productCost);
                            record.add(ownerPart);
                        } else {
                            record.add(null).add(null).add(null).add(null).add(null).add(null);
                        }
                    }
                }

                if (!userAmounts.isEmpty())
                    subscriptionUserAmounts.put(subscription.getId(), userAmounts);
            }
        }
    };

    @Override
    public ActionForward unspecified(final DynActionForm form, final ConnectionSet conSet) throws Exception {
        return super.unspecified(form, conSet);
    }

    @Override
    public String getTitle() {
        return Localization.getLocalizer(Localization.getLang(), Plugin.ID).l("Subscription Debts");
    }

    @Override
    protected String getHref() {
        return "report/subscription/debt";
    }

    @Override
    protected String getJsp() {
        return Plugin.PATH_JSP_USER + "/report/debt.jsp";
    }

    @Override
    public Columns getColumns() {
        return COLUMNS;
    }

    @Override
    protected Selector getSelector() {
        return SELECTOR;
    }
}
