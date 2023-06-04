package ru.bgcrm.model.process;

import org.bgerp.model.base.IdTitle;

public class Status extends IdTitle {
    private int pos;

    public Status() {
        super();
    }

    public Status(int id, String title) {
        super(id, title);
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}