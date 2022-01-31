package ru.bgcrm.util.sql;

import java.sql.Connection;

import org.bgerp.util.sql.PreparedQuery;

@Deprecated
public class PreparedDelay extends PreparedQuery {
    public PreparedDelay(Connection con) {
        super(con);
    }

    public PreparedDelay(Connection con, String query) {
        super(con, query);
    }
}