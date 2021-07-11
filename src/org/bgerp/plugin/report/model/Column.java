package org.bgerp.plugin.report.model;

import java.sql.Timestamp;
import java.util.Date;

import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgerp.l10n.Localizer;
import ru.bgerp.l10n.Titled;
import ru.bgerp.util.Log;

/**
 * Report's column.
 * <p>Value of {@link #getId()} allows to access column by it.
 * <p>With empty {@link #getTitle()} value column is not shown to user.
 * 
 * @author Shamil Vakhitov
 */
public abstract class Column implements Titled {
    private int index;
    private final String id;
    private final String title;
    private final String ltitle;

    public Column(String id, String title, String ltitle) {
        this.id = id;
        this.title = title;
        this.ltitle = ltitle;
        if (Utils.notBlankString(title) && Utils.notBlankString(ltitle))
            throw new IllegalArgumentException("Only one of 'title' and 'ltitle' fields can be defined.");
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String toString(Object object) {
        return String.valueOf(object);
    }

    public boolean isVisible() {
        return Utils.notBlankString(title) || Utils.notBlankString(ltitle);
    }

    @Override
    public String getTitle(Localizer l) {
        if (l != null && Utils.notBlankString(ltitle))
            return l.l(ltitle);
        
        return title;
    }

    public Object accept(Object value) {
        throw new IllegalArgumentException(Log.format("Incorrect object class: '{}' for column: '{}'", 
            value.getClass().getName(), getId()));
    }

    public static class ColumnString extends Column {
        public ColumnString(String id, String title, String ltitle) {
            super(id, title, ltitle);
        }

        @Override
        public String accept(Object value) {
            return String.valueOf(value);
        }
    }

    public static class ColumnInteger extends Column {
        public ColumnInteger(String id, String title, String ltitle) {
            super(id, title, ltitle);
        }

        @Override
        public Object accept(Object value) {
            if (value == null)
                return 0;
            if (value instanceof Integer)
                return (Integer) value;
            return super.accept(value);
        }
    }
    public static class ColumnDateTime extends Column {
        private final String format;

        public ColumnDateTime(String id, String title, String ltitle, String format) {
            super(id, title, ltitle);
            this.format = format;
        }

        @Override
        public Object accept(Object value) {
            if (value == null)
                return null;
            if (value instanceof Date)
                return (Date) value;
            if (value instanceof Timestamp)
                return TimeUtils.convertTimestampToDate((Timestamp) value);
            return super.accept(value);
        }

        @Override
        public String toString(Object object) {
            return TimeUtils.format((Date) object, format);
        }
    }

    /**
     * Count of unique and not empty values in another column.
     * This column used only for {@link Chart}.
     */
    public static class ColumnCount extends Column {
        private final Column column;

        public ColumnCount(Column column) {
            super("", null, null);
            this.column = column;
        }

        public Column getColumn() {
            return column;
        }
    }
}
