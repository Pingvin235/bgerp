package ru.bgcrm.model.config;

import org.apache.commons.lang3.StringUtils;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

/**
 * User isolation config.
 *
 * @author Shamil Vakhitov
 */
public class IsolationConfig extends ru.bgcrm.util.Config {
    private final IsolationProcess isolationProcess;

    protected IsolationConfig(ParameterMap config) throws BGMessageException {
        super(config);
        isolationProcess = loadProcessIsolation(config);
    }

    public IsolationProcess getIsolationProcess() {
        return isolationProcess;
    }

    private IsolationProcess loadProcessIsolation(ParameterMap config) throws BGMessageException {
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
