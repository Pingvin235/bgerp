package ru.bgcrm.plugin.bgbilling.proto.model.voice;

import java.util.stream.Stream;

import org.bgerp.model.base.IdTitle;

public enum VoiceAccountState {
    /**
     * Состояние сервиса - удален (со связанных устройств).
     */
    STATE_DELETED((short) -1, "удален"),
    /**
     * Состояние сервиса/соединения - доступ отключен.
     */
    STATE_DISABLE((short) 0, "отключен"),
    /**
     * Состояние сервиса/соединения - доступ включен.
     */
    STATE_ENABLE((short) 1, "включен");

    private final short code;
    private final String title;

    private VoiceAccountState(short code, String title) {
        this.code = code;
        this.title = title;
    }

    public short getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public IdTitle toIdTitle() {
        return new IdTitle(code, title);
    }

    public static VoiceAccountState getVoiceAccountState(int code) {
        return Stream.of(values()).filter(s -> s.getCode() == code).findFirst().orElse(null);
    }
}
