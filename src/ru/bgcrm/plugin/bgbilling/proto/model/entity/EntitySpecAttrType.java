package ru.bgcrm.plugin.bgbilling.proto.model.entity;


import java.util.stream.Stream;

public enum EntitySpecAttrType {
    SEPARATOR( -1, "separator", "Разделитель" ),
    UNKNOWN( 0, "unknown", "Неизвестный тип" ),
    TEXT( 1, "text", "Текст" ),
    INT( 2, "int", "Число (int)" ),
    BOOLEAN( 3, "boolean", "Флаг" ),
    LIST( 4, "list", "Список" ),
    DATE( 5, "date", "Дата" ),
    PERIOD( 6, "period", "Период" ),
    HOUSE( 7, "house", "Здание" ),
    ADDRESS( 8, "address", "Адрес" ),
    EMAIL( 9, "email", "Электронный адрес" ),
    PHONE( 10, "phone", "Телефон" ),
    MULTILIST( 11, "multilist", "Мультисписок" ),
    LONG( 22, "long", "Число (long)" ),
    ENTITY( 30, "entity", "Ссылка на параметр" ),
    CONTRACT( 31, "contract", "Ссылка на договор" ),
    SERVICING_PERSON( 32, "person", "Обслуживание договора" ); // 4 - "contract_parameter_type_4"

    public static EntitySpecAttrType getEntitySpecAttrType(final int code) {
        return Stream.of(values()).filter(a -> a.code == code).findFirst().orElse(null);
    }

    private int code;
    private String typeName = null;
    private String typeTitle = null;

    private EntitySpecAttrType(int code, String typeName, String typeTitle) {
        this.code = code;
        this.typeName = typeName;
        this.typeTitle = typeTitle;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeTitle() {
        return typeTitle;
    }

    public void setTypeTitle(String typeTitle) {
        this.typeTitle = typeTitle;
    }

    @Override
    public String toString() {
        return getTypeTitle();
    }
}