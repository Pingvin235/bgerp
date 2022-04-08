package ru.bgcrm.plugin;

import java.util.Set;

/**
 * Database table, used by plugin.
 *
 * @author Shamil Vakhitov
 */
public class Table {
    /** Table name or a prefix for monthly tables.*/
    private final String name;
    private final Set<Table.Type> types;

    public Table(String name, Table.Type... type) {
        this.name = name.trim();
        this.types = Set.of(type);
    }

    public String getName() {
        return name;
    }

    public Set<Table.Type> getTypes() {
        return types;
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
         * Unimportant data, may be lost without significant damage.
         * As example, logs.
         */
        TRASH,
        /**
         * Monthly broken.
         */
        MONTHLY,
        /**
         * Not more used table.
         */
        DEPRECATED
    }
}