package ru.bgcrm.plugin.bgbilling.proto.model.entity;


import ru.bgcrm.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityAttrEmail
        extends EntityAttr {
    private String data;
    private List<EmailContact> contactList;
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^(.*)\\s*+\\<(.*)\\>$");

    protected EntityAttrEmail() {
        super(EntitySpecAttrType.EMAIL);
    }

    public EntityAttrEmail(int entityId, int entitySpecAttrId) {
        super(EntitySpecAttrType.EMAIL, entityId, entitySpecAttrId);
    }

    public EntityAttrEmail(int entityId, int entitySpecAttrId, String name, String address) {
        super(EntitySpecAttrType.EMAIL, entityId, entitySpecAttrId);
        setContactList(Collections.singletonList(new EmailContact(name, address)));
    }

    /**
     * Добавление контакта в список
     *
     * @param name    имя контакта.
     * @param address email-адрес.
     * @return
     */
    public EntityAttrEmail addContact(String name, String address) {
        getContactList().add(new EmailContact(name, address));
        return this;
    }


    public String getData() {
        if (contactList != null) {
            return write(contactList);
        }

        return data;
    }

    /**
     * Для внутреннего использования. Для обычной работы используйте {@link #setContactList(List)} or {@link #addContact(String, String)}
     *
     * @param data
     */
    public void setData(String data) {
        this.data = data;
        this.contactList = null;
    }

    /**
     * @return
     * @deprecated use {@link #getContactList()} or {@link #getAddressList()} or {@link #getSimpleContactList()} or {@link #getData()}
     */
    @Deprecated
    public String getValue() {
        return getData();
    }

    /**
     * @param value
     * @deprecated use {@link #setContactList(List)} or {@link #addContact(String, String)} or {@link #setData(String)}
     */
    @Deprecated
    public void setValue(String value) {
        setData(value);
    }

    @Override
    public String toString() {
        String value;
        if (contactList != null) {
            value = write(contactList);
        } else {
            value = this.data;
        }

        return value == null ? "" : value.replaceAll("\n", "; ");
    }

    //официальный стандартизированный regexp для е-майла
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])",
                    Pattern.CASE_INSENSITIVE);


    private static List<EmailContact> read(String value) {
        final List<EmailContact> result = new ArrayList<>(4);

        if (Utils.isBlankString(value)) {
            return result;
        }

        for (String email : value.split("\n")) {
            if (email.trim().isEmpty()) {
                continue;
            }

            final EmailContact emailData = new EmailContact();

            final Matcher matcher = FORMAT_PATTERN.matcher(email);
            if (matcher.matches()) {
                emailData.setAddress(matcher.group(2).trim());
                emailData.setName(matcher.group(1).trim());
            } else {
                emailData.setAddress(email.trim());
                emailData.setName("");
            }

            result.add(emailData);
        }

        return result;
    }

    private static String write(final List<EmailContact> list) {
        if (list == null || list.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(60);
        for (EmailContact contact : list) {
            if (Utils.isBlankString(contact.getAddress())) {
                continue;
            }

            if (Utils.notBlankString(contact.getName())) {
                sb.append(contact.getName().trim()).append(' ');
            }

            sb.append('<').append(contact.getAddress()).append('>').append('\n');
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * Получение списка контактов (email и имя).
     *
     * @return
     */
    public List<EmailContact> getContactList() {
        if (this.contactList == null) {
            this.contactList = read(this.data);
            this.data = null;
        }

        return contactList;
    }

    /**
     * Установка списка контактов.
     *
     * @param list
     * @return this
     */
    public EntityAttrEmail setContactList(final List<EmailContact> list) {
        if (list == null || list.size() == 0) {
            this.contactList = new ArrayList<>(2);
        } else {
            this.contactList = new ArrayList<>(list);
        }

        this.data = null;

        return this;
    }

    /**
     * Получение списка строк вида <b>"Иванов Петр Сидорович &lt;ivanov@gmail.com&gt;"</b>
     *
     * @return
     */
    public List<String> getSimpleContactList() {
        final List<String> result = new ArrayList<>(5);

        final List<EmailContact> list = getContactList();
        if (list == null || list.size() == 0) {
            return result;
        }

        StringBuilder sb = new StringBuilder(60);
        for (EmailContact contact : list) {
            if (Utils.notBlankString(contact.getName())) {
                sb.append(contact.getName()).append(' ');
            }

            sb.append('<').append(contact.getAddress()).append('>');

            result.add(sb.toString());
            sb.setLength(0);
        }

        return result;
    }

    /**
     * Получение списка email без имени контакта и без &lt; и &gt;.
     *
     * @return
     */
    public List<String> getAddressList() {
        final List<EmailContact> list = getContactList();

        List<String> result = new ArrayList<>(list.size());
        for (EmailContact contact : list) {
            result.add(contact.getAddress());
        }

        return result;
    }

    public static EntityAttrEmail parse(final int entityId, final int entitySpecAttrId, final String value) {
        final EntityAttrEmail result = new EntityAttrEmail(entityId, entitySpecAttrId);
        result.data = value;
        return result;
    }

    /**
     * Контакт для EntityAttrEmail. Содержит email - {@link #address} и имя - {@link #name}.
     *
     * @author amir
     */
    public static class EmailContact {
        /**
         * Имя контакта.
         */
        private String name;

        /**
         * Email адрес.
         */
        private String address;

        public EmailContact() {
        }

        public EmailContact(String name, String address) {
            this.name = name;
            this.address = address;
        }

        /**
         * Получение имени контакта.
         *
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * Установка имени контакта.
         *
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Получение email.
         *
         * @return
         */
        public String getAddress() {
            return address;
        }

        /**
         * Установка email.
         *
         * @param address
         */
        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return (name != null ? name + " " : "") + "<" + address + ">";
        }
    }
}
