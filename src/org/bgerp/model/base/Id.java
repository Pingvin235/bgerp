package org.bgerp.model.base;

import java.io.Serializable;

/**
 * Basic entity with unique integer ID.
 *
 * @author Shamil Vakhitov
 */
public class Id implements Serializable, org.bgerp.model.base.iface.Id {
    protected int id;

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(int value) {
        this.id = value;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (id != ((Id) obj).id)
            return false;
        return true;
    }
}
