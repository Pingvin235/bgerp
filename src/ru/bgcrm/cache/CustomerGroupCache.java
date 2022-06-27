package ru.bgcrm.cache;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.util.Log;

import ru.bgcrm.dao.CustomerGroupDAO;
import ru.bgcrm.model.customer.CustomerGroup;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

public class CustomerGroupCache extends Cache<CustomerGroupCache> {
    private static Log log = Log.getLog();

    private static CacheHolder<CustomerGroupCache> holder = new CacheHolder<CustomerGroupCache>(new CustomerGroupCache());

    public static List<CustomerGroup> getGroupList() {
        return holder.getInstance().groupList;
    }

    public static Map<Integer, CustomerGroup> getGroupMap() {
        return holder.getInstance().groupMapById;
    }

    public static void flush(Connection con) {
        holder.flush(con);
    }

    // конец статической части
    private List<CustomerGroup> groupList;
    private Map<Integer, CustomerGroup> groupMapById;

    @Override
    protected CustomerGroupCache newInstance() {
        CustomerGroupCache result = new CustomerGroupCache();

        Setup setup = Setup.getSetup();

        Connection con = setup.getDBConnectionFromPool();
        try {
            result.groupList = new CustomerGroupDAO(con).getGroupList();
            result.groupMapById = new HashMap<Integer, CustomerGroup>();

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