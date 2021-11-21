package org.bgerp.plugin.bil.billing.invoice.model;

import java.math.BigDecimal;

import ru.bgcrm.model.IdStringTitle;

public class Position extends IdStringTitle {
    private BigDecimal summa;

    public BigDecimal getSumma() {
        return summa;
    }

    public void setSumma(BigDecimal summa) {
        this.summa = summa;
    }
}
