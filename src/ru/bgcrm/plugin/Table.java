package ru.bgcrm.plugin;

/**
 * Database table, used by plugin.
 * @author Shamil Vakhitov 
 */
public class Table {
    /** Table name or a prefix for monthly tables.*/
    private final String name;
    private final Table.Type type;

    public Table(String name, Table.Type data) {
        this.name = name.trim();
        this.type = data;
    }

    public String getName() {
        return name;
    }

    public Table.Type getData() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Table other = (Table) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    /** 
     * Table data type.
     */
    public static enum Type {
        /**
         * Directory, rare updated and cached.
         * Referenced from data tables.
         */
        DIRECTORY,
        /**
         * Data table.
         */
        DATA,
        /**
         * Data table, monthly broken.
         */
        DATA_MONTHLY,
        /**
         * Unimportant data, may be lost without significant damage.
         * As example, logs.
         */
        TRASH,
        /**
         * Trash table, monthly broken.
         */
        TRASH_MONTHLY,
        /**
         * Not more used table.
         */
        DEPRECATED
    }
}