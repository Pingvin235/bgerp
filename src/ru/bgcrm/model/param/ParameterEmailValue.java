package ru.bgcrm.model.param;

import org.bgerp.app.exception.BGMessageException;

import ru.bgcrm.util.Utils;

public class ParameterEmailValue {
    /** Email value. */
    private String value;
    /** Name of person. */
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

    /**
     * Setter.
     * @param value Email value.
     * @throws BGMessageException when not Email value is being set.
     */
    public void setValue(String value) throws BGMessageException {
        if (!Utils.isValidEmail(value)) {
            throw new BGMessageException("Неверное значение параметра email!");
        }
        this.value = value;
    }

    /**
     * @return Email value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @return part of Email before {@code @}.
     */
    public String getUsername() {
        if (value.indexOf("@") == -1) {
            return "";
        }
        return value.split("@")[0];
    }

    /**
     * @return part of Email after  {@code @}.
     */
    public String getDomain() {
        try {
            return value.split("@")[1];
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * @return person's title.
     */
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        if (Utils.notBlankString(comment))
            return comment + " <" + value  + ">";
        return value;
    }

    public static final String toString(Iterable<ParameterEmailValue> emails) {
        var result = new StringBuilder();
        for (ParameterEmailValue val : emails) {
            if (result.length() > 0)
                result.append(", ");
            result.append(val.toString());
        }
        return result.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParameterEmailValue other = (ParameterEmailValue) obj;
        if (comment == null) {
            if (other.comment != null)
                return false;
        } else if (!comment.equals(other.comment))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}
