package org.bgerp.plugin.msg.email.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bgerp.dao.param.Tables;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

import static ru.bgcrm.dao.Tables.*;
import static ru.bgcrm.dao.user.Tables.*;

public class EMailDAO extends CommonDAO {
    public EMailDAO(Connection con) {
        super(con);
    }

     /**
     * Selects E-Mails in process parameters.
     * @param processId process ID.
     * @return
     * @throws SQLException
     */
    public List<ParameterEmailValue> getProcessEmails(int processId) throws SQLException {
        var result = new ArrayList<ParameterEmailValue>(100);

        var query =
            SQL_SELECT + "param.value, param.comment, param.comment" + SQL_FROM + Tables.TABLE_PARAM_EMAIL + "AS param" +
            SQL_INNER_JOIN + Tables.TABLE_PARAM_PREF + "AS pref ON param.param_id=pref.id AND pref.object=?" +
            SQL_WHERE + "param.id=?";

        try (var pq = new PreparedQuery(con, query)) {
            pq.addString(Process.OBJECT_TYPE);
            pq.addInt(processId);

            loadEmails(pq, result);
        }

        return result;
    }

    /**
     * Selects E-Mails in parameters of users excluding in {@link ru.bgcrm.model.user.User#STATUS_DISABLED}.
     * @param ids optional set of user IDs.
     * @return
     * @throws SQLException
     */
    public List<ParameterEmailValue> getUserEmails(Set<Integer> ids) throws SQLException {
        var result = new ArrayList<ParameterEmailValue>(100);

        var query =
            SQL_SELECT + "param.value, param.comment, user.title" + SQL_FROM + Tables.TABLE_PARAM_EMAIL + "AS param" +
            SQL_INNER_JOIN + Tables.TABLE_PARAM_PREF + "AS pref ON param.param_id=pref.id AND pref.object=?" +
            SQL_INNER_JOIN + TABLE_USER + "AS user ON param.id=user.id AND user.status!=?";

        try (var pq = new PreparedQuery(con, query)) {
            pq.addString(User.OBJECT_TYPE);
            pq.addInt(User.STATUS_DISABLED);

            if (ids != null && !ids.isEmpty())
                pq.addQuery(" AND user.id IN (" + Utils.toString(ids) + ")");

            loadEmails(pq, result);
        }

        return result;
    }

    /**
     * Selects E-Mails in parameters of customers.
     * @param ids optional set of customer IDs.
     * @return
     * @throws SQLException
     */
    public List<ParameterEmailValue> getCustomerEmails(Set<Integer> ids) throws SQLException {
        var result = new ArrayList<ParameterEmailValue>(100);

        var query =
            SQL_SELECT + "param.value, param.comment, c.title" + SQL_FROM + Tables.TABLE_PARAM_EMAIL + "AS param" +
            SQL_INNER_JOIN + Tables.TABLE_PARAM_PREF + "AS pref ON param.param_id=pref.id AND pref.object=?" +
            SQL_INNER_JOIN + TABLE_CUSTOMER + "AS c ON param.id=c.id";

        try (var pq = new PreparedQuery(con, query)) {
            pq.addString(Customer.OBJECT_TYPE);

            if (ids != null && !ids.isEmpty())
                pq.addQuery(" AND c.id IN (" + Utils.toString(ids) + ")");

            loadEmails(pq, result);
        }

        return result;
    }

    private void loadEmails(PreparedQuery pq, List<ParameterEmailValue> result) throws SQLException {
        var rs = pq.executeQuery();
        while (rs.next()) {
            var value = new ParameterEmailValue(rs.getString(1), rs.getString(2));
            if (Utils.isBlankString(value.getUsername()))
                continue;
            if (Utils.isBlankString(value.getComment()))
                value.setComment(rs.getString(3));
            result.add(value);
        }
    }
}
