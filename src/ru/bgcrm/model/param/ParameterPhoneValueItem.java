package ru.bgcrm.model.param;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;
import org.bgerp.util.PhoneFormat;

import ru.bgcrm.util.PatternFormatter;

public class ParameterPhoneValueItem {
    private static final Log log = Log.getLog();

    private String phone = "";
    private String comment = "";
    // deprecated fields
    private String format;
    private int flags = 0;

    public ParameterPhoneValueItem() {}

    public ParameterPhoneValueItem(String phone, String comment) {
        this.phone = phone;
        this.comment = comment;
    }

    @Deprecated
    public ParameterPhoneValueItem(String phone, String format, String comment) {
        log.warnd("Deprecated constructor was called.");
        this.phone = phone;
        this.format = format;
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
        return format;
    }

    @Deprecated
    public void setFormat(String format) {
        log.warndMethod("setFormat", null);
        this.format = format;
    }

    @Deprecated
    public int getFlags() {
        log.warndMethod("getFlags", null);
        return flags;
    }

    @Deprecated
    public void setFlags(int flags) {
        log.warndMethod("setFlags", null);
        this.flags = flags;
    }

    @Override
    public String toString() {
        Setup setup = Setup.getSetup();

        String itemFormat = setup.get("param.phone.format", "(${number})( [${comment}])");
        String numberFormat = setup.get("param.phone.format.number");

        String result = PatternFormatter.insertPatternPart(itemFormat, "number", new PhoneFormat(numberFormat).format(phone));
        result = PatternFormatter.insertPatternPart(result, "comment", comment);

        return result;
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