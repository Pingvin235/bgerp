package org.bgerp.cache;

import java.sql.Connection;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ru.bgcrm.util.sql.SQLUtils;

public class CacheHolder<C extends Cache<C>> {
    private C factory;
    private volatile C cache;
    private volatile Date lastAccess;

    private Logger log = Logger.getLogger(this.getClass());

    public CacheHolder(C factory) {
        this.factory = factory;
    }

    public C getInstance() {
        synchronized (factory) {
            if (cache == null || !DateUtils.isSameDay(lastAccess, new Date())) {
                log.debug("cache newInstance");
                cache = factory.newInstance();
                lastAccess = new Date();
            }
        }
        return cache;
    }

    public void flush(Connection con) {
        SQLUtils.commitConnection(con);
        cache = null;
        lastAccess = null;
    }
}