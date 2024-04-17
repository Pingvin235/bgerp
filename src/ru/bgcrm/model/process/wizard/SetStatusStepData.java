package ru.bgcrm.model.process.wizard;

import java.sql.Connection;

import ru.bgcrm.struts.form.DynActionForm;

public class SetStatusStepData extends StepData<SetStatusStep> {
    public SetStatusStepData(SetStatusStep step, WizardData data) {
        super(step, data);
    }

    @Override
    public boolean isFilled(DynActionForm form, Connection con) {
        return true;
    }
}
