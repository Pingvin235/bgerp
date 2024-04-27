package org.bgerp.cache;

import java.sql.Connection;

import org.bgerp.util.Log;

import ru.bgcrm.util.sql.SQLUtils;

/**
 * Cache holder
 * @param <C> the class of held cache
 * @author Amir Absalilov
 */
public class CacheHolder<C extends Cache<C>> {
    private Log log = Log.getLog(this.getClass());

    private C factory;
    private volatile C cache;

    public CacheHolder(C factory) {
        this.factory = factory;
    }

    public C getInstance() {
        synchronized (factory) {
            if (cache == null || !cache.isValid()) {
                log.info("Creating new cache instance");
                cache = factory.newInstance();
            }
        }
        return cache;
    }

    public void flush(Connection con) {
        SQLUtils.commitConnection(con);
        cache = null;
    }
}