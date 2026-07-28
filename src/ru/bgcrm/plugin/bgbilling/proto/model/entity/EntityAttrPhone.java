package ru.bgcrm.plugin.bgbilling.proto.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.bgcrm.util.Utils;

/**
 * Attribute/parameter - phones. To get the phone list, use the {@link #getContactList()}, {@link #toPhoneList()}, {@link #toUnformattedPhoneList()} methods
 * @author amir
 */
public class EntityAttrPhone extends EntityAttr {
    private String data;
    private List<PhoneContact> contactList;

    protected EntityAttrPhone() {
        super(EntitySpecAttrType.PHONE);
    }

    /**
     * @param entityId the contract/object/entity ID
     * @param entitySpecAttrId the parameter/attribute type ID
     */
    public EntityAttrPhone(int entityId, int entitySpecAttrId) {
        super(EntitySpecAttrType.PHONE, entityId, entitySpecAttrId);
    }

    public EntityAttrPhone(int entityId, int entitySpecAttrId, String phone, String comment) {
        this(entityId, entitySpecAttrId);
        addContact(phone, comment);
    }

    /**
     * @return the contact list
     */
    public List<PhoneContact> getContactList() {
        return contactList;
    }

    /**
     * @param phoneList the contact list
     */
    public void setContactList(List<PhoneContact> phoneList) {
        this.contactList = phoneList;
    }

    /**
     * Adds a phone (contact)
     * @param phone the phone number
     * @param comment the comment
     * @return this
     */
    public EntityAttrPhone addContact(String phone, String comment) {
        if (contactList == null) {
            contactList = new ArrayList<>();
        }

        contactList.add(new PhoneContact(phone, comment));

        return this;
    }

    public String getData() {
        return data;
    }

    /**
     * For internal use. To add a phone, use {@link #addContact(String, String)} or {@link #setContactList(List)}
     * @param data the raw data
     */
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getContactList() != null) {
            for (PhoneContact p : getContactList()) {
                sb.append(p.getPhone());
                if (Utils.notBlankString(p.getComment())) {
                    sb.append(" [").append(p.getComment()).append(']');
                }

                sb.append("; ");
            }

            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2);
            }
        }

        return sb.toString();
    }

    /**
     * @return the formatted phone list
     */
    public List<String> toPhoneList() {
        if (contactList == null) {
            return Collections.emptyList();
        }

        return contactList.stream().filter(a -> Utils.notBlankString(a.getPhone())).map(PhoneContact::getPhone).toList();
    }

    /**
     * @return the unformatted phone list
     */
    public List<String> toUnformattedPhoneList() {
        return contactList == null ? Collections.emptyList()
                : contactList.stream().map(PhoneContact::toPhoneUnformatted).filter(Utils::notBlankString).toList();
    }

    /**
     * Converts a formatted phone number into a set of digits
     * @param phone +7 (347) 2 924-823
     * @return 73472924823
     */
    // may in theory be called often, and the logic is simple enough to not warrant creating a Matcher object each time
    public static String phoneUnformatted(final String phone) {
        if (Utils.isEmptyString(phone)) {
            return "";
        }

        StringBuilder sb = null;

        for (int i = 0, size = phone.length(); i < size; i++) {
            char c = phone.charAt(i);

            if (Character.isDigit(c)) {
                if (sb != null) {
                    sb.append(c);
                }
            } else {
                if (sb == null) {
                    sb = new StringBuilder(phone.length());

                    for (int j = 0; j < i; j++) {
                        sb.append(phone.charAt(j));
                    }
                }
            }
        }

        if (sb != null) {
            return sb.toString();
        } else {
            return phone;
        }
    }

    /**
     * Contact - phone + comment
     */
    public static class PhoneContact {
        /**
         * Phone
         */
        private String phone = null;

        /**
         * Comment
         */
        private String comment = null;

        public PhoneContact() {
        }

        public PhoneContact(String phone, String comment) {
            this.phone = phone;
            this.comment = comment;
        }

        /**
         * @return the formatted phone
         */
        public String getPhone() {
            return phone;
        }

        /**
         * @param phone the formatted phone
         */
        public void setPhone(String phone) {
            this.phone = phone;
        }

        /**
         * @return the comment
         */
        public String getComment() {
            return comment;
        }

        /**
         * @param comment the comment
         */
        public void setComment(String comment) {
            this.comment = comment;
        }

        /**
         * @return the unformatted phone (73472123456)
         */
        public String toPhoneUnformatted() {
            return phoneUnformatted(phone);
        }
    }
}
