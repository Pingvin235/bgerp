package ru.bgcrm.plugin.bgbilling.proto.model.contract;

import java.util.Date;

public class ContractCreateData {
    private String title;
    private String customTitle;
    private String password;
    private int patternId;
    private int contractSuperId;
    private int contractSubMode;
    private Date dateFrom;

    public ContractCreateData() {
    }

    public String getTitle() {
        return title;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public String getPassword() {
        return password;
    }

    public int getPatternId() {
        return patternId;
    }

    public int getContractSuperId() {
        return contractSuperId;
    }

    public int getContractSubMode() {
        return contractSubMode;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }

    public void setContractSuperId(int contractSuperId) {
        this.contractSuperId = contractSuperId;
    }

    public void setContractSubMode(int contractSubMode) {
        this.contractSubMode = contractSubMode;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ContractCreateData data;

        private Builder() {
            data = new ContractCreateData();
        }

        public Builder setTitle(String title) {
            data.title = title;
            return this;
        }

        public Builder setCustomTitle(String customTitle) {
            data.customTitle = customTitle;
            return this;
        }

        public Builder setPassword(String password) {
            data.password = password;
            return this;
        }

        public Builder setPatternId(int patternId) {
            data.patternId = patternId;
            return this;
        }

        public Builder setContractSuperId(int contractSuperId) {
            data.contractSuperId = contractSuperId;
            return this;
        }

        public Builder setContractSubMode(int contractSubMode) {
            data.contractSubMode = contractSubMode;
            return this;
        }

        public Builder setDateFrom(Date dateFrom) {
            data.dateFrom = dateFrom;
            return this;
        }

        public ContractCreateData build() {
            try {
                return data;
            } finally {
                data = null;
            }
        }
    }
}
