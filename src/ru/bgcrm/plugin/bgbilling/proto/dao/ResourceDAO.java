package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bgerp.app.exception.BGException;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.RequestJsonRpc;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.IpResourceRange;
import ru.bgcrm.util.inet.IPUtils;

public class ResourceDAO extends BillingModuleDAO {
    private static final String RESOURCE_MODULE_ID = "ru.bitel.oss.systems.inventory.resource";

    public ResourceDAO(User user, DBInfo dbInfo, int moduleId) throws BGException {
        super(user, dbInfo, moduleId);
    }

    public ResourceDAO(User user, String billingId, int moduleId) throws BGException {
        super(user, billingId, moduleId);
    }

    /**
     * Возвращает список свободных IP адресов.
     * @param categoryIds
     * @param range
     * @param max
     * @return
     */
    public List<IpResourceRange> getFreeIpResourceRangeList(Set<Integer> categoryIds, int range, int max) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(RESOURCE_MODULE_ID, moduleId, "ResourceService",
                "freeIpResourceRangeList");
        Date now = new Date();
        req.setParam("ipResourceCategoryIds", categoryIds);
        req.setParam("range", range);
        req.setParam("max", max);
        req.setParam("dateFrom", now);
        req.setParam("dateTo", now);

        JsonNode ret = transferData.postDataReturn(req, user);
        List<IpResourceRange> result = readJsonValue(ret.traverse(),
                jsonTypeFactory.constructCollectionType(List.class, IpResourceRange.class));

        // перекодирование IP адресов
        for (IpResourceRange r : result) {
            r.setFrom(IPUtils.base64ToString(r.getFrom()));
            r.setTo(IPUtils.base64ToString(r.getTo()));
        }

        return result;
    }

    public Integer getFreeVlan(Set<Integer> vlanResourceCategoryIds, Date dateFrom, Date dateTo) throws BGException {
        RequestJsonRpc req = new RequestJsonRpc(RESOURCE_MODULE_ID, moduleId, "ResourceService",
                "freeVlan");
        Date now = new Date();
        req.setParam("vlanResourceCategoryIds", vlanResourceCategoryIds);
        req.setParam("dateFrom", dateFrom);
        req.setParam("dateTo", dateTo);

        JsonNode response = transferData.postDataReturn(req, user);
        return jsonMapper.convertValue(response, Integer.class);
    }
}