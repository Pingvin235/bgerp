package ru.bgcrm.model.process;

/**
 * Link process to another process.
 */
public class ProcessLinkProcess extends ProcessLink {
    public ProcessLinkProcess(int processId, String linkType, int linkedProcessId) {
        super(processId, linkType, linkedProcessId, "");
    }
    
    public static class Made extends ProcessLinkProcess {
        public Made(int processId, int linkedProcessId) {
            super(processId, Process.LINK_TYPE_MADE, linkedProcessId);
        }
        
        public Made(Process process, Process linkedProcess) {
            this(process.getId(), linkedProcess.getId());
        }
    }
    
    public static class Depend extends ProcessLinkProcess {
        public Depend(int processId, int linkedProcessId) {
            super(processId, Process.LINK_TYPE_DEPEND, linkedProcessId);
        }
        
        public Depend(Process process, Process linkedProcess) {
            this(process.getId(), linkedProcess.getId());
        }
    }
    
    public static class Link extends ProcessLinkProcess {
        public Link(int processId, int linkedProcessId) {
            super(processId, Process.LINK_TYPE_LINK, linkedProcessId);
        }
        
        public Link(Process process, Process linkedProcess) {
            this(process.getId(), linkedProcess.getId());
        }
    }
}
