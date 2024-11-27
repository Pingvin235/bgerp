package ru.bgcrm.plugin.bgbilling.proto.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.bgcrm.util.Utils;

/**
 * Атрибут/параметр - телефоны. Чтобы получить список телефонов, используйте методы
 * {@link #getContactList()}, {@link #toPhoneList()} ()}, {@link #toUnformattedPhoneList()} ()}.
 * @author amir
 */
public class EntityAttrPhone extends EntityAttr {
    private String data;
    private List<PhoneContact> contactList;

    protected EntityAttrPhone() {
        super(EntitySpecAttrType.PHONE);
    }

    /**
     * @param entityId ID договора/объекта/сущности
     * @param entitySpecAttrId ID типа параметра/атрибута
     */
    public EntityAttrPhone(int entityId, int entitySpecAttrId) {
        super(EntitySpecAttrType.PHONE, entityId, entitySpecAttrId);
    }

    public EntityAttrPhone(int entityId, int entitySpecAttrId, String phone, String comment) {
        this(entityId, entitySpecAttrId);
        addContact(phone, comment);
    }

    /**
     * Получение списка контактов.
     * @return
     */
    public List<PhoneContact> getContactList() {
        return contactList;
    }

    /**
     * Установка списка контактов.
     * @param phoneList
     */
    public void setContactList(List<PhoneContact> phoneList) {
        this.contactList = phoneList;
    }

    /**
     * Добавление телефона (контакта).
     * @param phone номер телефона.
     * @param comment комментарий.
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
     * Для внутреннего использования. Для добавления телефона используйте {@link #addPhone(String, String)} или {@link #setContactList(List)}
     * @param value
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
     * Получение списка телефонов (в формате).
     * @return
     */
    public List<String> toPhoneList() {
        if (contactList == null) {
            return Collections.emptyList();
        }

        return contactList.stream().filter(a -> Utils.notBlankString(a.getPhone())).map(PhoneContact::getPhone).toList();
    }

    /**
     * Получение списка телефонов без форматирования.
     */
    public List<String> toUnformattedPhoneList() {
        return contactList == null ? Collections.emptyList()
                : contactList.stream().map(PhoneContact::toPhoneUnformatted).filter(Utils::notBlankString).toList();
    }

    /**
     * Превращает форматированный телефон в набор цифр
     * @param phone +7 (347) 2 924-823
     * @return 73472924823
     */
    // вызываться теоретически может часто, а логика довольно простая, чтобы создавать каждый раз объекты Matcher.
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
     * Контакт - телефон + комментарий.
     */
    public static class PhoneContact {
        /**
         * Телефон.
         */
        private String phone = null;

        /**
         * Комментарий.
         */
        private String comment = null;

        public PhoneContact() {
        }

        public PhoneContact(String phone, String comment) {
            this.phone = phone;
            this.comment = comment;
        }

        /**
         * Получение телефона в формате.
         * @return
         */
        public String getPhone() {
            return phone;
        }

        /**
         * Установка телефона.
         * @param phone
         */
        public void setPhone(String phone) {
            this.phone = phone;
        }

        /**
         * Получение комментария.
         * @return
         */
        public String getComment() {
            return comment;
        }

        /**
         * Установка комментария.
         * @param comment
         */
        public void setComment(String comment) {
            this.comment = comment;
        }

        /**
         * Получение неотформатированного телефона (73472123456).
         * @return
         */
        public String toPhoneUnformatted() {
            return phoneUnformatted(phone);
        }
    }
}
