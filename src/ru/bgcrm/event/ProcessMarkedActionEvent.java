package ru.bgcrm.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.event.base.UserEvent;

import ru.bgcrm.model.process.queue.Processor;
import ru.bgcrm.struts.form.DynActionForm;

public class ProcessMarkedActionEvent extends UserEvent {
    private final Processor processor;
    private final List<Integer> processIds;
    private final Map<String, String> parameters = new HashMap<String, String>();

    // ответ уже отправлен в HttpServletResponse
    private boolean streamResponse;

    public ProcessMarkedActionEvent(DynActionForm form, Processor processor, List<Integer> processIds) {
        super(form);

        this.processor = processor;
        this.processIds = processIds;

        for (String paramName : form.getParam().keySet()) {
            parameters.put(paramName, form.getParam(paramName));
        }
    }

    public Processor getProcessor() {
        return processor;
    }

    public List<Integer> getProcessIds() {
        return processIds;
    }

    public boolean isStreamResponse() {
        return streamResponse;
    }

    public void setStreamResponse(boolean streamResponse) {
        this.streamResponse = streamResponse;
    }
}
