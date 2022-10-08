package ru.bgcrm.plugin.bgbilling.event.client;

import ru.bgcrm.event.client.ClientEvent;

/**
 * Сообщение о необходимости открыть вкладку договора,
 * либо обновить, если она уже открыта.
 */
public class ContractOpenEvent extends ClientEvent {
    private String billingId;
    private int contractId;

    public ContractOpenEvent(String billingId, int contractId) {
        this.billingId = billingId;
        this.contractId = contractId;
    }

    public String getBillingId() {
        return billingId;
    }

    public void setBillingId(String billingId) {
        this.billingId = billingId;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }
}
