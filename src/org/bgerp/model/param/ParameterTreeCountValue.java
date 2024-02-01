package org.bgerp.model.param;

import java.math.BigDecimal;

public class ParameterTreeCountValue {
    private BigDecimal count;
    private String comment = "";

    public BigDecimal getCount() {
        return count;
    }

    public void setCount(BigDecimal count) {
        this.count = count;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "ParameterTreeCountValue [count=" + count + ", comment=" + comment + "]";
    }
}
