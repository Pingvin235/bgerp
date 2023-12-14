package org.bgerp.dao.message.process;

import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.util.List;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.user.User;

@Bean
public class MessagePossibleProcessSearchMessageFrom extends MessagePossibleProcessSearch {
    public MessagePossibleProcessSearchMessageFrom(int id, ConfigMap configMap) {
        super(id, configMap);
    }

    @Override
    public boolean addQuery(User user, PreparedQuery pq, boolean first, String from, List<ProcessLink> links, Boolean open) {
        if (!first)
            pq.addQuery(SQL_UNION_ALL);

        pq.addQuery(SQL_SELECT + "p.*, ? AS type" + SQL_FROM + TABLE_PROCESS + "AS p ");
        pq.addQuery(SQL_INNER_JOIN + TABLE_MESSAGE + "AS m ON m.process_id=p.id AND m.from=?");
        pq.addInt(getId());
        pq.addString(from);

        addOpenFilter(pq, open);

        return true;
    }
}
