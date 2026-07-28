package ru.bgcrm.plugin.bgbilling.proto.model.bill;

import org.bgerp.model.base.IdTitle;

/**
 * Attribute type. Attribute types used to not be stored in the DB, but were configured in the config. Now (since version 6.3) they are stored in the bill_attribute_type_$mid table
 * <br><br>
 * name - the key<br>
 * title - the description
 */
public class AttributeType extends IdTitle {
    private String name = null;
    private String check = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }
}