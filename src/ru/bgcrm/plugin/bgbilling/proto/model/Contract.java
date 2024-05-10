package ru.bgcrm.plugin.bgbilling.proto.model;

import org.apache.commons.lang3.StringUtils;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.SearchableIdTitle;

public class Contract extends SearchableIdTitle implements Comparable<Contract> {
    public final static String OBJECT_TYPE = "contract";

    private String billingId;
    private String comment;

    public Contract() {
    }

    public Contract(CommonObjectLink contractLink) {
        this(StringUtils.substringAfter(contractLink.getLinkObjectType(), ":"), contractLink.getLinkObjectId(), contractLink.getLinkObjectTitle(),
                contractLink.getLinkObjectComment());
    }

    public Contract(String billingId, int id, String title, String comment) {
        this.billingId = billingId;
        this.id = id;
        this.title = title;
        this.comment = comment;
    }

    public String getBillingId() {
        return billingId;
    }

    public void setBillingId(String billingId) {
        this.billingId = billingId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int compareTo(Contract o) {
        return o.getTitle().compareTo(title);
    }

    @Override
    public boolean equals(Object obj) {
        Contract contract = (Contract) obj;
        return contract.billingId.equals(billingId) && contract.id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}