package org.bgerp.dao.message.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.util.List;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.dao.param.Tables;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

@Bean
public class MessagePossibleProcessSearchFoundLinksCustomerAddressCity extends MessagePossibleProcessSearch {
    private final int processCityParamId;

    public MessagePossibleProcessSearchFoundLinksCustomerAddressCity(int id, ConfigMap configMap) {
        super(id, configMap);
        processCityParamId = configMap.getInt("processCityParamId");
    }

    @Override
    public boolean addQuery(User user, PreparedQuery pq, boolean first, String from, List<ProcessLink> links, Boolean open) {
        String customerIds = links.stream()
                .filter(link -> link.getLinkObjectType().startsWith(Customer.OBJECT_TYPE))
                .map(link -> String.valueOf(link.getLinkObjectId()))
                .collect(Collectors.joining(","));

        if (Utils.isBlankString(customerIds))
            return false;

        if (!first)
            pq.addQuery(SQL_UNION_ALL);

        pq.addQuery(SQL_SELECT + "p.*, ? AS type" + SQL_FROM + TABLE_PROCESS + "AS p");
        pq.addInt(getId());
        pq.addQuery(SQL_INNER_JOIN + Tables.TABLE_PARAM_LIST + "AS pcity ON p.id=pcity.id AND pcity.param_id=?");
        pq.addInt(processCityParamId);
        pq.addQuery(SQL_INNER_JOIN + Tables.TABLE_PARAM_ADDRESS + "AS caddr ON caddr.id IN (" + customerIds + ")");
        pq.addQuery(SQL_INNER_JOIN + Tables.TABLE_ADDRESS_HOUSE + "AS chouse ON caddr.house_id=chouse.id");
        pq.addQuery(SQL_INNER_JOIN + Tables.TABLE_ADDRESS_STREET + "AS cstreet ON chouse.street_id=cstreet.id AND cstreet.city_id=pcity.value");

        addOpenFilter(pq, open);

        return true;
    }
}
