package ru.bgcrm.model.param;

import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.model.param.PhoneFormat;
import org.bgerp.util.Log;

import ru.bgcrm.util.PatternFormatter;

public class ParameterPhoneValueItem {
    private static final Log log = Log.getLog();

    private String phone = "";
    private String comment = "";

    public ParameterPhoneValueItem() {}

    public ParameterPhoneValueItem(String phone, String comment) {
        this.phone = phone;
        this.comment = comment;
    }

    @Deprecated
    public ParameterPhoneValueItem(String phone, String format, String comment) {
        log.warnd("Deprecated constructor was called.");
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

    @Deprecated
    public String getFormat() {
        log.warndMethod("getFormat", null);
        return "";
    }

    @Deprecated
    public void setFormat(String format) {
        log.warndMethod("setFormat", null);
    }

    @Deprecated
    public int getFlags() {
        log.warndMethod("getFlags", null);
        return 0;
    }

    @Deprecated
    public void setFlags(int flags) {
        log.warndMethod("setFlags", null);
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