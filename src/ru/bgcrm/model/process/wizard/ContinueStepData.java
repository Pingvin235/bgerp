package ru.bgcrm.model.process.wizard;

import java.sql.Connection;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.struts.form.DynActionForm;

public class ContinueStepData extends StepData<ContinueStep> {
    private boolean filled = false;

    public ContinueStepData(ContinueStep step, WizardData data) {
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