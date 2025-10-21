package ru.bgcrm.plugin.bgbilling.proto.model.contract;

import ru.bgcrm.plugin.bgbilling.proto.model.Contract;

public class ContractSearchDto {
    private int contractId;
    private String contractTitle;
    private String contractComment;

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public String getContractTitle() {
        return contractTitle;
    }

    public void setContractTitle(String contractTitle) {
        this.contractTitle = contractTitle;
    }

    public String getContractComment() {
        return contractComment;
    }

    public void setContractComment(String contractComment) {
        this.contractComment = contractComment;
    }

    public Contract toContract() {
        var result = new Contract();
        result.setId(contractId);
        result.setTitle(contractTitle);
        result.setComment(contractComment);
        return result;
    }
}
