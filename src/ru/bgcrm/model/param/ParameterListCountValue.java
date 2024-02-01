package ru.bgcrm.model.param;

import java.math.BigDecimal;

import ru.bgcrm.util.Utils;

@Deprecated
public class ParameterListCountValue {
    private BigDecimal count;
    private String comment = "";

    public ParameterListCountValue(String count) {
        this.count = Utils.parseBigDecimal(count);
    }

    public ParameterListCountValue(BigDecimal count, String comment) {
        this.count = count;
        this.comment = comment;
    }

    public BigDecimal getCount() {
        return count;
    }

    public void setCount(BigDecimal paramDouble) {
        this.count = paramDouble;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String paramString) {
        this.comment = paramString;
    }

    @Override
    public String toString() {
        return "ParameterListCountValue [count=" + count + ", comment=" + comment + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParameterListCountValue other = (ParameterListCountValue) obj;
        if (count == null) {
            if (other.count != null)
                return false;
        } else if (!count.equals(other.count))
            return false;
        if (comment == null) {
            if (other.comment != null)
                return false;
        } else if (!comment.equals(other.comment))
            return false;
        return true;
    }
}
