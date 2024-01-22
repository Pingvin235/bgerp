package org.bgerp.plugin.bil.subscription.action;

import static org.bgerp.plugin.bil.invoice.dao.Tables.TABLE_INVOICE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_EXECUTOR;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

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
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.dao.param.Tables;
import org.bgerp.plugin.bil.subscription.Config;
import org.bgerp.plugin.bil.subscription.Plugin;
import org.bgerp.plugin.report.action.ReportActionBase;
import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Columns;
import org.bgerp.plugin.report.model.Data;
import org.bgerp.util.TimeConvert;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/report/plugin/subscription/payment")
public class ReportPaymentAction extends ReportActionBase {
    private static final Columns COLUMNS = new Columns(
        new Column.ColumnInteger("subscription_id", null, null),
        new Column.ColumnInteger("process_id", null, "Subscription Process"),
        new Column.ColumnInteger("customer_id", null, null),
        new Column.ColumnString("customer_title", null, "Customer"),
        new Column.ColumnDecimal("payment_amount", null, "Amount"),
        new Column.ColumnInteger("months", null, "Months"),
        new Column.ColumnDecimal("service_cost", null, "Service Cost"),
        new Column.ColumnInteger("service_consultant_user_id", null, null),
        new Column.ColumnString("service_consultant_user_title", null, "Service Consultant"),
        new Column.ColumnDecimal("discount", null, "Discount"),
        new Column.ColumnDecimal("owners_amount", null, "Owners Amount"),
        new Column.ColumnInteger("product_id", null, null),
        new Column.ColumnString("product_description", null, "Product Process"),
        new Column.ColumnInteger("product_owner_user_id", null, null),
        new Column.ColumnString("product_owner_user_title", null, "Owner"),
        new Column.ColumnDecimal("product_cost", null, "Product Cost"),
        new Column.ColumnDecimal("product_cost_part", null, "Product Cost Part")
    );

    @Override
    public ActionForward unspecified(final DynActionForm form, final ConnectionSet conSet) throws Exception {
        return super.unspecified(form, conSet);
    }

    @Override
    public String getTitle() {
        return Localization.getLocalizer(Localization.getLang(), Plugin.ID).l("Subscription Payments");
    }

    @Override
    protected String getHref() {
        return "report/subscription/payment";
    }

    @Override
    protected String getJsp() {
        return Plugin.PATH_JSP_USER + "/report/payment.jsp";
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

                BigDecimal incomingTaxPercent = null;
                if (config.getParamUserIncomingTaxPercentId() > 0)
                    incomingTaxPercent = new ParamValueDAO(con).getParamMoney(userId, config.getParamUserIncomingTaxPercentId());

                form.setResponseData("incomingTaxPercent", incomingTaxPercent);

                // key - subscription ID, value - per user amounts
                final Map<Integer, Map<Integer, BigDecimal>> subscriptionUserAmounts = new TreeMap<>();
                form.setResponseData("subscriptionUserAmounts", subscriptionUserAmounts);

                for (var subscription : config.getSubscriptions()) {
                    // key - user ID, value - amount
                    final Map<Integer, BigDecimal> userAmounts = new TreeMap<>();

                    // subscription process ID, for that added service costs
                    final Set<Integer> serviceCostAddedProcessIds = new TreeSet<>();

                    // primary data: payed invoices, amounts, subscription costs
                    try (var pq = new PreparedQuery(con)) {
                        pq.addQuery(
                            SQL_SELECT_COUNT_ROWS +
                            "invoice.amount, invoice.process_id, invoice.date_from, invoice.date_to, " +
                            "invoice_customer.object_id, invoice_customer.object_title, " +
                            "discount.value, service_cost.value, service_consultant.user_id, " +
                            "product.id, product.description, product_owner.user_id, product_cost.count" +
                            SQL_FROM +
                            TABLE_INVOICE + "AS invoice " +
                            SQL_LEFT_JOIN + TABLE_PROCESS_LINK + "AS invoice_customer ON invoice.process_id=invoice_customer.process_id AND invoice_customer.object_type=?" +
                            SQL_INNER_JOIN + Tables.TABLE_PARAM_LIST + "AS subscription ON invoice.process_id=subscription.id AND subscription.param_id=? AND subscription.value=?");

                        pq.addString(Customer.OBJECT_TYPE);
                        pq.addInt(config.getParamSubscriptionId());
                        pq.addInt(subscription.getId());

                        pq.addQuery(
                            SQL_INNER_JOIN + Tables.TABLE_PARAM_LIST + "AS param_limit ON invoice.process_id=param_limit.id AND param_limit.param_id=?" +
                            SQL_LEFT_JOIN + Tables.TABLE_PARAM_MONEY + "AS discount ON invoice.process_id=discount.id AND discount.param_id=?" +
                            SQL_LEFT_JOIN + Tables.TABLE_PARAM_MONEY + "AS service_cost ON invoice.process_id=service_cost.id AND service_cost.param_id=?" +
                            SQL_LEFT_JOIN + TABLE_PROCESS_EXECUTOR + "AS service_consultant ON invoice.process_id=service_consultant.process_id AND service_consultant.role_id=0"
                        );

                        pq.addInt(config.getParamLimitId());
                        pq.addInt(config.getParamDiscountId());
                        pq.addInt(config.getParamServiceCostId());

                        pq.addQuery(
                            SQL_INNER_JOIN + TABLE_PROCESS_LINK + "AS subscription_product ON subscription_product.object_id=invoice.process_id AND subscription_product.object_type=?" +
                            SQL_INNER_JOIN + TABLE_PROCESS + "AS product ON subscription_product.process_id=product.id" +
                            SQL_LEFT_JOIN + TABLE_PROCESS_EXECUTOR + "AS product_owner ON product.id=product_owner.process_id AND product_owner.role_id=1" +
                            SQL_INNER_JOIN + Tables.TABLE_PARAM_LISTCOUNT + "AS product_cost ON product.id=product_cost.id AND product_cost.param_id=? AND param_limit.value=product_cost.value"
                        );

                        pq.addString(Process.LINK_TYPE_DEPEND);
                        pq.addInt(subscription.getParamLimitPriceId());

                        pq.addQuery(
                            SQL_WHERE + "invoice.payment_user_id=? AND ?<=invoice.payment_date AND invoice.payment_date<=?" +
                            SQL_ORDER_BY + "invoice.payment_date"
                        );

                        pq.addInt(userId);
                        pq.addDate(date);
                        pq.addDate(TimeUtils.getEndMonth(date));

                        final var rs = pq.executeQuery();
                        while (rs.next()) {
                            final var amount = rs.getBigDecimal("invoice.amount");
                            final Date dateFrom = rs.getDate("invoice.date_from");
                            final Date dateTo = Utils.maskNull(rs.getDate("invoice.date_to"), dateFrom);
                            final var months = BigDecimal.valueOf(ChronoUnit.MONTHS.between(TimeConvert.toYearMonth(dateFrom), TimeConvert.toYearMonth(dateTo)) + 1);
                            final int subscriptionProcessId = rs.getInt("invoice.process_id");
                            final var discount = Utils.maskNullDecimal(rs.getBigDecimal("discount.value")).multiply(months);
                            final var serviceCost = Utils.maskNullDecimal(rs.getBigDecimal("service_cost.value")).multiply(months);
                            final int serviceConsultantId = rs.getInt("service_consultant.user_id");
                            final var productCost = Utils.maskNullDecimal(rs.getBigDecimal("product_cost.count")).multiply(months);
                            final int productOwnerId = rs.getInt("product_owner.user_id");

                            final var record = data.addRecord();

                            record.add(subscription.getId());
                            record.add(subscriptionProcessId);
                            record.add(rs.getInt("invoice_customer.object_id"));
                            record.add(rs.getString("invoice_customer.object_title"));
                            record.add(amount);
                            record.add(months.intValue());
                            record.add(serviceCost);
                            record.add(serviceConsultantId);
                            record.add(UserCache.getUser(serviceConsultantId).getTitle());
                            record.add(discount);

                            // add consulter's part
                            if (serviceCostAddedProcessIds.add(subscriptionProcessId)) {
                                final var userAmount = userAmounts
                                    .computeIfAbsent(productOwnerId, unused -> BigDecimal.ZERO)
                                    .add(incomingTax(incomingTaxPercent, serviceCost));
                                userAmounts.put(serviceConsultantId, userAmount);
                            }

                            var ownersAmount = amount.subtract(serviceCost);

                            record.add(ownersAmount);
                            record.add(rs.getInt("product.id"));
                            record.add(rs.getString("product.description"));
                            record.add(productOwnerId);
                            record.add(UserCache.getUser(productOwnerId).getTitle());
                            record.add(productCost);

                            // add owner's part
                            if (productOwnerId != userId) {
                                final var fullCost = ownersAmount.add(discount);

                                final var ownerPart = productCost
                                    .divide(fullCost, RoundingMode.HALF_UP)
                                    .multiply(ownersAmount)
                                    .setScale(2, RoundingMode.HALF_UP);

                                final var userAmount = userAmounts
                                    .computeIfAbsent(productOwnerId, unused -> BigDecimal.ZERO)
                                    .add(ownerPart);
                                userAmounts.put(productOwnerId, incomingTax(incomingTaxPercent, userAmount));

                                record.add(ownerPart);
                            } else
                                record.add(null);
                        }
                    }

                    if (!userAmounts.isEmpty())
                        subscriptionUserAmounts.put(subscription.getId(), userAmounts);
                }
            }

            private BigDecimal incomingTax(BigDecimal incomingTaxPercent, BigDecimal value) {
                if (incomingTaxPercent == null)
                    return value;

                return value.multiply(
                    BigDecimal.ONE.subtract(
                        incomingTaxPercent.divide(new BigDecimal("100"))
                    )
                ).setScale(2, RoundingMode.HALF_UP);
            }
        };
    }
}
