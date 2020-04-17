package ru.bgcrm.cache;

import java.sql.Connection;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ru.bgcrm.util.sql.SQLUtils;

public class CacheHolder<C extends Cache<C>> {
    private C factory;
    private volatile C cache;
    private volatile Date lastAcces;

    private Logger log = Logger.getLogger(this.getClass());

    public CacheHolder(C factory) {
        this.factory = factory;
    }

    public C getInstance() {
        synchronized (factory) {
            if (cache == null || !DateUtils.isSameDay(lastAcces, new Date())) {
                log.debug("cache newInstance");
                cache = factory.newInstance();
                lastAcces = new Date();
            }
        }
        return cache;
    }

    public void flush(Connection con) {
        SQLUtils.commitConnection(con);
        cache = null;
        lastAcces = null;
    }
}