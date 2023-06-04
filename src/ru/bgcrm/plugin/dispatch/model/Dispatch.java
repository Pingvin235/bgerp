package ru.bgcrm.plugin.dispatch.model;

import org.bgerp.model.base.IdTitleComment;

public class Dispatch extends IdTitleComment {
    private int accountCount;

    public int getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(int accountCount) {
        this.accountCount = accountCount;
    }
}
