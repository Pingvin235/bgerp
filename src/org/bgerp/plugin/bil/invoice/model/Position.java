package org.bgerp.plugin.bil.invoice.model;

import java.math.BigDecimal;

import org.bgerp.model.base.IdStringTitle;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Position extends IdStringTitle {
    private BigDecimal amount;
    /** E.g. hour */
    private String unit;
    private int quantity;

    public Position() {}

    public Position(String id, String title, BigDecimal amount, String unit, int quantity) {
        super(id, title);
        this.amount = amount;
        this.unit = unit;
        this.quantity = quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @JsonIgnore
    public BigDecimal getPrice() {
        if (quantity == 0 || amount == null)
            return BigDecimal.ZERO;
        return amount.divide(BigDecimal.valueOf(quantity));
    }
}
