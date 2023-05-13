package org.bgerp.plugin.bil.subscription;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.app.l10n.Localization;
import org.bgerp.plugin.bil.subscription.dao.SubscriptionDAO;
import org.bgerp.plugin.bil.subscription.model.Subscription;
import org.bgerp.plugin.bil.subscription.model.SubscriptionLicense;
import org.bgerp.util.Log;
import org.bgerp.util.lic.License;

import javassist.NotFoundException;
import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.event.ParamChangedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
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

    // user param

    /** Param type 'money', incoming tax percentage for a user. */
    private final int paramUserIncomingTaxPercentId;

    // subscription process params

    /** Param type 'list', pointing to the subscription. */
    private final int paramSubscriptionId;
    /** Contact E-Mail for Subscription. */
    private final int paramEmailId;
    /** Param type 'list', subscriptions' limit, e.g. sessions */
    private final int paramLimitId;
    /** End date, placed in license file. */
    private final int paramDateToId;
    /** type 'file', license file */
    private final int paramLicFileId;
    /** type 'money', cost updated on change */
    private final int paramServiceCostId;
    // type 'money', cost updated on change
    private final int paramDiscountId;
    // type 'money', calculated on changes, not editable
    private final int paramCostId;

    private final Set<Integer> costUpdateParams;
    private final Set<Integer> licFileUpdateParams;

    // product process params

    /** Param type 'text', product ID. For product processes. */
    private final int paramProductId;

    /** Maximum months from the current date to Date To */
    private final int maxDateToMonths;

    protected Config(ParameterMap config) {
        super(null);
        config = config.sub(Plugin.ID + ":");
        subscriptions = loadSubscriptions(config);

        signKeyFile = config.get("sign.key.file", System.getProperty("user.home") + "/.ssh/id_rsa");
        signKeyPswd = config.get("sign.key.pswd");

        paramUserIncomingTaxPercentId = config.getInt("param.user.incoming.tax.percent");

        paramSubscriptionId = config.getInt("param.subscription");
        paramLicFileId = config.getInt("param.lic");
        paramEmailId = config.getInt("param.email");
        paramLimitId = config.getInt("param.limit");
        paramDateToId = config.getInt("param.date.to");
        paramServiceCostId = config.getInt("param.cost.service");
        paramDiscountId = config.getInt("param.cost.discount");
        paramCostId = config.getInt("param.cost");

        paramProductId = config.getInt("param.product.id");

        maxDateToMonths = config.getInt("max.date.to.months", 3);

        // explicit new HashSet is used to ignore duplicated 0 values
        costUpdateParams = Set.of(paramLimitId, paramDateToId, paramServiceCostId, paramDiscountId);
        licFileUpdateParams = Set.of(paramEmailId, paramLimitId, paramDateToId);
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

    public int getParamUserIncomingTaxPercentId() {
        return paramUserIncomingTaxPercentId;
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

    public int getParamServiceCostId() {
        return paramServiceCostId;
    }

    public int getParamDiscountId() {
        return paramDiscountId;
    }

    void paramChanged(ParamChangedEvent e, ConnectionSet conSet) throws Exception {
        int paramId = e.getParameter().getId();

        boolean licUpdate = true;

        if (paramId == paramDateToId)
            licUpdate = dateToChanged(e, conSet);

        if (licUpdate && licFileUpdateParams.contains(paramId))
            updateLic(e.getObjectId(), conSet);

        if (costUpdateParams.contains(paramId))
            updateCost(e.getObjectId(), conSet);
    }

    /**
     * Checks DateTo value to do not be later as defined months.
     * @param e
     * @param conSet
     * @return license update can be performed.
     * @throws BGMessageException Date To is too far in the future.
     */
    private boolean dateToChanged(ParamChangedEvent e, ConnectionSet conSet) throws Exception {
        Date value = (Date) e.getValue();
        if (value == null) {
            new ParamValueDAO(conSet.getConnection()).updateParamFile(e.getObjectId(), paramLicFileId, -1, null);
            return false;
        } else if (maxDateToMonths > 0 && maxDateToMonths < Period.between(LocalDate.now(), value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).getMonths())
            throw new BGMessageException(Localization.getLocalizer(Plugin.ID, e.getForm().getHttpRequest()), "Date To is too far in the future.");
        else
            return true;
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
        var paramDao = new ParamValueDAO(conSet.getConnection());

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
        byte[] signed = lic.sign(signKeyFile, signKeyPswd);

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
