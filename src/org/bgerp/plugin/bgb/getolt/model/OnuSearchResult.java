package org.bgerp.plugin.bgb.getolt.model;

import java.util.List;

/**
 * Result of ONU search from GetOLT API.
 */
public class OnuSearchResult {
    private boolean success;
    private String errorMessage;
    private List<OnuData> onus;
    private String contractNumber;
    private Integer cid;
    private String operator;

    public OnuSearchResult() {
        this.success = false;
    }

    public static OnuSearchResult error(String message) {
        OnuSearchResult result = new OnuSearchResult();
        result.success = false;
        result.errorMessage = message;
        return result;
    }

    public static OnuSearchResult success(List<OnuData> onus) {
        OnuSearchResult result = new OnuSearchResult();
        result.success = true;
        result.onus = onus;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<OnuData> getOnus() {
        return onus;
    }

    public void setOnus(List<OnuData> onus) {
        this.onus = onus;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean hasOnus() {
        return onus != null && !onus.isEmpty();
    }
}
