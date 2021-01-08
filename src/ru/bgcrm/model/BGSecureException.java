package ru.bgcrm.model;

import ru.bgcrm.struts.form.DynActionForm;

/**
 * Suspicious situation, to be monitored.
 * @author Shamil Vakhitov
 */
public class BGSecureException extends BGException {
    private final DynActionForm form;

    public BGSecureException(String message, DynActionForm form) {
        super(message);
        this.form = form;
    }

    /**
     * Request information.
     * @return
     */
    public DynActionForm getForm() {
        return form;
    }
}
