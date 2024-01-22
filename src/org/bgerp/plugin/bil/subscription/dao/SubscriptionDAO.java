package org.bgerp.plugin.bil.subscription.dao;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.dao.param.Tables;
import org.bgerp.plugin.bil.subscription.Config;
import org.bgerp.plugin.bil.subscription.model.Subscription;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;

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

        var query = SQL_SELECT + "SUM(count)" + SQL_FROM + Tables.TABLE_PARAM_LISTCOUNT + SQL_WHERE
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
     * Gets subscription cost out of product prices, minus service cost and discount.
     * @param config
     * @param subscription
     * @param subscriptionProcessId
     * @return
     * @throws SQLException
     * @throws BGMessageException
     */
    public BigDecimal getCost(Config config, Subscription subscription, int subscriptionProcessId) throws SQLException, BGMessageException {
        var paramValueDAO = new ParamValueDAO(con);

        Integer limitId = Utils.getFirst(paramValueDAO.getParamList(subscriptionProcessId, config.getParamLimitId()));
        if (limitId == null)
            throw new BGException("Undefined limit value");

        var result = BigDecimal.ZERO;

        var pq = new PreparedQuery(con, SQL_SELECT + "SUM(param_price.count)" + SQL_FROM + Tables.TABLE_PARAM_LISTCOUNT + "AS param_price");
        addProductsJoin(pq, config, "param_price", subscriptionProcessId);
        pq.addQuery(SQL_WHERE + "param_price.param_id=? AND param_price.value=?");
        pq.addInt(subscription.getParamLimitPriceId());
        pq.addInt(limitId);

        try (var rs = pq.executeQuery()) {
            if (rs.next()) {
                result = result.add(Utils.maskNullDecimal(rs.getBigDecimal(1)));
            }
        }

        if (config.getParamDiscountId() > 0) {
            var discount = paramValueDAO.getParamMoney(subscriptionProcessId, config.getParamDiscountId());
            if (discount != null) {
                if (discount.compareTo(result) > 0)
                    throw new BGMessageException("Discount can't be more than product's cost: {}", discount.toPlainString());
                result = result.subtract(discount);
            }
        }

        if (config.getParamServiceCostId() > 0) {
            var service = paramValueDAO.getParamMoney(subscriptionProcessId, config.getParamServiceCostId());
            if (service != null)
                result = result.add(service);
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

        var query = SQL_SELECT + "param_product.value" + SQL_FROM + Tables.TABLE_PARAM_TEXT + "AS param_product"
            + SQL_INNER_JOIN + TABLE_PROCESS_LINK + "AS pl ON param_product.id=pl.process_id AND pl.object_id=? AND pl.object_type=?"
            + SQL_WHERE + "param_product.param_id=?"
            + SQL_ORDER_BY + "value";
        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(subscriptionProcessId);
            pq.addString(Process.LINK_TYPE_DEPEND);
            pq.addInt(config.getParamProductId());

            var rs = pq.executeQuery();
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

        try (var pq = new PreparedDelay(con, SQL_SELECT + "param_product_id.value, param_price.count, p.description"
                + SQL_FROM + TABLE_PARAM_LISTCOUNT + "AS param_price")) {
            addProductsJoin(pq, config, "param_price", subscriptionProcessId);
            pq.addQuery(SQL_INNER_JOIN + TABLE_PROCESS + "AS p ON param_price.id=p.id");
            pq.addQuery(SQL_WHERE + "param_price.param_id=? AND param_price.value=?");
            pq.addInt(subscription.getParamLimitPriceId());
            pq.addInt(limitId);

            var rs = pq.executeQuery();
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

    private void addProductsJoin(PreparedQuery pq, Config config, String table, int subscriptionProcessId) throws SQLException {
        pq.addQuery(SQL_INNER_JOIN + TABLE_PROCESS_LINK
                + "AS pl ON " + table + ".id=pl.process_id AND pl.object_id=? AND pl.object_type=?"
                + SQL_INNER_JOIN + Tables.TABLE_PARAM_TEXT + "AS param_product_id ON pl.process_id=param_product_id.id AND param_product_id.param_id=?");
        pq.addInt(subscriptionProcessId);
        pq.addString(Process.LINK_TYPE_DEPEND);
        pq.addInt(config.getParamProductId());
    }
}
