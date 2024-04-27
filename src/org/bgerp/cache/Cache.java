package org.bgerp.cache;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

/**
 * In-memory cache and factory of itself
 * @param <C> tricky generic for being a factory
 * @author Amir Absalilov
 */
public abstract class Cache<C extends Cache<C>> {
    /**
     * Creation time.
     */
    private final Date created = new Date();

    /**
     * Protected constructor, available only for children
     */
    protected Cache() {}

    /**
     * Creates a new cache instance
     * @return the instance
     */
    protected abstract C newInstance();

    /**
     * Is the cache valid
     * @return the validity state
     */
    public boolean isValid() {
        return DateUtils.isSameDay(created, new Date());
    }
}