package ru.bgcrm.plugin.bgbilling.proto.model.entity;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.bgcrm.util.Utils;

public class EntityAttrEmail extends EntityAttr {
    private String data;
    private List<EmailContact> contactList;
    private static final Pattern FORMAT_PATTERN = Pattern.compile("^(.*)\\s*+\\<(.*)\\>$");

    protected EntityAttrEmail() {
        super(EntitySpecAttrType.EMAIL);
    }

    public EntityAttrEmail(int entityId, int entitySpecAttrId) {
        super(EntitySpecAttrType.EMAIL, entityId, entitySpecAttrId);
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
