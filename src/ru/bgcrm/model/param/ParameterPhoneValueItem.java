package ru.bgcrm.model.param;

import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.model.param.PhoneFormat;
import org.bgerp.util.text.PatternFormatter;

public class ParameterPhoneValueItem {
    private String phone = "";
    private String comment = "";

    public ParameterPhoneValueItem() {}

    public ParameterPhoneValueItem(String phone, String comment) {
        this.phone = phone;
        this.comment = comment;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        Setup setup = Setup.getSetup();

        String itemFormat = setup.get("param.phone.format", "(${number})( [${comment}])");
        String numberFormat = setup.get("param.phone.format.number");

        return PatternFormatter.processPattern(itemFormat, Map.of("number", new PhoneFormat(numberFormat).format(phone), "comment", comment));
    }

    @Override
    public int hashCode() {
        return phone.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ParameterPhoneValueItem && ((ParameterPhoneValueItem) obj).phone.equals(phone);
    }
}