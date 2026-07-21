package ru.bgcrm.plugin.bgbilling.proto.model.voice;

import java.util.stream.Stream;

import org.bgerp.model.base.IdTitle;

public enum VoiceAccountStatus {
    STATUS_ON(0, "Включен"), STATUS_OFF(1, "Выключен"), STATUS_BLOCKED(2, "Заблокирован");

    private final int code;
    private final String title;

    private VoiceAccountStatus(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public IdTitle toIdTitle() {
        return new IdTitle(code, title);
    }

    public static VoiceAccountStatus getVoiceAccountStatus(int code) {
        return Stream.of(values()).filter(s -> s.getCode() == code).findFirst().orElse(null);
    }
}