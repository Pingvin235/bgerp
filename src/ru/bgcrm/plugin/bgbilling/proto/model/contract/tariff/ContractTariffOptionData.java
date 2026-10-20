package ru.bgcrm.plugin.bgbilling.proto.model.contract.tariff;

import java.math.BigDecimal;

import ru.bgcrm.model.Period;

public class ContractTariffOptionData {
    private int contractId;
    private int optionId;
    private int modeId;
    private BigDecimal chargeSum;
    private Period period;
    private String comment;
    private boolean web = false;

    public int getContractId() {
        return contractId;
    }

    public int getOptionId() {
        return optionId;
    }

    public int getModeId() {
        return modeId;
    }

    public BigDecimal getChargeSum() {
        return chargeSum;
    }

    public Period getPeriod() {
        return period;
    }

    public String getComment() {
        return comment;
    }

    public boolean isWeb() {
        return web;
    }

    public ContractTariffOptionData setContractId(int contractId) {
        this.contractId = contractId;
        return this;
    }

    public ContractTariffOptionData setOptionId(int optionId) {
        this.optionId = optionId;
        return this;
    }

    public ContractTariffOptionData setModeId(int modeId) {
        this.modeId = modeId;
        return this;
    }

    public ContractTariffOptionData setChargeSum(BigDecimal chargeSum) {
        this.chargeSum = chargeSum;
        return this;
    }

    public ContractTariffOptionData setPeriod(Period period) {
        this.period = period;
        return this;
    }

    public ContractTariffOptionData setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public ContractTariffOptionData setWeb(boolean web) {
        this.web = web;
        return this;
    }
}