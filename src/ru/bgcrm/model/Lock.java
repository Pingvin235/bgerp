package ru.bgcrm.model;

/**
 * Блокировка пользователем ресурса (обычно для правки).
 * @author shamil
 */
public class Lock {
    // стандартная блокировка - 20 секунд
    public static final long LOCK_TIME = 20 * 1000;

    private final String id;
    private final int userId;
    private volatile long toTime;

    public Lock(String id, int userId) {
        this.id = id;
        this.userId = userId;
        this.toTime = System.currentTimeMillis() + LOCK_TIME;
    }

    public String getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public long getToTime() {
        return toTime;
    }

    public void continueTime() {
        toTime = System.currentTimeMillis() + Lock.LOCK_TIME;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Lock other = (Lock) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}