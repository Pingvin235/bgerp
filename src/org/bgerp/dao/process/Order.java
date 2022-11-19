package org.bgerp.dao.process;

public enum Order {
    DESCRIPTION {
        @Override
        public String sql(String prefix) {
            return prefix + "description";
        }
    },

    CREATE_DT_DESC {
        @Override
        public String sql(String prefix) {
            return prefix + "create_dt DESC";
        }
    };

    public abstract String sql(String prefix);
}