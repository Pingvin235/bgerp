package ru.bgcrm.model.param;

import java.util.List;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.Utils;

public class ParameterEmailValue {
    private String value;
    private String comment;

    public ParameterEmailValue() {}

    public ParameterEmailValue(String email) {
        this.value = email;
        this.comment = "";
    }

    public ParameterEmailValue(String email, String comment) {
        this.value = email;
        this.comment = comment;
    }

    public void setValue(String value) throws BGException {
        check(value);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getUsername() {
        if (value.indexOf("@") == -1) {
            return "";
        }
        return value.split("@")[0];
    }

    public String getDomain() {
        try {
            return value.split("@")[1];
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private void check(String email) throws BGException {
        if (!Utils.validateEmail(email)) {
            throw new BGMessageException("Неверное значение параметра email!");
        }
    }

    public static final String getEmails(List<ParameterEmailValue> emails) {
        StringBuffer result = new StringBuffer();
        for (ParameterEmailValue val : emails) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(val.getValue());
            if (!"".equals(val.getComment())) {
                result.append(" [ " + val.getComment() + " ]");
            }
            result.append(";");
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ParameterEmailValue))
            return false;

        ParameterEmailValue emailValue = (ParameterEmailValue) object;

        if (!value.equals(emailValue.value))
            return false;
        if (!comment.equals(emailValue.comment))
            return false;

        return true;
    }
}
