package org.bgerp.model.process.config;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGMessageException;

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
            return IsolationProcess.EXECUTOR;
        if ("group".equals(isolation)) {
            var result = IsolationProcess.GROUP;

            var executorFilteredTypes = Utils.toIntegerSet(config.get("isolation.process.group.executor.typeIds"));
            if (!executorFilteredTypes.isEmpty())
                result.executorTypeIds = Utils.toString(executorFilteredTypes);

            return result;
        }

        if (StringUtils.isNotBlank(isolation))
            throw new BGMessageException("Unsupported isolation level: " + config);

        return null;
    }

    public static class IsolationProcess {
        public static final IsolationProcess EXECUTOR = new IsolationProcess();
        public static final IsolationProcess GROUP = new IsolationProcess();

        private String executorTypeIds;

        /**
         * Additional process type IDs, which should be filtered by executors with general group isolation.
         * @return
         */
        public String getExecutorTypeIds() {
            return executorTypeIds;
        }
    }
}
