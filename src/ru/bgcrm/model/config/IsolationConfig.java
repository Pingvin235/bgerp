package ru.bgcrm.model.config;

import org.apache.commons.lang3.StringUtils;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.ParameterMap;

/**
 * Принцип изоляции пользователя.
 * 
 * @author Shamil
 */
public class IsolationConfig extends ru.bgcrm.util.Config {
    private final IsolationProcess isolationProcess; 
    
    protected IsolationConfig(ParameterMap setup, boolean validate) throws BGMessageException {
        super(setup, validate);
        isolationProcess = loadProcessIsolation(setup);
    }
    
    public IsolationProcess getIsolationProcess() {
        return isolationProcess;
    }
    
    private IsolationProcess loadProcessIsolation (ParameterMap config) throws BGMessageException {
        IsolationProcess result = null;
        
        String isolation = config.get("isolation.process");
        if ("executor".equals(isolation))
            return IsolationProcess.EXECUTOR;
        if ("group".equals(isolation) || "executor_group".equals(isolation))
            return IsolationProcess.GROUP;
        
        if (validate && StringUtils.isNotBlank(isolation))
            throw new BGMessageException("Unsupported isolation level: " + config);
        
        return result;
    }
    
    public static enum IsolationProcess {
        EXECUTOR,
        GROUP
    }
}
