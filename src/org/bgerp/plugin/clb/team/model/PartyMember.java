package org.bgerp.plugin.clb.team.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import ru.bgcrm.model.IdTitle;

public class PartyMember extends IdTitle {
    private int partyId;
    private List<PartyPayment> payments = Collections.emptyList();

    public int getPartyId() {
        return partyId;
    }

    public void setPartyId(int partyId) {
        this.partyId = partyId;
    }

    public List<PartyPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<PartyPayment> payments) {
        this.payments = payments;
    }

    public BigDecimal paymentsAmount() {
        return payments.stream()
            .map(PartyPayment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
