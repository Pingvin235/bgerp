package org.bgerp.dao.process;

import java.util.List;

import ru.bgcrm.util.Utils;

public abstract class Order {
    public static final Order DESCRIPTION = new Order() {
        @Override
        public String sql(String prefix) {
            return prefix + "description";
        }
    };

    public static final Order CREATE_DT_DESC = new Order() {
        @Override
        public String sql(String prefix) {
            return prefix + "create_dt DESC";
        }
    };

    public static class StatusesDescription extends Order {
        private final List<Integer> statusIds;

        public StatusesDescription(List<Integer> statusIds) {
            this.statusIds = statusIds;
        }

        @Override
        public String sql(String prefix) {
            return "FIELD(" + prefix + "status_id, " + Utils.toString(statusIds) + "), " + prefix + "description";
        }
    }

    public abstract String sql(String prefix);
}