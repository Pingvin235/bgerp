package org.bgerp.plugin.bil.billing.invoice.model;

import java.math.BigDecimal;

import ru.bgcrm.model.IdStringTitle;

public class Position extends IdStringTitle {
    private BigDecimal amount;

    public Position() {}

    public Position(String id, String title, BigDecimal amount) {
        super(id, title);
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal value) {
        this.amount = value;
    }
}
