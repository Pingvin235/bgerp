package org.bgerp.plugin.sec.secret.dao;

import java.sql.Connection;
import java.sql.SQLException;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

/**
 * Secret DAO for secrets with 32 ASCII chars,
 * used for accessing URLs.
 * 
 * @author Shamil Vakhitov
 */
public class SecretDAO extends CommonDAO {
    private final String tableName = Tables.TABLE_SECRET_OPEN;

    public SecretDAO(Connection con) {
        super(con);
    }

    /**
     * Updates existing secret or inserts missing with a randomly generated.
     * @param key unique ID.
     * @return generated value. 
     * @throws SQLException
     */
    public String update(String key) throws SQLException {
        var secret = Utils.generateSecret();

        updateOrInsert(
            SQL_UPDATE + tableName + SQL_SET + "secret=?, dt=NOW()" + SQL_WHERE + "id=?",
            SQL_INSERT + tableName + "(id, secret, dt) VALUES (?, ?, NOW())",
            key, secret);

        return secret;
    }

    /**
     * Selects secret.
     * @param key unique ID.
     * @return found secret or {@code null}.
     * @throws SQLException
     */
    public String get(String key) throws SQLException {
        try (var pd = new PreparedDelay(con, SQL_SELECT + "secret" + SQL_FROM + tableName + SQL_WHERE + "id=?")) {
            pd.addString(key);
            var rs = pd.executeQuery();
            if (rs.next())
                return rs.getString(1);
        }
        return null;
    }
}
