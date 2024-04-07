package org.bgerp.event.process.queue;

import org.bgerp.event.base.UserEvent;
import org.bgerp.model.process.queue.Column;

public class QueueColumnEvent extends UserEvent {
    private final Column columnDefault;
    private Column column;

    public QueueColumnEvent(Column columnDefault) {
        super(null);
        this.columnDefault = columnDefault;
    }

    public Column getColumnDefault() {
        return columnDefault;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }
}
