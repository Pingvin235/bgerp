package ru.bgcrm.plugin.bgbilling.message;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.dao.message.process.MessagePossibleProcessSearch;
import org.bgerp.dao.param.Tables;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.util.Utils;

@Bean
public class BGBillingMessagePossibleProcessSearchFoundLinksContractAddressCity extends MessagePossibleProcessSearch {
    /**
     * key - billing ID, default value under key ''
     */
    private final Map<String, Integer> contractAddressParamIds;
    private final int processCityParamId;

    public BGBillingMessagePossibleProcessSearchFoundLinksContractAddressCity(int id, ConfigMap configMap) {
        super(id, configMap);
        contractAddressParamIds = loadContractAddressParamId(configMap);
        processCityParamId = configMap.getInt("processCityParamId");

        if (contractAddressParamIds.isEmpty())
            throw new IllegalArgumentException("'contractAddressParamId' is not specified");
        if (processCityParamId <= 0)
            throw new IllegalArgumentException("'processCityParamId' must be positive int");
    }

    private Map<String, Integer> loadContractAddressParamId(ConfigMap configMap) {
        Map<String, Integer> result = new HashMap<>();

        final String key = "contractAddressParamId";

        int addressParamId = configMap.getInt(key);
        if (addressParamId > 0)
            result.put("", addressParamId);

        for (var me : configMap.sub(key + ".").entrySet())
            result.put(me.getKey(), Utils.parseInt(me.getValue()));

        return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean addQuery(User user, PreparedQuery pq, boolean first, String from, List<ProcessLink> links, Boolean open) {
        Set<Integer> cityIds = new TreeSet<>();

        for (CommonObjectLink link : links) {
            if (!link.getLinkObjectType().startsWith(Contract.OBJECT_TYPE))
                continue;

            log.debug("linkObjectType: {}, linkObjectId: {}", link.getLinkObjectType(), link.getLinkObjectId());

            String billingId = new Contract(link).getBillingId();
            Integer paramId = contractAddressParamIds.getOrDefault(billingId, contractAddressParamIds.get(""));
            if (paramId == null) {
                log.error("Not found 'contractAddressParamId' for billing ID: {}", billingId);
                continue;
            }

            var addr = new ContractParamDAO(user, billingId).getAddressParam(link.getLinkObjectId(), paramId);
            if (addr != null)
                cityIds.add(addr.getCityId());
        }

        if (cityIds.isEmpty())
            return false;

        if (!first)
            pq.addQuery(SQL_UNION_ALL);

        pq.addQuery(SQL_SELECT + "p.*, ? AS type" + SQL_FROM + TABLE_PROCESS + "AS p");
        pq.addInt(getId());
        pq.addQuery(SQL_INNER_JOIN + Tables.TABLE_PARAM_LIST + "AS pcity ON p.id=pcity.id AND pcity.param_id=? AND pcity.value IN (" + Utils.toString(cityIds) + ")");
        pq.addInt(processCityParamId);

        addOpenFilter(pq, open);

        return true;
    }
}
