package ru.bgcrm.model.process;

/**
 * Use {@link org.bgerp.model.process.link.ProcessLinkProcess}.
 */
@Deprecated
public class ProcessLinkProcess extends ProcessLink {
    @Deprecated
    public ProcessLinkProcess(int processId, String linkType, int linkProcessId) {
        super(processId, linkType, linkProcessId, "");
    }

    @Deprecated
    public static class Made extends ProcessLinkProcess {
        public Made(int processId, int linkProcessId) {
            super(processId, Process.LINK_TYPE_MADE, linkProcessId);
        }

        public Made(Process process, Process linkedProcess) {
            this(process.getId(), linkedProcess.getId());
        }
    }

    @Deprecated
    public static class Depend extends ProcessLinkProcess {
        public Depend(int processId, int linkProcessId) {
            super(processId, Process.LINK_TYPE_DEPEND, linkProcessId);
        }

        public Depend(Process process, Process linkedProcess) {
            this(process.getId(), linkedProcess.getId());
        }
    }

    @Deprecated
    public static class Link extends ProcessLinkProcess {
        public Link(int processId, int linkProcessId) {
            super(processId, Process.LINK_TYPE_LINK, linkProcessId);
        }

        public Link(Process process, Process linkedProcess) {
            this(process.getId(), linkedProcess.getId());
        }
    }
}
