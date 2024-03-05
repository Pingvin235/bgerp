package ru.bgcrm.plugin.bgbilling.proto.model.bill;

import org.bgerp.model.base.IdTitle;

/** Тип реквизита.<br>
 * Типы реквизитов ранее не хранились в БД, а заводились в конфиге. Но теперь(с версии 6.3) они храняться в таблице bill_attribute_type_$mid<br><br>
 * name - ключ<br>
 * title - описание
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