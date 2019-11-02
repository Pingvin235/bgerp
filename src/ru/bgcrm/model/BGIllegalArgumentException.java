package ru.bgcrm.model;

public class BGIllegalArgumentException extends BGMessageException {
    public BGIllegalArgumentException() {
        super("Ошибка параметров запроса.");
    }
}
