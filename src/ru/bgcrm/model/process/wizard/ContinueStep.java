package ru.bgcrm.model.process.wizard;

import java.sql.Connection;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;

import ru.bgcrm.model.process.wizard.base.Step;
import ru.bgcrm.model.process.wizard.base.StepData;
import ru.bgcrm.model.process.wizard.base.WizardData;
import ru.bgcrm.struts.form.DynActionForm;

@Bean
public class ContinueStep extends Step {
    private static final Log log = Log.getLog();

    private final Parameter param;

    public ContinueStep(ConfigMap config) {
        super(config);

        int paramId = config.getInt("parameterId", 0);
        param = ParameterCache.getParameter(paramId);
        if (param == null) {
            log.error("Not found param: {}", paramId);
        }
    }

    public Parameter getParam() {
        return param;
    }

    @Override
    public String getJsp() {
        return PATH_JSP + "/step_continue.jsp";
    }

    @Override
    public StepData<?> data(WizardData data) {
        return new Data(this, data);
    }

    public static class Data extends StepData<ContinueStep> {
        private boolean filled = false;

        private Data(ContinueStep step, WizardData data) {
            super(step, data);
        }

        @Override
        public boolean isFilled(DynActionForm form, Connection con) throws Exception {
            return filled = new ParamValueDAO(con).isParameterFilled(data.getProcess().getId(), step.getParam());
        }

        public boolean isFilled() {
            return filled;
        }

        public int getProcessId() {
            return data.getProcess().getId();
        }
    }
}