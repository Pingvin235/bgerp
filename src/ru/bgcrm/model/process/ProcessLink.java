package ru.bgcrm.model.process;

import ru.bgcrm.model.CommonObjectLink;

/** 
 * Link another object to a process.
 */
public class ProcessLink extends CommonObjectLink {
    public ProcessLink(int processId, String linkType, int linkedProcessId, String linkedObjectTitle) {
        super(Process.OBJECT_TYPE, processId, linkType, linkedProcessId, linkedObjectTitle);
    }
}
