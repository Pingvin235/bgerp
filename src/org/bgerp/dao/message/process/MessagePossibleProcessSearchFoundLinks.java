package org.bgerp.dao.message.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

@Bean
public class MessagePossibleProcessSearchFoundLinks extends MessagePossibleProcessSearch {
    public MessagePossibleProcessSearchFoundLinks(int id, ConfigMap configMap) {
        super(id, configMap);
    }

    @Override
    public boolean addQuery(User user, PreparedQuery pq, boolean first, String from, List<ProcessLink> links, Boolean open) {
        if (links.isEmpty())
            return false;

        if (!first)
            pq.addQuery(SQL_UNION_ALL);

        Set<Integer> objectIds = new TreeSet<>();
        StringBuilder objectFilter = new StringBuilder();

        objectFilter.append("(0>1 ");

        for (CommonObjectLink link : links) {
            objectIds.add(link.getLinkObjectId());
            if (Customer.OBJECT_TYPE.equals(link.getLinkObjectType()))
                objectFilter.append(" OR (pl.object_type LIKE 'customer%' AND pl.object_id=" + link.getLinkObjectId() + ")");
            else
                objectFilter.append(" OR (pl.object_type='" + link.getLinkObjectType() + "' AND pl.object_id=" + link.getLinkObjectId() + ")");
        }

        objectFilter.append(" ) ");

        pq.addQuery(SQL_SELECT + "p.*, ? AS type" + SQL_FROM + TABLE_PROCESS + "AS p");
        pq.addInt(getId());
        pq.addQuery(SQL_INNER_JOIN + TABLE_PROCESS_LINK);
        pq.addQuery("AS pl ON pl.process_id=p.id AND pl.object_id IN (" + Utils.toString(objectIds) + ") AND ");
        pq.addQuery(objectFilter.toString());

        addOpenFilter(pq, open);

        return true;
    }
}
