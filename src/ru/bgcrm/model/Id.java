package ru.bgcrm.model;

import java.io.Serializable;

public class Id implements Serializable {
    protected int id;

    public int getId() {
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
