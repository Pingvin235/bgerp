package org.bgerp.plugin.clb.team.model;

import java.math.BigDecimal;

import ru.bgcrm.model.Id;

public class PartyPayment extends Id {
    private int partyId;
    private int memberId;
    private BigDecimal amount;
    private String description;

    public int getPartyId() {
        return partyId;
    }

    public void setPartyId(int partyId) {
        this.partyId = partyId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
