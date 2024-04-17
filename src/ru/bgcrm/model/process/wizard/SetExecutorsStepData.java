package ru.bgcrm.model.process.wizard;

import java.sql.Connection;

import ru.bgcrm.struts.form.DynActionForm;

public class SetExecutorsStepData extends StepData<SetExecutorsStep> {
    public SetExecutorsStepData(SetExecutorsStep step, WizardData data) {
        super(step, data);
    }

    @Override
    public boolean isFilled(DynActionForm form, Connection con) {
        return data.getProcess().getExecutorIds().size() > 0;
    }
}
