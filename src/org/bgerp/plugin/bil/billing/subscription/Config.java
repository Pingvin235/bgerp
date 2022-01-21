package org.bgerp.plugin.bil.billing.subscription;

import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.plugin.bil.billing.subscription.dao.SubscriptionDAO;
import org.bgerp.plugin.bil.billing.subscription.model.Subscription;
import org.bgerp.plugin.bil.billing.subscription.model.SubscriptionLicense;
import org.bgerp.util.Log;
import org.bgerp.util.lic.License;

import javassist.NotFoundException;
import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class Config extends ru.bgcrm.util.Config {
    /** Map with all subscriptions. */
    private final SortedMap<Integer, Subscription> subscriptions;

    private final String signKeyFile;
    private final String signKeyPswd;

    // subscription process params

    /** Param type 'list', pointing to the subscription. */
    private final int paramSubscriptionId;
    /** Contact E-Mail for Subscription. */
    private final int paramEmailId;
    /** Param type 'list', subscriptions' limit, e.g. sessions */
    private final int paramLimitId;
    /** Begin date, needed for price calculation for the first month. */
    @SuppressWarnings("unused")
    private final int paramDateFromId;
    /** End date, placed in license file. */
    private final int paramDateToId;
    /** type 'file', license file */
    private final int paramLicFileId;
    // type 'money', cost update on change
    private final int paramDiscountId;
    // type 'money', calculated on type change, not editable
    private final int paramCostId;

    private final Set<Integer> costUpdateParams;
    private final Set<Integer> licFileUpdateParams;

    // product process params

    /** Param type 'text', product ID. For product processes. */
    private final int paramProductId;

    protected Config(ParameterMap config) {
        super(null);
        config = config.sub(Plugin.ID + ":");
        subscriptions = loadSubscriptions(config);
        /* subscriptionProcessTypeIds = subscriptions.values().stream()
            .map(Subscription::getProcessTypeId).collect(Collectors.toSet()); */

        signKeyFile = config.get("sign.key.file", System.getProperty("user.home") + "/.ssh/id_rsa");
        signKeyPswd = config.get("sign.key.pswd");

        paramSubscriptionId = config.getInt("param.subscription");
        paramLicFileId = config.getInt("param.lic");
        paramEmailId = config.getInt("param.email");
        paramLimitId = config.getInt("param.limit");
        paramDateFromId = config.getInt("param.date.from");
        paramDateToId = config.getInt("param.date.to");
        paramDiscountId = config.getInt("param.cost.discount");
        paramCostId = config.getInt("param.cost");

        paramProductId = config.getInt("param.product.id");

        // explicit new HashSet is used to ignore duplicated 0 values
        costUpdateParams = Collections.unmodifiableSet(
                new HashSet<>(List.of(paramSubscriptionId, paramLimitId, paramDateToId, paramDiscountId)));
        licFileUpdateParams = Collections.unmodifiableSet(
                new HashSet<>(List.of(paramSubscriptionId, paramEmailId, paramLimitId, paramDateToId)));
    }

    private SortedMap<Integer, Subscription> loadSubscriptions(ParameterMap config) {
        var result = new TreeMap<Integer, Subscription>();

        for (var me : config.subIndexed("subscription.").entrySet()) {
            result.put(me.getKey(), new Subscription(me.getKey(), me.getValue()));
        }

        return Collections.unmodifiableSortedMap(result);
    }

    public Subscription getSubscription(int id) throws NotFoundException {
        var result = subscriptions.get(id);
        if (result == null)
            throw new NotFoundException(Log.format("Not found subscription ID: {}", id));
        return result;
    }

    /**
     * @return subscription sorted by IDs.
     */
    public Collection<Subscription> getSubscriptions() {
        return subscriptions.values();
    }

    public int getParamEmailId() {
        return paramEmailId;
    }

    public int getParamSubscriptionId() {
        return paramSubscriptionId;
    }

    public int getParamLimitId() {
        return paramLimitId;
    }

    public int getParamProductId() {
        return paramProductId;
    }

    public int getParamDiscountId() {
        return paramDiscountId;
    }

    void paramChanged(ParamChangedEvent e, ConnectionSet conSet) throws Exception {
        int paramId = e.getParameter().getId();
        if (costUpdateParams.contains(paramId))
            updateCost(e.getObjectId(), conSet);
        if (licFileUpdateParams.contains(paramId))
            updateLic(e.getObjectId(), conSet);
    }

    /**
     * Updates subscription cost parameter value.
     * @param processId
     * @param conSet
     * @throws Exception
     */
    private void updateCost(int processId, ConnectionSet conSet) throws Exception {
        var subscription = getSubscription(conSet, processId);

        var dao = new SubscriptionDAO(conSet.getSlaveConnection());
        var cost = dao.getCost(this, subscription, processId);

        new ParamValueDAO(conSet.getConnection()).updateParamMoney(processId, paramCostId, cost);
    }

    private void updateLic(int processId, ConnectionSet conSet) throws Exception {
        var paramDao = new ParamValueDAO(conSet.getSlaveConnection());

        var limitId = Utils.getFirst(paramDao.getParamList(processId, paramLimitId));
        var limit = limitId != null ? ParameterCache.getListParamValuesMap(paramLimitId).get(limitId).getTitle() : "";

        var license = new SubscriptionLicense()
            .withId(String.valueOf(processId))
            .withEmail(Utils.getFirst(paramDao.getParamEmail(processId, paramEmailId).values()))
            .withDateTo(TimeUtils.format(paramDao.getParamDate(processId, paramDateToId), TimeUtils.PATTERN_DDMMYYYY))
            .withLimit(limit);

        var dao = new SubscriptionDAO(conSet.getSlaveConnection());

        for (var productId : dao.getProducts(this, processId))
            license.withPlugin(productId);

        var lic = new License(license.build());
        byte[] signed = lic.getSignedData(signKeyFile, signKeyPswd);

        var fd = Utils.getFirst(paramDao.getParamFile(processId, paramLicFileId).values());
        paramDao = new ParamValueDAO(conSet.getConnection());
        if (fd == null) {
            fd = new FileData();
            fd.setTitle(License.FILE_NAME);
            new FileDataDAO(conSet.getConnection()).add(fd);
            paramDao.updateParamFile(processId, paramLicFileId, 0, fd);
        } else {
            fd.setOutputStream(new FileOutputStream(new FileDataDAO(null).getFile(fd)));
        }

        try (var out = fd.getOutputStream()) {
            out.write(signed);
        }
    }

    private Subscription getSubscription(ConnectionSet conSet, int processId) throws Exception {
        var paramValueDao = new ParamValueDAO(conSet.getSlaveConnection());
        Integer subscriptionId = Utils.getFirst(paramValueDao.getParamList(processId, paramSubscriptionId));
        if (subscriptionId == null)
            throw new BGException("Undefined subscription");
        return getSubscription(subscriptionId);
    }
}
