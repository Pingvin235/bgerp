package org.bgerp.model.process.config;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.model.process.config.IsolationConfig.IsolationProcess.Type;

import ru.bgcrm.util.Utils;

/**
 * User isolation config.
 *
 * @author Shamil Vakhitov
 */
public class IsolationConfig extends Config {
    private final IsolationProcess isolationProcess;

    protected IsolationConfig(ConfigMap config) throws BGMessageException {
        super(null);
        isolationProcess = loadProcessIsolation(config);
    }

    public IsolationProcess getIsolationProcess() {
        return isolationProcess;
    }

    private IsolationProcess loadProcessIsolation(ConfigMap config) throws BGMessageException {
        var isolation = config.get("isolation.process");
        if ("executor".equals(isolation))
            return new IsolationProcess(Type.EXECUTOR);
        if ("group".equals(isolation)) {
            var result = new IsolationProcess(Type.GROUP);

            var executorFilteredTypes = Utils.toIntegerSet(config.get("isolation.process.group.executor.typeIds"));
            if (!executorFilteredTypes.isEmpty())
                result.executorTypeIds = Utils.toString(executorFilteredTypes);

            return result;
        }

        if (Utils.notBlankString(isolation))
            throw new BGException("Unsupported isolation: " + isolation);

        return IsolationProcess.EMPTY;
    }

    public static class IsolationProcess {
        public static enum Type {
            EXECUTOR, GROUP
        }

        public static final IsolationProcess EMPTY = new IsolationProcess(null);

        private final Type type;
        private String executorTypeIds;

        private IsolationProcess(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        /**
         * Additional process type IDs, which should be filtered by executors with general group isolation.
         * @return
         */
        public String getExecutorTypeIds() {
            return executorTypeIds;
        }

        @Override
        public String toString() {
            return "IsolationProcess [type=" + type + ", executorTypeIds=" + executorTypeIds + "]";
        }
    }
}
