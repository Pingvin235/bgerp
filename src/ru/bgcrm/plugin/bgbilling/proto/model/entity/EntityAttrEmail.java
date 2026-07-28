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
     * Adds a contact to the list
     * @param name the contact name
     * @param address the email address
     * @return this
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
     * @return the contact list (email and name)
     */
    public List<EmailContact> getContactList() {
        if (this.contactList == null) {
            this.contactList = read(this.data);
            this.data = null;
        }

        return contactList;
    }

    /**
     * Contact for {@link EntityAttrEmail}, contains the email - {@link #address} and the name - {@link #name}
     *
     * @author amir
     */
    public static class EmailContact {
        /**
         * Contact name
         */
        private String name;

        /**
         * Email address
         */
        private String address;

        public EmailContact() {
        }

        public EmailContact(String name, String address) {
            this.name = name;
            this.address = address;
        }

        /**
         * @return the contact name
         */
        public String getName() {
            // the name may come from the billing wrapped in quotes
            return Utils.maskNull(name).replace("\"", "");
        }

        /**
         * @param name the contact name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the email address
         */
        public String getAddress() {
            return address;
        }

        /**
         * @param address the email address
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
