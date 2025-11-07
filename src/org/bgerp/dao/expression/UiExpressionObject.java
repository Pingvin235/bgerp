package org.bgerp.dao.expression;

import java.util.Map;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Expression object for UI manipulations
 *
 * @author Shamil Vakhitov
 */
public class UiExpressionObject implements ExpressionObject {
    private static final String KEY = "ui";

    private final DynActionForm form;
    private final Process process;

    public UiExpressionObject(DynActionForm form, Process process) {
        this.form = form;
        this.process = process;
    }

    @Override
    public void toContext(Map<String, Object> context) {
        context.put(KEY, this);
    }

    /**
     * Open the current process card or refresh it, if already is open
     */
    public void open() {
        form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessOpenEvent(process.getId()));
    }

    /**
     * Close the current process card if it was open
     */
    public void close() {
        form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessCloseEvent(process.getId()));
    }

    /**
     * Refresh currently open process queue
     */
    public void refreshQueue() {
        form.getResponse().addEvent(new ru.bgcrm.event.client.ProcessCurrentQueueRefreshEvent());
    }
}
