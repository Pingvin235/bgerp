package ru.bgcrm.model;

/**
 * Incorrect action parameters.
 * 
 * @author Shamil Vakhitov
 */
public class BGIllegalArgumentException extends BGMessageException {
    private final String name;

    public BGIllegalArgumentException() {
        this("");
    }

    /**
     * Constructor with parameter name.
     * @param name
     */
    public BGIllegalArgumentException(String name) {
        super("Ошибка в параметрах запроса");
        this.name = name;
    }

    /**
     * First failing parameter name = name of HTML form input.
     * @return
     */
    public String getName() {
        return name;
    }
}
