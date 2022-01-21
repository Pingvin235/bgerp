package org.bgerp.plugin.bil.billing.subscription.dao;

import static ru.bgcrm.dao.Tables.TABLE_PARAM_LISTCOUNT;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TEXT;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bgerp.plugin.bil.billing.subscription.Config;
import org.bgerp.plugin.bil.billing.subscription.model.Subscription;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

public class SubscriptionDAO extends CommonDAO {
    public SubscriptionDAO(Connection con) {
        super(con);
    }

    /**
     * Gets subscription cost out of product prices.
     * @param subscription subscription.
     * @param limitId sessions limit ID.
     * @param productProcessIds product processes IDs.
     * @return
     */
    public BigDecimal getCost(Subscription subscription, int limitId, Collection<Integer> productProcessIds) throws SQLException {
        var result = BigDecimal.ZERO;

        var query = SQL_SELECT + "SUM(count)" + SQL_FROM + TABLE_PARAM_LISTCOUNT + SQL_WHERE
                + "param_id=? AND value=? AND id IN (" + Utils.toString(productProcessIds, "-1", ",") + ")";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, subscription.getParamLimitPriceId());
            ps.setInt(2, limitId);

            var rs = ps.executeQuery();
            if (rs.next()) {
                result = result.add(Utils.maskNullDecimal(rs.getBigDecimal(1)));
            }
        }

        return result;
    }

    /**
     * Gets subscription cost out of product prices with discounts.
     * @param config
     * @param subscription
     * @param subscriptionProcessId
     * @return
     * @throws Exception
     */
    public BigDecimal getCost(Config config, Subscription subscription, int subscriptionProcessId) throws Exception {
        var paramValueDAO = new ParamValueDAO(con);

        Integer limitId = Utils.getFirst(paramValueDAO.getParamList(subscriptionProcessId, config.getParamLimitId()));
        if (limitId == null)
            throw new BGException("Undefined limit value");

        var result = BigDecimal.ZERO;

        var pd = new PreparedDelay(con, SQL_SELECT + "SUM(param_price.count)" + SQL_FROM + TABLE_PARAM_LISTCOUNT + "AS param_price");
        addProductsJoin(pd, config, "param_price", subscriptionProcessId);
        pd.addQuery(SQL_WHERE + "param_price.param_id=? AND param_price.value=?");
        pd.addInt(subscription.getParamLimitPriceId());
        pd.addInt(limitId);

        try (var rs = pd.executeQuery()) {
            if (rs.next()) {
                result = result.add(Utils.maskNullDecimal(rs.getBigDecimal(1)));
            }
        }

        if (config.getParamDiscountId() > 0) {
            var discount = paramValueDAO.getParamMoney(subscriptionProcessId, config.getParamDiscountId());
            if (discount != null) {
                result = result.subtract(discount);
            }
        }

        return result;
    }

    /**
     * Selects product IDs, related to a subscription process.
     * @param config
     * @param subscriptionProcessId
     * @return
     * @throws SQLException
     */
    public List<String> getProducts(Config config, int subscriptionProcessId) throws SQLException {
        var result = new ArrayList<String>(50);

        var query = SQL_SELECT + "param_product.value" + SQL_FROM + TABLE_PARAM_TEXT + "AS param_product"
            + SQL_INNER_JOIN + TABLE_PROCESS_LINK + "AS pl ON param_product.id=pl.process_id AND pl.object_id=? AND pl.object_type=?"
            + SQL_WHERE + "param_product.param_id=?"
            + SQL_ORDER_BY + "value";
        try (var pd = new PreparedDelay(con, query)) {
            pd.addInt(subscriptionProcessId);
            pd.addString(Process.LINK_TYPE_DEPEND);
            pd.addInt(config.getParamProductId());

            var rs = pd.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
        }

        return result;
    }

   /*  public List<Position> getInvoicePositions(Config config, Subscription subscription, int subscriptionProcessId) throws SQLException {
        var result = new ArrayList<Position>(20);

        Integer limitId = Utils.getFirst(new ParamValueDAO(con).getParamList(subscriptionProcessId, config.getParamLimitId()));
        if (limitId == null) {
            log.debug("Undefined limit value");
            return result;
        }

        try (var pd = new PreparedDelay(con, SQL_SELECT + "param_product_id.value, param_price.count, p.description"
                + SQL_FROM + TABLE_PARAM_LISTCOUNT + "AS param_price")) {
            addProductsJoin(pd, config, "param_price", subscriptionProcessId);
            pd.addQuery(SQL_INNER_JOIN + TABLE_PROCESS + "AS p ON param_price.id=p.id");
            pd.addQuery(SQL_WHERE + "param_price.param_id=? AND param_price.value=?");
            pd.addInt(subscription.getParamLimitPriceId());
            pd.addInt(limitId);

            var rs = pd.executeQuery();
            while (rs.next()) {
                var pos = new Position();
                pos.setId(rs.getString(1));
                pos.setAmount(Utils.maskNullDecimal(rs.getBigDecimal(2)));
                pos.setTitle(rs.getString(3));
                result.add(pos);
            }
        }

        return result;
    } */

    private void addProductsJoin(PreparedDelay pd, Config config, String table, int subscriptionProcessId) throws SQLException {
        pd.addQuery(SQL_INNER_JOIN + TABLE_PROCESS_LINK
                + "AS pl ON " + table + ".id=pl.process_id AND pl.object_id=? AND pl.object_type=?"
                + SQL_INNER_JOIN + TABLE_PARAM_TEXT + "AS param_product_id ON pl.process_id=param_product_id.id AND param_product_id.param_id=?");
        pd.addInt(subscriptionProcessId);
        pd.addString(Process.LINK_TYPE_DEPEND);
        pd.addInt(config.getParamProductId());
    }
}
