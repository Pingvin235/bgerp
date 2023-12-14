package org.bgerp.dao.process;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bgerp.app.cfg.Setup;
import org.bgerp.dao.message.process.MessagePossibleProcessConfig;
import org.bgerp.dao.message.process.MessagePossibleProcessSearch;
import org.bgerp.model.Pageable;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

public class ProcessMessageDAO extends ProcessDAO {
    /**
     * @inheritDoc
     */
    public ProcessMessageDAO(Connection con, DynActionForm form) {
        super(con, form);
    }

    /**
     * Searches processes, related to a message using search types from {@link MessagePossibleProcessSearch}.
     * @param searchResult result list sorted by {@link MessagePossibleProcessSearch} config IDs, {@link Pair#getSecond()} defines type of relation.
     * @param from message from address.
     * @param links not {@code null} list with found link objects.
     * @param open when not {@code null} - filter only opened or closed processes.
     * @throws SQLException
     */
    public void searchProcessListForMessage(Pageable<Pair<Process, MessagePossibleProcessSearch>> searchResult, String from, List<ProcessLink> links,
            Boolean open) throws SQLException {
        var config = Setup.getSetup().getConfig(MessagePossibleProcessConfig.class);
        if (config == null)
            return;

        var typesIt = config.getSearches().entrySet().iterator();

        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Pair<Process, MessagePossibleProcessSearch>> list = searchResult.getList();

            PreparedQuery pq = new PreparedQuery(con);

            pq.addQuery(SQL_SELECT_COUNT_ROWS + "*" + SQL_FROM + " (");

            boolean first = true;
            while (typesIt.hasNext()) {
                boolean added = typesIt.next().getValue().addQuery(form.getUser(), pq, first, from, links, open);
                first = first && !added;
            }

            // something was added to the pq
            if (!first) {
                pq.addQuery(") p");

                pq.addQuery(SQL_GROUP_BY + "id");
                pq.addQuery(SQL_ORDER_BY + "create_dt" + SQL_DESC);

                pq.addQuery(getPageLimit(page));

                ResultSet rs = pq.executeQuery();
                while (rs.next())
                    list.add(new Pair<>(getProcessFromRs(rs, ""), config.getSearches().get(rs.getInt("type"))));

                setRecordCount(page, pq.getPrepared());
                pq.close();
            }
        }
    }
}
