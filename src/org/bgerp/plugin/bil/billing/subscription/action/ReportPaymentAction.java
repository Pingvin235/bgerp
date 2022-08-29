package org.bgerp.plugin.bil.billing.subscription.action;

import static org.bgerp.plugin.bil.billing.invoice.dao.Tables.TABLE_INVOICE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LIST;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LISTCOUNT;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_MONEY;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_EXECUTOR;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.struts.action.ActionForward;
import org.bgerp.l10n.Localization;
import org.bgerp.plugin.bil.billing.subscription.Config;
import org.bgerp.plugin.bil.billing.subscription.Plugin;
import org.bgerp.plugin.report.action.ReportActionBase;
import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Columns;
import org.bgerp.plugin.report.model.Data;
import org.bgerp.util.sql.PreparedQuery;

import javassist.NotFoundException;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/report/plugin/subscription/payment")
public class ReportPaymentAction extends ReportActionBase {
    private static final Column COL_PROCESS_ID = new Column.ColumnInteger("subscription_id", null, "Subscription");
    private static final Column COL_CUSTOMER_TITLE = new Column.ColumnString("customer_title", null, "Customer");
    private static final Column COL_PAYMENT_AMOUNT = new Column.ColumnDecimal("payment_amount", null, "Amount");
    private static final Column COL_DISCOUNT = new Column.ColumnDecimal("discount", null, "Discount");
    private static final Column COL_SERVICE_COST = new Column.ColumnDecimal("service_cost", null, "Service Cost");
    private static final Column COL_PRODUCT_DESCRIPTION = new Column.ColumnString("product_description", null, "Product");
    private static final Column COL_PRODUCT_OWNER = new Column.ColumnString("product_owner", null, "Owner");
    private static final Column COL_PRODUCT_COST = new Column.ColumnDecimal("product_cost", null, "Product Cost");

    private static final Columns COLUMNS = new Columns(
        COL_PROCESS_ID,
        COL_CUSTOMER_TITLE,
        COL_PAYMENT_AMOUNT,
        COL_DISCOUNT,
        COL_SERVICE_COST,
        COL_PRODUCT_DESCRIPTION,
        COL_PRODUCT_OWNER,
        COL_PRODUCT_COST
    );

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return super.unspecified(form, conSet);
    }

    @Override
    public String getTitle() {
        return Localization.getLocalizer(Localization.getSysLang(), Plugin.ID).l("Subscription Payments");
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
            protected void select(ConnectionSet conSet, Data data) throws Exception {
                final var con = conSet.getSlaveConnection();

                final var form = data.getForm();

                int userId = form.getUserId();
                final Date date = form.getParamDate("dateFrom");
                if (date == null)
                    return;

                final int subscriptionId = form.getParamInt("subscriptionId", Utils::isPositive);

                var config = Setup.getSetup().getConfig(Config.class);
                var subscription = config.getSubscription(subscriptionId);
                if (subscription == null)
                    throw new NotFoundException("Not found subscription with ID: " + subscriptionId);

                String query = SQL_SELECT_COUNT_ROWS +
                    "invoice.amount, invoice.process_id, invoice_customer.object_title, discount.value, service_cost.value, product.description, product_owner.user_id, product_cost.count" +
                    SQL_FROM +
                    TABLE_INVOICE + "AS invoice " +
                    SQL_LEFT_JOIN + TABLE_PROCESS_LINK + "AS invoice_customer ON invoice.process_id=invoice_customer.process_id AND invoice_customer.object_type=?" +
                    SQL_INNER_JOIN + TABLE_PARAM_LIST + "AS param_subscription ON invoice.process_id=param_subscription.id AND param_subscription.param_id=? AND param_subscription.value=?" +
                    //
                    SQL_INNER_JOIN + TABLE_PARAM_LIST + "AS param_limit ON invoice.process_id=param_limit.id AND param_limit.param_id=?" +
                    SQL_LEFT_JOIN + TABLE_PARAM_MONEY + "AS discount ON invoice.process_id=discount.id AND discount.param_id=?" +
                    SQL_LEFT_JOIN + TABLE_PARAM_MONEY + "AS service_cost ON invoice.process_id=service_cost.id AND service_cost.param_id=?" +
                    //
                    SQL_INNER_JOIN + TABLE_PROCESS_LINK + "AS subscription_product ON subscription_product.object_id=invoice.process_id AND subscription_product.object_type=?" +
                    SQL_INNER_JOIN + TABLE_PROCESS + "AS product ON subscription_product.process_id=product.id" +
                    SQL_INNER_JOIN + TABLE_PROCESS_EXECUTOR + "AS product_owner ON product.id=product_owner.process_id AND product_owner.user_id!=?" +
                    SQL_INNER_JOIN + TABLE_PARAM_LISTCOUNT + "AS product_cost ON product.id=product_cost.id AND product_cost.param_id=? AND param_limit.value=product_cost.value" +
                    //
                    SQL_WHERE + "invoice.payment_user_id=? AND ?<=invoice.payment_date AND invoice.payment_date<=?" +
                    SQL_ORDER_BY + "invoice.payment_date";


                // serviceAmount

                // key - owner user ID, value - amount
                Map<Integer, BigDecimal> ownerAmount = new TreeMap<>();
                form.setResponseData("ownerAmount", ownerAmount);

                // primary data: payed invoices, amounts, subscription costs
                try (var pq = new PreparedQuery(con, query)) {
                    pq.addString(Customer.OBJECT_TYPE);
                    pq.addInt(config.getParamSubscriptionId());
                    pq.addInt(subscriptionId);

                    pq.addInt(config.getParamLimitId());
                    pq.addInt(config.getParamDiscountId());
                    pq.addInt(config.getParamServiceCostId());

                    pq.addString(Process.LINK_TYPE_DEPEND);
                    pq.addInt(userId);
                    pq.addInt(subscription.getParamLimitPriceId());
                    pq.addInt(userId);
                    pq.addDate(date);
                    pq.addDate(TimeUtils.getEndMonth(date));

                    var rs = pq.executeQuery();
                    while (rs.next()) {
                        var amount = rs.getBigDecimal("invoice.amount");
                        var discount = Utils.maskNullDecimal(rs.getBigDecimal("discount.value"));
                        var serviceCost = Utils.maskNullDecimal(rs.getBigDecimal("service_cost.value"));
                        var productCost = Utils.maskNullDecimal(rs.getBigDecimal("product_cost.count"));

                        var record = data.addRecord();
                        record.add(rs.getInt("invoice.process_id"));
                        record.add(rs.getString("invoice_customer.object_title"));
                        record.add(amount);
                        record.add(discount);
                        record.add(serviceCost);
                        record.add(rs.getString("product.description"));
                        record.add(UserCache.getUser(userId = rs.getInt("product_owner.user_id")).getTitle());
                        record.add(productCost);

                        var ownersPart = amount.subtract(serviceCost);
                        var fullCost = ownersPart.add(discount);

                        var ownerPart = productCost
                            .divide(fullCost, RoundingMode.HALF_UP)
                            .multiply(ownersPart);

                        // calculate owner's part
                        amount = ownerAmount
                            .computeIfAbsent(userId, unused -> BigDecimal.ZERO)
                            .add(ownerPart);
                        ownerAmount.put(userId, amount);
                    }
                }
            }
        };
    }
}
