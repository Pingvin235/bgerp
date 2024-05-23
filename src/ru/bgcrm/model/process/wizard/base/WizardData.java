package ru.bgcrm.model.process.wizard.base;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.app.exception.BGException;
import org.bgerp.util.Log;

import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.Wizard;
import ru.bgcrm.model.process.wizard.LinkCustomerStep;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;

public class WizardData {
    private static final Log log = Log.getLog();

    private final Process process;
    private final DynActionForm form;
    private final User user;
    private final List<StepData<?>> stepDataList = new ArrayList<>();
    private final boolean allFilled;

    public WizardData(Connection con, DynActionForm form, Wizard wizard, Process process, List<Step> stepList) {
        this.process = process;
        this.form = form;
        this.user = form.getUser();

        boolean allFilled = true;

        for (Step step : stepList) {
            StepData<?> stepData = step.data(this);

            try {
                if (stepData == null)
                    throw new BGException("For the step " + step.getTitle() + " was returned null StepData!");

                if (stepData.check(con)) {
                    stepDataList.add(stepData);

                    // going until the first unfilled step
                    if (!stepData.isFilled(form, con)) {
                        allFilled = false;
                        break;
                    }
                }
            } catch (Exception e) {
                log.error(e);
                allFilled = false;
                break;
            }
        }

        this.allFilled = allFilled;
    }

    public Process getProcess() {
        return process;
    }

    public DynActionForm getForm() {
        return form;
    }

    public User getUser() {
        return user;
    }

    public List<StepData<?>> getStepDataList() {
        return stepDataList;
    }

    public boolean isAllFilled() {
        return allFilled;
    }

    public Customer getLinkedCustomer() {
        Customer result = null;

        for (StepData<?> stepData : stepDataList) {
            if (stepData instanceof LinkCustomerStep.Data scStepData) {
                result = scStepData.getCustomer();
            }
        }

        return result;
    }
}