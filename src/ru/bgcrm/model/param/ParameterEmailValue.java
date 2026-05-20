package ru.bgcrm.model.param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bgerp.app.exception.BGMessageException;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import ru.bgcrm.util.Utils;

public class ParameterEmailValue {
    /**
     * Parse list of email values out of a comma-separated string
     * @param values the source string
     * @return the list of values
     */
    public static final List<ParameterEmailValue> of(String values) {
        var result = new ArrayList<ParameterEmailValue>();

        for (String token : Utils.toList(values)) {
            try {
                var address = InternetAddress.parse(token)[0];
                var value = new ParameterEmailValue();
                value.setValue(address.getAddress());
                value.setComment(Utils.maskNull(address.getPersonal()));
                result.add(value);
            } catch (AddressException | BGMessageException e) {
            }
        }

        return result;
    }

    /**
     * Unified representation 'email' parameter values as a string
     * The logic is duplicated in {@link ru.bgcrm.dao.ParamValueSelect#paramSelectQuery(String, String, StringBuilder, StringBuilder, boolean) for process queues.
     * @param values the parameter values
     * @return the comma-separated string with {@link #toString()} generated parts
     */
    public static final String toString(Collection<ParameterEmailValue> values) {
        return Utils.toString(values);
    }

    /** email value */
    private String value;
    /** Display name, personal */
    private String comment;

    public ParameterEmailValue() {}

    /**
     * Public constructor
     * @param value the email value
     */
    public ParameterEmailValue(String value) {
        this.value = value;
        this.comment = "";
    }

    /**
     * Public constructor
     * @param value the email value
     * @param comment the display name
     */
    public ParameterEmailValue(String value, String comment) {
        this.value = value;
        this.comment = comment;
    }

    /**
     * Setter
     * @param value email value
     * @throws BGMessageException when not correct email value is being set
     */
    public void setValue(String value) throws BGMessageException {
        if (!Utils.isValidEmail(value)) {
            throw new BGMessageException("Incorrect email value: {}", value);
        }
        this.value = value;
    }

    /**
     * @return email value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return part of email before {@code @}
     */
    public String getUsername() {
        if (value.indexOf("@") == -1) {
            return "";
        }
        return value.split("@")[0];
    }

    /**
     * @return part of email after {@code @}
     */
    public String getDomain() {
        try {
            return value.split("@")[1];
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * @return display name
     */
    public String getComment() {
        return comment;
    }

    /**
     * Setter for a display name
     * @param comment the title
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        if (Utils.notBlankString(comment))
            return comment + " <" + value  + ">";
        return value;
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
