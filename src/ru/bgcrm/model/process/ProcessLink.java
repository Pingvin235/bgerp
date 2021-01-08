package ru.bgcrm.model.process;

import ru.bgcrm.model.CommonObjectLink;

/** 
 * Link another object to a process.
 */
public class ProcessLink extends CommonObjectLink {
    public ProcessLink(int processId, String linkType, int linkedObjectId, String linkedObjectTitle) {
        super(Process.OBJECT_TYPE, processId, linkType, linkedObjectId, linkedObjectTitle);
    }
}
