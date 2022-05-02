package org.bgerp.plugin.svc.dba.model;

import java.util.EnumSet;

/**
 * Simple prefix-based query type detector.
 *
 * @author Shamil Vakhitov
 */
public enum QueryType {
    SELECT("SELECT"),
    SHOW("SHOW"),
    INSERT("INSERT"),
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    DROP("DROP");

    /** Capital case SQL query beginning. */
    private final String prefix;

    private QueryType(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return {@link #prefix}.
     */
    public String prefix() {
        return prefix;
    }

    /**
     * Detects query type.
     * @param query SQL query.
     * @return type or {@code null}.
     */
    public static QueryType of(String query) {
        query = query.trim().toUpperCase();

        for (QueryType q : EnumSet.allOf(QueryType.class)) {
            if (query.startsWith(q.prefix))
                return q;
        }

        return null;
    }
}
