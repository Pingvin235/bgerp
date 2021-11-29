package org.bgerp.plugin.sec.secret;

import java.sql.SQLException;

import org.bgerp.plugin.sec.secret.dao.SecretDAO;

import ru.bgcrm.util.Setup;

public class ExpressionObject {
    /**
     * Gets open generated secret with 32 ASCII chars.
     * @param key unique ID.
     * @param update update to a newly generated.
     * @return secret value.
     */
    public String open(String key, boolean update) throws SQLException {
        try (var con = Setup.getSetup().getDBConnectionFromPool()) {
            var dao = new SecretDAO(con);
            var result = update ? dao.update(key) : dao.get(key);
            con.commit();
            return result;
        }
    }
}
