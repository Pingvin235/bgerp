package org.bgerp.plugin.pln.callboard.cache;

import java.sql.Connection;
import java.util.Map;

import org.bgerp.plugin.pln.callboard.dao.WorkTypeDAO;
import org.bgerp.plugin.pln.callboard.model.WorkType;
import org.bgerp.util.Log;

import ru.bgcrm.cache.Cache;
import ru.bgcrm.cache.CacheHolder;
import ru.bgcrm.util.Setup;

public class CallboardCache extends Cache<CallboardCache> {
    private static Log log = Log.getLog();

    private static CacheHolder<CallboardCache> holder = new CacheHolder<CallboardCache>(new CallboardCache());

    public static WorkType getWorkType(int id) {
        return holder.getInstance().workTypeMap.get(id);
    }

    public static Map<Integer, WorkType> getWorkTypeMap() {
        return holder.getInstance().workTypeMap;
    }

    public static void flush(Connection con) {
        holder.flush(con);
    }

    // конец статической части

    private Map<Integer, WorkType> workTypeMap;

    @Override
    protected CallboardCache newInstance() {
        CallboardCache result = new CallboardCache();

        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            WorkTypeDAO typeDao = new WorkTypeDAO(con);

            result.workTypeMap = typeDao.getWorkTypeMap();
        } catch (Exception ex) {
            log.error(ex);
        }

        return result;
    }
}