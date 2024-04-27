package org.bgerp.cache;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;

import ru.bgcrm.dao.CustomerGroupDAO;
import ru.bgcrm.model.customer.CustomerGroup;
import ru.bgcrm.util.sql.SQLUtils;

public class CustomerGroupCache extends Cache<CustomerGroupCache> {
    private static final Log log = Log.getLog();

    private static final CacheHolder<CustomerGroupCache> HOLDER = new CacheHolder<>(new CustomerGroupCache());

    public static List<CustomerGroup> getGroupList() {
        return HOLDER.getInstance().groupList;
    }

    public static Map<Integer, CustomerGroup> getGroupMap() {
        return HOLDER.getInstance().groupMapById;
    }

    public static void flush(Connection con) {
        HOLDER.flush(con);
    }

    // end of static part

    private List<CustomerGroup> groupList;
    private Map<Integer, CustomerGroup> groupMapById;

    @Override
    protected CustomerGroupCache newInstance() {
        CustomerGroupCache result = new CustomerGroupCache();

        Setup setup = Setup.getSetup();

        Connection con = setup.getDBConnectionFromPool();
        try {
            result.groupList = new CustomerGroupDAO(con).getGroupList();
            result.groupMapById = new HashMap<>();

            for (CustomerGroup group : result.groupList) {
                result.groupMapById.put(group.getId(), group);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);
        }

        return result;
    }
}