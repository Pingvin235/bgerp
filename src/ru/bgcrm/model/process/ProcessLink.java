package ru.bgcrm.model.process;

import org.bgerp.util.Log;

/**
 * Use {@link org.bgerp.model.process.link.ProcessLink}.
 */
@Deprecated
public class ProcessLink extends org.bgerp.model.process.link.ProcessLink {
    private static final Log log = Log.getLog();

    @Deprecated
    public ProcessLink(int processId, String linkType, int linkedObjectId, String linkedObjectTitle) {
        super(processId, linkType, linkedObjectId, linkedObjectTitle);
        log.warnd("Deprecated class 'ru.bgcrm.model.process.ProcessLink' was created, use 'org.bgerp.model.process.link.ProcessLink' instead.");
    }
}
